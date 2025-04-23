package com.customermanagementapp.CustomerManager.service;

import com.customermanagementapp.CustomerManager.dto.CustomerResponseDTO;
import com.customermanagementapp.CustomerManager.entity.Customer;
import com.customermanagementapp.CustomerManager.error.CustomerNotFoundException;
import com.customermanagementapp.CustomerManager.repository.CustomerRepository;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CustomerService {
    private final CustomerRepository customerRepository;

    @Autowired
    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    // --- Retrieval Methods (Now returning DTOs) ---

    public List<CustomerResponseDTO> getAllCustomersDTO() {
        return customerRepository.findAll().stream()
                .map(customer -> CustomerResponseDTO.fromEntity(customer, calculateMembershipTier(customer, LocalDateTime.now())))
                .collect(Collectors.toList());
    }

    public CustomerResponseDTO getCustomerByIdDTO(Long id) throws CustomerNotFoundException {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id: " + id));
        return CustomerResponseDTO.fromEntity(customer, calculateMembershipTier(customer, LocalDateTime.now()));
    }

    public List<CustomerResponseDTO> getCustomersByNameDTO(String name) {
        return customerRepository.findByName(name).stream()
                .map(customer -> CustomerResponseDTO.fromEntity(customer, calculateMembershipTier(customer, LocalDateTime.now())))
                .collect(Collectors.toList());
    }

    public Optional<CustomerResponseDTO> getCustomerByEmailDTO(String email) {
        Optional<Customer> customerOptional = customerRepository.findByEmail(email);
        // Map the Optional<Customer> to Optional<CustomerResponseDTO>
        return customerOptional.map(customer -> CustomerResponseDTO.fromEntity(customer, calculateMembershipTier(customer, LocalDateTime.now())));
    }

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public Customer getCustomerById(Long id) throws CustomerNotFoundException {
        return customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id: " + id));
    }

    public Customer createCustomer(Customer customer) {
        /*// checks here if email already exists means customer already exists
        if(customer.getCustomerId() != null) {
            throw new RuntimeException("Customer already exists");
        }*/
        return customerRepository.save(customer);
    }

    public Customer updateCustomer(Long id, Customer customerDetails) throws CustomerNotFoundException {
        Customer customer = getCustomerById(id); // Throws exception if not found

        // Update fields
        customer.setName(customerDetails.getName());
        customer.setEmail(customerDetails.getEmail());
        customer.setAnnualSpend(customerDetails.getAnnualSpend());
        customer.setLastPurchaseDate(customerDetails.getLastPurchaseDate());

        return customerRepository.save(customer);
    }

    public void deleteCustomer(Long id) throws CustomerNotFoundException {
        // Optional: Check if exists before deleting, or rely on JPA's behavior
        if (!customerRepository.existsById(id)) {
            throw new CustomerNotFoundException("Customer not found with id: " + id);
        }
        customerRepository.deleteById(id);
    }

    // --- Tier Calculation Logic ---

    /**
     * Calculates the membership tier based on annual spend and last purchase date.
     * New Tier Logic:
     * Platinum: Annual spend >= $10000 AND purchased within the last 6 months.
     * Gold:     Annual spend >= $1000 AND < $10000 AND purchased within the last 12 months.
     * Bronze:   Otherwise.
     * Returns "Invalid Spend" if annualSpend is null or negative.
     *
     * @param customer The customer entity.
     * @param effectiveNow The LocalDateTime to use as the current time reference for date calculations.
     * @return The membership tier as a String.
     */
    public String calculateMembershipTier(Customer customer, LocalDateTime effectiveNow) {
        if (customer == null || customer.getAnnualSpend() == null || customer.getAnnualSpend().compareTo(BigDecimal.ZERO) < 0) {
            return "Invalid Spend";
        }

        BigDecimal spendThresholdGold = new BigDecimal("1000");
        BigDecimal spendThresholdPlatinum = new BigDecimal("10000");
        LocalDateTime lastPurchaseDate = customer.getLastPurchaseDate();

        // Check for Platinum tier
        if (customer.getAnnualSpend().compareTo(spendThresholdPlatinum) >= 0) {
            if (lastPurchaseDate != null && lastPurchaseDate.isAfter(effectiveNow.minusMonths(6))) {
                return "Platinum";
            }
        }

        // Check for Gold tier
        if (customer.getAnnualSpend().compareTo(spendThresholdGold) >= 0 && customer.getAnnualSpend().compareTo(spendThresholdPlatinum) < 0) {
            if (lastPurchaseDate != null && lastPurchaseDate.isAfter(effectiveNow.minusMonths(12))) {
                return "Gold";
            }
        }

        // Default to Bronze if not Platinum or Gold
        return "Bronze";
    }

    // Provide a version of the calculation method that uses LocalDateTime.now() for convenience in controllers
    public String calculateMembershipTier(Customer customer) {
        return calculateMembershipTier(customer, LocalDateTime.now());
    }

}
