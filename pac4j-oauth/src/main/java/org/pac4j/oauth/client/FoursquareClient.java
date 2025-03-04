package org.pac4j.oauth.client;

import com.github.scribejava.apis.Foursquare2Api;
import org.pac4j.core.util.HttpActionHelper;
import org.pac4j.core.util.CommonHelper;
import org.pac4j.oauth.profile.foursquare.FoursquareProfile;
import org.pac4j.oauth.profile.foursquare.FoursquareProfileCreator;
import org.pac4j.oauth.profile.foursquare.FoursquareProfileDefinition;

import java.util.Optional;

/**
 * <p>This class is the OAuth client to authenticate users in Foursquare.
 * It returns a {@link FoursquareProfile}.</p>
 * <p>More information at https://developer.foursquare.com/overview/auth.html</p>
 *
 * @author Alexey Ogarkov
 * @since 1.5.0
 */
public class FoursquareClient extends OAuth20Client {

    public FoursquareClient() {}

    public FoursquareClient(String key, String secret) {
        setKey(key);
        setSecret(secret);
    }

    @Override
    protected void internalInit(final boolean forceReinit) {
        CommonHelper.assertNotNull("configuration", configuration);
        configuration.setApi(Foursquare2Api.instance());
        configuration.setProfileDefinition(new FoursquareProfileDefinition());
        configuration.setScope("user");
        setProfileCreatorIfUndefined(new FoursquareProfileCreator(configuration, this));
        setLogoutActionBuilderIfUndefined((ctx, store, profile, targetUrl) ->
            Optional.of(HttpActionHelper.buildRedirectUrlAction(ctx, "https://www.foursquare.com/logout")));

        super.internalInit(forceReinit);
    }
}
