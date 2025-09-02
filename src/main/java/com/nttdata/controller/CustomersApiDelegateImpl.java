package com.nttdata.controller;

import com.nttdata.model.CustomerCreateRequest;
import com.nttdata.model.CustomerResponse;
import com.nttdata.model.CustomerSegment;
import com.nttdata.model.CustomerType;
import com.nttdata.model.CustomerUpdateRequest;
import com.nttdata.model.DocumentType;
import com.nttdata.model.EligibilityResponse;
import com.nttdata.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomersApiDelegateImpl implements CustomersApiDelegate {

  private final CustomerService service;

  @Override
  public Mono<ResponseEntity<Flux<CustomerResponse>>> listCustomers(
          CustomerType type, CustomerSegment segment, ServerWebExchange exchange) {
    return Mono.just(ResponseEntity.ok(service.list(type, segment)));
  }

  @Override
  public Mono<ResponseEntity<CustomerResponse>> createCustomer(
          Mono<CustomerCreateRequest> request, ServerWebExchange exchange) {
    return request.flatMap(service::create)
            .map(r -> ResponseEntity.status(HttpStatus.CREATED).body(r));
  }

  @Override
  public Mono<ResponseEntity<CustomerResponse>> getCustomerById(
          String id, ServerWebExchange exchange) {
    return service.getById(id).map(ResponseEntity::ok);
  }

  @Override
  public Mono<ResponseEntity<CustomerResponse>> updateCustomer(
          String id, Mono<CustomerUpdateRequest> request, ServerWebExchange exchange) {
    return request.flatMap(r -> service.update(id, r))
            .map(ResponseEntity::ok);
  }

  @Override
  public Mono<ResponseEntity<Void>> deleteCustomer(String id, ServerWebExchange exchange) {
    return service.delete(id).thenReturn(ResponseEntity.noContent().build());
  }

  @Override
  public Mono<ResponseEntity<EligibilityResponse>> getEligibility(
          DocumentType documentType, String documentNumber, ServerWebExchange exchange) {
    return service.getEligibility(documentType, documentNumber)
            .map(ResponseEntity::ok);
  }

  @Override
  public Mono<ResponseEntity<CustomerResponse>> getCustomerByDocumentNumber(
          String documentNumber, ServerWebExchange exchange) {
    return service.getByDocumentNumber(documentNumber)
            .map(ResponseEntity::ok);
  }
}

