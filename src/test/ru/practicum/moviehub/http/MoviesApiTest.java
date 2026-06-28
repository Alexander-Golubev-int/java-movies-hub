package ru.practicum.moviehub.http;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MoviesApiTest {
    private static final String BASE = "http://localhost:8080";
    private static MoviesServer server;
    private static HttpClient client;

    @BeforeAll
    static void beforeAll() {
        client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        server = new MoviesServer();
        server.start();
    }

    @AfterAll
    static void afterAll() {
        server.stop();
    }

    @Test
    @Order(1)
    void getMovies_whenEmpty_returnsEmptyArray() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE + "/movies"))
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, resp.statusCode(), "GET /movies должен вернуть 200");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String body = resp.body().trim();
        String expected = "{\"Films\": []}";
        assertEquals(expected, body,
                "Ожидается JSON-массив");
    }

    @Test
    @Order(2)
    void addNewFilm_return_idWithTitleAndYear() throws Exception {
        String body = "{\"Title\":\"Смертельная битва\",\"Year\":1995}";
        HttpRequest req = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(201, resp.statusCode(), "POST /movies должен вернуть 201 Created");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        JsonElement jsonElement = JsonParser.parseString(resp.body());
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        int id = jsonObject.get("id").getAsInt();
        int idExpected = 0;
        String title = jsonObject.get("Title").getAsString();
        String titleExpected = "Смертельная битва";
        int year = jsonObject.get("Year").getAsInt();
        int yearExpected = 1995;
        assertEquals(idExpected, id, "Должен вернутся id:"  + idExpected);
        assertEquals(titleExpected, title, "Должен вернутся Title:" + titleExpected);
        assertEquals(yearExpected, year, "Должен вернутся Year:" + yearExpected);
    }

    @Test
    @Order(3)
    void getMovies_whenHaveOneFilm_returnsArray() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE + "/movies"))
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, resp.statusCode(), "GET /movies должен вернуть 200");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String body = resp.body();
        String expected = "{\"Films\": [{\"id\":0,\"Смертельная битва\":1995}]}";
        assertEquals(expected, body,
                "Ожидается JSON-массив");
    }

    @Test
    @Order(5)
    void addNewFilm_WithEmptyHeaders() throws Exception {
        String body = "{\"Title\":\"Смертельная битва\",\"Year\": 1995}";
        HttpRequest req = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(415, resp.statusCode(), "POST /movies/ должен вернуть 415");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String errorExpected = "{\"error\": \"Получен запрос с неправильным значением заголовка\"}";
        String error = resp.body();
        assertEquals(errorExpected, error);
    }

    @Test
    @Order(6)
    void addNewFilm_WithEmptyBody() throws Exception {
        String body = "{\"Title\":\"Смертельная битва\",\"Year\": 1995";
        HttpRequest req = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(422, resp.statusCode(), "POST /movies/ должен вернуть 422");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String errorExpected =
                "{\"error\":\"Название не должно быть пустым, год должен быть между 1888 и " +
                        LocalDate.now().getYear() + "\"}";
        String error = resp.body();
        assertEquals(errorExpected, error);
    }

    @Test
    @Order(7)
    void addNewFilm_WithUncorrectedBody() throws Exception {
        String body = "12345";
        HttpRequest req = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(422, resp.statusCode(), "POST /movies/ должен вернуть 422");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String errorExpected =
                "{\"error\":\"Ошибка валидации. Передан некорректный объект.\"}";
        String error = resp.body();
        assertEquals(errorExpected, error);
    }

    @Test
    @Order(8)
    void addNewFilm_WithoutTitle() throws Exception {
        String body = "{\"Year\": 1995}";
        HttpRequest req = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(422, resp.statusCode(), "POST /movies/ должен вернуть 422");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String errorExpected =
                "{\"error\":\"Ошибка валидации\"}";
        String error = resp.body();
        assertEquals(errorExpected, error);
    }

    @Test
    @Order(9)
    void addNewFilm_WithoutYear() throws Exception {
        String body = "{\"Title\":\"Смертельная битва\"}";
        HttpRequest req = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(422, resp.statusCode(), "POST /movies/ должен вернуть 422");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String errorExpected =
                "{\"error\":\"Ошибка валидации\"}";
        String error = resp.body();
        assertEquals(errorExpected, error);
    }

    @Test
    @Order(10)
    void addNewFilm_whereTitleNotAString() throws Exception {
        String body = "{\"Title\":123,\"Year\": 1995}";
        HttpRequest req = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(422, resp.statusCode(), "POST /movies/ должен вернуть 422");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String errorExpected = "{\"error\":\"Ошибка валидации. Поле Title должно быть строкой\"}";
        String error = resp.body();
        assertEquals(errorExpected, error);
    }

    @Test
    @Order(11)
    void addNewFilm_whereTitleIsBlank() throws Exception {
        String body = "{\"Title\":\"\",\"Year\": 1995}";
        HttpRequest req = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(422, resp.statusCode(), "POST /movies/ должен вернуть 422");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String errorExpected = "{\"error\":\"Название не должно быть пустым\"}";
        String error = resp.body();
        assertEquals(errorExpected, error);
    }

    @Test
    @Order(12)
    void addNewFilm_whereTitleHaveLengthMoreThen100() throws Exception {
        String body = "{\"Title\":\"АаааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааааАаааааааааааааааааааааааааааааааааааааа\",\"Year\": 1995}";
        HttpRequest req = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(422, resp.statusCode(), "POST /movies/ должен вернуть 422");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String errorExpected = "{\"error\":\"Ошибка валидации. Название не должно превышать 100 символов\"}";
        String error = resp.body();
        assertEquals(errorExpected, error);
    }

    @Test
    @Order(13)
    void addNewFilm_whereYearNotAPrimitive() throws Exception {
        String body = "{\"Title\":\"Смертельная битва\",\"Year\": null}";
        HttpRequest req = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(422, resp.statusCode(), "POST /movies/ должен вернуть 422");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String errorExpected = "{\"error\":\"Ошибка валидации. Поле Year должно быть числом и не должно быть " +
                "равно null";
        String error = resp.body();
        assertEquals(errorExpected, error);
    }

    @Test
    @Order(14)
    void addNewFilm_whereYearIsString() throws Exception {
        String body = "{\"Title\":\"Смертельная битва\",\"Year\": \"1999\"}";
        HttpRequest req = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(422, resp.statusCode(), "POST /movies/ должен вернуть 422");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String errorExpected = "{\"error\":\"Ошибка валидации. Поле Year должно быть числом\"}";
        String error = resp.body();
        assertEquals(errorExpected, error);
    }

    @Test
    @Order(15)
    void addNewFilm_whereYearIsLessThen1888() throws Exception {
        String body = "{\"Title\":\"Смертельная битва\",\"Year\": 1887}";
        HttpRequest req = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(422, resp.statusCode(), "POST /movies/ должен вернуть 422");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String errorExpected = "{\"error\":\"Год должен быть между 1888 и " + LocalDate.now().getYear() + "\"}";
        String error = resp.body();
        assertEquals(errorExpected, error);
    }

    @Test
    @Order(16)
    void getFilmByID() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE + "/movies/0"))
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, resp.statusCode(), "GET /movies/0 должен вернуть 200");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String bodyExpected = "{\"Title\":\"Смертельная битва\",\"Year\":1995}";
        String body = resp.body();
        assertEquals(bodyExpected, body);
    }

    @Test
    @Order(17)
    void getFilmByIDNotAPrimitive() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE + "/movies/Yandex"))
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(400, resp.statusCode(), "GET /movies/Yandex должен вернуть 400");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String bodyExpected = "{\"error\":\"Некорректный id. Ожидается число\"}";
        String body = resp.body();
        assertEquals(bodyExpected, body);
    }

    @Test
    @Order(18)
    void getFilmNonExistentID() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE + "/movies/10"))
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(404, resp.statusCode(), "GET /movies/Yandex должен вернуть 404");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String bodyExpected = "{\"Фильм не найден\"}";
        String body = resp.body();
        assertEquals(bodyExpected, body);
    }

    @Test
    @Order(19)
    void sendGetMethodNotAllowed() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE + "/movies/10/6"))
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(405, resp.statusCode(), "GET /movies/Yandex должен вернуть 405");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String bodyExpected = "{\"Метод не поддерживается\"}";
        String body = resp.body();
        assertEquals(bodyExpected, body);
    }

    @Test
    @Order(20)
    void deleteFilmByID() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .DELETE()
                .uri(URI.create(BASE + "/movies/0"))
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(204, resp.statusCode(), "GET /movies/Yandex должен вернуть 204");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");
    }

    @Test
    @Order(21)
    void deleteFilmNonExistentID() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .DELETE()
                .uri(URI.create(BASE + "/movies/101"))
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(404, resp.statusCode(), "GET /movies/Yandex должен вернуть 404");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");
        String bodyExpected = "{\"Фильм не найден\"}";
        String body = resp.body();
        assertEquals(bodyExpected, body);
    }

    @Test
    @Order(22)
    void deleteFilmByIDNotAPrimitive() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .DELETE()
                .uri(URI.create(BASE + "/movies/Yandex"))
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(400, resp.statusCode(), "GET /movies/Yandex должен вернуть 400");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String bodyExpected = "{\"error\":\"Некорректный id. Ожидается число\"}";
        String body = resp.body();
        assertEquals(bodyExpected, body);
    }

    @Test
    @Order(23)
    void deleteMethodNotAllowed() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .DELETE()
                .uri(URI.create(BASE + "/movies/10/6"))
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(405, resp.statusCode(), "GET /movies/Yandex должен вернуть 405");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String bodyExpected = "{\"Метод не поддерживается\"}";
        String body = resp.body();
        assertEquals(bodyExpected, body);
    }

    @Test
    @Order(24)
    void addNewFilm_return_idWithTitleAndYearForNextTest() throws Exception {
        String body = "{\"Title\":\"Смертельная битва\",\"Year\":1995}";
        HttpRequest req = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(201, resp.statusCode(), "POST /movies должен вернуть 201 Created");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        JsonElement jsonElement = JsonParser.parseString(resp.body());
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        int id = jsonObject.get("id").getAsInt();
        int idExpected = 0;
        String title = jsonObject.get("Title").getAsString();
        String titleExpected = "Смертельная битва";
        int year = jsonObject.get("Year").getAsInt();
        int yearExpected = 1995;
        assertEquals(idExpected, id, "Должен вернутся id:"  + idExpected);
        assertEquals(titleExpected, title, "Должен вернутся Title:" + titleExpected);
        assertEquals(yearExpected, year, "Должен вернутся Year:" + yearExpected);
    }

    @Test
    @Order(25)
    void searchMovieByYear() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE + "/movies?year=1995"))
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, resp.statusCode(), "GET /movies?year=1995 должен вернуть 200");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String bodyExpected = "{\"Title\":\"Смертельная битва\",\"Year\":1995}";
        String body = resp.body();
        assertEquals(bodyExpected, body);
    }

    @Test
    @Order(26)
    void searchMovieByYearWithNoValidParameter() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE + "/movies?year=aboba"))
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(400, resp.statusCode(), "GET /movies?year=aboba должен вернуть 400");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String bodyExpected = "{\"error\":\"Некорректный id. Ожидается число\"}";
        String body = resp.body();
        assertEquals(bodyExpected, body);
    }

    @Test
    @Order(27)
    void searchMovieByYearWithNoExistParameter() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE + "/movies?year=66"))
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(404, resp.statusCode(), "GET /movies?year=66 должен вернуть 404");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String bodyExpected = "{\"Фильм не найден\"}";
        String body = resp.body();
        assertEquals(bodyExpected, body);
    }

}