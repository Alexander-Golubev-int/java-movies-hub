package ru.practicum.moviehub.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StorageFilms {
    private static int keyForMap_id = -1;
    private static final Map<Integer, Movie> allFilms = new HashMap<>();

    public StorageFilms() {}

    public String getAllFilms() {
        if (getKeyForMap() == -1) {
            return "{\"Films\": []}";
        }
        StringBuilder films = new StringBuilder("{\"Films\": [");
        for (Map.Entry<Integer, Movie> entry : allFilms.entrySet()) {
            Movie film = entry.getValue();
            String keyAndValue = "{\"id\":" + entry.getKey() +",\"" + film.getTitle() + "\":" + film.getYear() + "}";
            films.append(keyAndValue).append(",");
        }
        films.deleteCharAt(films.length() - 1).append("]}");
        return films.toString();
    }

    public String addNewFilm(String title, Integer year) {
        increaseKeyForMap();
        Movie movie = new Movie(title, year);
        allFilms.put(getKeyForMap(), movie);
        return "{\"id\":" + getKeyForMap() + ", \"Title\": " + "\"" + title + "\"" + ", \"Year\":" + year + "}";
    }

    public String findFilmByID(int id) {
        if (id > getKeyForMap()) {
            return "{\"Фильм не найден\"}";
        } else if (!allFilms.containsKey(id)){
            return "{\"Фильм не найден\"}";
        }
        Movie movie = allFilms.get(id);
        return "{\"Title\":" + "\"" + movie.getTitle() + "\"" + ",\"Year\":" + movie.getYear() + "}";
    }

    public String findFilmByYear(int year) {
        List<String> stringList = new ArrayList<>();
        for (Map.Entry<Integer, Movie> map : allFilms.entrySet()) {
            Movie movie = map.getValue();
            if (movie.getYear() == year) {
                stringList.add(movie.toString());
            }
        }

        if (stringList.isEmpty()) {
            return "{\"Фильм не найден\"}";
        }

        return "[" + stringList + "]";
    }

    public String deleteFilmByID(int id) {
        if (id > getKeyForMap() || id < -1) {
            return "{\"Фильм не найден\"}";
        } else if (!allFilms.containsKey(id)){
            return "{\"Фильм не найден\"}";
        }
        allFilms.remove(id);
        minusKeyForMap();
        return "{}";
    }



    public Integer getKeyForMap() {
        return keyForMap_id;
    }

    public void increaseKeyForMap() {
        keyForMap_id = keyForMap_id + 1;
    }

    public void minusKeyForMap() {
        keyForMap_id = keyForMap_id - 1;
    }
}
