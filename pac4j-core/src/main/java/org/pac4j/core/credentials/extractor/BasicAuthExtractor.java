package org.pac4j.core.credentials.extractor;

import lombok.val;
import org.pac4j.core.context.HttpConstants;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.credentials.TokenCredentials;
import org.pac4j.core.credentials.UsernamePasswordCredentials;
import org.pac4j.core.exception.CredentialsException;
import org.pac4j.core.profile.factory.ProfileManagerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

/**
 * To extract basic auth header.
 *
 * @author Jerome Leleu
 * @since 1.8.0
 */
public class BasicAuthExtractor implements CredentialsExtractor {

    private final HeaderExtractor extractor;

    public BasicAuthExtractor() {
        this(HttpConstants.AUTHORIZATION_HEADER, HttpConstants.BASIC_HEADER_PREFIX);
    }

    public BasicAuthExtractor(final String headerName, final String prefixHeader) {
        this.extractor = new HeaderExtractor(headerName, prefixHeader);
    }

    @Override
    public Optional<Credentials> extract(final WebContext context, final SessionStore sessionStore,
                                         final ProfileManagerFactory profileManagerFactory) {
        val optCredentials = this.extractor.extract(context, sessionStore, profileManagerFactory);
        return optCredentials.map(cred -> {

            val credentials = (TokenCredentials) cred;
            final byte[] decoded;
            try {
                decoded = Base64.getDecoder().decode(credentials.getToken());
            } catch (IllegalArgumentException e) {
                throw new CredentialsException("Bad format of the basic auth header");
            }
            val token = new String(decoded, StandardCharsets.UTF_8);

            val delim = token.indexOf(":");
            if (delim < 0) {
                throw new CredentialsException("Bad format of the basic auth header");
            }
            return new UsernamePasswordCredentials(token.substring(0, delim), token.substring(delim + 1));
        });
    }
}
