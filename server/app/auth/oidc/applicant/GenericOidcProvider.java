package auth.oidc.applicant;

import auth.ProfileFactory;
import auth.oidc.OidcCiviFormProvider;

import com.google.inject.Inject;
import com.typesafe.config.Config;
import javax.inject.Provider;
import org.pac4j.core.profile.creator.ProfileCreator;
import org.pac4j.oidc.client.OidcClient;
import org.pac4j.oidc.config.OidcConfiguration;
import repository.UserRepository;

public class GenericOidcProvider extends OidcCiviFormProvider {

  protected String attributePrefix = "generic_oidc";

  @Inject
  public GenericOidcProvider(
      Config configuration,
      ProfileFactory profileFactory,
      Provider<UserRepository> applicantRepositoryProvider) {
    super(configuration, profileFactory, applicantRepositoryProvider);
  }

  @Override
  protected String attributePrefix() {
    return attributePrefix;
  };

  @Override
  public ProfileCreator getProfileAdapter(OidcConfiguration config, OidcClient client, Config appConfig) {
    return new GenericOidcProfileAdapter(config, client, appConfig, profileFactory, applicantRepositoryProvider);
  }

}
