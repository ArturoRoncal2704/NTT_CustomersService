package com.nttdata.customersService.service;

import com.nttdata.domain.Customer;
import com.nttdata.model.CustomerSegment;
import com.nttdata.model.CustomerType;
import com.nttdata.repository.CustomerRepository;
import com.nttdata.service.impl.CustomerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

  @Mock
  CustomerRepository repo;

  @InjectMocks
  CustomerServiceImpl service;

  private Customer base;

  @BeforeEach
  void setUp() {
    base = new Customer();
    base.setId("id-1");
    base.setFirstName("Arturo");
    base.setLastName("Roncal");
    base.setEmail("a@b.com");
    base.setDocumentNumber("12345678");
    base.setType(CustomerType.PERSONAL);
    base.setSegment(CustomerSegment.STANDARD);
    base.setActive(true);
  }

  @Test
  void create_ok_saves_when_not_exists_and_standard_segment() {
    when(repo.existsByDocumentNumberAndActiveIsTrue("12345678")).thenReturn(Mono.just(false));
    when(repo.save(any(Customer.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

    StepVerifier.create(service.create(base))
            .expectNextMatches(c -> "12345678".equals(c.getDocumentNumber()) && Boolean.TRUE.equals(c.getActive()))
            .verifyComplete();

    verify(repo).existsByDocumentNumberAndActiveIsTrue("12345678");
    verify(repo).save(any(Customer.class));
  }

  @Test
  void create_sets_defaults_when_missing_active_and_segment() {
    Customer c = new Customer();
    c.setDocumentNumber("X1");
    c.setType(CustomerType.PERSONAL); // active=null, segment=null -> TRUE + STANDARD

    when(repo.existsByDocumentNumberAndActiveIsTrue("X1")).thenReturn(Mono.just(false));
    when(repo.save(any(Customer.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

    StepVerifier.create(service.create(c))
            .expectNextMatches(saved ->
                    Boolean.TRUE.equals(saved.getActive()) &&
                            CustomerSegment.STANDARD.equals(saved.getSegment()))
            .verifyComplete();
  }

  @Test
  void create_conflict_when_document_already_exists() {
    when(repo.existsByDocumentNumberAndActiveIsTrue("12345678")).thenReturn(Mono.just(true));

    StepVerifier.create(service.create(base))
            .expectErrorSatisfies(err -> {
              var ex = (ResponseStatusException) err;
              assertEquals(409, ex.getStatus().value());
            })
            .verify();

    verify(repo).existsByDocumentNumberAndActiveIsTrue("12345678");
    verify(repo, never()).save(any());
  }

  @Test
  void create_conflict_when_duplicateKeyException() {
    when(repo.existsByDocumentNumberAndActiveIsTrue("12345678")).thenReturn(Mono.just(false));
    when(repo.save(any())).thenReturn(Mono.error(new DuplicateKeyException("duplicate")));

    StepVerifier.create(service.create(base))
            .expectErrorSatisfies(err -> {
              var ex = (ResponseStatusException) err;
              assertEquals(409, ex.getStatus().value());
            })
            .verify();
  }

  @Test
  void create_badRequest_when_missing_document_or_type() {
    Customer c1 = new Customer();
    c1.setType(CustomerType.PERSONAL);
    StepVerifier.create(service.create(c1))
            .expectError(ResponseStatusException.class).verify();

    Customer c2 = new Customer();
    c2.setDocumentNumber("X");
    StepVerifier.create(service.create(c2))
            .expectError(ResponseStatusException.class).verify();
  }

  @Test
  void create_unprocessable_when_segment_not_standard() {
    base.setSegment(CustomerSegment.VIP); // en create solo STANDARD
    StepVerifier.create(service.create(base))
            .expectErrorSatisfies(err -> {
              var ex = (ResponseStatusException) err;
              assertEquals(422, ex.getStatus().value());
            })
            .verify();
  }

  @Test
  void findAll_variants() {
    when(repo.findAll()).thenReturn(Flux.just(base));
    StepVerifier.create(service.findAll(null, null)).expectNextCount(1).verifyComplete();

    when(repo.findByType(CustomerType.PERSONAL)).thenReturn(Flux.just(base));
    StepVerifier.create(service.findAll(CustomerType.PERSONAL, null)).expectNextCount(1).verifyComplete();

    when(repo.findByTypeAndSegment(CustomerType.PERSONAL, CustomerSegment.STANDARD))
            .thenReturn(Flux.just(base));
    StepVerifier.create(service.findAll(CustomerType.PERSONAL, CustomerSegment.STANDARD))
            .expectNextCount(1).verifyComplete();
  }

  @Test
  void findById_ok_and_notFound() {
    when(repo.findById("id-1")).thenReturn(Mono.just(base));
    when(repo.findById("missing")).thenReturn(Mono.empty());

    StepVerifier.create(service.findById("id-1")).expectNext(base).verifyComplete();
    StepVerifier.create(service.findById("missing"))
            .expectErrorSatisfies(err -> {
              var ex = (ResponseStatusException) err;
              assertEquals(404, ex.getStatus().value());
            })
            .verify();
  }

  @Test
  void update_ok_with_uniqueness_check_when_active_true() {
    Customer incoming = new Customer();
    incoming.setDocumentNumber("999");
    incoming.setType(CustomerType.PERSONAL);
    incoming.setSegment(CustomerSegment.STANDARD);

    when(repo.findById("id-1")).thenReturn(Mono.just(base));
    when(repo.existsByDocumentNumberAndActiveIsTrueAndIdNot("999", "id-1"))
            .thenReturn(Mono.just(false));
    when(repo.save(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

    StepVerifier.create(service.update("id-1", incoming))
            .expectNextMatches(c -> "999".equals(c.getDocumentNumber()))
            .verifyComplete();
  }

  @Test
  void update_active_false_bypasses_uniqueness_check() {
    Customer incoming = new Customer();
    incoming.setActive(false);
    incoming.setType(CustomerType.PERSONAL);
    incoming.setSegment(CustomerSegment.STANDARD);

    when(repo.findById("id-1")).thenReturn(Mono.just(base));
    when(repo.save(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

    StepVerifier.create(service.update("id-1", incoming))
            .expectNextMatches(c -> Boolean.FALSE.equals(c.getActive()))
            .verifyComplete();

    verify(repo, never()).existsByDocumentNumberAndActiveIsTrueAndIdNot(anyString(), anyString());
  }

  @Test
  void update_badRequest_when_type_becomes_null() {
    Customer db = new Customer();
    db.setId("id-1");
    db.setDocumentNumber("123");
    db.setType(null); // provoca 400
    db.setSegment(CustomerSegment.STANDARD);

    when(repo.findById("id-1")).thenReturn(Mono.just(db));

    StepVerifier.create(service.update("id-1", new Customer()))
            .expectErrorSatisfies(err -> {
              var ex = (ResponseStatusException) err;
              assertEquals(400, ex.getStatus().value());
            })
            .verify();
  }

  @Test
  void update_badRequest_when_document_missing() {
    Customer db = new Customer();
    db.setId("id-1");
    db.setDocumentNumber(null); // provoca 400
    db.setType(CustomerType.PERSONAL);
    db.setSegment(CustomerSegment.STANDARD);

    when(repo.findById("id-1")).thenReturn(Mono.just(db));

    StepVerifier.create(service.update("id-1", new Customer()))
            .expectErrorSatisfies(err -> {
              var ex = (ResponseStatusException) err;
              assertEquals(400, ex.getStatus().value());
            })
            .verify();
  }

  @Test
  void update_unprocessable_when_segment_type_mismatch_business_vip() {
    Customer incoming = new Customer();
    incoming.setType(CustomerType.BUSINESS);
    incoming.setSegment(CustomerSegment.VIP); // no permitido para BUSINESS
    when(repo.findById("id-1")).thenReturn(Mono.just(base)); // base PERSONAL+STANDARD

    StepVerifier.create(service.update("id-1", incoming))
            .expectErrorSatisfies(err -> {
              var ex = (ResponseStatusException) err;
              assertEquals(422, ex.getStatus().value());
            })
            .verify();
  }

  @Test
  void update_unprocessable_when_personal_pyme_not_allowed() {
    Customer incoming = new Customer();
    incoming.setType(CustomerType.PERSONAL);
    incoming.setSegment(CustomerSegment.PYME); // no permitido para PERSONAL
    when(repo.findById("id-1")).thenReturn(Mono.just(base));

    StepVerifier.create(service.update("id-1", incoming))
            .expectErrorSatisfies(err -> {
              var ex = (ResponseStatusException) err;
              assertEquals(422, ex.getStatus().value());
            })
            .verify();
  }

  @Test
  void delete_ok_and_notFound() {
    when(repo.existsById("id-1")).thenReturn(Mono.just(true));
    when(repo.deleteById("id-1")).thenReturn(Mono.empty());
    when(repo.existsById("missing")).thenReturn(Mono.just(false));

    StepVerifier.create(service.delete("id-1")).verifyComplete();
    StepVerifier.create(service.delete("missing"))
            .expectErrorSatisfies(err -> {
              var ex = (ResponseStatusException) err;
              assertEquals(404, ex.getStatus().value());
            })
            .verify();
  }

  @Test
  void findActiveByDocumentNumber_happy_path_and_errors() {
    when(repo.findByDocumentNumberAndActiveIsTrue("123")).thenReturn(Flux.just(base));
    StepVerifier.create(service.findActiveByDocumentNumber("123"))
            .expectNext(base).verifyComplete();

    StepVerifier.create(service.findActiveByDocumentNumber(" "))
            .expectError(ResponseStatusException.class).verify();

    when(repo.findByDocumentNumberAndActiveIsTrue("none")).thenReturn(Flux.empty());
    StepVerifier.create(service.findActiveByDocumentNumber("none"))
            .expectErrorSatisfies(err -> {
              var ex = (ResponseStatusException) err;
              assertEquals(404, ex.getStatus().value());
            })
            .verify();

    when(repo.findByDocumentNumberAndActiveIsTrue("multi")).thenReturn(Flux.just(base, base));
    StepVerifier.create(service.findActiveByDocumentNumber("multi"))
            .expectErrorSatisfies(err -> {
              var ex = (ResponseStatusException) err;
              assertEquals(409, ex.getStatus().value());
            })
            .verify();
  }
}
