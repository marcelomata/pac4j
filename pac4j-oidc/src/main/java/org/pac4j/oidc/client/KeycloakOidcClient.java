package org.pac4j.oidc.client;

import lombok.val;
import org.pac4j.oidc.authorization.generator.KeycloakRolesAuthorizationGenerator;
import org.pac4j.oidc.config.KeycloakOidcConfiguration;
import org.pac4j.oidc.profile.OidcProfileDefinition;
import org.pac4j.oidc.profile.creator.OidcProfileCreator;
import org.pac4j.oidc.profile.keycloak.KeycloakOidcProfile;

/**
 * <p>This class is the OpenID Connect client to authenticate users in Keycloak.</p>
 * <p>A KeycloakOidcConfiguration is needed to create a client with to additional properties: </p>
 * <ul>
 *   <li>baseUri : Base auth server url, e.g., https://keycloak.example.com/auth </li>
 *   <li>realm : keycloak realm</li>
 * </ul>
 * <p> As seen in test case org.pac4j.oidc.run.RunKeycloakOidcClient</p>
 *
 * @author Julio Arrebola
 * @since 2.0.0
 */
public class KeycloakOidcClient extends OidcClient {

    public KeycloakOidcClient() {
    }

    public KeycloakOidcClient(final KeycloakOidcConfiguration configuration) {
        super(configuration);
    }

    @Override
    protected void internalInit(final boolean forceReinit) {
        val profileCreator = new OidcProfileCreator(getConfiguration(), this);
        profileCreator.setProfileDefinition(new OidcProfileDefinition(x -> new KeycloakOidcProfile()));
        setProfileCreatorIfUndefined(profileCreator);

        addAuthorizationGenerator(new KeycloakRolesAuthorizationGenerator(getConfiguration().getClientId()));

        super.internalInit(forceReinit);
    }
}
