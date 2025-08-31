package com.nttdata.mapper;

import com.nttdata.domain.Customer;
import com.nttdata.model.Address;
import com.nttdata.model.CustomerCreateRequest;
import com.nttdata.model.CustomerResponse;
import com.nttdata.model.CustomerSegment;
import com.nttdata.model.CustomerType;
import com.nttdata.model.CustomerUpdateRequest;
import com.nttdata.model.DocumentType;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;


public final class CustomerMapper {
  private CustomerMapper() {}

  // ========= CREATE (POST) =========
  public static Customer toDomain(CustomerCreateRequest r) {
    Address a = r.getAddress(); // en tu contrato Address nunca debe ser null (required)
    Customer c = Customer.builder()
            .id(null)
            .type(asString(r.getType()))
            .segment(Customer.defaultSegment()) // STANDARD por regla de negocio
            .firstName(r.getFirstName())
            .lastName(r.getLastName())
            .businessName(r.getBusinessName())
            .email(r.getEmail())
            .documentType(asString(r.getDocumentType()))
            .documentNumber(r.getDocumentNumber())
            .phone(r.getPhone())
            .addressLine1(a != null ? a.getLine1() : null)
            .addressCity(a != null ? a.getCity() : null)
            .addressDistrict(a != null ? a.getDistrict() : null)
            .addressCountry(a != null ? a.getCountry() : null)
            .active(r.getActive() == null ? Boolean.TRUE : r.getActive())
            .createdAt(OffsetDateTime.now().toInstant())
            .displayName(null)
            .build();
    c.refreshDisplayName();
    return c;
  }

  // ========= UPDATE (PUT) =========
  public static void applyUpdate(Customer target, CustomerUpdateRequest r) {
    target.setType(asString(r.getType()));
    target.setSegment(asString(r.getSegment()));
    target.setFirstName(r.getFirstName());
    target.setLastName(r.getLastName());
    target.setBusinessName(r.getBusinessName());
    target.setEmail(r.getEmail());
    target.setDocumentType(asString(r.getDocumentType()));
    target.setDocumentNumber(r.getDocumentNumber());
    target.setPhone(r.getPhone());

    Address a = r.getAddress();
    if (a != null) {
      target.setAddressLine1(a.getLine1());
      target.setAddressCity(a.getCity());
      target.setAddressDistrict(a.getDistrict());
      target.setAddressCountry(a.getCountry());
    } else {
      target.setAddressLine1(null);
      target.setAddressCity(null);
      target.setAddressDistrict(null);
      target.setAddressCountry(null);
    }

    target.setActive(r.getActive());
    // Reglas de negocio de segmento según tipo
    target.validateSegment();
    target.refreshDisplayName();
  }

  // ========= DOMAIN → API (RESPONSE) =========
  public static CustomerResponse toApi(Customer d) {
    Address addr = new Address()
            .line1(d.getAddressLine1())
            .city(d.getAddressCity())
            .district(d.getAddressDistrict())
            .country(d.getAddressCountry());

    CustomerResponse resp = new CustomerResponse();
    resp.setId(d.getId());
    resp.setCreatedAt(d.getCreatedAt() == null ? null : d.getCreatedAt().atOffset(ZoneOffset.UTC));
    resp.setDisplayName(d.getDisplayName());
    resp.setType(toTypeEnum(d.getType()));
    resp.setSegment(toSegmentEnum(d.getSegment()));
    resp.setFirstName(d.getFirstName());
    resp.setLastName(d.getLastName());
    resp.setBusinessName(d.getBusinessName());
    resp.setEmail(d.getEmail());
    resp.setDocumentType(toDocEnum(d.getDocumentType()));
    resp.setDocumentNumber(d.getDocumentNumber());
    resp.setPhone(d.getPhone());
    resp.setAddress(addr);
    resp.setActive(d.getActive());
    return resp;
  }

  // ===== Helpers =====

  // Enum → String (para el dominio)
  private static String asString(CustomerType e)     { return e == null ? null : e.getValue(); }
  private static String asString(CustomerSegment e)  { return e == null ? null : e.getValue(); }
  private static String asString(DocumentType e)     { return e == null ? null : e.getValue(); }

  // String (del dominio) → Enum (para el response)
  // Si tu generator NO tiene getValue()/fromValue, cambia a valueOf(s).
  private static CustomerType toTypeEnum(String s)        { return s == null ? null : CustomerType.fromValue(s); }
  private static CustomerSegment toSegmentEnum(String s)  { return s == null ? null : CustomerSegment.fromValue(s); }
  private static DocumentType toDocEnum(String s)         { return s == null ? null : DocumentType.fromValue(s); }
}
