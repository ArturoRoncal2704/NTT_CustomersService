package com.nttdata.controller;

import com.nttdata.mapper.CustomerMapper;
import com.nttdata.model.CustomerCreateRequest;
import com.nttdata.model.CustomerRequest;
import com.nttdata.model.CustomerResponse;
import com.nttdata.model.CustomerSegment;
import com.nttdata.model.CustomerType;
import com.nttdata.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomersApiDelegateImpl implements CustomersApiDelegate {

  private final CustomerService service;

  @Override
  public Mono<ResponseEntity<Flux<CustomerResponse>>> listCustomers(
      CustomerType type, CustomerSegment segment, ServerWebExchange exchange) {

    Flux<CustomerResponse> body = service.findAll(type, segment).map(CustomerMapper::toResponse);

    return Mono.just(ResponseEntity.ok(body));
  }

  @Override
  public Mono<ResponseEntity<CustomerResponse>> createCustomer(
      Mono<CustomerCreateRequest> request, ServerWebExchange exchange) {

    return request
        .map(CustomerMapper::toEntity)
        .flatMap(service::create)
        .map(CustomerMapper::toResponse)
        .map(resp -> ResponseEntity.status(HttpStatus.CREATED).body(resp));
  }

  @Override
  public Mono<ResponseEntity<CustomerResponse>> getCustomerById(
      String id, ServerWebExchange exchange) {

    return service.findById(id).map(CustomerMapper::toResponse).map(ResponseEntity::ok);
  }

  @Override
  public Mono<ResponseEntity<CustomerResponse>> updateCustomer(
      String id, Mono<CustomerRequest> request, ServerWebExchange exchange) {

    return request
        .map(CustomerMapper::toEntity)
        .flatMap(c -> service.update(id, c))
        .map(CustomerMapper::toResponse)
        .map(ResponseEntity::ok);
  }

  @Override
  public Mono<ResponseEntity<Void>> deleteCustomer(String id, ServerWebExchange exchange) {

    return service.delete(id).thenReturn(ResponseEntity.noContent().<Void>build());
  }
}
