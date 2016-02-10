package dk.thoughtcrime.surveillance.server.dataobjects;

import org.springframework.stereotype.Component;

import javax.persistence.*;

@Component
@Entity(name="Reading")
public class Reading {
	@Id
	@SequenceGenerator(name="pk_sequence",sequenceName="reading_id_seq", allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE,generator="pk_sequence")
	private long id;
	private String host;
	@Column(name="sensorId")
	private String sensor;
	private String timestamp;
	private double reading;
	
	public Reading(){
		super();
	}
	
	public Reading(int id, String host, String sensor, String timestamp, double reading) {
		super();
		this.id = id;
		this.host = host;
		this.sensor = sensor;
		this.timestamp = timestamp;
		this.reading = reading;
	}
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public String getSensor() {
		return sensor;
	}
	public void setSensor(String sensor) {
		this.sensor = sensor;
	}
	public String getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	public double getReading() {
		return reading;
	}
	public void setReading(double reading) {
		this.reading = reading;
	}
	
	
}
