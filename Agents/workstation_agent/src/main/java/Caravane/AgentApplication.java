package Caravane;

import Caravane.publisher.ListnerPublisher;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class AgentApplication {

    public static void main(String[] args) {
        // Start Spring Boot context
        ApplicationContext context = SpringApplication.run(AgentApplication.class, args);


    }
}
