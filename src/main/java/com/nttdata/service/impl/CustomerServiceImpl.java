package com.nttdata.service.impl;

import com.nttdata.domain.Customer;
import com.nttdata.mapper.CustomerMapper;
import com.nttdata.model.CustomerCreateRequest;
import com.nttdata.model.CustomerResponse;
import com.nttdata.model.CustomerSegment;
import com.nttdata.model.CustomerType;
import com.nttdata.model.CustomerUpdateRequest;
import com.nttdata.model.DocumentType;
import com.nttdata.model.EligibilityResponse;
import com.nttdata.repository.CustomerRepository;
import com.nttdata.service.CustomerService;
import com.nttdata.service.errors.ConflictException;
import com.nttdata.service.errors.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository repo;

    // List
    @Override
    public Flux<CustomerResponse> list(CustomerType type, CustomerSegment segment) {
        if (type != null && segment != null) {
            return repo.findByType(type.getValue())
                    .filter(c -> segment.getValue().equals(c.getSegment()))
                    .map(CustomerMapper::toApi);
        } else if (type != null) {
            return repo.findByType(type.getValue()).map(CustomerMapper::toApi);
        } else if (segment != null) {
            return repo.findBySegment(segment.getValue()).map(CustomerMapper::toApi);
        } else {
            return repo.findAll().map(CustomerMapper::toApi);
        }
    }
    // Create
    @Override
    public Mono<CustomerResponse> create(CustomerCreateRequest request) {
        return repo.existsByDocumentTypeAndDocumentNumberAndActiveIsTrue(
                        safe(request.getDocumentType()), request.getDocumentNumber())
                .flatMap(exists -> {
                    if (exists) return Mono.error(new ConflictException("Documento ya existe (activo)"));
                    Customer domain = CustomerMapper.toDomain(request);
                    domain.validateSegment();
                    return repo.save(domain)
                            .map(CustomerMapper::toApi)
                            .onErrorMap(DuplicateKeyException.class,
                                    ex -> new ConflictException("Documento ya existe (índice único)"));
                });
    }
    // Get
    @Override
    public Mono<CustomerResponse> getById(String id) {
        return repo.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Cliente no encontrado")))
                .map(CustomerMapper::toApi);
    }
    // Update
    @Override
    public Mono<CustomerResponse> update(String id, CustomerUpdateRequest request) {
        return repo.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Cliente no encontrado")))
                .flatMap(found -> {
                    CustomerMapper.applyUpdate(found, request);
                    return repo.save(found)
                            .map(CustomerMapper::toApi)
                            .onErrorMap(DuplicateKeyException.class,
                                    ex -> new ConflictException("Documento ya existe (índice único)"));
                });
    }
    // Delete
    @Override
    public Mono<Void> delete(String id) {
        return repo.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Cliente no encontrado")))
                .flatMap(repo::delete);
    }
    // Eligibility
    @Override
    public Mono<EligibilityResponse> getEligibility(DocumentType documentType, String documentNumber) {
        return repo.findAllByDocumentTypeAndDocumentNumberAndActiveIsTrue(
                        safe(documentType), documentNumber)
                .collectList()
                .flatMap(list -> {
                    if (list.isEmpty()) {
                        return Mono.error(new NotFoundException("No existe cliente activo con ese documento"));
                    }
                    if (list.size() > 1) {
                        return Mono.error(new ConflictException("Más de un cliente activo con el mismo documento"));
                    }
                    Customer c = list.get(0);
                    EligibilityResponse resp = new EligibilityResponse()
                            .customerId(c.getId())
                            .type(toTypeEnum(c.getType()))
                            .profile(toSegmentEnum(c.getSegment()))
                            // TODO: integrar con créditos / cards
                            .hasActiveCreditCard(false);
                    return Mono.just(resp);
                });
    }

    @Override
    public Mono<CustomerResponse> getByDocumentNumber(String documentNumber) {
        return repo.findByDocumentNumberAndActiveIsTrue(documentNumber)
                .switchIfEmpty(Mono.error(new NotFoundException("Cliente no encontrado")))
                .map(CustomerMapper::toApi);
    }

    // Helpers
    private static String safe(DocumentType d) {
        return d == null ? null : d.getValue();
    }

    private static com.nttdata.model.CustomerType toTypeEnum(String s) {
        return s == null ? null : com.nttdata.model.CustomerType.fromValue(s);
    }

    private static com.nttdata.model.CustomerSegment toSegmentEnum(String s) {
        return s == null ? null : com.nttdata.model.CustomerSegment.fromValue(s);
    }
}
