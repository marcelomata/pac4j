package org.pac4j.core.authorization.generator;

import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.profile.UserProfile;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

/**
 * Grant default roles to a user profile.
 *
 * @author Jerome Leleu
 * @since 1.8.0
 */
public class DefaultRolesAuthorizationGenerator implements AuthorizationGenerator {

    private final Collection<String> defaultRoles;

    public DefaultRolesAuthorizationGenerator(final Collection<String> defaultRoles) {
        this.defaultRoles = defaultRoles;
    }

    public DefaultRolesAuthorizationGenerator(final String[] defaultRoles) {
        if (defaultRoles != null) {
            this.defaultRoles = Arrays.asList(defaultRoles);
        } else {
            this.defaultRoles = null;
        }
    }

    @Override
    public Optional<UserProfile> generate(final WebContext context, final SessionStore sessionStore, final UserProfile profile) {
        if (defaultRoles != null) {
            profile.addRoles(defaultRoles);
        }
        return Optional.of(profile);
    }
}
