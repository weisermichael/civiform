package auth.oidc.applicant;

import javax.inject.Provider;

import com.google.inject.Inject;
import com.typesafe.config.Config;

import org.pac4j.core.profile.creator.ProfileCreator;
import org.pac4j.oidc.client.OidcClient;
import org.pac4j.oidc.config.OidcConfiguration;

import auth.ProfileFactory;
import auth.oidc.OidcCiviFormProvider;
import repository.UserRepository;

/**
 * This class customized the OIDC provider to a specific provider, allowing
 * overrides to be set.
 */
public class IdcsOidcProvider extends OidcCiviFormProvider {
  protected String attributePrefix = "idcs";

  @Inject
  public IdcsOidcProvider(
      Config configuration,
      ProfileFactory profileFactory,
      Provider<UserRepository> applicantRepositoryProvider) {
    super(configuration, profileFactory, applicantRepositoryProvider);
  }

  @Override
  protected String getProviderName() {
    return "";
  }

  @Override
  protected String getResponseMode() {
    return "form_post";
  }

  @Override
  protected String[] getExtraScopes() {
    return new String[] {};
  }

  @Override
  protected String attributePrefix() {
    return attributePrefix;
  };

  @Override
  public ProfileCreator getProfileAdapter(OidcConfiguration config, OidcClient client, Config appConfig) {
    return new IdcsProfileAdapter(config, client, configuration, profileFactory, applicantRepositoryProvider);
  }
}
