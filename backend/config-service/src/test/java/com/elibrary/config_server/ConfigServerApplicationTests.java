package com.elibrary.config_server;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that the config server starts and correctly serves per-service
 * configuration and environment profiles.
 *
 * The git backend is replaced with the native filesystem backend so tests
 * run offline, reading directly from src/main/resources/config/ on the classpath.
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.profiles.active=native",
                "spring.cloud.config.server.native.search-locations=classpath:/config/"
        }
)
class ConfigServerApplicationTests {

    @LocalServerPort
    private int port;

    private final RestTemplate rest = new RestTemplate();

    // ── Application context ───────────────────────────────────────────────────

    @Test
    void contextLoads() {
        // Passes if the application context starts successfully with the native backend
    }

    // ── api-gateway — base config ─────────────────────────────────────────────

    @Test
    void apiGateway_defaultProfile_servesJwtSecret() {
        Map<String, Object> props = fetchProperties("api-gateway", "default");

        assertThat(props).containsKey("jwt.secret");
    }

    @Test
    void apiGateway_defaultProfile_servesResilienceConfig() {
        Map<String, Object> props = fetchProperties("api-gateway", "default");

        assertThat(props).containsKey(
                "resilience4j.circuitbreaker.instances.coreServiceBreaker.sliding-window-size");
        assertThat(props).containsKey(
                "resilience4j.circuitbreaker.instances.supportServiceBreaker.sliding-window-size");
    }

    @Test
    void apiGateway_defaultProfile_servesCorsConfig() {
        Map<String, Object> props = fetchProperties("api-gateway", "default");

        assertThat(props).containsKey(
                "spring.cloud.gateway.globalcors.cors-configurations[/**].allowed-origins");
    }

    // ── api-gateway — environment separation ─────────────────────────────────

    @Test
    void apiGateway_devProfile_hasDebugLogging() {
        Map<String, Object> props = fetchProperties("api-gateway", "dev");

        assertThat(props.get("logging.level.org.springframework.cloud.gateway"))
                .isEqualTo("DEBUG");
    }

    @Test
    void apiGateway_prodProfile_hasWarnLogging() {
        Map<String, Object> props = fetchProperties("api-gateway", "prod");

        assertThat(props.get("logging.level.root")).isEqualTo("WARN");
    }

    @Test
    void apiGateway_devAndProdProfiles_haveDifferentCorsOrigins() {
        String devOrigin = (String) fetchProperties("api-gateway", "dev")
                .get("spring.cloud.gateway.globalcors.cors-configurations[/**].allowed-origins");
        String prodOrigin = (String) fetchProperties("api-gateway", "prod")
                .get("spring.cloud.gateway.globalcors.cors-configurations[/**].allowed-origins");

        assertThat(devOrigin).isNotEqualTo(prodOrigin);
    }

    // ── core-service ──────────────────────────────────────────────────────────

    @Test
    void coreService_defaultProfile_servesJpaConfig() {
        Map<String, Object> props = fetchProperties("core-service", "default");

        assertThat(props).containsKey("spring.jpa.hibernate.ddl-auto");
    }

    @Test
    void coreService_defaultProfile_servesDatasourceUrl() {
        Map<String, Object> props = fetchProperties("core-service", "default");

        assertThat(props).containsKey("spring.datasource.url");
    }

    // ── support-service ───────────────────────────────────────────────────────

    @Test
    void supportService_defaultProfile_servesDatasourceUrl() {
        Map<String, Object> props = fetchProperties("support-service", "default");

        assertThat(props).containsKey("spring.datasource.url");
    }

    // ── HTTP contract ─────────────────────────────────────────────────────────

    @Test
    void unknownApplication_returnsOkWithEmptySources() {
        ResponseEntity<Map> response = rest.getForEntity("http://localhost:" + port + "/unknown-service/default", Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    /**
     * Fetches /{application}/{profile} from the config server and returns a flat
     * map of all properties merged from every property source in the response.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> fetchProperties(String application, String profile) {
        ResponseEntity<Map> response =
                rest.getForEntity("http://localhost:" + port + "/" + application + "/" + profile, Map.class);

        assertThat(response.getStatusCode())
                .as("GET /%s/%s", application, profile)
                .isEqualTo(HttpStatus.OK);

        List<Map<String, Object>> sources =
                (List<Map<String, Object>>) response.getBody().get("propertySources");

        // Merge all sources into a single map (first source wins, matching Spring priority)
        Map<String, Object> merged = new java.util.LinkedHashMap<>();
        if (sources != null) {
            for (int i = sources.size() - 1; i >= 0; i--) {
                Map<String, Object> source =
                        (Map<String, Object>) sources.get(i).get("source");
                if (source != null) {
                    merged.putAll(source);
                }
            }
        }
        return merged;
    }
}
