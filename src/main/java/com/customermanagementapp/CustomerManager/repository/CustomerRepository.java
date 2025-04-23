package com.customermanagementapp.CustomerManager.repository;

import com.customermanagementapp.CustomerManager.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    // Method to find customers by last name
    List<Customer> findByName(String name);

    // Method to find a customer by email (assuming email is unique)
    Optional<Customer> findByEmail(String email);
}
