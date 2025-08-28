package com.nttdata.service.impl;

import com.nttdata.domain.Customer;
import com.nttdata.model.CustomerSegment;
import com.nttdata.model.CustomerType;
import com.nttdata.repository.CustomerRepository;
import com.nttdata.service.CustomerService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

  private final CustomerRepository repo;

  @Override
  public Mono<Customer> create(Customer c) {
    if (!StringUtils.hasText(c.getDocumentNumber())) {
      return Mono.error(
          new ResponseStatusException(HttpStatus.BAD_REQUEST, "documentNumber is required"));
    }
    if (c.getType() == null) {
      return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "type is required"));
    }
    if (c.getActive() == null) c.setActive(Boolean.TRUE);

    if (c.getSegment() == null) {
      c.setSegment(CustomerSegment.STANDARD);
    } else if (c.getSegment() != CustomerSegment.STANDARD) {
      return Mono.error(
          new ResponseStatusException(
              HttpStatus.UNPROCESSABLE_ENTITY, "Segment must be STANDARD on creation"));
    }

    validateTypeAndSegment(c.getType(), c.getSegment());

    return repo.existsByDocumentNumberAndActiveIsTrue(c.getDocumentNumber())
        .flatMap(
            exists ->
                exists
                    ? Mono.error(
                        new ResponseStatusException(HttpStatus.CONFLICT, "Document already exists"))
                    : repo.save(c))
        .onErrorMap(
            DuplicateKeyException.class,
            ex -> new ResponseStatusException(HttpStatus.CONFLICT, "Document already exists", ex));
  }

  @Override
  public Flux<Customer> findAll(CustomerType type, CustomerSegment segment) {
    return Optional.ofNullable(type)
        .map(
            t ->
                Optional.ofNullable(segment)
                    .map(s -> repo.findByTypeAndSegment(t, s))
                    .orElseGet(() -> repo.findByType(t)))
        .orElseGet(repo::findAll);
  }

  @Override
  public Mono<Customer> findById(String id) {
    return repo.findById(id)
        .switchIfEmpty(
            Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found")));
  }

  @Override
  public Mono<Customer> update(String id, Customer c) {
    return repo.findById(id)
        .switchIfEmpty(
            Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found")))
        .flatMap(
            db -> {
              // Merge null-safe
              Optional.ofNullable(c.getFirstName()).ifPresent(db::setFirstName);
              Optional.ofNullable(c.getLastName()).ifPresent(db::setLastName);
              Optional.ofNullable(c.getEmail()).ifPresent(db::setEmail);
              Optional.ofNullable(c.getDocumentNumber()).ifPresent(db::setDocumentNumber);
              Optional.ofNullable(c.getType()).ifPresent(db::setType);
              Optional.ofNullable(c.getSegment()).ifPresent(db::setSegment);
              Optional.ofNullable(c.getPhone()).ifPresent(db::setPhone);
              Optional.ofNullable(c.getAddressLine1()).ifPresent(db::setAddressLine1);
              Optional.ofNullable(c.getCity()).ifPresent(db::setCity);
              Optional.ofNullable(c.getCountry()).ifPresent(db::setCountry);
              Optional.ofNullable(c.getActive()).ifPresent(db::setActive);

              if (db.getType() == null) {
                return Mono.error(
                    new ResponseStatusException(HttpStatus.BAD_REQUEST, "type is required"));
              }
              if (!StringUtils.hasText(db.getDocumentNumber())) {
                return Mono.error(
                    new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "documentNumber is required"));
              }

              String msg = validateTypeAndSegment(db.getType(), db.getSegment());
              if (msg != null) {
                return Mono.error(
                    new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, msg));
              }

              if (Boolean.TRUE.equals(db.getActive())) {
                return repo.existsByDocumentNumberAndActiveIsTrueAndIdNot(
                        db.getDocumentNumber(), id)
                    .flatMap(
                        exists ->
                            exists
                                ? Mono.error(
                                    new ResponseStatusException(
                                        HttpStatus.CONFLICT, "Document already exists"))
                                : repo.save(db))
                    .onErrorMap(
                        DuplicateKeyException.class,
                        ex ->
                            new ResponseStatusException(
                                HttpStatus.CONFLICT, "Document already exists", ex));
              }
              return repo.save(db);
            });
  }

  @Override
  public Mono<Void> delete(String id) {
    return repo.existsById(id)
        .flatMap(
            exists ->
                exists
                    ? repo.deleteById(id)
                    : Mono.error(
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found")));
  }

  @Override
  public Mono<Customer> findActiveByDocumentNumber(String documentNumber) {
    if (!StringUtils.hasText(documentNumber)) {
      return Mono.error(
          new ResponseStatusException(HttpStatus.BAD_REQUEST, "documentNumber is required"));
    }
    return repo.findByDocumentNumberAndActiveIsTrue(documentNumber)
        .collectList()
        .flatMap(
            list -> {
              if (list.isEmpty()) {
                return Mono.error(
                    new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
              }
              if (list.size() > 1) {
                return Mono.error(
                    new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "More than one active customer with this documentNumber"));
              }
              return Mono.just(list.get(0));
            });
  }

  private String validateTypeAndSegment(CustomerType type, CustomerSegment seg) {
    if (type == null || seg == null) return null;
    if (seg == CustomerSegment.STANDARD) return null;
    if (seg == CustomerSegment.VIP && type != CustomerType.PERSONAL) {
      return "BUSINESS no puede ser VIP";
    }
    if (seg == CustomerSegment.PYME && type != CustomerType.BUSINESS) {
      return "PERSONAL no puede ser PYME";
    }
    return null;
  }
}
