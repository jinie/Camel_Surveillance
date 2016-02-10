package dk.thoughtcrime.surveillance.server.database;

import dk.thoughtcrime.surveillance.server.dataobjects.Sensor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

public class SensorDAOImpl implements SensorDAO {

    @Autowired
    SessionFactory sessionFactory;

    public SensorDAOImpl() {

    }

    public SensorDAOImpl(SessionFactory sessionFactory) {
        super();
        this.sessionFactory = sessionFactory;
    }

    @Transactional
    public void persist(Sensor s) {
        Session sess = sessionFactory.getCurrentSession();
        sess.saveOrUpdate(s);
    }

    @SuppressWarnings("unchecked")
	@Transactional
    public List<Sensor> getSensors() {
        Session session = sessionFactory.getCurrentSession();
        List<Sensor> ret;
        List<Sensor> sensors = session.createQuery("FROM Sensor ORDER BY id").list();
        if(sensors!=null)
            ret = sensors;
        else
        ret = new ArrayList<Sensor>();
        return ret;
    }

    @SuppressWarnings("unchecked")
	@Transactional
    public Sensor getSensor(String host, String sensor) {
        Session session = sessionFactory.getCurrentSession();
        List<Sensor> sensors = session.createQuery("FROM Sensor WHERE host=:host AND sensor=:sensor")
                .setString("host", host)
                .setString("sensor", sensor)
                .list();
        if(null != sensors && sensors.size() > 0){
            return sensors.get(0);
        }
        return null;
    }
}
