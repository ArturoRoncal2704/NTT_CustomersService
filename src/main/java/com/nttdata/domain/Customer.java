package com.nttdata.domain;

import java.time.Instant;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;


@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "customers")
@CompoundIndex(
        name = "ux_doc_active",
        def = "{'documentType':1,'documentNumber':1,'active':1}",
        unique = true
)
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

  private String displayName;

  public void refreshDisplayName() {
    if ("PERSONAL".equals(type)) {
      this.displayName =
              (firstName == null ? "" : firstName) + " " + (lastName == null ? "" : lastName);
    } else {
      this.displayName = businessName;
    }
  }

  public void validateSegment() {
    if ("PERSONAL".equals(type) && "PYME".equals(segment)) {
      throw new IllegalStateException("A PERSONAL customer cannot be PYME");
    }
    if ("BUSINESS".equals(type) && "VIP".equals(segment)) {
      throw new IllegalStateException("A BUSINESS customer cannot be VIP");
    }
  }

  public static String defaultSegment() {
    return "STANDARD";
  }
}
