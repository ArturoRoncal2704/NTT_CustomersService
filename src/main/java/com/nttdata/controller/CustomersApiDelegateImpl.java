package com.nttdata.controller;

import com.nttdata.model.CustomerRequest;
import com.nttdata.model.CustomerResponse;
import com.nttdata.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomersApiDelegateImpl implements CustomersApiDelegate {

    private final CustomerService service;

    @Override
    public Mono<ResponseEntity<Flux<CustomerResponse>>> listCustomers(String type, ServerWebExchange exchange) {
        log.info("Listando clientes. Filtro por tipo: {}", type != null ? type : "todos");
        CustomerRequest.TypeEnum typeEnum = null;
        if (type != null) {
            typeEnum = CustomerRequest.TypeEnum.fromValue(type);
        }
        return Mono.just(ResponseEntity.ok(service.findAll(typeEnum)))
                .doOnSuccess(r -> log.debug("Clientes listados correctamente"))
                .doOnError(e -> log.error("Error al listar clientes", e));
    }

    @Override
    public Mono<ResponseEntity<CustomerResponse>> createCustomer(Mono<CustomerRequest> request,
                                                                 ServerWebExchange exchange) {
        log.info("Creando un nuevo cliente...");
        return request
                .flatMap(service::create)
                .map(ResponseEntity::ok)
                .doOnSuccess(r -> log.info("Cliente creado con ID: {}", r.getBody().getId()))
                .doOnError(e -> log.error("Error al crear cliente", e));
    }

    @Override
    public Mono<ResponseEntity<CustomerResponse>> getCustomerById(String id, ServerWebExchange exchange) {
        log.info("Consultando cliente con ID: {}", id);
        return service.findById(id)
                .map(ResponseEntity::ok)
                .doOnSuccess(r -> log.debug("Cliente {} recuperado correctamente", id))
                .doOnError(e -> log.error("Error al consultar cliente {}", id, e));
    }

    @Override
    public Mono<ResponseEntity<CustomerResponse>> updateCustomer(String id,
                                                                 Mono<CustomerRequest> request,
                                                                 ServerWebExchange exchange) {
        log.info("Actualizando cliente con ID: {}", id);
        return request
                .flatMap(r -> service.update(id, r))
                .map(ResponseEntity::ok)
                .doOnSuccess(r -> log.info("Cliente {} actualizado correctamente", id))
                .doOnError(e -> log.error("Error al actualizar cliente {}", id, e));
    }

    @Override
    public Mono<ResponseEntity<Void>> deleteCustomer(String id, ServerWebExchange exchange) {
        log.info("Eliminando cliente con ID: {}", id);

        return service.delete(id)
                .then(Mono.fromSupplier(() -> ResponseEntity.noContent().<Void>build()))
                .doOnSuccess(r -> log.info("Cliente {} eliminado correctamente", id))
                .doOnError(e -> log.error("Error al eliminar cliente {}", id, e));
    }
}
