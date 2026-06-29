package ru.practicum.moviehub.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import ru.practicum.moviehub.model.StorageFilms;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class RequestValidator {
    private final StorageFilms storageFilms = new StorageFilms();

    public Map<Boolean, Map<Integer, String>> getValidator(HttpExchange exchange) {
        Map<Boolean, Map<Integer, String>> error = new HashMap<>();

        URI uri = exchange.getRequestURI();
        String bodyPath = uri.getPath();
        String[] path = bodyPath.split("/");

        int id;
        try {
            id = Integer.parseInt(path[2]);
        } catch (Exception e) {
            Map<Integer, String> errors = new HashMap<>();
            errors.put(400,
                    "{\"error\":\"Некорректный id. Ожидается число\"}");
            error.put(false, errors);
            return error;
        }

        String movie = storageFilms.findFilmByID(id);
        Map<Integer, String> success = new HashMap<>();
        if (movie.contains("{\"Фильм не найден\"}")) {
            success.put(404, movie);
            error.put(false, success);
            return error;
        }
        success.put(200, movie);
        error.put(true, success);
        return error;
    }

    public Map<Boolean, Map<Integer, String>> getParametersValidator(HttpExchange exchange) {
        Map<Boolean, Map<Integer, String>> error = new HashMap<>();

        String parameters = exchange.getRequestURI().getQuery();
        String[] queryYear;
        int year;
        try {
            queryYear = parameters.split("=");
            year = Integer.parseInt(queryYear[1]);
        } catch (Exception e) {
            Map<Integer, String> errors = new HashMap<>();
            errors.put(400,
                    "{\"error\":\"Некорректный id. Ожидается число\"}");
            error.put(false, errors);
            return error;
        }

        String list = storageFilms.findFilmByYear(year);
        String listMovies = list.replaceAll("\\[|\\]", "");
        Map<Integer, String> success = new HashMap<>();
        if (listMovies.contains("{\"Фильм не найден\"}")) {
            success.put(404, listMovies);
            error.put(false, success);
            return error;
        }
        success.put(200, listMovies);
        error.put(true, success);
        return error;
    }


    public Map<Boolean, Map<Integer, String>> postValidator(HttpExchange exchange) throws IOException {
        Map<Boolean, Map<Integer, String>> error = new HashMap<>();

        Headers headers = exchange.getRequestHeaders();
        String contentType = headers.getFirst("Content-Type");
        if (contentType == null || !contentType.equals("application/json; charset=UTF-8")) {
            Map<Integer, String> errors = new HashMap<>();
            errors.put(415, "{\"error\": \"Получен запрос с неправильным значением заголовка\"}");
            error.put(false, errors);
            return error;
        }

        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        JsonElement jsonElement;
        try {
            jsonElement = JsonParser.parseString(body);
        } catch (Exception e) {
            Map<Integer, String> errors = new HashMap<>();
            errors.put(422,
                    "{\"error\":\"Название не должно быть пустым, год должен быть между 1888 и " +
                            LocalDate.now().getYear() + "\"}");
            error.put(false, errors);
            return error;
        }

        JsonObject jsonObject;
        try {
            jsonObject = jsonElement.getAsJsonObject();
        } catch (Exception e) {
            Map<Integer, String> errors = new HashMap<>();
            errors.put(422, "{\"error\":\"Ошибка валидации. Передан некорректный объект.\"}");
            error.put(false, errors);
            return error;
        }

        if (!(jsonObject.has("Title")) || !(jsonObject.has("Year"))) {
            Map<Integer, String> errors = new HashMap<>();
            errors.put(422, "{\"error\":\"Ошибка валидации\"}");
            error.put(false, errors);
            return error;
        }

        String title;
        int year;

        try {
            JsonElement element = jsonObject.get("Title");
            if (element == null || !element.isJsonPrimitive()) {
                Map<Integer, String> errors = new HashMap<>();
                errors.put(422, "{\"error\":\"Ошибка валидации. Поле Title должно быть строкой\"}");
                error.put(false, errors);
                return error;
            }
            if (!element.getAsJsonPrimitive().isString()) {
                Map<Integer, String> errors = new HashMap<>();
                errors.put(422, "{\"error\":\"Ошибка валидации. Поле Title должно быть строкой\"}");
                error.put(false, errors);
                return error;
            }
            title = jsonObject.get("Title").getAsString();
        } catch (Exception e) {
            Map<Integer, String> errors = new HashMap<>();
            errors.put(422, "{\"error\":\"Ошибка валидации. Поле Title должно быть строкой\"}");
            error.put(false, errors);
            return error;
        }

        if (title.isBlank()) {
            Map<Integer, String> errors = new HashMap<>();
            errors.put(422, "{\"error\":\"Название не должно быть пустым\"}");
            error.put(false, errors);
            return error;
        }

        if (title.length() > 100) {
            Map<Integer, String> errors = new HashMap<>();
            errors.put(422, "{\"error\":\"Ошибка валидации. Название не должно превышать 100 символов\"}");
            error.put(false, errors);
            return error;
        }

        try {
            JsonElement element = jsonObject.get("Year");
            if (element == null || !element.isJsonPrimitive()) {
                Map<Integer, String> errors = new HashMap<>();
                errors.put(422, "{\"error\":\"Ошибка валидации. Поле Year должно быть числом и не должно быть " +
                        "равно null");
                error.put(false, errors);
                return error;
            }
            if (!element.getAsJsonPrimitive().isNumber()) {
                Map<Integer, String> errors = new HashMap<>();
                errors.put(422, "{\"error\":\"Ошибка валидации. Поле Year должно быть числом\"}");
                error.put(false, errors);
                return error;
            }
            year = jsonObject.get("Year").getAsInt();
        } catch (Exception e) {
            Map<Integer, String> errors = new HashMap<>();
            errors.put(422, "{\"error\":\"Ошибка валидации. Поле Year должно быть числом\"}");
            error.put(false, errors);
            return error;
        }

        if (year < 1888 || year > LocalDate.now().getYear()) {
            Map<Integer, String> errors = new HashMap<>();
            errors.put(422, "{\"error\":\"Год должен быть между 1888 и " + LocalDate.now().getYear() + "\"}");
            error.put(false, errors);
            return error;
        }

        Map<Integer, String> success = new HashMap<>();
        success.put(201, storageFilms.addNewFilm(title, year));
        error.put(true, success);
        return error;
    }

    public Map<Boolean, Map<Integer, String>> deleteValidator(HttpExchange exchange) {
        Map<Boolean, Map<Integer, String>> error = new HashMap<>();

        URI uri = exchange.getRequestURI();
        String bodyPath = uri.getPath();
        String[] path = bodyPath.split("/");

        if (path.length != 3) {
            Map<Integer, String> errors = new HashMap<>();
            errors.put(405, "{\"Метод не поддерживается\"}");
            error.put(false, errors);
            return error;
        }

        int id;
        try {
            id = Integer.parseInt(path[2]);
        } catch (Exception e) {
            Map<Integer, String> errors = new HashMap<>();
            errors.put(400,
                    "{\"error\":\"Некорректный id. Ожидается число\"}");
            error.put(false, errors);
            return error;
        }

        String movie = storageFilms.deleteFilmByID(id);
        Map<Integer, String> success = new HashMap<>();
        if (movie.contains("{\"Фильм не найден\"}")) {
            success.put(404, movie);
            error.put(false, success);
            return error;
        }
        success.put(204, movie);
        error.put(true, success);
        return error;
    }
}