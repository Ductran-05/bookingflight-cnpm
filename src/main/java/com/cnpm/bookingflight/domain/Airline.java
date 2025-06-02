package com.cnpm.bookingflight.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Airline {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String airlineCode;
    String airlineName;
    String logo;

    @Builder.Default
    Boolean isDeleted = false;
}