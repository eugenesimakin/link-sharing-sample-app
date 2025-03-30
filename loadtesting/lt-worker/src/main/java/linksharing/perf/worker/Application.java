package linksharing.perf.worker;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    @Qualifier("metricsQueue")
    public Queue<Metric> metricsQueue() {
        return new ConcurrentLinkedQueue<>();
    }

    @Bean
    @Qualifier("timedRestTemplate")
    public RestTemplate timedRestTemplate(TimingInterceptor timingInterceptor) {
        var restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add(timingInterceptor);
        return restTemplate;
    }

    @Bean
    @Qualifier("restTemplate")
    @Primary
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
