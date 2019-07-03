package weather;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;

//@Configuration

/*
@Bean
public RestTemplate restTemplate(RestTemplateBuilder builder) {
    return builder.build();
}
*/

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WhatsTheWeatherApplication {

    public static void main(String[] args) {
        SpringApplication.run(WhatsTheWeatherApplication.class, args);
    }

}
