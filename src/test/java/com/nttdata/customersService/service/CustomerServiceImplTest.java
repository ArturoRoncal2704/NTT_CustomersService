package com.nttdata.customersService.service;

import com.nttdata.config.CustomerRequestValidator;
import com.nttdata.domain.Customer;
import com.nttdata.repository.CustomerRepository;
import com.nttdata.service.RequestSanitizer;
import com.nttdata.service.impl.CustomerServiceImpl;
import com.nttdata.service.errors.ConflictException;
import com.nttdata.service.errors.NotFoundException;
import com.nttdata.service.errors.UnprocessableException;
import com.nttdata.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Sort;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.List;

import static com.nttdata.customersService.support.Fixtures.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
public class CustomerServiceImplTest {

    @Mock
    CustomerRepository repo;
    @Mock
    RequestSanitizer sanitizer;
    @Mock
    CustomerRequestValidator validator;

    CustomerServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CustomerServiceImpl(repo, sanitizer, validator);
    }

    @Test
    void create_ok() {
        CustomerCreateRequest req = newPersonalCreateReq();
        when(repo.existsByDocumentTypeAndDocumentNumberAndActiveIsTrue(eq("DNI"), eq("12345678")))
                .thenReturn(Mono.just(false));
        when(repo.save(any(Customer.class))).thenAnswer(inv -> Mono.just((Customer) inv.getArgument(0)));

        StepVerifier.create(service.create(req))
                .assertNext(resp -> {
                    assertEquals(CustomerType.PERSONAL, resp.getType());
                    assertEquals("12345678", resp.getDocumentNumber());
                })
                .verifyComplete();
        verify(sanitizer).sanitize(eq(req));
        verify(validator).validateCreate(eq(req));
    }

    @Test
    void create_conflict_si_existe_documento_activo() {
        CustomerCreateRequest req = newPersonalCreateReq();
        when(repo.existsByDocumentTypeAndDocumentNumberAndActiveIsTrue(anyString(), anyString()))
                .thenReturn(Mono.just(true));

        StepVerifier.create(service.create(req))
                .expectError(ConflictException.class)
                .verify();
    }

    @Test
    void create_mapea_DuplicateKey_a_Conflict() {
        CustomerCreateRequest req = newPersonalCreateReq();
        when(repo.existsByDocumentTypeAndDocumentNumberAndActiveIsTrue(anyString(), anyString()))
                .thenReturn(Mono.just(false));
        when(repo.save(any(Customer.class))).thenReturn(Mono.error(new DuplicateKeyException("dup")));

        StepVerifier.create(service.create(req))
                .expectError(ConflictException.class)
                .verify();
    }


    @Test
    void getById_notFound() {
        when(repo.findById("x")).thenReturn(Mono.empty());
        StepVerifier.create(service.getById("x"))
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    void update_falla_si_segment_es_nulo() {
        Customer existing = newDomainPersonal().toBuilder().id("c1").active(true).build();
        when(repo.findById("c1")).thenReturn(Mono.just(existing));

        CustomerUpdateRequest req = new CustomerUpdateRequest(); // segment null
        StepVerifier.create(service.update("c1", req))
                .expectError(UnprocessableException.class)
                .verify();
    }

    @Test
    void update_conflict_si_otro_tiene_mismo_documento() {
        Customer existing = newDomainPersonal().toBuilder().id("c1").build();
        when(repo.findById("c1")).thenReturn(Mono.just(existing));
        when(repo.existsByDocumentTypeAndDocumentNumberAndActiveIsTrueAndIdNot(anyString(), anyString(), anyString()))
                .thenReturn(Mono.just(true));

        CustomerUpdateRequest req = newUpdateReq().segment(CustomerSegment.STANDARD);
        StepVerifier.create(service.update("c1", req))
                .expectError(ConflictException.class)
                .verify();
    }

    @Test
    void update_ok_aplica_cambios_y_guarda() {
        Customer existing = newDomainPersonal().toBuilder()
                .id("c1").firstName("Arturo").lastName("Roncal").build();
        when(repo.findById("c1")).thenReturn(Mono.just(existing));
        when(repo.existsByDocumentTypeAndDocumentNumberAndActiveIsTrueAndIdNot(anyString(), anyString(), anyString()))
                .thenReturn(Mono.just(false));
        when(repo.save(any(Customer.class))).thenAnswer(inv -> Mono.just((Customer) inv.getArgument(0)));

        CustomerUpdateRequest req = newUpdateReq().segment(CustomerSegment.VIP).firstName("Pepe");
        StepVerifier.create(service.update("c1", req))
                .assertNext(resp -> {
                    assertEquals("Pepe", resp.getFirstName());
                    assertEquals(CustomerSegment.VIP, resp.getSegment());
                })
                .verifyComplete();
    }

    @Test
    void delete_soft_marca_inactivo_y_estampa_deletedAt() {
        Customer existing = newDomainPersonal().toBuilder().id("c1").active(true).build();
        when(repo.findById("c1")).thenReturn(Mono.just(existing));
        when(repo.save(any(Customer.class))).thenAnswer(inv -> Mono.just((Customer) inv.getArgument(0)));

        StepVerifier.create(service.delete("c1")).verifyComplete();

        ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);
        verify(repo).save(captor.capture());
        Customer saved = captor.getValue();
        assertFalse(Boolean.TRUE.equals(saved.getActive()));
        assertNotNull(saved.getDeletedAt());
        assertTrue(saved.getDeletedAt().isBefore(Instant.now().plusSeconds(5)));
    }

    @Test
    void list_filtra_por_type_y_segment_con_paginacion() {
        Customer a = newDomainPersonal().toBuilder().id("a").build();
        Customer b = newDomainPersonal().toBuilder().id("b").build();
        Customer c = newDomainPersonal().toBuilder().id("c").build();

        when(repo.findByTypeAndSegment(eq("PERSONAL"), eq("VIP"), any(Sort.class)))
                .thenReturn(Flux.fromIterable(List.of(a, b, c)));

        StepVerifier.create(
                        service.list(CustomerType.PERSONAL, CustomerSegment.VIP, 0, 2, "createdAt", "asc"))
                .expectNextCount(2)
                .verifyComplete();

        StepVerifier.create(
                        service.list(CustomerType.PERSONAL, CustomerSegment.VIP, 1, 2, "createdAt", "asc"))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void eligibility_not_found_si_no_hay_activo() {
        when(repo.findAllByDocumentTypeAndDocumentNumberAndActiveIsTrue(eq("DNI"), eq("12345678")))
                .thenReturn(Flux.empty());

        StepVerifier.create(service.getEligibility(DocumentType.DNI, "12345678"))
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    void eligibility_conflict_si_hay_multiples() {
        Customer x = newDomainPersonal();
        Customer y = newDomainPersonal().toBuilder().id("c3").build();
        when(repo.findAllByDocumentTypeAndDocumentNumberAndActiveIsTrue(anyString(), anyString()))
                .thenReturn(Flux.fromIterable(List.of(x, y)));

        StepVerifier.create(service.getEligibility(DocumentType.DNI, "12345678"))
                .expectError(ConflictException.class)
                .verify();
    }

    @Test
    void eligibility_unico_devuelve_respuesta() {
        Customer x = newDomainPersonal().toBuilder().id("c9").build();
        when(repo.findAllByDocumentTypeAndDocumentNumberAndActiveIsTrue(eq("DNI"), eq("12345678")))
                .thenReturn(Flux.just(x));

        StepVerifier.create(service.getEligibility(DocumentType.DNI, "12345678"))
                .assertNext(resp -> {
                    assertEquals("c9", resp.getCustomerId());
                    assertEquals(CustomerType.PERSONAL, resp.getType());
                    assertEquals(CustomerSegment.STANDARD, resp.getProfile());
                    assertFalse(Boolean.TRUE.equals(resp.getHasActiveCreditCard()));
                })
                .verifyComplete();
    }

    @Test
    void getByDocumentNumber_notFound() {
        when(repo.findByDocumentNumberAndActiveIsTrue("X")).thenReturn(Mono.empty());
        StepVerifier.create(service.getByDocumentNumber("X"))
                .expectError(NotFoundException.class)
                .verify();
    }

}
