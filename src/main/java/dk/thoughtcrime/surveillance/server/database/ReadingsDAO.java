package dk.thoughtcrime.surveillance.server.database;

import dk.thoughtcrime.surveillance.server.dataobjects.Reading;

import java.util.List;

/**
 * Created by jimmy on 09/07/15.
 */
public interface ReadingsDAO {
    void persist(Reading r);
    List<Reading> getReadings(String sensorId);
    Reading getReading(long id);
    public int deleteReadings(int maxAge);
}
