package dk.thoughtcrime.surveillance.server.routes;

import dk.thoughtcrime.surveillance.server.beans.RRDHelper;
import dk.thoughtcrime.surveillance.server.database.ReadingsDAO;
import dk.thoughtcrime.surveillance.server.database.SensorDAO;
import dk.thoughtcrime.surveillance.server.dataobjects.Sensor;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.spring.SpringRouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Created by jimmy on 13/07/15.
 */
@Component
public class RegisterSensor extends SpringRouteBuilder {
    @Autowired(required = true)
    SensorDAO mgs;

    @Autowired(required = true)
    ReadingsDAO mgr;

    @Autowired(required = true)
    RRDHelper rrd;

    @Override
    public void configure() throws Exception {
        from("direct:registerSensor")
                .routeId("RegisterSensor")
                .process(registerSensorProcessor());
    }

	/**
	 * @return
	 */
	private Processor registerSensorProcessor() {
		return new Processor() {
		    public void process(Exchange exchange) throws Exception {
		        @SuppressWarnings("unchecked")
				Map<String,Object> o = exchange.getIn().getBody(Map.class);
		        String host = (String) o.get("host");
		        String sensor = (String) o.get("sensor");
		        Sensor s = mgs.getSensor(host,sensor);

		        if(s != null)
		            return;

		        Sensor sens = new Sensor();
		        sens.setSensor(sensor);
		        sens.setHost(host);
		        mgs.persist(sens);

		        rrd.createRRDb(sens);
		    }
		};
	}
}
