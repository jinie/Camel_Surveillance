package dk.thoughtcrime.surveillance.server.routes;

import dk.thoughtcrime.surveillance.server.processors.NotificationParserProcessor;
import org.apache.camel.ExchangePattern;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.spring.SpringRouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class SurveillanceTopicRoute extends SpringRouteBuilder {

    @Autowired
    String mqtt;

    @Override
    public void configure() throws Exception {
        from("mqtt://temperature?host=tcp://"+mqtt+"&subscribeTopicNames=/surveillance/temperature/#")
                .routeId("SurveillanceTopicRoute")
                .setExchangePattern(ExchangePattern.InOnly)
                .unmarshal().json(JsonLibrary.Gson, Map.class)
                .process(new NotificationParserProcessor())
                .choice()
                    .when(exchangeProperty("operationType").isEqualTo("reading"))
                        .to("direct:registerReading")
                    .when(exchangeProperty("operationType").isEqualTo("register_sensor"))
                        .to("direct:registerSensor")
                    .otherwise()
                        .throwException(new RuntimeException("Unknown object type ${exchange[operationType]}"))
                .end();


    }

}
