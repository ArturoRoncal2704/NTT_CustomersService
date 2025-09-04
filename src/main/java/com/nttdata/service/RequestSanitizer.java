package com.nttdata.service;

import com.nttdata.model.Address;
import com.nttdata.model.CustomerCreateRequest;
import com.nttdata.model.CustomerUpdateRequest;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;
import java.util.stream.Collectors;


@Component
public class RequestSanitizer {

    private static final Pattern SPACES = Pattern.compile("\\s+");

    private static String titleCase(String in){
        if(in == null) return null;
        return SPACES.splitAsStream(in.trim().toLowerCase())
                .filter(t -> !t.isEmpty())
                .map(t -> Character.toUpperCase(t.charAt(0)) + t.substring(1))
                .collect(Collectors.joining(" "));
    }

    private static String normalizeSpaces(String in) {
        if (in == null) return null;
        return SPACES.splitAsStream(in.trim())
                .filter(t -> !t.isEmpty())
                .collect(Collectors.joining(" "));
    }

    public void sanitize(CustomerCreateRequest r) {
        if (r == null) return;

        r.setFirstName(titleCase(r.getFirstName()));
        r.setLastName(titleCase(r.getLastName()));
        r.setBusinessName(normalizeSpaces(r.getBusinessName()));
        if (r.getDocumentNumber() != null){
            r.setDocumentNumber(r.getDocumentNumber().trim());
        }
        if (r.getPhone() != null){
            r.setPhone(r.getPhone().trim());
        }
        if(r.getEmail() != null){
            r.setEmail(r.getEmail().trim().toLowerCase());
        }

        Address a = r.getAddress();
        if(a != null){
            a.setLine1(normalizeSpaces(a.getLine1()));
            a.setCity(titleCase(a.getCity()));
            a.setDistrict(titleCase(a.getDistrict()));
            a.setCountry(titleCase(a.getCountry()));
        }
    }

    public void sanitize(CustomerUpdateRequest r) {
        if (r == null) return;

        r.setFirstName(titleCase(r.getFirstName()));
        r.setLastName(titleCase(r.getLastName()));
        r.setBusinessName(normalizeSpaces(r.getBusinessName()));

        if (r.getDocumentNumber() != null) {
            r.setDocumentNumber(r.getDocumentNumber().trim());
        }
        if (r.getPhone() != null) {
            r.setPhone(r.getPhone().trim());
        }
        if (r.getEmail() != null) {
            r.setEmail(r.getEmail().trim().toLowerCase());
        }

        Address a = r.getAddress();
        if (a != null) {
            a.setLine1(normalizeSpaces(a.getLine1()));
            a.setCity(titleCase(a.getCity()));
            a.setDistrict(titleCase(a.getDistrict()));
            a.setCountry(titleCase(a.getCountry()));
        }
    }
}
