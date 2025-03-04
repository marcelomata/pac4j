package org.pac4j.http.credentials.extractor;

import lombok.ToString;
import lombok.val;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.credentials.TokenCredentials;
import org.pac4j.core.credentials.extractor.CredentialsExtractor;
import org.pac4j.core.profile.factory.ProfileManagerFactory;

import java.util.Optional;

/**
 * Extracts a cookie value from the request context.
 *
 * @author Misagh Moayyed
 * @since 1.8.0
 */
@ToString
public class CookieExtractor implements CredentialsExtractor {

    private final String cookieName;

    public CookieExtractor(final String cookieName) {
        this.cookieName = cookieName;
    }

    @Override
    public Optional<Credentials> extract(final WebContext context, final SessionStore sessionStore,
                                         final ProfileManagerFactory profileManagerFactory) {
        val col = context.getRequestCookies();
        for (val c : col) {
            if (c.getName().equals(this.cookieName)) {
                return Optional.of(new TokenCredentials(c.getValue()));
            }
        }
        return Optional.empty();
    }
}
