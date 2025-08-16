package com.nttdata.api;

import com.nttdata.model.CustomerRequest;
import com.nttdata.model.CustomerResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class CustomersApiDelegateImpl implements CustomersApiDelegate {

    private final CustomerService service;

    @Override
    public Mono<ResponseEntity<Flux<CustomerResponse>>> listCustomers(String type, ServerWebExchange exchange) {
        CustomerRequest.TypeEnum typeEnum = null;
        if (type != null) {
            typeEnum = CustomerRequest.TypeEnum.fromValue(type);
        }
        return Mono.just(ResponseEntity.ok(service.findAll(typeEnum)));
    }

    @Override
    public Mono<ResponseEntity<CustomerResponse>> createCustomer(Mono<CustomerRequest> request,
                                                                 ServerWebExchange exchange) {
        return request
                .flatMap(service::create)
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<CustomerResponse>> getCustomerById(String id, ServerWebExchange exchange) {
        return service.findById(id)
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<CustomerResponse>> updateCustomer(String id,
                                                                 Mono<CustomerRequest> request,
                                                                 ServerWebExchange exchange) {
        return request
                .flatMap(r -> service.update(id, r))
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<Void>> deleteCustomer(String id, ServerWebExchange exchange) {
        return service.delete(id)
                .thenReturn(ResponseEntity.noContent().build());
    }
}
