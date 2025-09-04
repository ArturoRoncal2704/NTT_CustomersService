package com.nttdata.config;

import com.nttdata.model.CustomerCreateRequest;
import com.nttdata.model.CustomerType;
import com.nttdata.model.DocumentType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class CustomerRequestValidator {


    public void validateCreate(CustomerCreateRequest r) {
        if (r == null) {
            throw new IllegalArgumentException("request es nulo");
        }
        if (r.getType() == null) {
            throw new IllegalArgumentException("type es obligatorio");
        }
        if (r.getDocumentType() == null) {
            throw new IllegalArgumentException("documentType es obligatorio");
        }
        if (!StringUtils.hasText(r.getDocumentNumber())) {
            throw new IllegalArgumentException("documentNumber es obligatorio");
        }

        if (r.getType() == CustomerType.PERSONAL) {
            if (!hasTextBetween(r.getFirstName(), 1, 60) || !hasTextBetween(r.getLastName(), 1, 60)) {
                throw new IllegalStateException("firstName y lastName son obligatorios para PERSONAL");
            }
        } else if (r.getType() == CustomerType.BUSINESS) {
            if (!hasTextBetween(r.getBusinessName(), 2, 120)) {
                throw new IllegalStateException("businessName es obligatorio para BUSINESS");
            }
        }

        String num = r.getDocumentNumber().trim();
        DocumentType dt = r.getDocumentType();
        if (dt == DocumentType.DNI && !num.matches(RE_DNI)) {
            throw new IllegalStateException("DNI debe tener 8 dígitos");
        }
        if (dt == DocumentType.RUC && !num.matches(RE_RUC)) {
            throw new IllegalStateException("RUC debe tener 11 dígitos");
        }
        if (dt == DocumentType.CE && !num.matches(RE_CE)) {
            throw new IllegalStateException("CE debe ser alfanumérico de 9 a 12 caracteres");
        }
    }

    private boolean hasTextBetween(String v, int min, int max) {
        if (v == null) return false;
        String s = v.trim();
        return !s.isEmpty() && s.length() >= min && s.length() <= max;
    }
    private static final String RE_DNI = "^\\d{8}$";
    private static final String RE_RUC = "^\\d{11}$";
    private static final String RE_CE  = "^[A-Za-z0-9-]{9,12}$";
}
