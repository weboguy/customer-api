package com.customermanagementapp.CustomerManager.controller;

import com.customermanagementapp.CustomerManager.dto.CustomerResponseDTO;
import com.customermanagementapp.CustomerManager.entity.Customer;
import com.customermanagementapp.CustomerManager.error.CustomerNotFoundException;
import com.customermanagementapp.CustomerManager.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/customers")
@Tag(name = "Customers", description = "Customer Management API")
public class CustomerController {
    private final CustomerService customerService;

    @Autowired
    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @Operation(summary = "Get customers", description = "Retrieve all customers, or filter by name or email. Returns Customer details including calculated membership tier.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customers found",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = CustomerResponseDTO.class)) }), // Schema now points to DTO
            @ApiResponse(responseCode = "404", description = "Customer not found by email",
                    content = @Content)
    })
    @GetMapping
    public ResponseEntity<?> getCustomers(
            @Parameter(description = "Filter by customer's name") @RequestParam(required = false) String name,
            @Parameter(description = "Filter by customer's email") @RequestParam(required = false) String email) throws CustomerNotFoundException {

        if (email != null && !email.isEmpty()) {
            // Lookup by email (returns Optional<DTO>)
            Optional<CustomerResponseDTO> customerDTO = customerService.getCustomerByEmailDTO(email);
            if (customerDTO.isPresent()) {
                return ResponseEntity.ok(customerDTO.get()); // Return the single DTO
            } else {
                throw new CustomerNotFoundException("Customer not found with email: " + email); // Return 404 if not found
            }
        } else if (name != null && !name.isEmpty()) {
            // Lookup by name ( name in this implementation) (returns List<DTO>)
            List<CustomerResponseDTO> customerDTOs = customerService.getCustomersByNameDTO(name);
            return ResponseEntity.ok(customerDTOs); // Return a list of DTOs
        } else {
            // No parameters, return all customers (returns List<DTO>)
            List<CustomerResponseDTO> customerDTOs = customerService.getAllCustomersDTO();
            return ResponseEntity.ok(customerDTOs); // Return a list of DTOs
        }
    }


    @Operation(summary = "Get a customer by ID", description = "Retrieve a customer by their unique ID, including calculated membership tier.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customer found",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = CustomerResponseDTO.class)) }), // Schema now points to DTO
            @ApiResponse(responseCode = "404", description = "Customer not found",
                    content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponseDTO> getCustomerById( // Return DTO
                                                                @Parameter(description = "ID of the customer to retrieve") @PathVariable Long id) throws CustomerNotFoundException {
        CustomerResponseDTO customerDTO = customerService.getCustomerByIdDTO(id); // Call service method returning DTO
        return ResponseEntity.ok(customerDTO);
    }

    @Operation(summary = "Create a new customer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Customer created",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = Customer.class)) }), // Input is still Customer entity
            @ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content)
    })
    @PostMapping
    public ResponseEntity<Customer> createCustomer(@Valid @RequestBody Customer customer) {
        // POST input is still the Customer entity
        Customer createdCustomer = customerService.createCustomer(customer);
        // NOTE: The response here is the saved entity.
        // For simplicity, returning the saved entity as requested in the original POST requirement.
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCustomer);
    }

    @Operation(summary = "Update an existing customer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customer updated",
                    content = { @Content(mediaType = "application/json", schema = @Schema(implementation = Customer.class)) }), // Input/Output usually the updated entity for PUT
            @ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Customer not found",
                    content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<Customer> updateCustomer( // PUT input/output often uses entity
                                                    @Parameter(description = "ID of the customer to update") @PathVariable Long id,
                                                    @Valid @RequestBody Customer customerDetails) throws CustomerNotFoundException {
        Customer updatedCustomer = customerService.updateCustomer(id, customerDetails);
        // NOTE: Similar to POST, returning the saved entity. To get the DTO with tier,
        // need to fetch/map it here.
        return ResponseEntity.ok(updatedCustomer);
    }

    @Operation(summary = "Delete a customer by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Customer deleted"),
            @ApiResponse(responseCode = "404", description = "Customer not found",
                    content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(
            @Parameter(description = "ID of the customer to delete") @PathVariable Long id) throws CustomerNotFoundException {
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }

    // Note: The /tier endpoint can still exist, but the main GET endpoints now
    // include the tier directly in the customer object structure.
    // This /tier endpoint can be removed if one prefers.
    @Operation(summary = "Get membership tier for a customer by ID", description = "Calculate and return the membership tier for a customer.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tier calculated successfully",
                    content = { @Content(mediaType = "text/plain", schema = @Schema(type = "string")) }),
            @ApiResponse(responseCode = "404", description = "Customer not found",
                    content = @Content)
    })
    @GetMapping("/{id}/tier")
    public ResponseEntity<String> getCustomerMembershipTier(
            @Parameter(description = "ID of the customer to get tier for") @PathVariable Long id) throws CustomerNotFoundException {
        // This endpoint still fetches the entity to calculate the tier string directly
        Customer customer = customerService.getCustomerById(id); // Uses old getCustomerById returning entity
        String tier = customerService.calculateMembershipTier(customer); // Uses calc method that takes entity
        return ResponseEntity.ok(tier);
    }
}
