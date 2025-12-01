package com.example.demo;

import com.example.demo.dto.GenerateWebhookRequest;
import com.example.demo.dto.GenerateWebhookResponse;
import com.example.demo.dto.SolutionRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class DemoApplication implements CommandLineRunner {

	@Autowired
	private RestTemplate restTemplate;

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Override
	public void run(String... args) {

		// Step 1 - call generateWebhook
		GenerateWebhookResponse webhookResponse = callGenerateWebhook();

		if (webhookResponse == null) {
			System.out.println("Webhook generation failed");
			return;
		}

		// Step 2 - Get details
		String webhook = webhookResponse.getWebhook();
		String token = webhookResponse.getAccessToken();

		// Step 3 - SQL Answer (Question 1)
		String finalQuery = """
				    WITH filtered_payments AS (
				        SELECT EMP_ID, SUM(AMOUNT) AS total_salary
				        FROM PAYMENTS
				        WHERE DAY(PAYMENT_TIME) <> 1
				        GROUP BY EMP_ID
				    ),
				    employee_totals AS (
				        SELECT e.EMP_ID, e.FIRST_NAME, e.LAST_NAME, e.DOB, e.DEPARTMENT, fp.total_salary
				        FROM EMPLOYEE e
				        JOIN filtered_payments fp ON e.EMP_ID = fp.EMP_ID
				    ),
				    dept_max AS (
				        SELECT DEPARTMENT, MAX(total_salary) AS max_salary
				        FROM employee_totals
				        GROUP BY DEPARTMENT
				    )
				    SELECT
				        d.DEPARTMENT_NAME,
				        et.total_salary AS SALARY,
				        CONCAT(et.FIRST_NAME,' ',et.LAST_NAME) AS EMPLOYEE_NAME,
				        TIMESTAMPDIFF(YEAR, et.DOB, CURDATE()) AS AGE
				    FROM employee_totals et
				    JOIN dept_max dmON et.DEPARTMENT = dm.DEPARTMENT AND et.total_salary = dm.max_salary
				    JOIN DEPARTMENT d ON d.DEPARTMENT_ID = et.DEPARTMENT;
				""";

		// Step 4 - Send result
		sendSolution(webhook, token, finalQuery);
	}

	private GenerateWebhookResponse callGenerateWebhook() {

		String url = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";

		GenerateWebhookRequest request = new GenerateWebhookRequest(
				"Subin Kumar",
				"22BIT0525",
				"isubinkr@gmail.com");

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<GenerateWebhookRequest> entity = new HttpEntity<>(request, headers);

		ResponseEntity<GenerateWebhookResponse> response = restTemplate.postForEntity(url, entity,
				GenerateWebhookResponse.class);

		return response.getBody();
	}

	private void sendSolution(String webhook, String token, String finalQuery) {

		String url = "https://bfhldevapigw.healthrx.co.in/hiring/testWebhook/JAVA";

		SolutionRequest body = new SolutionRequest(finalQuery);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("Authorization", token);

		HttpEntity<SolutionRequest> entity = new HttpEntity<>(body, headers);

		ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

		System.out.println("Status: " + response.getStatusCode());
		System.out.println("Response: " + response.getBody());
	}
}
