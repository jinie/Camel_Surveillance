package dk.thoughtcrime.surveillance.server.routes;

import dk.thoughtcrime.surveillance.server.processors.RegisterReadingProcessor;
import org.apache.camel.spring.SpringRouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by jimmy on 13/07/15.
 */
@Component
public class RegisterReading extends SpringRouteBuilder {

    @Autowired
    RegisterReadingProcessor registerReadingProcessor;
    @Override
    public void configure() throws Exception {
        from("direct:registerReading")
                .routeId("RegisterReading")
                .process(registerReadingProcessor);
    }


}
