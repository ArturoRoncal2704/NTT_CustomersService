package com.nttdata.service;

import com.nttdata.model.CustomerRequest;
import com.nttdata.model.CustomerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CustomerService {

    Mono<CustomerResponse> create(CustomerRequest r);

    Flux<CustomerResponse> findAll(CustomerRequest.TypeEnum type);

    Mono<CustomerResponse> findById(String id);

    Mono<CustomerResponse> update(String id, CustomerRequest r);

    Mono<Void> delete(String id);
}
