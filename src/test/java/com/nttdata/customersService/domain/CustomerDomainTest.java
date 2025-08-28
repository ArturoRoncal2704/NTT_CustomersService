package com.nttdata.customersService.domain;

import com.nttdata.domain.Customer;
import com.nttdata.model.CustomerSegment;
import com.nttdata.model.CustomerType;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class CustomerDomainTest {

  @Test
  void getters_setters_and_lombok_methods() {
    Instant now = Instant.now();

    Customer c = new Customer();
    c.setId("id1");
    c.setFirstName("Arturo");
    c.setLastName("Roncal");
    c.setEmail("a@b.com");
    c.setDocumentNumber("123");
    c.setType(CustomerType.PERSONAL);
    c.setSegment(CustomerSegment.STANDARD);
    c.setPhone("999");
    c.setAddressLine1("Av 1");
    c.setCity("Lima");
    c.setCountry("PE");
    c.setActive(true);
    c.setCreatedAt(now);
    c.setUpdatedAt(now);

    // getters
    assertEquals("id1", c.getId());
    assertEquals("Arturo", c.getFirstName());
    assertEquals("Roncal", c.getLastName());
    assertEquals("a@b.com", c.getEmail());
    assertEquals("123", c.getDocumentNumber());
    assertEquals(CustomerType.PERSONAL, c.getType());
    assertEquals(CustomerSegment.STANDARD, c.getSegment());
    assertEquals("999", c.getPhone());
    assertEquals("Av 1", c.getAddressLine1());
    assertEquals("Lima", c.getCity());
    assertEquals("PE", c.getCountry());
    assertTrue(c.getActive());
    assertEquals(now, c.getCreatedAt());
    assertEquals(now, c.getUpdatedAt());

    Customer c2 = new Customer();
    c2.setId("id1");
    c2.setDocumentNumber("123");
    c2.setType(CustomerType.PERSONAL);
    c2.setSegment(CustomerSegment.STANDARD);

    assertNotEquals(c, new Customer());
    assertNotEquals(0, c.hashCode());
    assertTrue(c.toString().contains("Arturo"));
  }
}
