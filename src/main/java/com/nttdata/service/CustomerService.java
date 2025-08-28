package com.nttdata.service;

import com.nttdata.domain.Customer;
import com.nttdata.model.CustomerSegment;
import com.nttdata.model.CustomerType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CustomerService {

    Mono<Customer> create(Customer c);
    Flux<Customer> findAll(CustomerType type, CustomerSegment segment);
    Mono<Customer> findById(String id);
    Mono<Customer> update(String id, Customer c);
    Mono<Void> delete(String id);
    Mono<Customer> findActiveByDocumentNumber(String documentNumber);
}