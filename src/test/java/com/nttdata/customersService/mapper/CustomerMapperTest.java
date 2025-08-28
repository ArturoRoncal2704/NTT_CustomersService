package com.nttdata.customersService.mapper;

import com.nttdata.domain.Customer;
import com.nttdata.mapper.CustomerMapper;
import com.nttdata.model.Address;
import com.nttdata.model.CustomerCreateRequest;
import com.nttdata.model.CustomerRequest;
import com.nttdata.model.CustomerResponse;
import com.nttdata.model.CustomerSegment;
import com.nttdata.model.CustomerType;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class CustomerMapperTest {

  @Test
  void toEntity_fromCreateRequest_sets_defaults_and_address() {
    CustomerCreateRequest r = new CustomerCreateRequest();
    r.setFirstName("Arturo");
    r.setLastName("Roncal");
    r.setDocumentNumber("123");
    r.setType(CustomerType.PERSONAL);
    r.setEmail(JsonNullable.of("a@b.com"));

    Address addr = new Address();
    addr.setLine1("Av 1");
    addr.setCity("Lima");
    addr.setCountry("PE");
    r.setAddress(JsonNullable.of(addr));

    Customer c = CustomerMapper.toEntity(r);

    assertEquals("Arturo", c.getFirstName());
    assertEquals("123", c.getDocumentNumber());
    assertEquals("Av 1", c.getAddressLine1());
    assertTrue(c.getActive());
  }

  @Test
  void toEntity_fromUpdateRequest_propagates_nullable_fields() {
    CustomerRequest r = new CustomerRequest();
    r.setFirstName("A");
    r.setLastName("B");
    r.setDocumentNumber("999");
    r.setType(CustomerType.PERSONAL);
    r.setSegment(CustomerSegment.STANDARD);
    r.setActive(Boolean.FALSE);

    Customer c = CustomerMapper.toEntity(r);

    assertEquals("999", c.getDocumentNumber());
    assertEquals(CustomerSegment.STANDARD, c.getSegment());
    assertFalse(c.getActive());
  }

  @Test
  void toResponse_fromDomain_sets_jsonNullable_and_createdAt() {
    Customer c = new Customer();
    c.setId("x1");
    c.setFirstName("Arturo");
    c.setEmail("a@b.com");
    c.setDocumentNumber("123");
    c.setType(CustomerType.PERSONAL);
    c.setSegment(CustomerSegment.STANDARD);
    c.setAddressLine1("Av 1");
    c.setCity("Lima");
    c.setCountry("PE");
    c.setActive(true);
    c.setCreatedAt(Instant.now());

    CustomerResponse resp = CustomerMapper.toResponse(c);

    assertEquals("x1", resp.getId());
    assertTrue(resp.getEmail().isPresent());
    assertTrue(resp.getAddress().isPresent());
    assertNotNull(resp.getCreatedAt());
  }
}
