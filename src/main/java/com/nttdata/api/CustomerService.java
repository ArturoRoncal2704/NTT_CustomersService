package com.nttdata.api;

import com.nttdata.customers.domain.Customer;
import com.nttdata.customers.repository.CustomerRepository;
import com.nttdata.model.CustomerRequestAddress;
import com.nttdata.model.CustomerRequest;
import com.nttdata.model.CustomerResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository repo;

    public Mono<CustomerResponse> create(CustomerRequest r) {
        return repo.existsByDocumentNumberAndActiveIsTrue(r.getDocumentNumber())
                .flatMap(exists -> exists
                        ? Mono.error(new IllegalStateException("Document already exists"))
                        : repo.save(toEntity(r)).map(this::toResponse));
    }

    public Flux<CustomerResponse> findAll(CustomerRequest.TypeEnum type) {
        return (type == null ? repo.findAll() : repo.findByType(type))
                .map(this::toResponse);
    }

    public Mono<CustomerResponse> findById(String id) {
        return repo.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Customer not found")))
                .map(this::toResponse);
    }

    public Mono<CustomerResponse> update(String id, CustomerRequest r) {
        return repo.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Customer not found")))
                .flatMap(db -> {
                    if (r.getFirstName() != null) db.setFirstName(r.getFirstName());
                    if (r.getLastName() != null) db.setLastName(r.getLastName());
                    if (r.getEmail() != null) db.setEmail(r.getEmail());
                    if (r.getDocumentNumber() != null) db.setDocumentNumber(r.getDocumentNumber());
                    if (r.getType() != null) db.setType(r.getType());
                    if (r.getPhone() != null) db.setPhone(r.getPhone());
                    if (r.getAddress() != null) {
                        if (r.getAddress().getLine1() != null) db.setAddressLine1(r.getAddress().getLine1());
                        if (r.getAddress().getCity() != null) db.setCity(r.getAddress().getCity());
                        if (r.getAddress().getCountry() != null) db.setCountry(r.getAddress().getCountry());
                    }
                    if (r.getActive() != null) db.setActive(r.getActive());
                    return repo.save(db);
                })
                .map(this::toResponse);
    }

    public Mono<Void> delete(String id) {
        return repo.existsById(id)
                .flatMap(exists -> exists
                        ? repo.deleteById(id)
                        : Mono.error(new IllegalArgumentException("Customer not found")));
    }

    private Customer toEntity(CustomerRequest r) {
        Customer c = new Customer();
        c.setFirstName(r.getFirstName());
        c.setLastName(r.getLastName());
        c.setEmail(r.getEmail());
        c.setDocumentNumber(r.getDocumentNumber());
        c.setType(r.getType());
        c.setPhone(r.getPhone());
        if (r.getAddress() != null) {
            c.setAddressLine1(r.getAddress().getLine1());
            c.setCity(r.getAddress().getCity());
            c.setCountry(r.getAddress().getCountry());
        }
        c.setActive(Boolean.TRUE.equals(r.getActive()));
        return c;
    }

    private CustomerResponse toResponse(Customer c) {
        CustomerResponse resp = new CustomerResponse();
        resp.setId(c.getId());
        resp.setFirstName(c.getFirstName());
        resp.setLastName(c.getLastName());
        resp.setEmail(c.getEmail());
        resp.setDocumentNumber(c.getDocumentNumber());
        resp.setType(CustomerResponse.TypeEnum.fromValue(c.getType().getValue()));
        resp.setPhone(c.getPhone());

        CustomerRequestAddress addr = new CustomerRequestAddress();
        addr.setLine1(c.getAddressLine1());
        addr.setCity(c.getCity());
        addr.setCountry(c.getCountry());
        resp.setAddress(addr);

        resp.setActive(c.getActive());
        return resp;
    }

}
