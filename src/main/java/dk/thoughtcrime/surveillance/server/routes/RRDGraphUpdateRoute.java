package dk.thoughtcrime.surveillance.server.routes;

import dk.thoughtcrime.surveillance.server.beans.RRDHelper;
import dk.thoughtcrime.surveillance.server.database.ReadingsDAO;
import dk.thoughtcrime.surveillance.server.database.SensorDAO;
import dk.thoughtcrime.surveillance.server.dataobjects.Sensor;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.spring.SpringRouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Created by jimmy on 15/07/15.
 */
@Component
public class RRDGraphUpdateRoute extends SpringRouteBuilder {
    @Autowired(required = true)
    SensorDAO mgs;

    @Autowired(required = true)
    ReadingsDAO mgr;

    @Autowired(required = true)
    RRDHelper rrd;


    @Override
    public void configure() throws Exception {
        from("quartz2://rrdUpdateTimer?cron=0+*/10+*+*+*+?")
                .routeId("RRDUpdateRoute")
                .process(updateRRDGraphProcessor());
    }


	/**
	 * @return
	 */
	private Processor updateRRDGraphProcessor() {
		return new Processor() {
		    public void process(Exchange exchange) throws Exception {
		        long now = new Date().getTime();
		        for(Sensor s :mgs.getSensors()) {
		            if((now - s.getLast_update())/1000 > 600 && !s.isNotification_sent()) {
		                String alert = "Sensor " + s.getHost() + ":" + s.getSensor() + ", last updated " + new Date(s.getLast_update()).toString();
		                ProducerTemplate template = exchange.getContext().createProducerTemplate();
		                template.sendBody("direct:sendNotificationRoute", alert);
		                s.setNotification_sent(true);
		                mgs.persist(s);
		            }
		        }
		        rrd.updateGraphs();
		    }
		};
	}
}
