package auth.oidc.admin;

import javax.inject.Provider;

import com.google.common.collect.ImmutableSet;
import com.typesafe.config.Config;

import org.pac4j.core.credentials.Credentials;
import org.pac4j.oidc.client.OidcClient;
import org.pac4j.oidc.config.OidcConfiguration;
import org.pac4j.oidc.profile.OidcProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import auth.CiviFormProfile;
import auth.ProfileFactory;
import auth.Roles;
import auth.oidc.applicant.OidcApplicantProfileAdapter;
import repository.UserRepository;

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
  protected String emailAttributeName() {
    return getEmailAttributeName();
  }

  @Override
  protected ImmutableSet<Roles> roles(CiviFormProfile profile, OidcProfile oidcProfile) {
    if (this.isGlobalAdmin(oidcProfile)) {
      return ImmutableSet.of(Roles.ROLE_CIVIFORM_ADMIN);
    }
    if (this.isProgramAdmin(oidcProfile)) {
      return ImmutableSet.of(Roles.ROLE_PROGRAM_ADMIN);
    }
    if (this.isTrustedIntermediary(profile)) {
      return ImmutableSet.of(Roles.ROLE_APPLICANT, Roles.ROLE_TI);
    }
    return ImmutableSet.of(Roles.ROLE_APPLICANT);
  }

  @Override
  protected void adaptForRole(CiviFormProfile profile, ImmutableSet<Roles> roles) {
    if (roles.contains(Roles.ROLE_CIVIFORM_ADMIN)) {
      profile
          .getAccount()
          .thenAccept(
              account -> {
                account.setGlobalAdmin(true);
                account.save();
              })
          .join();
    }
  }

  private boolean isGlobalAdmin(OidcProfile profile) {
    return false;
  }

  private boolean isProgramAdmin(OidcProfile profile) {
    return false;
  }

  private boolean isTrustedIntermediary(CiviFormProfile profile) {
    return profile.getAccount().join().getMemberOfGroup().isPresent();
  }

  @Override
  public CiviFormProfile createEmptyCiviFormProfile(OidcProfile profile) {
    if (this.isGlobalAdmin(profile)) {
      return profileFactory.wrapProfileData(profileFactory.createNewAdmin());
    }
    return profileFactory.wrapProfileData(profileFactory.createNewProgramAdmin());
  }

  @Override
  protected void possiblyModifyConfigBasedOnCred(Credentials cred) {
    // No need!
  }

  @Override
  protected String attributePrefix() {
    // TODO Auto-generated method stub
    return null;
  }
}
