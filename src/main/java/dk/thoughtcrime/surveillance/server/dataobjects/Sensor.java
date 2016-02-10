package dk.thoughtcrime.surveillance.server.dataobjects;

import org.springframework.stereotype.Component;

import javax.persistence.*;


@Component
@Entity(name="Sensor")
public class Sensor {

	@Id
	@SequenceGenerator(name="pk_sequence",sequenceName="sensors_id_seq", allocationSize=1)
	@GeneratedValue(strategy= GenerationType.SEQUENCE,generator="pk_sequence")
	private long id;
	private String host;
	private String sensor;
	private String alias;
	private boolean rrdGraph;
	private long last_update;
	private double alert_low;
	private double alert_high;
	private long alert_date;
	private boolean notification_sent;
	private double alert_temp;
	private String graphColor;
	
	public Sensor(){
		super();
	}

	public Sensor(long id, String host, String sensor, String alias, boolean rrdGraph, long last_update, double alert_low,
			double alert_high, long alert_date, boolean notification_sent, double alert_temp, String graphColor) {
		super();
		this.id = id;
		this.host = host;
		this.sensor = sensor;
		this.alias = alias;
		this.rrdGraph = rrdGraph;
		this.last_update = last_update;
		this.alert_low = alert_low;
		this.alert_high = alert_high;
		this.alert_date = alert_date;
		this.notification_sent = notification_sent;
		this.alert_temp = alert_temp;
		this.graphColor = graphColor;
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
	public String getAlias() {
		return alias;
	}
	public void setAlias(String alias) {
		this.alias = alias;
	}
	public boolean isRrdGraph() {
		return rrdGraph;
	}
	public void setRrdGraph(boolean rrdGraph) {
		this.rrdGraph = rrdGraph;
	}
	public long getLast_update() {
		return last_update;
	}
	public void setLast_update(long last_update) {
		this.last_update = last_update;
	}
	public double getAlert_low() {
		return alert_low;
	}
	public void setAlert_low(double alert_low) {
		this.alert_low = alert_low;
	}
	public double getAlert_high() {
		return alert_high;
	}
	public void setAlert_high(double alert_high) {
		this.alert_high = alert_high;
	}
	public long getAlert_date() {
		return alert_date;
	}
	public void setAlert_date(long alert_date) {
		this.alert_date = alert_date;
	}
	public boolean isNotification_sent() {
		return notification_sent;
	}
	public void setNotification_sent(boolean notification_sent) {
		this.notification_sent = notification_sent;
	}
	public double getAlert_temp() {
		return alert_temp;
	}
	public void setAlert_temp(double alert_temp) {
		this.alert_temp = alert_temp;
	}

	public String getGraphColor() {
		return graphColor;
	}

	public void setGraphColor(String graphColor) {
		this.graphColor = graphColor;
	}


}
