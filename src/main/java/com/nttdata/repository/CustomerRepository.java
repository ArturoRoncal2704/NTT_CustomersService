package com.nttdata.repository;

import com.nttdata.domain.Customer;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CustomerRepository extends ReactiveMongoRepository<Customer, String> {

  Mono<Customer> findByDocumentNumberAndActiveIsTrue(String documentNumber);
  Flux<Customer> findAllByDocumentTypeAndDocumentNumberAndActiveIsTrue(String documentType, String documentNumber);
  Mono<Boolean> existsByDocumentTypeAndDocumentNumberAndActiveIsTrue(String documentType, String documentNumber);
  Flux<Customer> findByType(String type);
  Flux<Customer> findBySegment(String segment);
  Flux<Customer> findByTypeAndSegment(String type, String segment);
  Mono<Boolean> existsByDocumentTypeAndDocumentNumberAndActiveIsTrueAndIdNot(
          String documentType, String documentNumber, String id);
}
