package com.cnpm.bookingflight.domain;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Airline {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String airlineCode;
    String airlineName;
    String logo;

    @OneToMany
    List<Plane> planes;
}
