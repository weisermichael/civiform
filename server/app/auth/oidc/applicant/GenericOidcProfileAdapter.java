package auth.oidc.applicant;

import static com.google.common.base.Preconditions.checkNotNull;

import auth.CiviFormProfile;
import auth.CiviFormProfileData;
import auth.ProfileFactory;
import auth.Roles;

import com.google.common.collect.ImmutableSet;
import com.nimbusds.jose.shaded.json.JSONArray;
import com.typesafe.config.Config;
import javax.inject.Provider;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.oidc.client.OidcClient;
import org.pac4j.oidc.config.OidcConfiguration;
import org.pac4j.oidc.profile.OidcProfile;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import repository.UserRepository;
import com.typesafe.config.Config;

/**
 * This class takes an existing CiviForm profile and augments it with the
 * information from an AD
 * profile. Right now this is only extracting the email address, since that is
 * all that AD provides
 * right now.
 */
public class GenericOidcProfileAdapter extends OidcApplicantProfileAdapter {
  private static final Logger logger = LoggerFactory.getLogger(GenericOidcProfileAdapter.class);

  protected String attributePrefix = "generic_oidc";

  public GenericOidcProfileAdapter(
      OidcConfiguration oidc_configuration,
      OidcClient client,
      Config configuration,
      ProfileFactory profileFactory,
      Provider<UserRepository> applicantRepositoryProvider) {
    super(oidc_configuration, client, configuration, profileFactory, applicantRepositoryProvider);
  }

  @Override
  protected String attributePrefix() {
    return "generic";
  }
}
