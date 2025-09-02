package com.nttdata.service;

import com.nttdata.model.CustomerCreateRequest;
import org.springframework.stereotype.Component;


@Component
public class RequestSanitizer {

  public void sanitize(CustomerCreateRequest r) {
    if (r == null) return;
    if (r.getFirstName() != null)    r.setFirstName(r.getFirstName().trim());
    if (r.getLastName() != null)     r.setLastName(r.getLastName().trim());
    if (r.getBusinessName() != null) r.setBusinessName(r.getBusinessName().trim());
    if (r.getEmail() != null)        r.setEmail(r.getEmail().trim().toLowerCase());
    if (r.getPhone() != null)        r.setPhone(r.getPhone().trim());
    if (r.getDocumentNumber() != null) r.setDocumentNumber(r.getDocumentNumber().trim());


    if (r.getAddress() != null) {
      if (r.getAddress().getLine1() != null)   r.getAddress().setLine1(r.getAddress().getLine1().trim());
      if (r.getAddress().getCity() != null)    r.getAddress().setCity(r.getAddress().getCity().trim());
      if (r.getAddress().getDistrict() != null)r.getAddress().setDistrict(r.getAddress().getDistrict().trim());
      if (r.getAddress().getCountry() != null) r.getAddress().setCountry(r.getAddress().getCountry().trim());
    }
  }
}
