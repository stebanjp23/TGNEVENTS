package com.example.ttgneventos;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public final class Event
{
    // Fields
    private String _title;
    private String _category;
    private LocalDateTime _dateTime;
    private String _location;
    private double _price;
    private String _description;

    // Constructors
    public Event() {}

    public Event(String title, String category, LocalDateTime dateTime, String location, double price, String description)
    {
        _title = title;
        _category = category;
        _dateTime = dateTime;
        _location = location;
        _price = price;
        _description = description;
    }

    // Getters & setters
    public String getTitle() { return _title; }
    public void setTitle(String title) { _title = title; }

    public String getCategory() { return _category; }
    public void setCategory(String category) { _category = category; }

    public LocalDateTime getDateTime() { return _dateTime; }
    public LocalDate getDate() { return _dateTime.toLocalDate(); }
    public LocalTime getTime() { return _dateTime.toLocalTime(); }
    public void setDateTime(LocalDateTime dateTime) { _dateTime = dateTime; }

    public String getLocation() { return _location; }
    public void setLocation(String location) { _location = location; }

    public double getPrice() { return _price; }
    public void setPrice(double price) { _price = price; }

    public String getDescription() { return _description; }
    public void setDescription(String description) { _description = description; }
}
