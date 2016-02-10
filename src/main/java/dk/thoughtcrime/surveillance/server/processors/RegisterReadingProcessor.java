package dk.thoughtcrime.surveillance.server.processors;

import dk.thoughtcrime.surveillance.server.beans.RRDHelper;
import dk.thoughtcrime.surveillance.server.database.ReadingsDAO;
import dk.thoughtcrime.surveillance.server.database.SensorDAO;
import dk.thoughtcrime.surveillance.server.dataobjects.Reading;
import dk.thoughtcrime.surveillance.server.dataobjects.Sensor;
import dk.thoughtcrime.surveillance.server.routes.SendNotificationRoute;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jimmy on 18/11/2015.
 */
@Component
public class RegisterReadingProcessor implements Processor{
    @Autowired(required = true)
    SensorDAO mgs;

    @Autowired(required = true)
    ReadingsDAO mgr;

    @Autowired(required = true)
    RRDHelper rrd;

    @Autowired(required = true)
    ProducerTemplate template;

    private Logger log = LoggerFactory.getLogger(RegisterReadingProcessor.class);
    public void process(Exchange exchange) throws Exception {
        String host;
        String sensor;
        String timestamp;
        @SuppressWarnings("unchecked")
		Map<String, Object> o = exchange.getIn().getBody(Map.class);
        host = (String) o.get("host");
        sensor = (String) o.get("sensor");
        timestamp = (String) o.get("timestamp");
        double reading = (Double) o.get("reading");

        Sensor s = mgs.getSensor(host, sensor);
        if(s!=null){
            if(s.isNotification_sent()){
                log.info("Sensor "+s.getHost()+":"+s.getSensor()+" is OK again");
                template.sendBody("direct:sendNotificationRoute","Sensor "+s.getHost()+":"+s.getSensor()+" is OK again");
                s.setNotification_sent(false);
            }
            s.setLast_update(new Date().getTime());
            mgs.persist(s);

            Reading r = new Reading();
            r.setHost(host);
            r.setSensor(sensor);
            r.setTimestamp(timestamp);
            r.setReading(reading);
            mgr.persist(r);

            rrd.updateReading(s,r);
        }
    }
}
