package com.nttdata.customersService.controller;

import com.nttdata.controller.CustomersApiDelegateImpl;
import com.nttdata.model.*;
import com.nttdata.service.CustomerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.nttdata.customersService.support.Fixtures.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomersApiDelegateImplTest {

  @Mock CustomerService service;

  @InjectMocks CustomersApiDelegateImpl delegate;

  @Test
  void listCustomers_devuelve_200() {
    when(service.list(any(), any(), any(), any(), any(), any()))
        .thenReturn(Flux.empty());

    ResponseEntity<Flux<CustomerResponse>> resp =
        delegate.listCustomers(null, null, null, null, null, null, (ServerWebExchange) null).block();

    assertNotNull(resp);
    assertEquals(200, resp.getStatusCodeValue());
    assertNotNull(resp.getBody());
  }

  @Test
  void createCustomer_devuelve_201() {
    CustomerCreateRequest req = newPersonalCreateReq();
    when(service.create(any(CustomerCreateRequest.class)))
        .thenReturn(Mono.just(new CustomerResponse().type(CustomerType.PERSONAL)));

    ResponseEntity<CustomerResponse> resp =
        delegate.createCustomer(Mono.just(req), null).block();

    assertEquals(201, resp.getStatusCodeValue());
    assertEquals(CustomerType.PERSONAL, resp.getBody().getType());
  }

  @Test
  void getCustomerById_devuelve_200() {
    when(service.getById("id1")).thenReturn(Mono.just(new CustomerResponse().id("id1")));
    ResponseEntity<CustomerResponse> resp = delegate.getCustomerById("id1", null).block();
    assertEquals(200, resp.getStatusCodeValue());
    assertEquals("id1", resp.getBody().getId());
  }

  @Test
  void updateCustomer_devuelve_200() {
    when(service.update(eq("id2"), any(CustomerUpdateRequest.class)))
        .thenReturn(Mono.just(new CustomerResponse().id("id2")));
    ResponseEntity<CustomerResponse> resp =
        delegate.updateCustomer("id2", Mono.just(new CustomerUpdateRequest().segment(CustomerSegment.VIP)), null).block();
    assertEquals(200, resp.getStatusCodeValue());
    assertEquals("id2", resp.getBody().getId());
  }

  @Test
  void deleteCustomer_devuelve_204() {
    when(service.delete("id3")).thenReturn(Mono.empty());
    ResponseEntity<Void> resp = delegate.deleteCustomer("id3", null).block();
    assertEquals(204, resp.getStatusCodeValue());
  }

  @Test
  void getEligibility_devuelve_200() {
    when(service.getEligibility(eq(DocumentType.DNI), eq("123")))
        .thenReturn(Mono.just(new EligibilityResponse().customerId("c1")));
    ResponseEntity<EligibilityResponse> resp = delegate.getEligibility(DocumentType.DNI, "123", null).block();
    assertEquals(200, resp.getStatusCodeValue());
    assertEquals("c1", resp.getBody().getCustomerId());
  }

  @Test
  void getCustomerByDocumentNumber_devuelve_200() {
    when(service.getByDocumentNumber("123"))
        .thenReturn(Mono.just(new CustomerResponse().id("c2")));
    ResponseEntity<CustomerResponse> resp = delegate.getCustomerByDocumentNumber("123", null).block();
    assertEquals(200, resp.getStatusCodeValue());
    assertEquals("c2", resp.getBody().getId());
  }
}
