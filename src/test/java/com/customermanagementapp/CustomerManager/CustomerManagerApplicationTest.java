package com.customermanagementapp.CustomerManager;

import com.customermanagementapp.CustomerManager.entity.Customer;
import com.customermanagementapp.CustomerManager.repository.CustomerRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.hamcrest.Matchers.*; // Import necessary matchers
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest
@AutoConfigureMockMvc
class CustomerManagerApplicationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private CustomerRepository customerRepository;

	@Autowired
	private ObjectMapper objectMapper;

	private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

	// --- Helper methods for creating test data ---
	private Customer createCustomer(String name, String email, BigDecimal annualSpend, LocalDateTime lastPurchaseDate) {
		Customer customer = new Customer(null, name, email, annualSpend, lastPurchaseDate);
		return customerRepository.save(customer);
	}

	@BeforeEach
	void setUp() {
		customerRepository.deleteAll();
	}

	// --- GET /api/customers Tests (All, By Name, By Email - now returning DTOs) ---

	@Test
	void testGetAllCustomers_ReturnsDTOsWithTier() throws Exception {
		// Setup test data with dates relative to a hypothetical 'now' for predictable tier calculation
		LocalDateTime fixedNow = LocalDateTime.of(2025, 4, 22, 21, 0); // Use a fixed time for testing tier logic

		// Bronze Customer (< $1000)
		createCustomer("Bronze", "bronze1@example.com", new BigDecimal("500.00"), fixedNow.minusMonths(1)); // Purchase date irrelevant for Bronze based on spend

		// Gold Customer (>= $1000, < $10000, purchase in last 12 months)
		createCustomer("Gold", "gold2@example.com", new BigDecimal("2500.00"), fixedNow.minusMonths(10)); // Within 12 months

		// Bronze Customer (>= $1000, < $10000, purchase NOT in last 12 months)
		createCustomer("Bronze", "bronze3@example.com", new BigDecimal("3000.00"), fixedNow.minusMonths(13)); // Outside 12 months

		// Platinum Customer (>= $10000, purchase in last 6 months)
		createCustomer("Platinum", "platinum4@example.com", new BigDecimal("15000.00"), fixedNow.minusMonths(5)); // Within 6 months

		// Bronze Customer (>= $10000, purchase NOT in last 6 months)
		createCustomer("Bronze", "bronze5@example.com", new BigDecimal("12000.00"), fixedNow.minusMonths(7)); // Outside 6 months

		// Bronze Customer (No purchase date)
		createCustomer("Bronze", "bronze6@example.com", new BigDecimal("7500.00"), null);


		mockMvc.perform(get("/api/customers")
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(6)))
				// Assert specific customers by email and check their tier (or other fields)
				.andExpect(jsonPath("$[?(@.email == 'bronze1@example.com')].memberShipTier", contains("Bronze")))
				.andExpect(jsonPath("$[?(@.email == 'gold2@example.com')].memberShipTier", contains("Gold")))
				.andExpect(jsonPath("$[?(@.email == 'bronze3@example.com')].memberShipTier", contains("Bronze")))
				.andExpect(jsonPath("$[?(@.email == 'platinum4@example.com')].memberShipTier", contains("Platinum")))
				.andExpect(jsonPath("$[?(@.email == 'bronze5@example.com')].memberShipTier", contains("Bronze")))
				.andExpect(jsonPath("$[?(@.email == 'bronze6@example.com')].memberShipTier", contains("Bronze")));
	}

	@Test
	void testGetCustomersByName_ReturnsDTOsWithTier() throws Exception {
		LocalDateTime fixedNow = LocalDateTime.of(2025, 4, 22, 21, 0);
		createCustomer("John Doe", "john.doe1@example.com", new BigDecimal("3000.00"), fixedNow.minusMonths(10)); // Gold criteria
		createCustomer("Jane Doe", "jane.doe2@example.com", new BigDecimal("500.00"), fixedNow.minusMonths(1)); // Bronze criteria
		createCustomer("Peter Jones", "peter.jones1@example.com", new BigDecimal("12000.00"), fixedNow.minusMonths(4)); // Platinum criteria

		mockMvc.perform(get("/api/customers")
						.param("name", "Doe") // Search by name
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[?(@.name == 'John Doe')].memberShipTier", contains("Gold")))
				.andExpect(jsonPath("$[?(@.name == 'Jane Doe')].memberShipTier", contains("Bronze")));
	}

	@Test
	void testGetCustomersByEmail_ReturnsSingleDTOWithTier() throws Exception {
		LocalDateTime fixedNow = LocalDateTime.of(2025, 4, 22, 21, 0);
		Customer customer = createCustomer("Peter", "peter.jones1@example.com", new BigDecimal("12000.00"), fixedNow.minusMonths(4)); // Platinum criteria

		mockMvc.perform(get("/api/customers")
						.param("email", "peter.jones1@example.com") // Search by email
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id", is(customer.getCustomerId().intValue())))
				.andExpect(jsonPath("$.name", is("Peter")))
				.andExpect(jsonPath("$.memberShipTier", is("Platinum"))); // Check the tier
	}

	@Test
	void testGetCustomersByEmail_NotFound() throws Exception {
		mockMvc.perform(get("/api/customers")
						.param("email", "nonexistent@example.com")
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound()); // Expect 404
	}


	// --- GET /api/customers/{id} Test (now returning DTO) ---

	@Test
	void testGetCustomerById_Found_ReturnsDTOWithTier() throws Exception {
		LocalDateTime fixedNow = LocalDateTime.of(2025, 4, 22, 21, 0);
		LocalDateTime purchaseDate = fixedNow.minusMonths(5); // Within 6 months
		Customer customer = createCustomer("Platinum", "platinum.user@example.com", new BigDecimal("15000.00"), purchaseDate); // Platinum criteria

		mockMvc.perform(get("/api/customers/{id}", customer.getCustomerId())
						.contentType(MediaType.APPLICATION_JSON))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id", is(customer.getCustomerId().intValue())))
				.andExpect(jsonPath("$.name", is("Platinum")))
				.andExpect(jsonPath("$.lastPurchaseDate", is(purchaseDate.format(ISO_FORMATTER))))
				.andExpect(jsonPath("$.memberShipTier", is("Platinum"))); // Assert the calculated tier
	}

	@Test
	void testGetCustomerById_NotFound() throws Exception {
		mockMvc.perform(get("/api/customers/{id}", 999L)
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound());
	}

	// --- POST /api/customers Test (Input is still Entity) ---
	// No change here, input is still Customer entity. Response can be asserted on Customer structure.
	@Test
	void testCreateCustomer() throws Exception {
		LocalDateTime purchaseDate = LocalDateTime.of(2024, 4, 22, 20, 15, 0);
		Customer newCustomer = new Customer(null, "NewCustomer", "new.c@example.com", new BigDecimal("500.00"), purchaseDate);

		mockMvc.perform(post("/api/customers")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(newCustomer)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.customerId").exists())
				.andExpect(jsonPath("$.name", is("NewCustomer")))
				.andExpect(jsonPath("$.lastPurchaseDate", is(purchaseDate.format(ISO_FORMATTER))));

		assertEquals(1, customerRepository.count());
		Customer savedCustomer = customerRepository.findAll().get(0);
		assertEquals("NewCustomer", savedCustomer.getName());
		assertEquals(purchaseDate.withNano(0), savedCustomer.getLastPurchaseDate().withNano(0));
	}


	// --- PUT /api/customers/{id} Test (Input is still Entity) ---
	// No change here, input is still Customer entity. Response can be asserted on Customer structure.
	@Test
	void testUpdateCustomer_Found() throws Exception {
		Customer existingCustomer = createCustomer("Old", "old.n@example.com", new BigDecimal("100.00"), LocalDateTime.now().minusDays(10));

		LocalDateTime updatedPurchaseDate = LocalDateTime.of(2024, 5, 1, 10, 0, 0);
		Customer updatedDetails = new Customer(null, "UpdatedName", "updated.n@example.com", new BigDecimal("2500.00"), updatedPurchaseDate);

		mockMvc.perform(put("/api/customers/{id}", existingCustomer.getCustomerId())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(updatedDetails)))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.customerId", is(existingCustomer.getCustomerId().intValue())))
				.andExpect(jsonPath("$.name", is("UpdatedName")))
				.andExpect(jsonPath("$.email", is("updated.n@example.com")))
				.andExpect(jsonPath("$.annualSpend", is(2500.00)))
				.andExpect(jsonPath("$.lastPurchaseDate", is(updatedPurchaseDate.format(ISO_FORMATTER))));


		Customer updatedCustomer = customerRepository.findById(existingCustomer.getCustomerId()).orElseThrow();
		assertEquals("UpdatedName", updatedCustomer.getName());
		assertEquals("updated.n@example.com", updatedCustomer.getEmail());
		assertEquals(new BigDecimal("2500.00"), updatedCustomer.getAnnualSpend());
		assertEquals(updatedPurchaseDate.withNano(0), updatedCustomer.getLastPurchaseDate().withNano(0));
	}


	// --- DELETE /api/customers/{id} Tests ---
	@Test
	void testDeleteCustomer_Found() throws Exception {
		Customer customerToDelete = createCustomer("Delete", "delete.m@example.com", new BigDecimal("100.00"), null);

		mockMvc.perform(delete("/api/customers/{id}", customerToDelete.getCustomerId()))
				.andExpect(status().isNoContent());

		assertEquals(0, customerRepository.count());
	}


	// --- GET /api/customers/{id}/tier Test (remains the same, returns String) ---
	@Test
	void testGetCustomerMembershipTier_Gold() throws Exception {
		// This test still uses the old tier logic or needs adjustment if that logic is removed.
		// Assuming the old tier logic or a similar calculation is still available internally for this endpoint.
		// If the tier calculation is only done when fetching the DTOs, this endpoint might need removal or update.
		// For now, assuming it calls the service which internally uses the NEW logic.
		LocalDateTime fixedNow = LocalDateTime.of(2025, 4, 22, 21, 0);
		Customer customer = createCustomer("GoldTier", "gold.t@example.com", new BigDecimal("3000.00"), fixedNow.minusMonths(10)); // Should be Gold

		mockMvc.perform(get("/api/customers/{id}/tier", customer.getCustomerId()))
				.andExpect(status().isOk())
				.andExpect(content().string("Gold")); // Check the exact string response
	}

}
