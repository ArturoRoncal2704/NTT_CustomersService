package com.nttdata.repository;

import com.nttdata.domain.Customer;
import com.nttdata.model.CustomerSegment;
import com.nttdata.model.CustomerType;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CustomerRepository extends ReactiveMongoRepository<Customer, String> {




  Flux<Customer> findAllByDocumentTypeAndDocumentNumberAndActiveIsTrue(String documentType, String documentNumber);

  // Duplicado activo por documento
  Mono<Boolean> existsByDocumentTypeAndDocumentNumberAndActiveIsTrue(String documentType, String documentNumber);

  // Filtros simples para listados
  Flux<Customer> findByType(String type);
  Flux<Customer> findBySegment(String segment);
  Flux<Customer> findByTypeAndSegment(String type, String segment);
}
