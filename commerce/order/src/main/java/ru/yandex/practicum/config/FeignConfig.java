package ru.yandex.practicum.config;

import feign.Feign;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.yandex.practicum.client.decoder.FeignErrorDecoder;

@Configuration
@EnableFeignClients
public class FeignConfig {
    @Bean
    public Feign.Builder feignBuilder() {
        return Feign.builder()
                .errorDecoder(new FeignErrorDecoder());
    }
}
