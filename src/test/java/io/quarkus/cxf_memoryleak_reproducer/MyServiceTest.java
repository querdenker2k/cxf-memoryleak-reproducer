package io.quarkus.cxf_memoryleak_reproducer;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.ResourceArg;
import io.quarkus.test.junit.QuarkusTest;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@QuarkusTest
@QuarkusTestResource(value = CalculatorResource.class, restrictToAnnotatedClass = true, initArgs = {@ResourceArg(name = "Local", value = "true")})
@Disabled
public class MyServiceTest {
    @Test
    public void testHttp() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newBuilder()
            .build();
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:8186/rs/cxf?requests=1"))
            .POST(HttpRequest.BodyPublishers.noBody())
            .build();
        HttpResponse<String> response =
            client.send(request, HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals(2, response.statusCode() / 100);
        System.out.println(response.body());
    }
}
