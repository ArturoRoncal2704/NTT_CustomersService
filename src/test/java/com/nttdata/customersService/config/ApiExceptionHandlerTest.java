package com.nttdata.customersService.config;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.nttdata.config.ApiExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.core.codec.DecodingException;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


import static org.junit.jupiter.api.Assertions.*;

class ApiExceptionHandlerTest {

    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    private static UnrecognizedPropertyException unknownProp(String name) throws Exception {
        JsonFactory jf = new JsonFactory();
        JsonParser p = jf.createParser("{}");
        return UnrecognizedPropertyException.from(p, Object.class, name, List.of());
    }

    @Test
    void handleDecoding_unknownProperty_devuelve422() throws Exception {
        UnrecognizedPropertyException cause = unknownProp("campoX");
        ResponseEntity<String> response =
                handler.handleDecoding(new DecodingException("bad", cause)).block();
        assertEquals(422, response.getStatusCodeValue());
        assertTrue(response.getBody().contains("campoX"));
    }

    @Test
    void handleDecoding_unknownProperty_anidado_devuelve400_generico() throws Exception {
        UnrecognizedPropertyException cause = unknownProp("extra");
        DecodingException ex = new DecodingException("bad", new RuntimeException(cause));
        ResponseEntity<String> response = handler.handleDecoding(ex).block();
        assertEquals(400, response.getStatusCodeValue());
    }


    @Test
    void handleDecoding_generico_devuelve400() {
        ResponseEntity<String> resp = handler
                .handleDecoding(new DecodingException("bad", new IOException("e")))
                .block();
        assertEquals(400, resp.getStatusCodeValue());
    }

    @Test
    void handleDecoding_sin_causa_devuelve400() {
        ResponseEntity<String> resp = handler.handleDecoding(new DecodingException("bad", null)).block();
        assertEquals(400, resp.getStatusCodeValue());
    }

    @Test
    void handleDecoding_causa_random_devuelve400() {
        ResponseEntity<String> resp = handler
                .handleDecoding(new DecodingException("bad", new RuntimeException("x")))
                .block();
        assertEquals(400, resp.getStatusCodeValue());
    }
}