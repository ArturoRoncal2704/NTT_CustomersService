package com.nttdata.mapper;

import com.nttdata.domain.Customer;
import com.nttdata.model.Address;
import com.nttdata.model.CustomerCreateRequest;
import com.nttdata.model.CustomerRequest;
import com.nttdata.model.CustomerResponse;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.openapitools.jackson.nullable.JsonNullable;

public final class CustomerMapper {

  private CustomerMapper() {}

  // ===== Helpers para JsonNullable =====
  private static <T> T fromJN(JsonNullable<T> jn) {
    return (jn == null || !jn.isPresent()) ? null : jn.get();
  }

  private static <T> JsonNullable<T> toJN(T value) {
    return (value == null) ? JsonNullable.undefined() : JsonNullable.of(value);
  }

  // =========== API (POST) -> Dominio ===========
  public static Customer toEntity(CustomerCreateRequest r) {
    Customer c = new Customer();
    c.setFirstName(r.getFirstName());
    c.setLastName(r.getLastName());
    c.setEmail(fromJN(r.getEmail()));
    c.setDocumentNumber(r.getDocumentNumber());
    c.setType(r.getType());
    c.setPhone(fromJN(r.getPhone()));

    Address addr = fromJN(r.getAddress());
    if (addr != null) {
      c.setAddressLine1(addr.getLine1());
      c.setCity(addr.getCity());
      c.setCountry(addr.getCountry());
    }

    c.setActive(r.getActive() != null ? r.getActive() : Boolean.TRUE);
    return c;
  }

  // =========== API (PUT) -> Dominio ===========
  public static Customer toEntity(CustomerRequest r) {
    Customer c = new Customer();
    c.setFirstName(r.getFirstName());
    c.setLastName(r.getLastName());
    c.setEmail(fromJN(r.getEmail()));
    c.setDocumentNumber(r.getDocumentNumber());
    c.setType(r.getType());
    c.setSegment(r.getSegment());
    c.setPhone(fromJN(r.getPhone()));

    Address addr = fromJN(r.getAddress());
    if (addr != null) {
      c.setAddressLine1(addr.getLine1());
      c.setCity(addr.getCity());
      c.setCountry(addr.getCountry());
    }

    if (r.getActive() != null) c.setActive(r.getActive());
    return c;
  }

  // =========== Dominio -> API (Response) ===========
  public static CustomerResponse toResponse(Customer c) {
    CustomerResponse resp = new CustomerResponse();
    resp.setId(c.getId());
    resp.setFirstName(c.getFirstName());
    resp.setLastName(c.getLastName());
    resp.setEmail(toJN(c.getEmail()));
    resp.setDocumentNumber(c.getDocumentNumber());
    resp.setType(c.getType());
    resp.setSegment(c.getSegment());
    resp.setPhone(toJN(c.getPhone()));

    if (c.getAddressLine1() != null || c.getCity() != null || c.getCountry() != null) {
      Address addr = new Address();
      addr.setLine1(c.getAddressLine1());
      addr.setCity(c.getCity());
      addr.setCountry(c.getCountry());
      resp.setAddress(toJN(addr));
    } else {
      resp.setAddress(JsonNullable.undefined());
    }
    resp.setActive(c.getActive());
    if (c.getCreatedAt() != null) {
      resp.setCreatedAt(OffsetDateTime.ofInstant(c.getCreatedAt(), ZoneOffset.UTC));
    }

    return resp;
  }
}
