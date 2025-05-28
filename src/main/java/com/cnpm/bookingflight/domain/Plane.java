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

@Data
@NoArgsConstructor
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Plane {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String planeCode;
    String planeName;
    @Builder.Default
    Boolean isDeleted = false;

    @ManyToOne
    @JoinColumn(name = "airlineId")
    Airline airline;
}