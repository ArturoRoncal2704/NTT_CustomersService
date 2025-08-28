package com.nttdata.domain;

import com.nttdata.model.CustomerSegment;
import com.nttdata.model.CustomerType;
import java.time.Instant;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "customers")
public class Customer {

  @Id private String id;

  private String firstName;
  private String lastName;
  private String email;

  @Indexed private String documentNumber;

  private CustomerType type;
  private CustomerSegment segment;

  private String phone;
  private String addressLine1;
  private String city;
  private String country;

  private Boolean active;

  @CreatedDate private Instant createdAt;

  @LastModifiedDate private Instant updatedAt;
}
