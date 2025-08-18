package com.nttdata.repository;

import com.nttdata.domain.Customer;
import com.nttdata.model.CustomerRequest;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CustomerRepository extends ReactiveMongoRepository<Customer, String> {

  Mono<Boolean> existsByDocumentNumberAndActiveIsTrue(String documentNumber);
  Flux<Customer> findByType(CustomerRequest.TypeEnum type);
}
