package io.quarkus.cxf_memoryleak_reproducer;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.soap.SOAPBinding;
import java.util.Map;
import javax.xml.namespace.QName;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tempuri.CalculatorSoap;

@Path("/cxf")
public class MyService {
    private static final Logger log = LoggerFactory.getLogger(MyService.class);

    @ConfigProperty(name = "calculator.url")
    String url;

    @POST
    public String forward(@QueryParam("requests") int requestCount) {
        int response =
            0;
        for (int i = 0; i < requestCount; i++) {
            log.info("request nr: {}", i);
            CalculatorSoap client = createClient(this.url);
            response = client.bigData(new byte[1024 * 100]);
        }
        return "received: " + response;
    }

    private CalculatorSoap createClient(String url) {
        QName qName = new QName("http://tempuri.org/", "Calculator");
        final Service service = Service.create(MyService.class.getResource("/wsdl/calculator.wsdl"), qName);
        service.addPort(qName, SOAPBinding.SOAP12HTTP_BINDING, url);
        CalculatorSoap port = service.getPort(CalculatorSoap.class);

        BindingProvider bindingProvider = (BindingProvider) port;
        Map<String, Object> requestContext = bindingProvider.getRequestContext();
        requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, url);

        Conduit conduit = ClientProxy.getClient(port).getConduit();
        if (conduit instanceof HTTPConduit httpConduit) {
            HTTPClientPolicy client = httpConduit.getClient();
            client.setVersion("1.1");
        }
        return port;
    }
}
