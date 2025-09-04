package com.nttdata.customersService.mapper;

import com.nttdata.domain.Customer;
import com.nttdata.mapper.CustomerMapper;
import com.nttdata.model.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static com.nttdata.customersService.support.Fixtures.*;

class CustomerMapperTest {

  @Test
  void toDomain_fromCreateRequest() {
    CustomerCreateRequest req = newPersonalCreateReq();
    Customer domain = CustomerMapper.toDomain(req);
    assertEquals("PERSONAL", domain.getType());
    assertEquals("DNI", domain.getDocumentType());
    assertEquals("12345678", domain.getDocumentNumber());
  }

  @Test
  void toApi_fromDomain() {
    Customer c = newDomainBusiness();
    CustomerResponse res = CustomerMapper.toApi(c);
    assertEquals(CustomerType.BUSINESS, res.getType());
    assertEquals(CustomerSegment.PYME, res.getSegment());
    assertEquals("20123456789", res.getDocumentNumber());
  }

  @Test
  void toEligibility_fromDomain() {
    Customer c = newDomainPersonal();
    EligibilityResponse e = CustomerMapper.toEligibility(c);
    assertEquals("PERSONAL",String.valueOf(e.getType()));
    assertEquals("STANDARD",String.valueOf(e.getProfile()));
    assertFalse(Boolean.TRUE.equals(e.getHasActiveCreditCard()));
  }

  @Test
  void enums_helpers_roundtrip() {
    assertEquals("PERSONAL", CustomerMapper.asString(CustomerType.PERSONAL));
    assertEquals(CustomerType.PERSONAL, CustomerMapper.toTypeEnum("PERSONAL"));
    assertEquals("STANDARD", CustomerMapper.asString(CustomerSegment.STANDARD));
    assertEquals(CustomerSegment.STANDARD, CustomerMapper.toSegmentEnum("STANDARD"));
    assertEquals("DNI", CustomerMapper.asString(DocumentType.DNI));
    assertEquals(DocumentType.DNI, CustomerMapper.toDocEnum("DNI"));
  }
}
