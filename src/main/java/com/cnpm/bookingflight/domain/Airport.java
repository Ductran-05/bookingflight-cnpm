package com.cnpm.bookingflight.domain;

import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Airport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String airportCode;
    String airportName;
    Boolean isDeleted = false;

    @ManyToOne
    @JoinColumn(name = "cityId")
    City city;
}