package io.quarkus.cxf_memoryleak_reproducer;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled
public class MyManualTest {
    @Test
    public void testManually() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newBuilder()
            .build();
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:8185/rs/cxf"))
            .POST(HttpRequest.BodyPublishers.noBody())
            .build();
        HttpResponse<String> response =
            client.send(request, HttpResponse.BodyHandlers.ofString());
        Assertions.assertEquals(2, response.statusCode() / 100);
        System.out.println(response.body());
    }
}
