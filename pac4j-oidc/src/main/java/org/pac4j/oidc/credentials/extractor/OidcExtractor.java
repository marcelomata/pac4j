package org.pac4j.oidc.credentials.extractor;

import com.nimbusds.jwt.JWTParser;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.id.State;
import com.nimbusds.openid.connect.sdk.AuthenticationErrorResponse;
import com.nimbusds.openid.connect.sdk.AuthenticationResponse;
import com.nimbusds.openid.connect.sdk.AuthenticationResponseParser;
import com.nimbusds.openid.connect.sdk.AuthenticationSuccessResponse;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.credentials.extractor.CredentialsExtractor;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.exception.http.BadRequestAction;
import org.pac4j.core.exception.http.OkAction;
import org.pac4j.core.profile.factory.ProfileManagerFactory;
import org.pac4j.core.util.CommonHelper;
import org.pac4j.core.util.Pac4jConstants;
import org.pac4j.oidc.client.OidcClient;
import org.pac4j.oidc.config.OidcConfiguration;
import org.pac4j.oidc.credentials.OidcCredentials;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * Extract the authorization code on the callback.
 *
 * @author Jerome Leleu
 * @since 1.9.2
 */
@Slf4j
public class OidcExtractor implements CredentialsExtractor {

    protected OidcConfiguration configuration;

    protected OidcClient client;

    public OidcExtractor(final OidcConfiguration configuration, final OidcClient client) {
        CommonHelper.assertNotNull("configuration", configuration);
        CommonHelper.assertNotNull("client", client);
        this.configuration = configuration;
        this.client = client;
    }

    @Override
    public Optional<Credentials> extract(final WebContext context, final SessionStore sessionStore,
                                         final ProfileManagerFactory profileManagerFactory) {
        val logoutEndpoint = context.getRequestParameter(Pac4jConstants.LOGOUT_ENDPOINT_PARAMETER)
            .isPresent();
        if (logoutEndpoint) {
            val logoutToken = context.getRequestParameter("logout_token");
            // back-channel logout
            if (logoutToken.isPresent()) {
                try {
                    val jwt = JWTParser.parse(logoutToken.get());
                    // we should use the tokenValidator, but we can't as validation fails on missing claims: exp, iat...
                    //final IDTokenClaimsSet claims = configuration.findTokenValidator().validate(jwt, null);
                    //final String sid = (String) claims.getClaim(Pac4jConstants.OIDC_CLAIM_SESSIONID);
                    val sid = (String) jwt.getJWTClaimsSet().getClaim(Pac4jConstants.OIDC_CLAIM_SESSIONID);
                    LOGGER.debug("Handling back-channel logout for sessionId: {}", sid);
                    configuration.findLogoutHandler().destroySessionBack(context, sessionStore, profileManagerFactory, sid);
                } catch (final java.text.ParseException e) {
                    LOGGER.error("Cannot validate JWT logout token", e);
                    throw new BadRequestAction();
                }
            } else {
                val sid = context.getRequestParameter(Pac4jConstants.OIDC_CLAIM_SESSIONID).orElse(null);
                LOGGER.debug("Handling front-channel logout for sessionId: {}", sid);
                // front-channel logout
                configuration.findLogoutHandler().destroySessionFront(context, sessionStore, profileManagerFactory, sid);
            }
            context.setResponseHeader("Cache-Control", "no-cache, no-store");
            context.setResponseHeader("Pragma", "no-cache");
            throw new OkAction(Pac4jConstants.EMPTY_STRING);
        } else {
            val computedCallbackUrl = client.computeFinalCallbackUrl(context);
            val parameters = retrieveParameters(context);
            AuthenticationResponse response;
            try {
                response = AuthenticationResponseParser.parse(new URI(computedCallbackUrl), parameters);
            } catch (final URISyntaxException | ParseException e) {
                throw new TechnicalException(e);
            }

            if (response instanceof AuthenticationErrorResponse) {
                LOGGER.error("Bad authentication response, error={}",
                    ((AuthenticationErrorResponse) response).getErrorObject());
                return Optional.empty();
            }

            LOGGER.debug("Authentication response successful");
            var successResponse = (AuthenticationSuccessResponse) response;

            var metadata = configuration.getProviderMetadata();
            if (metadata.supportsAuthorizationResponseIssuerParam() &&
                !metadata.getIssuer().equals(successResponse.getIssuer())) {
                throw new TechnicalException("Issuer mismatch, possible mix-up attack.");
            }

            if (configuration.isWithState()) {
                // Validate state for CSRF mitigation
                val requestState = (State) configuration.getValueRetriever()
                    .retrieve(client.getStateSessionAttributeName(), client, context, sessionStore)
                    .orElseThrow(() -> new TechnicalException("State cannot be determined"));

                val responseState = successResponse.getState();
                if (responseState == null) {
                    throw new TechnicalException("Missing state parameter");
                }

                LOGGER.debug("Request state: {}/response state: {}", requestState, responseState);
                if (!requestState.equals(responseState)) {
                    throw new TechnicalException(
                        "State parameter is different from the one sent in authentication request.");
                }
            }

            val credentials = new OidcCredentials();
            // get authorization code
            val code = successResponse.getAuthorizationCode();
            if (code != null) {
                credentials.setCode(code);
            }
            // get ID token
            val idToken = successResponse.getIDToken();
            if (idToken != null) {
                credentials.setIdToken(idToken);
            }
            // get access token
            val accessToken = successResponse.getAccessToken();
            if (accessToken != null) {
                credentials.setAccessToken(accessToken);
            }

            return Optional.of(credentials);
        }
    }

    protected Map<String, List<String>> retrieveParameters(final WebContext context) {
        val requestParameters = context.getRequestParameters();
        final Map<String, List<String>> map = new HashMap<>();
        for (val entry : requestParameters.entrySet()) {
            map.put(entry.getKey(), Arrays.asList(entry.getValue()));
        }
        return map;
    }
}
