package dk.thoughtcrime.surveillance.server.routes;

import dk.thoughtcrime.surveillance.server.database.ReadingsDAO;
import org.apache.camel.spring.SpringRouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by jimmy on 23/11/2015.
 */


@Component
public class DatabaseMaintenance extends SpringRouteBuilder{

    @Autowired
    ReadingsDAO rda;

    @Override
    public void configure() throws Exception {
        from("quartz2://databaseMaintenanceTimer?cron=0+0+0+*+*+?")
                .routeId("DatabaseMaintenance")
                .process(exchange -> {
                    rda.deleteReadings(30);
                });
    }
}
