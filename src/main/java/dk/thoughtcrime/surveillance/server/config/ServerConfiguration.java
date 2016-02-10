package dk.thoughtcrime.surveillance.server.config;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy;
import ch.qos.logback.core.rolling.TimeBasedFileNamingAndTriggeringPolicy;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import dk.thoughtcrime.surveillance.server.beans.RRDHelper;
import dk.thoughtcrime.surveillance.server.database.ReadingsDAO;
import dk.thoughtcrime.surveillance.server.database.ReadingsDAOImpl;
import dk.thoughtcrime.surveillance.server.database.SensorDAO;
import dk.thoughtcrime.surveillance.server.database.SensorDAOImpl;
import dk.thoughtcrime.surveillance.server.dataobjects.Reading;
import dk.thoughtcrime.surveillance.server.dataobjects.Sensor;
import dk.thoughtcrime.surveillance.server.dataobjects.Surveillance;
import org.apache.camel.spi.ThreadPoolProfile;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.io.File;

/**
 * Created by jimmy on 08/07/15.
 */

@Configuration
@EnableTransactionManagement
public class ServerConfiguration {

	@Value("${prefix}")
	String prefix;

	@Value("${database.user}")
	String databaseUser;

	@Value("${database.password}")
	String databasePassword;

    @Value("${database.url}")
    String databaseUrl;

    @Value("${mqtt.host}")
    String mqttHost;

    @Value("${mqtt.port}")
    String mqttPort;

	@Value("${logging.file}")
	String logFile;

	@Bean
	public DriverManagerDataSource dataSource(){
		DriverManagerDataSource ds = new DriverManagerDataSource();
		ds.setDriverClassName("org.h2.Driver");
		ds.setUrl(databaseUrl);
		ds.setUsername(databaseUser);
		ds.setPassword(databasePassword);

		return ds;
	}

	@Bean LocalSessionFactoryBean sessionFactory(){
		LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
		sessionFactory.setDataSource(dataSource());
		org.hibernate.cfg.Configuration cfg = new org.hibernate.cfg.Configuration();
		cfg.addAnnotatedClass(Sensor.class);
		cfg.addAnnotatedClass(Reading.class);
		cfg.addAnnotatedClass(Surveillance.class);
		cfg.setProperty("hbm2ddl.auto", "update");
		cfg.setProperty("show_sql", "false");
		cfg.setProperty("format_sql", "false");
		cfg.setProperty("hibernate.c3p0.min_size", "5");
		cfg.setProperty("hibernate.c3p0.max_size", "20");
		cfg.setProperty("hibernate.c3p0.timeout", "300");
		cfg.setProperty("hibernate.c3p0.max_statements", "50");
		cfg.setProperty("hibernate.c3p0.idle_test_period", "3000");
		cfg.setProperty("hibernate.connection.url",databaseUrl);
		cfg.setProperty("hibernate.dialect","org.hibernate.dialect.H2Dialect");
		sessionFactory.setHibernateProperties(cfg.getProperties());
		sessionFactory.setPackagesToScan("dk.thoughtcrime.surveillance.server.dataobjects");
		return sessionFactory;
	}

	@Bean
	@Autowired
	public HibernateTransactionManager transactionManager(SessionFactory sessionFactory) {
		HibernateTransactionManager txManager = new HibernateTransactionManager();
		txManager.setSessionFactory(sessionFactory);
		txManager.setDataSource(dataSource());

		return txManager;
	}

	@Bean
	public ReadingsDAO readingsDAO(){
		return new ReadingsDAOImpl();
	}
	
	@Bean
	public SensorDAO sensorDAO(){
		return new SensorDAOImpl();
	}

	@Bean
	RRDHelper graphCreator() { return new RRDHelper(); }

    @Bean
    public String mqtt(){
        return String.format("%s:%s",mqttHost,mqttPort);
    }

	@Bean
	public ThreadPoolProfile threadPoolProfile(){
		ThreadPoolProfile ret = new ThreadPoolProfile("defaultThreadPoolProfile");
		ret.setMaxPoolSize(5);
		ret.setDefaultProfile(true);
		ret.setPoolSize(2);
		return ret;
	}
}
