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
import static org.junit.Assert.assertTrue;

/**
 * This class tests the {@link ParameterClient} class.
 *
 * @author Jerome Leleu
 * @since 1.8.0
 */
public final class ParameterClientTests implements TestsConstants {

    private final static String PARAMETER_NAME = "parameterName";

    @Test
    public void testMissingTokenAuthenticator() {
        val client = new ParameterClient(PARAMETER_NAME, (Authenticator) null);
        TestsHelper.expectException(() -> client.getCredentials(MockWebContext.create(), new MockSessionStore(),
                ProfileManagerFactory.DEFAULT), TechnicalException.class, "authenticator cannot be null");
    }

    @Test
    public void testMissingProfileCreator() {
        val client = new ParameterClient(PARAMETER_NAME, new SimpleTestTokenAuthenticator(), null);
        TestsHelper.expectException(() -> client.getUserProfile(new TokenCredentials(TOKEN),
                MockWebContext.create(), new MockSessionStore()), TechnicalException.class, "profileCreator cannot be null");
    }

    @Test
    public void testHasDefaultProfileCreator() {
        val client = new ParameterClient(null, new SimpleTestTokenAuthenticator());
        client.setParameterName(PARAMETER_NAME);
        client.init();
    }

    @Test
    public void testMissingParameterName() {
        val client = new ParameterClient(null, new SimpleTestTokenAuthenticator());
        TestsHelper.initShouldFail(client, "parameterName cannot be blank");
    }

    @Test
    public void testBadHttpMethod() {
        val client = new ParameterClient(PARAMETER_NAME, new SimpleTestTokenAuthenticator());
        val context = MockWebContext.create();
        context.addRequestParameter(PARAMETER_NAME, VALUE);
        context.setRequestMethod(HttpConstants.HTTP_METHOD.GET.name());
        val credentials = client.getCredentials(context, new MockSessionStore(),
            ProfileManagerFactory.DEFAULT);
        assertTrue(credentials.isEmpty());
    }

    @Test
    public void testAuthentication() {
        val client = new ParameterClient(PARAMETER_NAME, new SimpleTestTokenAuthenticator());
        client.setSupportGetRequest(true);
        val context = MockWebContext.create();
        context.addRequestParameter(PARAMETER_NAME, VALUE);
        context.setRequestMethod(HttpConstants.HTTP_METHOD.GET.name());
        val credentials = (TokenCredentials) client.getCredentials(context, new MockSessionStore(),
            ProfileManagerFactory.DEFAULT).get();
        assertEquals(VALUE, credentials.getToken());
        val profile = (CommonProfile) client.getUserProfile(credentials, context, new MockSessionStore()).get();
        assertEquals(VALUE, profile.getId());
    }

    @Test
    public void testProfileCreation() {
        val client = new ParameterClient(PARAMETER_NAME, new ProfileCreator() {
            @Override
            public Optional<UserProfile> create(Credentials credentials, WebContext context, SessionStore sessionStore) {
                val profile = new CommonProfile();
                profile.setId(KEY);
                return Optional.of(profile);
            }
        });
        client.setSupportGetRequest(true);
        val context = MockWebContext.create();
        context.addRequestParameter(PARAMETER_NAME, VALUE);
        context.setRequestMethod(HttpConstants.HTTP_METHOD.GET.name());
        val credentials = (TokenCredentials) client.getCredentials(context, new MockSessionStore(),
            ProfileManagerFactory.DEFAULT).get();
        assertEquals(VALUE, credentials.getToken());
        val profile = (CommonProfile) client.getUserProfile(credentials, context, new MockSessionStore()).get();
        assertEquals(KEY, profile.getId());
    }
}
