package com.nttdata.customersService.support;

import com.nttdata.domain.Customer;
import com.nttdata.model.*;

import java.time.Instant;

public final class Fixtures {
  private Fixtures(){}

  public static CustomerCreateRequest newPersonalCreateReq() {
    return new CustomerCreateRequest()
        .type(CustomerType.PERSONAL)
        .documentType(DocumentType.DNI)
        .documentNumber("12345678")
        .firstName("  arturo  ")
        .lastName("  perez ")
        .email("  MAIL@EXAMPLE.com  ")
        .address(new Address().line1(" jr. los sauces   123 ")
                              .city("  lima ")
                              .district("  san  isidro ")
                              .country("  peru "));
  }

  public static CustomerCreateRequest newBusinessCreateReq() {
    return new CustomerCreateRequest()
        .type(CustomerType.BUSINESS)
        .documentType(DocumentType.RUC)
        .documentNumber("20123456789")
        .businessName("  NTT   Data  Peru  S.A.  ");
  }

  public static CustomerUpdateRequest newUpdateReq() {
    return new CustomerUpdateRequest()
        .documentType(DocumentType.DNI)
        .documentNumber("87654321")
        .segment(CustomerSegment.STANDARD)
        .email("  otro@MAIL.com ");
  }

  public static Customer newDomainPersonal() {
    Customer c = Customer.builder()
        .id("c1")
        .type("PERSONAL")
        .segment("STANDARD")
        .documentType("DNI")
        .documentNumber("12345678")
        .firstName("Arturo")
        .lastName("Perez")
        .active(true)
        .createdAt(Instant.now())
        .build();
    c.refreshDisplayName();
    return c;
  }

  public static Customer newDomainBusiness() {
    Customer c = Customer.builder()
        .id("c2")
        .type("BUSINESS")
        .segment("PYME")
        .documentType("RUC")
        .documentNumber("20123456789")
        .businessName("NTT Data Peru S.A.")
        .active(true)
        .createdAt(Instant.now())
        .build();
    c.refreshDisplayName();
    return c;
  }
}
