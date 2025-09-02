package com.nttdata.service;


import com.nttdata.model.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CustomerService {

  // Listado con filtros opcionales
  Flux<CustomerResponse> list(CustomerType type, CustomerSegment segment);

  // CRUD
  Mono<CustomerResponse> create(CustomerCreateRequest request);
  Mono<CustomerResponse> getById(String id);
  Mono<CustomerResponse> update(String id, CustomerUpdateRequest request);
  Mono<Void> delete(String id);

  // Elegibilidad por documento
  Mono<EligibilityResponse> getEligibility(DocumentType documentType, String documentNumber);
  Mono<CustomerResponse> getByDocumentNumber(String documentNumber);
}
