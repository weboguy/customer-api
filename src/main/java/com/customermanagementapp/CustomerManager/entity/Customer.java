package com.customermanagementapp.CustomerManager.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long customerId;

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name cannot exceed 100 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Column(unique = true) // To ensure email uniqueness
    @Size(max = 255, message = "Email cannot exceed 255 characters")
    private String email;

    @NotNull(message = "Annual spend is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Annual spend cannot be negative")
    @Column(nullable = false)
    private BigDecimal annualSpend;

    @Column(nullable = true) // Make the column nullable in the database
    private LocalDateTime lastPurchaseDate;

    public Customer(Object o, String name, String email, BigDecimal annualSpend, LocalDateTime lastPurchaseDate) {
    }
}
