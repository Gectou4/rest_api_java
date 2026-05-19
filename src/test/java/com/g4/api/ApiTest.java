package com.g4.api;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ApiTest {

    private static final String BASE_URL =
            System.getenv().getOrDefault("API_BASE_URL", "http://localhost:8080");
    private static HttpClient client;
    private static int createdTaskId;

    @BeforeAll
    static void setup() {
        client = HttpClient.newHttpClient();
    }

    @Test
    @Order(1)
    void testGetUser() throws Exception {
        HttpRequest request =
                HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/user/1")).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        String body = response.body();
        assertTrue(body.contains("user_id"));
        assertTrue(body.contains("1"));
    }

    @Test
    @Order(2)
    void testGetUserTask() throws Exception {
        HttpRequest request =
                HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/user/1/task")).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        String body = response.body();
        assertTrue(body.contains("user_id"));
        assertTrue(body.contains("tasks"));
    }

    @Test
    @Order(3)
    void testAddTask() throws Exception {
        String body = "title=Test+task&description=Test+description&status=1";
        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/task"))
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
        String responseBody = response.body();
        assertTrue(responseBody.contains("task_id"));
        assertTrue(responseBody.contains("Test task"));

        createdTaskId =
                Integer.parseInt(
                        responseBody.split("task_id")[1].replaceAll("[^0-9]", "").substring(0, 1));
    }

    @Test
    @Order(4)
    void testEditTask() throws Exception {
        String body = "title=Updated+task&description=Updated+description&status=2";
        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/task/3"))
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
    }

    @Test
    @Order(5)
    void testAddTaskToUser() throws Exception {
        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/task/user/1/task/1"))
                        .POST(HttpRequest.BodyPublishers.noBody())
                        .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
    }

    @Test
    @Order(6)
    void testDelTaskToUser() throws Exception {
        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/task/user/1/task/1"))
                        .DELETE()
                        .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
    }

    @Test
    @Order(7)
    void testDelTask() throws Exception {
        HttpRequest request =
                HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/task/3")).DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
    }
}
