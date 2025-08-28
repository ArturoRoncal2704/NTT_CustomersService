package com.nttdata.customersService.controller;

import com.nttdata.controller.CustomersApiDelegateImpl;
import com.nttdata.domain.Customer;
import com.nttdata.model.*;
import com.nttdata.service.CustomerService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class CustomersApiDelegateImplTest {

    private Customer sample(String id) {
        Customer c = new Customer();
        c.setId(id);
        c.setFirstName("Arturo");
        c.setLastName("Roncal");
        c.setDocumentNumber("123");
        c.setType(CustomerType.PERSONAL);
        c.setSegment(CustomerSegment.STANDARD);
        c.setActive(true);
        return c;
    }

    @Test
    void listCustomers_ok() {
        CustomerService svc = Mockito.mock(CustomerService.class);
        when(svc.findAll(eq(CustomerType.PERSONAL), eq(CustomerSegment.STANDARD)))
                .thenReturn(Flux.just(sample("c1"), sample("c2")));

        CustomersApiDelegateImpl ctrl = new CustomersApiDelegateImpl(svc);

        StepVerifier.create(
                        ctrl.listCustomers(CustomerType.PERSONAL, CustomerSegment.STANDARD, null))
                .assertNext((ResponseEntity<Flux<CustomerResponse>> re) -> {
                    assertTrue(re.getStatusCode().is2xxSuccessful());
                    StepVerifier.create(re.getBody())
                            .expectNextCount(2)
                            .verifyComplete();
                })
                .verifyComplete();
    }

    @Test
    void createCustomer_created() {
        CustomerCreateRequest req = new CustomerCreateRequest();
        req.setFirstName("Arturo");
        req.setLastName("Roncal");
        req.setDocumentNumber("123");
        req.setType(CustomerType.PERSONAL);

        Customer saved = sample("new-1");

        CustomerService svc = Mockito.mock(CustomerService.class);
        when(svc.create(any(Customer.class))).thenReturn(Mono.just(saved));

        CustomersApiDelegateImpl ctrl = new CustomersApiDelegateImpl(svc);
        StepVerifier.create(ctrl.createCustomer(Mono.just(req), null))
                .assertNext((ResponseEntity<CustomerResponse> re) -> {
                    assertEquals(201, re.getStatusCodeValue());
                    assertEquals("new-1", re.getBody().getId());
                })
                .verifyComplete();
    }

    @Test
    void getCustomerById_ok() {
        CustomerService svc = Mockito.mock(CustomerService.class);
        when(svc.findById("c1")).thenReturn(Mono.just(sample("c1")));

        CustomersApiDelegateImpl ctrl = new CustomersApiDelegateImpl(svc);

        StepVerifier.create(ctrl.getCustomerById("c1", null))
                .assertNext((ResponseEntity<CustomerResponse> re) -> {
                    assertTrue(re.getStatusCode().is2xxSuccessful());
                    assertEquals("c1", re.getBody().getId());
                })
                .verifyComplete();
    }

    @Test
    void updateCustomer_ok() {
        CustomerRequest req = new CustomerRequest();
        req.setFirstName("NuevoNombre");
        req.setType(CustomerType.PERSONAL);
        req.setSegment(CustomerSegment.STANDARD);

        Customer updated = sample("c1");
        updated.setFirstName("NuevoNombre");

        CustomerService svc = Mockito.mock(CustomerService.class);
        when(svc.update(eq("c1"), any(Customer.class))).thenReturn(Mono.just(updated));

        CustomersApiDelegateImpl ctrl = new CustomersApiDelegateImpl(svc);
        StepVerifier.create(ctrl.updateCustomer("c1", Mono.just(req), null))
                .assertNext((ResponseEntity<CustomerResponse> re) -> {
                    assertTrue(re.getStatusCode().is2xxSuccessful());
                    assertEquals("NuevoNombre", re.getBody().getFirstName());
                })
                .verifyComplete();
    }

    @Test
    void deleteCustomer_noContent() {
        CustomerService svc = Mockito.mock(CustomerService.class);
        when(svc.delete("c1")).thenReturn(Mono.empty());

        CustomersApiDelegateImpl ctrl = new CustomersApiDelegateImpl(svc);

        StepVerifier.create(ctrl.deleteCustomer("c1", null))
                .assertNext((ResponseEntity<Void> re) -> assertEquals(204, re.getStatusCodeValue()))
                .verifyComplete();
    }
}