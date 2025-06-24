package uk.gov.justice.laa.maat.scheduled.tasks;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class MaatScheduledTasksApplication {

	public static void main(String[] args) {
		SpringApplication.run(MaatScheduledTasksApplication.class, args);
	}

}
