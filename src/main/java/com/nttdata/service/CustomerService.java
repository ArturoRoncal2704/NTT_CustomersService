package com.nttdata.service;


import com.nttdata.model.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CustomerService {

  Flux<CustomerResponse> list(CustomerType type,
                              CustomerSegment segment,
                              Integer page,
                              Integer size,
                              String sort,
                              String direction);  Mono<CustomerResponse> create(CustomerCreateRequest request);
  Mono<CustomerResponse> getById(String id);
  Mono<CustomerResponse> update(String id, CustomerUpdateRequest request);
  Mono<Void> delete(String id);
  Mono<EligibilityResponse> getEligibility(DocumentType documentType, String documentNumber);
  Mono<CustomerResponse> getByDocumentNumber(String documentNumber);
}
