package auth.oidc;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Collections;

import javax.inject.Provider;

import com.google.inject.Inject;
import com.typesafe.config.Config;

import org.pac4j.core.http.callback.PathParameterCallbackUrlResolver;
import org.pac4j.core.profile.creator.ProfileCreator;
import org.pac4j.oidc.client.OidcClient;
import org.pac4j.oidc.config.OidcConfiguration;

import auth.ProfileFactory;
import repository.UserRepository;

/**
 * This class provides the base applicant OIDC implementation.
 * It's abstract because AD and other providers need slightly
 * different implementations and profile adaptors,
 * and use different config values.
 */
public abstract class OidcCiviFormProvider implements Provider<OidcClient> {

  protected final Config configuration;
  protected final ProfileFactory profileFactory;
  protected final Provider<UserRepository> applicantRepositoryProvider;
  protected final String baseUrl;

  protected String[] defaultScopes = { "openid", "profile", "email" };

  protected String providerNameConfigName = "provider_name";
  protected String clientIDConfigName = "client_id";
  protected String clientSecretConfigName = "client_secret";
  protected String discoveryURIConfigName = "discovery_uri";
  protected String responseModeConfigName = "response_mode";
  protected String extraScopesConfigName = "additional_scopes";

  @Inject
  public OidcCiviFormProvider(
      Config configuration,
      ProfileFactory profileFactory,
      Provider<UserRepository> applicantRepositoryProvider) {
    this.configuration = checkNotNull(configuration);
    this.profileFactory = checkNotNull(profileFactory);
    this.applicantRepositoryProvider = applicantRepositoryProvider;

    baseUrl = configuration.getString("base_url");
  }

  /*
   * Provide the prefix used in the application.conf
   */
  protected abstract String attributePrefix();

  /*
   * Provide the profile adaptor that should be used.
   */
  public abstract ProfileCreator getProfileAdapter(OidcConfiguration config, OidcClient client, Config appConfig);

  protected String getConfigurationValue(String attr, String defaultValue) {
    if (configuration.hasPath(attr)) {
      return configuration.getString(attr);
    }
    return defaultValue;
  }

  protected String getConfigurationValue(String attr) {
    return getConfigurationValue(attr, "");
  }

  protected String getProviderName() {
    return getConfigurationValue(attributePrefix() + "." + providerNameConfigName);
  }

  protected String getClientID() {
    return getConfigurationValue(attributePrefix() + "." + clientIDConfigName);
  }

  protected String getClientSecret() {
    return getConfigurationValue(attributePrefix() + "." + clientSecretConfigName);
  }

  protected String getDiscoveryURI() {
    return getConfigurationValue(attributePrefix() + "." + discoveryURIConfigName);
  }

  protected String getResponseMode() {
    return getConfigurationValue(attributePrefix() + "." + responseModeConfigName);
  }

  protected String getResponseType() {
    // Our local fake IDCS doesn't support 'token' auth.
    if (baseUrl.contains("localhost:")) {
      return "id_token";
    }
    return "id_token token";
  }

  protected String[] getExtraScopes() {
    return getConfigurationValue(attributePrefix() + "." + extraScopesConfigName)
        .split(" ");
  }

  protected String getCallbackURL() {
    return baseUrl + "/callback";
  }

  protected String getScopes() {
    // Scopes are the other things that we want from the OIDC endpoint
    // (needs to also be configured on provider side).
    String[] extraScopes = getExtraScopes();

    ArrayList<String> allClaims = new ArrayList<>();
    Collections.addAll(allClaims, defaultScopes);
    Collections.addAll(allClaims, extraScopes);
    return String.join(" ", allClaims);
  }

  @Override
  public OidcClient get() {
    String clientID = getClientID();
    String clientSecret = getClientSecret();
    String discoveryURI = getDiscoveryURI();
    String responseMode = getResponseMode();
    String responseType = getResponseType();
    String callbackURL = getCallbackURL();
    String providerName = getProviderName(); // optional

    if (clientID.isEmpty() || clientSecret.isEmpty() ||
        discoveryURI.isEmpty() || responseMode.isEmpty() ||
        responseType.isEmpty() || callbackURL.isEmpty()) {
      return null;
    }
    OidcConfiguration config = new OidcConfiguration();

    config.setClientId(clientID);
    config.setSecret(clientSecret);
    config.setDiscoveryURI(discoveryURI);
    // Tells the OIDC provider what type of response to use when it sends info back
    // from the auth request.
    config.setResponseMode(responseMode);
    config.setResponseType(responseType);

    config.setUseNonce(true);
    config.setWithState(false);

    config.setScope(getScopes());

    OidcClient client = new OidcClient(config);

    if (!providerName.isEmpty()) {
      client.setName(providerName);
    }

    client.setCallbackUrl(callbackURL);
    client.setProfileCreator(getProfileAdapter(config, client, config));
    client.setCallbackUrlResolver(new PathParameterCallbackUrlResolver());
    client.init();
    return client;
  }
}
