package dk.thoughtcrime.surveillance.server.processors;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import java.util.Map;

/**
 * Created by jimmy on 18/11/2015.
 */
public class NotificationParserProcessor implements Processor{
    public void process(Exchange exchange) throws Exception {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> o = (Map<String, Object>) exchange.getIn().getBody(Map.class);
                    String ss = null;
                    for (String s : o.keySet()) {
                        ss = s;
                        exchange.setProperty("operationType", s);
                    }
                    exchange.getIn().setBody(o.get(ss));
                }
}
