package ru.practicum.moviehub.model;

public class Movie {
    private final String title;
    private final Integer year;

    public Movie(String title, Integer year) {
        this.title = title;
        this.year = year;
    }

    public String getTitle() {
        return title;
    }

    public Integer getYear() {
        return year;
    }

    @Override
    public String toString() {
        return "{\"Title\":\"" + getTitle() + "\",\"Year\":" + getYear() + "}";
    }
}