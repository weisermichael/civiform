package auth.oidc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import auth.ProfileFactory;
import auth.oidc.applicant.IdcsProfileAdapter;
import auth.oidc.applicant.IdcsProvider;
import com.google.common.collect.ImmutableMap;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import java.util.stream.Stream;
import javax.inject.Provider;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.pac4j.core.profile.creator.ProfileCreator;
import org.pac4j.oidc.client.OidcClient;
import org.pac4j.oidc.config.OidcConfiguration;
import play.api.test.Helpers;
import repository.ResetPostgres;
import repository.UserRepository;

public class OidcProviderTest extends ResetPostgres {
  private OidcProvider oidcProvider;
  private ProfileFactory profileFactory;
  private static UserRepository userRepository;
  private Config config;

  @Before
  public void setup() {
    userRepository = instanceOf(UserRepository.class);
    profileFactory = instanceOf(ProfileFactory.class);
    config = ConfigFactory.parseMap(oidcConfig());

    // Just need some complete adaptor to access methods.
    oidcProvider =
        new IdcsProvider(
            config,
            profileFactory,
            new Provider<UserRepository>() {
              @Override
              public UserRepository get() {
                return userRepository;
              }
            });
  }

  public static ImmutableMap<String, Object> oidcConfig() {
    return ImmutableMap.of(
        "idcs.client_id",
        "foo",
        "idcs.secret",
        "bar",
        "idcs.discovery_uri",
        "http://dev-oidc:3390/.well-known/openid-configuration",
        "base_url",
        String.format("http://localhost:%d", Helpers.testServerPort()));
  }

  @Test
  public void Test_getConfigurationValues() {
    String client_id = oidcProvider.getClientID();
    assertThat(client_id).isEqualTo("foo");

    String client_secret = oidcProvider.getClientSecret();
    assertThat(client_secret).isEqualTo("bar");
  }

  static Stream<Arguments> provideConfigsForGet() {
    return Stream.of(
        Arguments.of(
            "normal",
            "id_token",
            ImmutableMap.of(
                "idcs.client_id", "foo",
                "idcs.secret", "bar",
                "idcs.discovery_uri", "http://dev-oidc:3390/.well-known/openid-configuration",
                "base_url", String.format("http://localhost:%d", Helpers.testServerPort()))),
        Arguments.of(
            "Not Localhost",
            "id_token token",
            ImmutableMap.of(
                "idcs.client_id", "happy",
                "idcs.secret", "lucky",
                "idcs.discovery_uri", "http://civiform.dev/.well-known/openid-configuration",
                "base_url", "http://civiform.dec")),
        Arguments.of(
            "extra args that aren't used",
            "id_token",
            ImmutableMap.of(
                "idcs.client_id", "foo",
                "idcs.secret", "bar",
                "idcs.provider_name", "Provider Name here",
                "idcs.response_mode", "Try to override",
                "idcs.additional_scopes", "No more scopes",
                "idcs.discovery_uri", "http://dev-oidc:3390/.well-known/openid-configuration",
                "base_url", String.format("http://localhost:%d", Helpers.testServerPort()))));
  }

  @ParameterizedTest(name = "{index} {0} config should be parsable")
  @MethodSource("provideConfigsForGet")
  public void Test_get(String name, String wantResponseType, ImmutableMap<String, String> c) {
    Config config = ConfigFactory.parseMap(c);

    OidcProvider oidcProvider =
        new IdcsProvider(
            config,
            profileFactory,
            new Provider<UserRepository>() {
              @Override
              public UserRepository get() {
                return userRepository;
              }
            });
    OidcClient client = oidcProvider.get();

    assertThat(client.getCallbackUrl()).isEqualTo(c.get("base_url") + "/callback");
    assertThat(client.getName()).isEqualTo("");

    OidcConfiguration client_config = client.getConfiguration();

    assertThat(client_config.getClientId()).isEqualTo(c.get("idcs.client_id"));
    assertThat(client_config.getSecret()).isEqualTo(c.get("idcs.secret"));
    assertThat(client_config.getDiscoveryURI()).isEqualTo(c.get("idcs.discovery_uri"));
    assertThat(client_config.getScope()).isEqualTo("openid profile email");
    assertThat(client_config.getResponseType()).isEqualTo(wantResponseType);
    assertThat(client_config.getResponseMode()).isEqualTo("form_post");

    ProfileCreator adaptor = client.getProfileCreator();

    assertThat(adaptor.getClass()).isEqualTo(IdcsProfileAdapter.class);
  }

  static Stream<Arguments> provideConfigsForInvalidConfig() {
    return Stream.of(
        Arguments.of(
            "normal",
            ImmutableMap.of(
                "idcs.client_id", "foo",
                "idcs.secret", "bar",
                "idcs.discovery_uri", "http://dev-oidc:3390/.well-known/openid-configuration",
                "base_url", String.format("http://localhost:%d", Helpers.testServerPort()))),
        Arguments.of(
            "blank client_id",
            ImmutableMap.of(
                "idcs.client_id", "",
                "idcs.secret", "bar",
                "idcs.discovery_uri", "http://dev-oidc:3390/.well-known/openid-configuration",
                "base_url", String.format("http://localhost:%d", Helpers.testServerPort()))),
        Arguments.of(
            "blank secret",
            ImmutableMap.of(
                "idcs.client_id", "foo",
                "idcs.secret", "bar",
                "idcs.discovery_uri", "http://dev-oidc:3390/.well-known/openid-configuration",
                "base_url", String.format("http://localhost:%d", Helpers.testServerPort()))),
        Arguments.of(
            "missing secret",
            ImmutableMap.of(
                "idcs.client_id", "foo",
                "idcs.discovery_uri", "http://dev-oidc:3390/.well-known/openid-configuration",
                "base_url", String.format("http://localhost:%d", Helpers.testServerPort()))),
        Arguments.of(
            "missing base_url",
            ImmutableMap.of(
                "idcs.client_id", "foo",
                "idcs.secret", "bar",
                "idcs.discovery_uri", "http://dev-oidc:3390/.well-known/openid-configuration")),
        Arguments.of(
            "blank discovery uri",
            ImmutableMap.of(
                "idcs.client_id", "foo",
                "idcs.secret", "bar",
                "idcs.discovery_uri", "",
                "base_url", String.format("http://localhost:%d", Helpers.testServerPort()))));
  }

  @ParameterizedTest(name = "{index} {0} should throw exception")
  @MethodSource("provideConfigsForInvalidConfig")
  public void invalidConfig(String name, ImmutableMap<String, String> c) {
    Config empty_secret_config = ConfigFactory.parseMap(c);

    OidcProvider badOidcProvider =
        new IdcsProvider(
            empty_secret_config,
            profileFactory,
            new Provider<UserRepository>() {
              @Override
              public UserRepository get() {
                return userRepository;
              }
            });
    try {
      badOidcProvider.get();
      fail("Should not have successfully gotten an imcomplete provider.");
    } catch (RuntimeException e) {
      // pass.
    }
  }
}
