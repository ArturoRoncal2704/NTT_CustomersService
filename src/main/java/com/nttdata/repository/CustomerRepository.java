package com.nttdata.repository;

import com.nttdata.domain.Customer;
import com.nttdata.model.CustomerSegment;
import com.nttdata.model.CustomerType;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CustomerRepository extends ReactiveMongoRepository<Customer, String> {

  Mono<Boolean> existsByDocumentNumberAndActiveIsTrue(String documentNumber);

  Mono<Boolean> existsByDocumentNumberAndActiveIsTrueAndIdNot(String documentNumber, String id);

  Flux<Customer> findByType(CustomerType type);

  Flux<Customer> findByTypeAndSegment(CustomerType type, CustomerSegment segment);

  Mono<Customer> findFirstByDocumentNumberAndActiveIsTrue(String documentNumber);

  Flux<Customer> findByDocumentNumberAndActiveIsTrue(String documentNumber);
}
