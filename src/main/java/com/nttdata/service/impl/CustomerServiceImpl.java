package com.nttdata.service.impl;

import com.nttdata.domain.Customer;
import com.nttdata.repository.CustomerRepository;
import com.nttdata.model.CustomerRequest;
import com.nttdata.model.CustomerRequestAddress;
import com.nttdata.model.CustomerResponse;
import com.nttdata.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository repo;

    @Override
    public Mono<CustomerResponse> create(CustomerRequest r) {
        log.info("Creando cliente con documento {}", r.getDocumentNumber());

        return repo.existsByDocumentNumberAndActiveIsTrue(r.getDocumentNumber())
                .flatMap(exists -> {
                    if (exists) {
                        log.warn("El documento {} ya existe en la base de datos", r.getDocumentNumber());
                        return Mono.error(new IllegalStateException("El documento ya existe"));
                    }
                    return repo.save(toEntity(r))
                            .doOnSuccess(c -> log.info("Cliente {} creado con ID {}", c.getFirstName(), c.getId()))
                            .map(this::toResponse);
                })
                .doOnError(e -> log.error("Error al crear cliente: {}", e.getMessage()));
    }

    @Override
    public Flux<CustomerResponse> findAll(CustomerRequest.TypeEnum type) {
        log.info("Listando clientes. Filtro por tipo: {}", type != null ? type : "todos");

        return (type == null ? repo.findAll() : repo.findByType(type))
                .map(this::toResponse)
                .collectList()
                .flatMapMany(list -> {
                    var activos = list.stream()
                            .filter(CustomerResponse::getActive)
                            .collect(Collectors.toList());

                    log.debug("Se encontraron {} clientes activos", activos.size());
                    return Flux.fromIterable(activos);
                })
                .doOnError(e -> log.error("Error al listar clientes: {}", e.getMessage()));
    }

    @Override
    public Mono<CustomerResponse> findById(String id) {
        log.info("Buscando cliente con ID {}", id);

        return repo.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Cliente no encontrado")))
                .map(this::toResponse)
                .doOnSuccess(c -> log.info("Cliente {} recuperado correctamente", id))
                .doOnError(e -> log.error("Error al buscar cliente {}", id, e));
    }

    @Override
    public Mono<CustomerResponse> update(String id, CustomerRequest r) {
        log.info("Actualizando cliente con ID {}", id);

        return repo.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Cliente no encontrado")))
                .flatMap(db -> {
                    Optional.ofNullable(r.getFirstName()).ifPresent(db::setFirstName);
                    Optional.ofNullable(r.getLastName()).ifPresent(db::setLastName);
                    Optional.ofNullable(r.getEmail()).ifPresent(db::setEmail);
                    Optional.ofNullable(r.getDocumentNumber()).ifPresent(db::setDocumentNumber);
                    Optional.ofNullable(r.getType()).ifPresent(db::setType);
                    Optional.ofNullable(r.getPhone()).ifPresent(db::setPhone);

                    if (r.getAddress() != null) {
                        Optional.ofNullable(r.getAddress().getLine1()).ifPresent(db::setAddressLine1);
                        Optional.ofNullable(r.getAddress().getCity()).ifPresent(db::setCity);
                        Optional.ofNullable(r.getAddress().getCountry()).ifPresent(db::setCountry);
                    }

                    Optional.ofNullable(r.getActive()).ifPresent(db::setActive);

                    return repo.save(db)
                            .doOnSuccess(c -> log.info("Cliente {} actualizado correctamente", c.getId()));
                })
                .map(this::toResponse)
                .doOnError(e -> log.error("Error al actualizar cliente {}", id, e));
    }

    @Override
    public Mono<Void> delete(String id) {
        log.info("Eliminando cliente con ID {}", id);

        return repo.existsById(id)
                .flatMap(exists -> {
                    if (!exists) {
                        log.warn("Intento de eliminar cliente no existente con ID {}", id);
                        return Mono.error(new IllegalArgumentException("Cliente no encontrado"));
                    }
                    return repo.deleteById(id)
                            .doOnSuccess(v -> log.info("Cliente {} eliminado correctamente", id));
                })
                .doOnError(e -> log.error("Error al eliminar cliente {}", id, e));
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
