package org.pac4j.http.client.direct;

import lombok.val;
import org.junit.Test;
import org.pac4j.core.context.HttpConstants;
import org.pac4j.core.context.MockWebContext;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.MockSessionStore;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.credentials.TokenCredentials;
import org.pac4j.core.credentials.authenticator.Authenticator;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.core.profile.creator.ProfileCreator;
import org.pac4j.core.profile.factory.ProfileManagerFactory;
import org.pac4j.core.util.TestsConstants;
import org.pac4j.core.util.TestsHelper;
import org.pac4j.http.credentials.authenticator.test.SimpleTestTokenAuthenticator;

import java.util.Optional;

import static org.junit.Assert.assertEquals;

/**
 * This class tests the {@link DirectBearerAuthClient} class.
 *
 * @author Graham Leggett
 * @since 3.5.0
 */
public final class DirectBearerAuthClientTests implements TestsConstants {

    @Test
    public void testMissingTokenAuthenticator() {
        val bearerAuthClient = new DirectBearerAuthClient((Authenticator) null);
        TestsHelper.expectException(() -> bearerAuthClient.getCredentials(MockWebContext.create(), new MockSessionStore(),
                ProfileManagerFactory.DEFAULT), TechnicalException.class, "authenticator cannot be null");
    }

    @Test
    public void testMissingProfileCreator() {
        val bearerAuthClient = new DirectBearerAuthClient((ProfileCreator) null);
        TestsHelper.expectException(() -> bearerAuthClient.getCredentials(MockWebContext.create(), new MockSessionStore(),
                ProfileManagerFactory.DEFAULT), TechnicalException.class, "profileCreator cannot be null");
    }

    @Test
    public void testMissingProfileCreator2() {
        val bearerAuthClient = new DirectBearerAuthClient(new SimpleTestTokenAuthenticator(), null);
        TestsHelper.expectException(() -> bearerAuthClient.getUserProfile(new TokenCredentials(TOKEN),
            MockWebContext.create(), new MockSessionStore()), TechnicalException.class, "profileCreator cannot be null");
    }

    @Test
    public void testHasDefaultProfileCreator() {
        val bearerAuthClient = new DirectBearerAuthClient(new SimpleTestTokenAuthenticator());
        bearerAuthClient.init();
    }

    @Test
    public void testAuthentication() {
        val client = new DirectBearerAuthClient(new SimpleTestTokenAuthenticator());
        val context = MockWebContext.create();
        context.addRequestHeader(HttpConstants.AUTHORIZATION_HEADER,
                HttpConstants.BEARER_HEADER_PREFIX + TOKEN);
        val credentials = (TokenCredentials) client.getCredentials(context, new MockSessionStore(),
            ProfileManagerFactory.DEFAULT).get();
        assertEquals(TOKEN, credentials.getToken());
        val profile = (CommonProfile) client.getUserProfile(credentials, context, new MockSessionStore()).get();
        assertEquals(TOKEN, profile.getId());
    }

    @Test
    public void testProfileCreation() {
        val client = new DirectBearerAuthClient(new ProfileCreator() {
            @Override
            public Optional<UserProfile> create(Credentials credentials, WebContext context, SessionStore sessionStore) {
                val profile = new CommonProfile();
                profile.setId(KEY);
                return Optional.of(profile);
            }
        });
        val context = MockWebContext.create();
        context.addRequestHeader(HttpConstants.AUTHORIZATION_HEADER,
            HttpConstants.BEARER_HEADER_PREFIX + TOKEN);
        val credentials = (TokenCredentials) client.getCredentials(context, new MockSessionStore(),
            ProfileManagerFactory.DEFAULT).get();
        assertEquals(TOKEN, credentials.getToken());
        val profile = (CommonProfile) client.getUserProfile(credentials, context, new MockSessionStore()).get();
        assertEquals(KEY, profile.getId());
    }
}
