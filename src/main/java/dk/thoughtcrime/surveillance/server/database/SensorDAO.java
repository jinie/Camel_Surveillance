package dk.thoughtcrime.surveillance.server.database;

import dk.thoughtcrime.surveillance.server.dataobjects.Sensor;

import java.util.List;

/**
 * Created by jimmy on 09/07/15.
 */

public interface SensorDAO {
    List<Sensor> getSensors();
    Sensor getSensor(String host, String sensor);
    void persist(Sensor s);
}
