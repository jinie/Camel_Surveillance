package dk.thoughtcrime.surveillance.server.routes;

import net.pushover.client.PushoverClient;
import net.pushover.client.PushoverMessage;
import net.pushover.client.PushoverRestClient;
import net.pushover.client.Status;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Processor;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.spring.SpringRouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Created by jimmy on 29/09/2015.
 */

@Component
public class PushoverNotificationRoute extends SpringRouteBuilder {

    @Autowired
    String mqtt;

    @Value("${pushover.token}")
    String pushoverToken;

    @Value("${pushover.user}")
    String pushoverUser;
    private Logger log = LoggerFactory.getLogger(PushoverNotificationRoute.class);

    @Override
    public void configure() throws Exception {
        from("mqtt://temperature?host=tcp://"+mqtt+"&subscribeTopicNames=/surveillance/notification/#")
                .routeId("PushoverNotification")
                .setExchangePattern(ExchangePattern.InOnly)
                .unmarshal().json(JsonLibrary.Gson, Map.class)
                .process(pushoverMessageProcessor());
    }

	/**
	 * @return
	 */
	private Processor pushoverMessageProcessor() {
		return new Processor() {
		    public void process(Exchange exchange) throws Exception {
		        @SuppressWarnings("unchecked")
				Map<String, Object> o = (Map<String, Object>) exchange.getIn().getBody(Map.class);
		        final String message = (String)o.getOrDefault("notification",null);
		        if(message == null) {
		            throw new RuntimeException("Unknown Message Received :" + exchange.getIn().getBody());
		        }
		        log.info("Sending notification "+message);

		        PushoverClient client = new PushoverRestClient();
		        Status result = client.pushMessage(PushoverMessage.builderWithApiToken(pushoverToken)
		                .setUserId(pushoverUser)
		                .setMessage(message)
		                .build());
		        exchange.getIn().setBody(result);
		    }
		};
	}
}
