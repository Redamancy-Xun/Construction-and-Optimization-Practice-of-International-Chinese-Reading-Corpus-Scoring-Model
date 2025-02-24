package fun.redamancyxun.chinese.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ChineseScoringModelApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChineseScoringModelApplication.class, args);
    }

}
