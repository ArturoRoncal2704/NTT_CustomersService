package com.nttdata.customersService.config;

import com.nttdata.config.CustomerRequestValidator;
import com.nttdata.model.CustomerCreateRequest;
import com.nttdata.model.CustomerType;
import com.nttdata.model.DocumentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CustomerRequestValidatorTest {

  private CustomerRequestValidator validator;

  @BeforeEach
  void setUp() {
    validator = new CustomerRequestValidator();
  }

  private static CustomerCreateRequest base() {
    return new CustomerCreateRequest()
        .type(CustomerType.PERSONAL)
        .documentType(DocumentType.DNI)
        .documentNumber("12345678")
        .firstName("Ada")
        .lastName("Lovelace");
  }

  @Test
  void request_nulo_lanza_illeagalArgument() {
    assertThrows(IllegalArgumentException.class, () -> validator.validateCreate(null));
  }

  @Test
  void type_nulo_es_obligatorio() {
    CustomerCreateRequest r = base().type(null);
    assertThrows(IllegalArgumentException.class, () -> validator.validateCreate(r));
  }

  @Test
  void documentType_nulo_es_obligatorio() {
    CustomerCreateRequest r = base().documentType(null);
    assertThrows(IllegalArgumentException.class, () -> validator.validateCreate(r));
  }

  @Test
  void documentNumber_vacio_es_obligatorio() {
    CustomerCreateRequest r = base().documentNumber("  ");
    assertThrows(IllegalArgumentException.class, () -> validator.validateCreate(r));
  }

  @Test
  void personal_requiere_firstName_y_lastName() {
    CustomerCreateRequest r1 = base().firstName("  ");
    assertThrows(IllegalStateException.class, () -> validator.validateCreate(r1));

    CustomerCreateRequest r2 = base().lastName(null);
    assertThrows(IllegalStateException.class, () -> validator.validateCreate(r2));
  }

  @Test
  void business_requiere_businessName() {
    CustomerCreateRequest r = new CustomerCreateRequest()
        .type(CustomerType.BUSINESS)
        .documentType(DocumentType.RUC)
        .documentNumber("20123456789")
        .businessName("  ");
    assertThrows(IllegalStateException.class, () -> validator.validateCreate(r));
  }

  @Test
  void personal_nombre_y_apellido_hasta_60_ok_61_falla() {
    String sixty = "x".repeat(60);
    String sixtyOne = "x".repeat(61);

    CustomerCreateRequest ok = base().firstName(sixty).lastName(sixty);
    assertDoesNotThrow(() -> validator.validateCreate(ok));

    CustomerCreateRequest bad = base().firstName(sixtyOne);
    assertThrows(IllegalStateException.class, () -> validator.validateCreate(bad));
  }

  @Test
  void business_businessName_min2_max120() {
    String two = "ab";
    String oneTwenty = "x".repeat(120);

    CustomerCreateRequest okMin = new CustomerCreateRequest()
        .type(CustomerType.BUSINESS)
        .documentType(DocumentType.RUC)
        .documentNumber("20123456789")
        .businessName(two);
    assertDoesNotThrow(() -> validator.validateCreate(okMin));

    CustomerCreateRequest okMax = new CustomerCreateRequest()
        .type(CustomerType.BUSINESS)
        .documentType(DocumentType.RUC)
        .documentNumber("20123456789")
        .businessName(oneTwenty);
    assertDoesNotThrow(() -> validator.validateCreate(okMax));

    CustomerCreateRequest bad = new CustomerCreateRequest()
        .type(CustomerType.BUSINESS)
        .documentType(DocumentType.RUC)
        .documentNumber("20123456789")
        .businessName("a");
    assertThrows(IllegalStateException.class, () -> validator.validateCreate(bad));
  }


  @Test
  void dni_debe_tener_8_digitos() {
    CustomerCreateRequest ok = base().documentType(DocumentType.DNI).documentNumber(" 12345678 ");
    assertDoesNotThrow(() -> validator.validateCreate(ok));

    CustomerCreateRequest bad7 = base().documentType(DocumentType.DNI).documentNumber("1234567");
    assertThrows(IllegalStateException.class, () -> validator.validateCreate(bad7));

    CustomerCreateRequest bad9 = base().documentType(DocumentType.DNI).documentNumber("123456789");
    assertThrows(IllegalStateException.class, () -> validator.validateCreate(bad9));

    CustomerCreateRequest badChars = base().documentType(DocumentType.DNI).documentNumber("12A45678");
    assertThrows(IllegalStateException.class, () -> validator.validateCreate(badChars));
  }

  @Test
  void ruc_debe_tener_11_digitos() {
    CustomerCreateRequest ok = base()
        .type(CustomerType.BUSINESS)
        .documentType(DocumentType.RUC)
        .documentNumber("20123456789")
        .firstName(null).lastName(null)
        .businessName("Mi Empresa");
    assertDoesNotThrow(() -> validator.validateCreate(ok));

    CustomerCreateRequest bad = ok.documentNumber("2012345678");
    assertThrows(IllegalStateException.class, () -> validator.validateCreate(bad));
  }

  @Test
  void ce_alfanumerico_9_a_12_y_guion_permitido() {
    CustomerCreateRequest baseCe = base()
        .documentType(DocumentType.CE)
        .type(CustomerType.PERSONAL);

    CustomerCreateRequest ok9 = baseCe.documentNumber("ABC1234-9");
    assertDoesNotThrow(() -> validator.validateCreate(ok9));

    CustomerCreateRequest ok12 = baseCe.documentNumber("A1B2C3D4E5F6");
    assertDoesNotThrow(() -> validator.validateCreate(ok12));

    CustomerCreateRequest short8 = baseCe.documentNumber("A1B2C3D4");
    assertThrows(IllegalStateException.class, () -> validator.validateCreate(short8));

    CustomerCreateRequest long13 = baseCe.documentNumber("A1B2C3D4E5F6G");
    assertThrows(IllegalStateException.class, () -> validator.validateCreate(long13));

    CustomerCreateRequest badChar = baseCe.documentNumber("A1B2C3D4*");
    assertThrows(IllegalStateException.class, () -> validator.validateCreate(badChar));
  }


  @Test
  void personal_valido_pasa() {
    assertDoesNotThrow(() -> validator.validateCreate(base()));
  }

  @Test
  void business_valido_pasa() {
    CustomerCreateRequest r = new CustomerCreateRequest()
        .type(CustomerType.BUSINESS)
        .documentType(DocumentType.RUC)
        .documentNumber("20123456789")
        .businessName("Mi Empresa SAC");
    assertDoesNotThrow(() -> validator.validateCreate(r));
  }
}
