package com.nttdata.domain;

import java.time.Instant;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;


@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "customers")
public class Customer {

  @Id
  private String id;

  private String type;
  private String segment;
  private String firstName;
  private String lastName;
  private String businessName;

  private String email;
  private String documentType;
  private String documentNumber;

  private String phone;
  private String addressLine1;
  private String addressCity;
  private String addressDistrict;
  private String addressCountry;

  private Boolean active;

  private Instant createdAt;
  private Instant deletedAt;

  private String displayName;

  public static String defaultSegment() {
    return "STANDARD";
  }

  public void validateSegment() {
    if ("PERSONAL".equals(type) && "PYME".equals(segment)) {
      throw new IllegalStateException("A PERSONAL customer cannot be PYME");
    }
    if ("BUSINESS".equals(type) && "VIP".equals(segment)) {
      throw new IllegalStateException("A BUSINESS customer cannot be VIP");
    }
  }

  public void refreshDisplayName() {
   String name = Stream.of(getFirstName(),getLastName(),getBusinessName())
           .filter( s -> s != null && !s.isBlank())
           .collect(Collectors.joining(" "));
   setDisplayName(name.isBlank() ? null : name);
  }


}
