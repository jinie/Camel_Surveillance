package dk.thoughtcrime.surveillance.server.routes;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.spring.SpringRouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jimmy on 15/07/15.
 */
@Component
public class SendNotificationRoute extends SpringRouteBuilder {
    @Autowired
    String mqtt;



    @Override
    public void configure() throws Exception {
        from("direct:sendNotificationRoute")
                .routeId("SendNotificationRoute")
                .process(new Processor() {
                    public void process(Exchange exchange) throws Exception {
                        Map<String,Object> notification = new HashMap<String, Object>();
                        notification.put("notification",exchange.getIn().getBody());
                        exchange.getIn().setBody(notification);
                    }
                })
                .marshal().json(JsonLibrary.Gson, Map.class)
                .to("mqtt://notification?host=tcp://"+mqtt+"&publishTopicName=/surveillance/notification/temperature/alert");


    }
}
