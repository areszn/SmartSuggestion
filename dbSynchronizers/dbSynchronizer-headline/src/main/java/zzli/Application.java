
package zzli;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.system.ApplicationPidFileWriter;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableJpaRepositories
public class Application {

	public static void main(String[] args) throws Exception {
		SpringApplication application=new SpringApplication(Application.class);
		application.addListeners(new ApplicationPidFileWriter("./pid"));
		application.run(args);	}
}
