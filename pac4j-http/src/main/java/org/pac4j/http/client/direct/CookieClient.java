package org.pac4j.http.client.direct;

import org.pac4j.core.client.DirectClient;
import org.pac4j.core.credentials.authenticator.Authenticator;
import org.pac4j.core.util.CommonHelper;
import org.pac4j.http.credentials.extractor.CookieExtractor;

/**
 * Allows direct authentication based on a cookie.
 *
 * @author Misagh Moayyed
 * @since 1.8.0
 */
public class CookieClient extends DirectClient {

    private String cookieName;

    public CookieClient() {}

    public CookieClient(final String cookieName, final Authenticator cookieAuthenticator) {
        this.cookieName = cookieName;
        setAuthenticatorIfUndefined(cookieAuthenticator);
    }

    @Override
    protected void internalInit(final boolean forceReinit) {
        CommonHelper.assertNotBlank("cookieName", this.cookieName);

        setCredentialsExtractorIfUndefined(new CookieExtractor(this.cookieName));
    }

    public String getCookieName() {
        return cookieName;
    }

    public void setCookieName(String cookieName) {
        this.cookieName = cookieName;
    }
}
