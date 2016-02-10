package dk.thoughtcrime.surveillance.server.database;


import dk.thoughtcrime.surveillance.server.dataobjects.Reading;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.List;

public class ReadingsDAOImpl implements  ReadingsDAO {

	@Autowired
	SessionFactory sessionFactory;

	public ReadingsDAOImpl(){
		super();
	}

	public ReadingsDAOImpl(SessionFactory sessionFactory){
		super();
		this.sessionFactory = sessionFactory;
	}

	@Transactional
	public void persist(Reading r) {
		Session session = sessionFactory.getCurrentSession();
		session.saveOrUpdate(r);
	}

	@SuppressWarnings("unchecked")
	@Transactional
	public List<Reading> getReadings(String sensorId) {
		Session session = sessionFactory.getCurrentSession();
		List<Reading> sensors = session.createQuery("FROM Reading WHERE sensor=:sensor")
				.setString("sensor", sensorId)
				.list();
		return sensors;
	}

	@Transactional
	public int deleteReadings(int maxAge){
		Session session = sessionFactory.getCurrentSession();
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_MONTH,-maxAge);
		int i = session.createQuery("DELETE FROM Reading WHERE timestamp < ?")
				.setTimestamp(0,cal.getTime())
				.executeUpdate();
		return i;
	}

	@Transactional
	public Reading getReading(long id){
		Session session = sessionFactory.getCurrentSession();
		List<Reading> reading = session.createQuery("FROM Reading WHERE id=:id")
				.setLong("id",id)
				.list();
		if(reading.size() > 0) {
			return reading.get(0);
		}
		return null;
	}
}

