package io.quarkus.cxf_memoryleak_reproducer;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

import com.github.tomakehurst.wiremock.client.WireMock;
import io.quarkus.test.common.DevServicesContext;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import java.util.Map;
import java.util.Optional;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;

public class CalculatorResource implements QuarkusTestResourceLifecycleManager, DevServicesContext.ContextAware {
    private final GenericContainer<?> container = new GenericContainer<>("wiremock/wiremock:3.0.4")
        .withNetworkAliases("wiremock")
        .withClasspathResourceMapping("wiremock", "/home/wiremock", BindMode.READ_ONLY)
        .withExposedPorts(8080);
    private Map<String, String> initArgs;
    private Optional<String> containerNetworkId;

    public static void main(String[] args) {
        new CalculatorResource().start();
    }

    @Override
    public void init(Map<String, String> initArgs) {
        this.initArgs = initArgs;
    }

    @Override
    public Map<String, String> start() {
        this.containerNetworkId.ifPresent(s -> container.withNetwork(new KnownNetwork(s)));
        container.start();

        WireMock.configureFor(container.getHost(), container.getFirstMappedPort());

        WireMock.stubFor(WireMock.post(urlPathEqualTo("/"))
            .withHeader("Content-Type", WireMock.containing("application/soap+xml"))
            .willReturn(aResponse().withBodyFile("bigdata.xml").withHeader("Content-Type", "application/soap+xml;charset=UTF-8")));

        if (Boolean.parseBoolean(initArgs.get("Local"))) {
            return Map.of("calculator.url", "http://%s:%d".formatted(container.getHost(), container.getFirstMappedPort()));
        } else {
            return Map.of("calculator.url", "http://%s:%d".formatted("wiremock", 8080));
        }
    }

    @Override
    public void stop() {
        container.stop();
    }

    @Override
    public void setIntegrationTestContext(DevServicesContext context) {
        containerNetworkId = context.containerNetworkId();
    }

    private class KnownNetwork implements Network {
        private String networkId;

        public KnownNetwork(String networkId) {
            this.networkId = networkId;
        }

        @Override
        public String getId() {
            return networkId;
        }

        @Override
        public void close() {

        }

        @Override
        public Statement apply(Statement base, Description description) {
            return null;
        }
    }
}
