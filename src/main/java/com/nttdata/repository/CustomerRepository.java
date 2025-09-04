package com.nttdata.repository;

import com.nttdata.domain.Customer;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface CustomerRepository extends ReactiveMongoRepository<Customer, String> {

  Mono<Customer> findByDocumentNumberAndActiveIsTrue(String documentNumber);
  Flux<Customer> findAllByDocumentTypeAndDocumentNumberAndActiveIsTrue(String documentType, String documentNumber);
  Mono<Boolean> existsByDocumentTypeAndDocumentNumberAndActiveIsTrue(String documentType, String documentNumber);
  Mono<Boolean> existsByDocumentTypeAndDocumentNumberAndActiveIsTrueAndIdNot(
          String documentType, String documentNumber, String id);
  Flux<Customer> findByType(String type, Sort sort);
  Flux<Customer> findBySegment(String segment, Sort sort);
  Flux<Customer> findByTypeAndSegment(String type, String segment, Sort sort);

    List<Customer> findAllByTypeAndActiveIsTrue(String type, Boolean active);

    List<Customer> findAllByTypeAndSegmentAndActiveIsTrue(String type, String segment, Boolean active);

    List<Customer> findByTypeAndActiveIsTrue(String type, Boolean active);

    List<Customer> findByTypeAndSegmentAndActiveIsTrue(String type, String segment, Boolean active);
}
