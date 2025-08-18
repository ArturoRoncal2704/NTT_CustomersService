package com.nttdata.domain;

import com.nttdata.model.CustomerRequest;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "customers")
public class Customer {

  @Id
  private String id;
  private String firstName;
  private String lastName;
  private String email;
  private String documentNumber;
  private CustomerRequest.TypeEnum type;
  private String phone;
  private String addressLine1;
  private String city;
  private String country;

  private Boolean active;
}
