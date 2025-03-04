package org.pac4j.http.profile;

import lombok.ToString;
import org.pac4j.core.profile.CommonProfile;

/**
 * Profile for X509 certificate authentication.
 *
 * @author Jerome Leleu
 * @since 3.3.0
 */
@ToString(callSuper = true)
public class X509Profile extends CommonProfile {

    private static final long serialVersionUID = -7596662147066025651L;
}
