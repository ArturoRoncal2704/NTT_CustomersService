package com.nttdata.service.impl;

import com.nttdata.config.CustomerRequestValidator;
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
import com.nttdata.service.RequestSanitizer;
import com.nttdata.service.SortingUtil;
import com.nttdata.service.errors.ConflictException;
import com.nttdata.service.errors.NotFoundException;
import com.nttdata.service.errors.UnprocessableException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

import static com.nttdata.mapper.CustomerMapper.*;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository repo;
    private final RequestSanitizer sanitizer;
    private final CustomerRequestValidator validator;

    // List
    @Override
    public Flux<CustomerResponse> list(CustomerType type,
                                       CustomerSegment segment,
                                       Integer page,
                                       Integer size,
                                       String sort,
                                       String direction) {

        int p = (page == null || page < 0) ? 0 : page.intValue();
        int s = (size == null) ? 20 : Math.max(1, Math.min(size.intValue(), 100));

        SortingUtil.Spec spec = SortingUtil.parse(sort, direction);
        Sort springSort = Sort.by(spec.direction , spec.property);

        Flux<Customer> flux;
        if (type != null && segment != null) {
            flux = repo.findByTypeAndSegment(type.getValue(), segment.getValue(), springSort);
        } else if (type != null) {
            flux = repo.findByType(type.getValue(), springSort);
        } else if (segment != null) {
            flux = repo.findBySegment(segment.getValue(), springSort);
        } else {
            flux = repo.findAll(springSort);
        }
        long skip = (long) p * s;
        return flux
                .skip(skip)
                .take(s)
                .map(CustomerMapper::toApi);
    }
    // Create
    @Override
    public Mono<CustomerResponse> create(CustomerCreateRequest request) {
        sanitizer.sanitize(request);
        validator.validateCreate(request);

        final String docType = asString(request.getDocumentType());
        return repo.existsByDocumentTypeAndDocumentNumberAndActiveIsTrue(docType, request.getDocumentNumber())
                .flatMap(exists -> {
                            if (exists) {
                                return Mono.error(new ConflictException("Ya existe un cliente activo con ese documento"));
                            }
                            Customer entity = toDomain(request);
                    return repo.save(entity)
                            .map(CustomerMapper::toApi);
                })
                .onErrorMap(org.springframework.dao.DuplicateKeyException.class,
                        ex -> new ConflictException("Documento duplicado (índice único)"));
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
                    String newDocType = request.getDocumentType() != null ? asString(request.getDocumentType()) : found.getDocumentType();
                    String newDocNum = request.getDocumentNumber() != null ? request.getDocumentNumber() : found.getDocumentNumber();

                    if (request.getSegment() == null) {
                        return Mono.error(new UnprocessableException("segment es obligatorio en PUT"));
                    }
                    return repo.existsByDocumentTypeAndDocumentNumberAndActiveIsTrueAndIdNot(newDocType, newDocNum, found.getId())
                            .flatMap(dup -> {
                                if (dup) {
                                    return Mono.error(new ConflictException("Ya existe otro cliente activo con ese documento"));
                                }
                                CustomerMapper.applyUpdate(found, request);
                                return repo.save(found)
                                        .map(CustomerMapper::toApi);
                            });
                })
                .onErrorMap(DuplicateKeyException.class,
                                    ex -> new ConflictException("Documento ya existe (índice único)"));

    }
    // Delete
    @Override
    public Mono<Void> delete(String id) {
        return repo.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Cliente no encontrado")))
                .flatMap(c -> {
                    if (Boolean.FALSE.equals(c.getActive())){
                        return Mono.empty();
                    }
                    c.setActive(false);
                    c.setDeletedAt(Instant.now());
                    return repo.save(c).then();
                });
    }
    // Eligibility
    @Override
    public Mono<EligibilityResponse> getEligibility(DocumentType documentType, String documentNumber) {
        return repo.findAllByDocumentTypeAndDocumentNumberAndActiveIsTrue(
                        CustomerMapper.asString(documentType), documentNumber)
                .collectList()
                .flatMap(list -> {
                    if (list.isEmpty()) {
                        return Mono.error(new NotFoundException("No existe cliente activo con ese documento"));
                    }
                    if (list.size() > 1) {
                        return Mono.error(new ConflictException("Más de un cliente activo con el mismo documento"));
                    }
                    return Mono.just(CustomerMapper.toEligibility(list.get(0)));
                });
    }

    @Override
    public Mono<CustomerResponse> getByDocumentNumber(String documentNumber) {
        return repo.findByDocumentNumberAndActiveIsTrue(documentNumber)
                .switchIfEmpty(Mono.error(new NotFoundException("Cliente no encontrado")))
                .map(CustomerMapper::toApi);
    }


}
