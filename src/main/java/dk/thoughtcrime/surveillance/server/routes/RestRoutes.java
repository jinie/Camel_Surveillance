package dk.thoughtcrime.surveillance.server.routes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.ExchangePattern;
import org.apache.camel.component.restlet.DefaultRestletBinding;
import org.apache.camel.component.restlet.RestletBinding;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.spring.SpringRouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import dk.thoughtcrime.surveillance.server.database.ReadingsDAO;
import dk.thoughtcrime.surveillance.server.database.SensorDAO;
import dk.thoughtcrime.surveillance.server.dataobjects.Reading;
import dk.thoughtcrime.surveillance.server.dataobjects.Sensor;
import dk.thoughtcrime.surveillance.server.processors.NotificationParserProcessor;
import dk.thoughtcrime.surveillance.server.processors.RegisterReadingProcessor;
import net.pushover.client.PushoverClient;
import net.pushover.client.PushoverMessage;
import net.pushover.client.PushoverRestClient;
import net.pushover.client.Status;

/**
 * Created by jimmy on 18/11/2015.
 */
@Component
public class RestRoutes extends SpringRouteBuilder {

    @Value("${pushover.token}")
    String pushoverToken;

    @Value("${pushover.user}")
    String pushoverUser;

    @Autowired(required = true)
    SensorDAO mgs;

    @Autowired(required = true)
    ReadingsDAO rda;

    @Autowired
    RegisterReadingProcessor registerReadingProcessor;

    @Autowired
    CamelContext camelContext;

    @Override
    public void configure() throws Exception {
        RestletBinding b = new DefaultRestletBinding();
        restConfiguration().component("restlet").host("localhost").port(8282).bindingMode(RestBindingMode.auto).endpointProperty("synchronous", "true");
        rest("/surveillance")
            .put("/reading").to("direct:registerRestReadingRoute")
            .get("/reading/{id}").to("direct:getReading")
            .get("/readings").to("direct:getReadings")
            .put("/notification").to("direct:restNotification")
            .put("/register_sensor/").to("direct:registerRestSensorRoute")
            .get("/sensor/{id}").to("direct:getSensor")
            .get("/sensor/{id}/reading").to("direct:getSensorReadings")
            .get("/sensors").to("direct:getSensors");

        from("direct:getSensors")
            .routeId("restGetSensors")
            .setExchangePattern(ExchangePattern.InOut)
            .process(exchange -> {
                Map<String, Object> ret = new HashMap<String, Object>();
                for (Sensor s : mgs.getSensors()) {
                    ret.put(s.getSensor(), s);
                }
                exchange.getIn().setBody(ret);
            })
            .marshal().json(JsonLibrary.Gson);

        from("direct:restNotification")
            .routeId("restNotification")
            .unmarshal().json(JsonLibrary.Gson, Map.class)
            .process(exchange -> {
                @SuppressWarnings("unchecked")
                Map<String, Object> o = (Map<String, Object>) exchange.getIn().getBody(Map.class);
                final String message = (String) o.getOrDefault("notification", null);
                if (message == null) {
                    throw new RuntimeException("Unknown Message Received :" + exchange.getIn().getBody());
                }
                log.info("Sending notification " + message);

                PushoverClient client = new PushoverRestClient();
                Status result = client.pushMessage(PushoverMessage.builderWithApiToken(pushoverToken)
                        .setUserId(pushoverUser)
                        .setMessage(message)
                        .build());
                exchange.getIn().setBody(result);
            })
            .marshal().json(JsonLibrary.Gson);

        from("direct:registerRestReadingRoute")
            .routeId("restRegisterReading")
            .unmarshal().json(JsonLibrary.Gson, Map.class)
            .process(new NotificationParserProcessor())
            .to("direct:registerReading");

        from("direct:registerRestSensorRoute")
            .routeId("restRegisterSensor")
            .unmarshal().json(JsonLibrary.Gson, Map.class)
            .process(new NotificationParserProcessor())
            .to("direct:registerSensor");

        from("direct:getSensor")
            .routeId("restGetSensor")
            .process(exchange -> {
                List<Sensor> ret = new ArrayList<Sensor>();
                String id = exchange.getIn().getHeader("id", String.class);
                for (Sensor s : mgs.getSensors()) {
                    if (s.getSensor().compareTo(id) == 0)
                        ret.add(s);
                }
                exchange.getIn().setBody(ret);
            })
            .marshal().json(JsonLibrary.Gson);

        from("direct:getSensorReadings")
            .routeId("restGetSensorReadings")
            .process(exchange -> {
                String id = exchange.getIn().getHeader("id", String.class);
                List<Reading> ret = rda.getReadings(id);
                exchange.getIn().setBody(ret);
            })
            .marshal().json(JsonLibrary.Gson);

        from("direct:getReadings")
            .routeId("restGetReadings")
            .process(exchange -> {
                ArrayList<Reading> ret = new ArrayList<Reading>();
                for (Sensor s : mgs.getSensors()) {
                    ret.addAll(rda.getReadings(s.getSensor()));
                }
                exchange.getIn().setBody(ret);
            })
            .marshal().json(JsonLibrary.Gson);


        from("direct:getReading")
            .routeId("restGetReading")
            .process(exchange -> {
                long id = exchange.getIn().getHeader("id", Long.class);
                exchange.getIn().setBody(rda.getReading(id));
            })
            .marshal().json(JsonLibrary.Gson);
    }
}
