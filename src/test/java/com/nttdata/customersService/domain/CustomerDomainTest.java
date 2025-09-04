package com.nttdata.customersService.domain;

import com.nttdata.domain.Customer;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CustomerDomainTest {

  @Test
  void defaultSegment_es_STANDARD() {
    assertEquals("STANDARD", Customer.defaultSegment());
  }


  @Test
  void validateSegment_permite_PERSONAL_STANDARD_y_VIP() {
    Customer c1 = Customer.builder().type("PERSONAL").segment("STANDARD").build();
    Customer c2 = Customer.builder().type("PERSONAL").segment("VIP").build();
    assertDoesNotThrow(c1::validateSegment);
    assertDoesNotThrow(c2::validateSegment);
  }

  @Test
  void validateSegment_permite_BUSINESS_STANDARD_y_PYME() {
    Customer c1 = Customer.builder().type("BUSINESS").segment("STANDARD").build();
    Customer c2 = Customer.builder().type("BUSINESS").segment("PYME").build();
    assertDoesNotThrow(c1::validateSegment);
    assertDoesNotThrow(c2::validateSegment);
  }

  @Test
  void validateSegment_rechaza_PERSONAL_PYME() {
    Customer c = Customer.builder().type("PERSONAL").segment("PYME").build();
    IllegalStateException ex = assertThrows(IllegalStateException.class, c::validateSegment);
    assertTrue(ex.getMessage().contains("PERSONAL") && ex.getMessage().contains("PYME"));
  }

  @Test
  void validateSegment_rechaza_BUSINESS_VIP() {
    Customer c = Customer.builder().type("BUSINESS").segment("VIP").build();
    IllegalStateException ex = assertThrows(IllegalStateException.class, c::validateSegment);
    assertTrue(ex.getMessage().contains("BUSINESS") && ex.getMessage().contains("VIP"));
  }

  // ----- refreshDisplayName -----

  @Test
  void refreshDisplayName_toma_first_y_lastName_para_persona() {
    Customer c = Customer.builder().type("PERSONAL").segment("STANDARD")
        .firstName("Ada").lastName("Lovelace").build();
    c.refreshDisplayName();
    assertEquals("Ada Lovelace", c.getDisplayName());
  }

  @Test
  void refreshDisplayName_toma_businessName_si_existe() {
    Customer c = Customer.builder().type("BUSINESS").segment("PYME")
        .businessName("OpenAI Inc").build();
    c.refreshDisplayName();
    assertEquals("OpenAI Inc", c.getDisplayName());
  }

  @Test
  void refreshDisplayName_setea_null_si_todo_vacio() {
    Customer c = Customer.builder().type("PERSONAL").segment("STANDARD").build();
    c.setFirstName(" ");
    c.setLastName(null);
    c.setBusinessName("");
    c.refreshDisplayName();
    assertNull(c.getDisplayName());
  }


  @Test
  void equals_hashCode_y_toString_cubiertos() {
    Customer a = Customer.builder()
        .id("id-1")
        .type("PERSONAL").segment("STANDARD")
        .documentType("DNI").documentNumber("12345678")
        .firstName("Ada").lastName("Lovelace")
        .email("ada@example.com")
        .build();


    Customer b = a.toBuilder().build();
    assertEquals(a, b);
    assertEquals(a.hashCode(), b.hashCode());
    assertNotNull(a.toString());


    Customer c = a.toBuilder().id("id-2").build();
    assertNotEquals(a, c);
  }
}
