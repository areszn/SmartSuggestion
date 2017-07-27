package zzli;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.system.ApplicationPidFileWriter;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class Application
{
    public static void main(String[] args) {
        SpringApplication application=new SpringApplication(Application.class);
        application.addListeners(new ApplicationPidFileWriter("./pid"));
        application.run(args);
    }
}
