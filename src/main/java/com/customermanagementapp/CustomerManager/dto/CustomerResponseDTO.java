package com.customermanagementapp.CustomerManager.dto;

import com.customermanagementapp.CustomerManager.entity.Customer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerResponseDTO {

    private Long id;
    private String name;
    private String email;
    private BigDecimal annualSpend;
    private LocalDateTime lastPurchaseDate;
    private String memberShipTier;//Field for the calculated tier

    // Static method to create DTO from Customer entity and calculated tier
    public static CustomerResponseDTO fromEntity(Customer customer, String membershipTier) {
        CustomerResponseDTO dto = new CustomerResponseDTO();
        dto.setId(customer.getCustomerId());
        dto.setName(customer.getName());
        dto.setEmail(customer.getEmail());
        dto.setAnnualSpend(customer.getAnnualSpend());
        dto.setLastPurchaseDate(customer.getLastPurchaseDate());
        dto.setMemberShipTier(membershipTier); // Set the calculated tier
        return dto;
    }
}
