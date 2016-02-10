package dk.thoughtcrime.surveillance.server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.camel.spring.Main;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.FileAppender;

@SpringBootApplication
@ComponentScan("dk.thoughtcrime.surveillance.server")
@Configuration
public class SurveillanceServer {
    private Main main;
	
	public static void main(String [] args) throws Exception{
		SpringApplication.run(SurveillanceServer.class, args);

//        SurveillanceServer server = new SurveillanceServer();
//        server.boot();
	}
    public void boot() throws Exception {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
        // create a Main instance
        main = new Main();
        main.setApplicationContext(context);

        // enable hangup support so you can press ctrl + c to terminate the JVM
        main.enableHangupSupport();
        //Logger log = LoggerFactory.getLogger(this.getClass().getName());
        main.run();
    }

	
	public SurveillanceServer(){

	}
}
