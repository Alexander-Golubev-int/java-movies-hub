package ru.practicum.moviehub.http;

import com.sun.net.httpserver.HttpExchange;
import ru.practicum.moviehub.api.RequestValidator;
import ru.practicum.moviehub.model.StorageFilms;

import java.io.IOException;
import java.util.Map;


class MoviesHandler extends BaseHttpHandler {
    StorageFilms storageFilms = new StorageFilms();
    RequestValidator requestValidator = new RequestValidator();

    @Override
    public void handle(HttpExchange ex) throws IOException {

        String method = ex.getRequestMethod();
        switch (method) {
            case "GET": {
                callMethodGet(ex);
                break;
            }
            case "POST": {
                handlePOSTMovies(ex);
                break;
            }
            case "DELETE": {
                handleDeleteMovieByID(ex);
                break;
            }
            default: {
                sendMethodNotAllowed(ex);
            }
        }
    }

    private void handleGetMovies(HttpExchange exchange) throws IOException {
        sendJson(exchange, 200, storageFilms.getAllFilms());
    }

    private void handleGetMoviesById(HttpExchange exchange) throws IOException {
        Map<Boolean, Map<Integer, String>> validator = requestValidator.GetValidator(exchange);
        if (validator.containsKey(false)) {
            Map<Integer, String> map = validator.get(false);
            for (Map.Entry<Integer, String> entry : map.entrySet()) {
                int code = entry.getKey();
                String text = entry.getValue();
                sendErrorResponse(exchange, code, text);
            }
        }
        Map<Integer, String> map = validator.get(true);
        for (Map.Entry<Integer, String> entry : map.entrySet()) {
            int code = entry.getKey();
            String text = entry.getValue();
            sendJson(exchange, code, text);
        }
    }

    private void handleGetMoviesByYear(HttpExchange exchange) throws IOException {
        Map<Boolean, Map<Integer, String>> validator = requestValidator.GetParametersValidator(exchange);
        if (validator.containsKey(false)) {
            Map<Integer, String> map = validator.get(false);
            for (Map.Entry<Integer, String> entry : map.entrySet()) {
                int code = entry.getKey();
                String text = entry.getValue();
                sendErrorResponse(exchange, code, text);
            }
        }
        Map<Integer, String> map = validator.get(true);
        for (Map.Entry<Integer, String> entry : map.entrySet()) {
            int code = entry.getKey();
            String text = entry.getValue();
            sendJson(exchange, code, text);
        }
    }

    private void handlePOSTMovies(HttpExchange exchange) throws IOException {
        Map<Boolean, Map<Integer, String>> validator = requestValidator.PostValidator(exchange);
        if (validator.containsKey(false)) {
            Map<Integer, String> map = validator.get(false);
            for (Map.Entry<Integer, String> entry : map.entrySet()) {
                int code = entry.getKey();
                String text = entry.getValue();
                sendErrorResponse(exchange, code, text);
            }
        }

        Map<Integer, String> map = validator.get(true);
        for (Map.Entry<Integer, String> entry : map.entrySet()) {
            int code = entry.getKey();
            String text = entry.getValue();
            sendJson(exchange, code, text);
        }
    }

    private void handleDeleteMovieByID(HttpExchange exchange) throws IOException {
        Map<Boolean, Map<Integer, String>> validator = requestValidator.DeleteValidator(exchange);
        if (validator.containsKey(false)) {
            Map<Integer, String> map = validator.get(false);
            for (Map.Entry<Integer, String> entry : map.entrySet()) {
                int code = entry.getKey();
                String text = entry.getValue();
                sendErrorResponse(exchange, code, text);
            }
        }
        sendNoContent(exchange);
    }


    private void callMethodGet(HttpExchange exchange) throws IOException {
        String string = checkLengthPath(exchange);
        switch (string) {
            case "handleGetMovies" -> {
                handleGetMovies(exchange);
                return;
            }
            case "handleGetMoviesById" -> {
                handleGetMoviesById(exchange);
                return;
            }
            case "handleGetMoviesByYear" -> {
                handleGetMoviesByYear(exchange);
                return;
            }
        }
        sendMethodNotAllowed(exchange);
    }

    private String checkLengthPath(HttpExchange exchange) {
        String path = exchange.getRequestURI().getPath();
        String[] pathLength = path.split("/");
        if (pathLength.length == 2) {
            String parameters = exchange.getRequestURI().getQuery();
            if (parameters != null && parameters.contains("year=")) {
                return "handleGetMoviesByYear";
            }
            return "handleGetMovies";
        } else if (pathLength.length == 3) {
            return "handleGetMoviesById";
        }
        return "Unknow";
    }
}