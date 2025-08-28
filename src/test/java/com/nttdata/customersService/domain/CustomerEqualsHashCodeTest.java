package com.nttdata.customersService.domain;

import com.nttdata.domain.Customer;
import com.nttdata.model.CustomerSegment;
import com.nttdata.model.CustomerType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CustomerEqualsHashCodeTest {

  private Customer base(String id, String doc) {
    Customer c = new Customer();
    c.setId(id);
    c.setFirstName("Arturo");
    c.setLastName("Roncal");
    c.setEmail("a@b.com");
    c.setDocumentNumber(doc);
    c.setType(CustomerType.PERSONAL);
    c.setSegment(CustomerSegment.STANDARD);
    c.setPhone("999");
    c.setAddressLine1("Av 1");
    c.setCity("Lima");
    c.setCountry("PE");
    c.setActive(true);
    return c;
  }

  static class CustomerChild extends Customer { }

  @Test
  void equals_true_and_hashcode_equal_for_same_values() {
    Customer c1 = base("id1", "123");
    Customer c2 = base("id1", "123");

    assertEquals(c1, c2);
    assertEquals(c1.hashCode(), c2.hashCode());
  }

  @Test
  void equals_false_when_different_values_and_with_different_type_triggers_canEqual() {
    Customer c1 = base("id1", "123");
    Customer c2 = base("id2", "999");

    assertNotEquals(c1, c2); // rama false
    CustomerChild child = new CustomerChild();
    child.setId("id1");
    child.setDocumentNumber("123");
    child.setType(CustomerType.PERSONAL);
    child.setSegment(CustomerSegment.STANDARD);

    assertNotEquals(c1, child);
  }

  @Test
  void equals_false_against_null_and_other_type() {
    Customer c1 = base("id1", "123");
    assertNotEquals(c1, null);
    assertNotEquals(c1, "otro tipo");
  }
}
