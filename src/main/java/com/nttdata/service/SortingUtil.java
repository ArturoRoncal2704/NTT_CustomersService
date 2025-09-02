package com.nttdata.service;

import org.springframework.data.domain.Sort;

import java.util.Set;

public class SortingUtil {
    private SortingUtil() {}

    private static final Set<String> ALLOWED =
            Set.of("createdAt", "firstName", "lastName", "businessName");

    public static class Spec{
        public final String property;
        public final Sort.Direction direction;
        public Spec(String property, Sort.Direction direction) {
            this.property = property;
            this.direction = direction;
        }
    }

    public static Spec parse(String sort, String direction){
        String prop = (sort != null && ALLOWED.contains(sort)) ? sort : "createdAt";
        Sort.Direction dir = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return new Spec(prop, dir);
    }
}
