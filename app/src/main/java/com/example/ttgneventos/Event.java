package com.example.ttgneventos;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public final class Event
{
    // Fields
    private String _title;
    private String _category;
    private LocalDateTime _dateTime; // Converted to Timestamp in Firestore
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

    @Exclude public LocalDateTime getDateTime() { return _dateTime; }
    @Exclude public LocalDate getDate() { return _dateTime.toLocalDate(); }
    @Exclude public LocalTime getTime() { return _dateTime.toLocalTime(); }
    @Exclude public void setDateTime(LocalDateTime dateTime) { _dateTime = dateTime; }

    public String getLocation() { return _location; }
    public void setLocation(String location) { _location = location; }

    public double getPrice() { return _price; }
    public void setPrice(double price) { _price = price; }

    public String getDescription() { return _description; }
    public void setDescription(String description) { _description = description; }

    // Firestore specific getters and setters
    @PropertyName("dateTime")
    public String getDateTimeString() { return _dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")); }

    @PropertyName("dateTime")
    public void setDateTimeString(String formattedDate) { _dateTime = LocalDateTime.parse(formattedDate, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")); }
}
