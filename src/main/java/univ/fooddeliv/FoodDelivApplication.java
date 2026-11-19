package univ.fooddeliv;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FoodDelivApplication {

    public static void main(String[] args) {
        SpringApplication.run(FoodDelivApplication.class, args);
    }

}
