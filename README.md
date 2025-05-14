# CustomerManagementAPI
 This is the HandsOn Assignment with API's for CustomerManagement
 
Run the application and then access the H2 database console to view the data.

Here are the steps:

Run the Spring Boot Application:

Use the method you prefer:
a) From IntelliJ (Run Configuration or Main class - CustomerManagerApplication.java).
b) From the terminal using mvn spring-boot:run or java -jar target/customer-api-0.0.1-SNAPSHOT.jar.
Access the H2 Console:

Once the application has successfully started (you'll see messages in the console indicating Tomcat started and the port number), open a web browser.
Go to the H2 console URL: http://localhost:8082/h2-console
Log in to the H2 Console:

The H2 console login page will appear. You need to enter the correct connection details for the in-memory database managed by Spring Boot.
JDBC URL: jdbc:h2:mem:dcbapp (This is the default URL used by Spring Boot for in-memory H2 unless explicitly changed the spring.datasource.url to an H2 path in application.properties).
User Name: sa (This is the default H2 username).
Password: password (The default H2 password for the 'sa' user when used with Spring Boot's defaults is typically empty).
Click the "Connect" button.
Explore the Database:

You should now be connected to the in-memory H2 database.
On the left side, you'll see the database schema (PUBLIC).
Under PUBLIC, you should see CUSTOMER table (H2 often uses uppercase for table names by default).
In the large text area, you can type SQL queries.
Test Data with SQL Queries:

To see all customers:
SQL

SELECT * FROM CUSTOMER;
To see a specific customer (replace 1 with an actual customer ID if you've created one via the API):
SQL

SELECT * FROM CUSTOMER WHERE ID = 1;
To see the schema of the table:
SQL

SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'CUSTOMER';
Click the "Run" button (or hit Ctrl+Enter / Cmd+Enter) to execute the query.
Important Note about H2 In-Memory:

Since application.properties likely still has spring.jpa.hibernate.ddl-auto=create-drop, the database schema is created when the application starts and completely dropped when application is stopped. This means:
Any data added via the API will be visible in the H2 console while the application is running. As soon as the application is stopped, all data in the H2 database will be lost.

Here are the ways to test the application:
---------------------------------------------------
Method 1: Using Swagger UI (Recommended for easy interactive testing)

Ensure Spring Boot application is running.
Open web browser and go to http://localhost:8082/swagger-ui.html.
You will see the API documentation generated from the code and annotations. It lists the "Customers" endpoints.
Expand an Endpoint: Click on an endpoint, like POST /api/customers or GET /api/customers/{id}, to expand its details.
Click "Try it out": This button makes the request parameters and request body fields editable.
Fill in Details:
For POST /api/customers: Edit the example JSON in the "Request body" textarea. Enter details for a new customer (Name, email, annualSpend, lastPurchaseDate - id should not be included). Ensure the format is valid JSON.
For GET /api/customers/{id}: Enter a customer id in the path parameter box.
For GET /api/customers (with parameters): Enter values in the name or email query parameter boxes.
For PUT /api/customers/{id}: Enter the id in the path parameter and edit the JSON body with updated details.
For DELETE /api/customers/{id}: Enter the id in the path parameter.
Click "Execute": Swagger UI will send the HTTP request to the running application.
View the Response: Scroll down to see the "Responses" section. You'll see:
"Code": The HTTP status code (e.g., 200, 201, 400, 404, 500).
"Details": A description of the response code.
"Response body": The data returned by the API (e.g., the created Customer, the retrieved Customer DTO, a list of customers, an error message).
"Response headers": HTTP headers returned.
"Curl": The exact curl command that Swagger UI executed (useful for command-line testing later).
Example Testing Flow using Swagger UI:

1) Expand POST /api/customers, click "Try it out", enter valid customer details (without id), click "Execute". Check for 201 Created response and the response body containing the created customer with a generated ID. Note the ID.
2) Expand GET /api/customers/{id}, click "Try it out", enter the ID you just got from the POST response, click "Execute". Check for 200 OK and the response body containing the customer details (including the calculated tier).
3) Expand GET /api/customers, click "Try it out" without entering any parameters, click "Execute". Check for 200 OK and a list of all customers (including the one you just created).
4) Expand GET /api/customers, click "Try it out", enter a name parameter (e.g., "Doe" if you created a customer with that last name), click "Execute". Check for 200 OK and a list of customers matching that name.
5) Expand GET /api/customers, click "Try it out", enter an email parameter (e.g., the email of the customer you created), click "Execute". Check for 200 OK and the single customer matching that email.
6) Expand PUT /api/customers/{id}, click "Try it out", enter the ID, modify some fields in the request body JSON (e.g., annualSpend), click "Execute". Check for 200 OK and the updated customer in the response body.
7) Expand GET /api/customers/{id} again, enter the ID, execute. Verify the changes you made in the PUT step (e.g., check the updated annualSpend and the potentially changed membership tier).
8) Expand DELETE /api/customers/{id}, click "Try it out", enter the ID, click "Execute". Check for 204 No Content.
9) Expand GET /api/customers again (without parameters), execute. Check for 200 OK and verify that the customer you deleted is no longer in the list.

Method 2: Using Command-Line curl

Ensure application is running. Open your terminal.

GET All Customers:
Bash

curl http://localhost:8082/api/customers
GET Customer by ID (Replace {id}):
Bash

curl http://localhost:8082/api/customers/{id}
GET Customers by Name (Replace {name}):
Bash

curl "http://localhost:8082/api/customers?name={name}"
# Example: curl "http://localhost:8082/api/customers?name=Doe"
(Use quotes around the URL when using parameters)
GET Customer by Email (Replace {email}):
Bash

curl "http://localhost:8082/api/customers?email={email}"
# Example: curl "http://localhost:8082/api/customers?email=test@example.com"
POST Create Customer:
Bash

curl -X POST http://localhost:8082/api/customers \
-H "Content-Type: application/json" \
-d '{
      "firstName": "John",
      "lastName": "Doe",
      "email": "john.doe@example.com",
      "annualSpend": 1200.50,
      "lastPurchaseDate": "2024-04-22T10:00:00"
    }'
PUT Update Customer (Replace {id}):
Bash

curl -X PUT http://localhost:8082/api/customers/{id} \
-H "Content-Type: application/json" \
-d '{
      "firstName": "John",
      "lastName": "Doe",
      "email": "john.doe.updated@example.com",
      "annualSpend": 5500.00,
      "lastPurchaseDate": "2024-05-01T15:30:00"
    }'
DELETE Customer by ID (Replace {id}):
Bash

curl -X DELETE http://localhost:8082/api/customers/{id}

Method 3: Using API Development Tools (Postman, Insomnia, IntelliJ HTTP Client)
These tools provide a graphical interface for building and managing complex API requests.

##For generating the openapi.yaml the maven plugin springdoc-openapi-maven-plugin has been included in pom.xml.
##But if there are any issues generating it from the mvn clean package -U command then use following
##command after the application is started. 
curl http://localhost:8082/v3/api-docs.yaml -o docs/openapi.yaml


##**The CustomerService class has the tier calculation business logic****

