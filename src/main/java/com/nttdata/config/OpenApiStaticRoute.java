package com.nttdata.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.*;

@Configuration
public class OpenApiStaticRoute {

    @Bean
    public RouterFunction<ServerResponse> openApiYamlRoute(){
        return RouterFunctions.route(RequestPredicates.GET("/openapi.yml"),
                req -> ServerResponse.ok()
                        .contentType(MediaType.valueOf("application/yaml"))
                        .body(BodyInserters.fromResource(new ClassPathResource("openapi.yml"))));
    }

}
