package com.example.ttgneventos.model;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.ttgneventos.R;
import com.example.ttgneventos.pojo.Event;

import java.time.format.DateTimeFormatter;

public final class EventDetails extends AppCompatActivity
{
    // References IDs
    private TextView
        _eventTitleText,
        _eventCategoryText,
        _eventDateText,
        _eventTimeText,
        _eventLocationText,
        _eventEntrancePrice,
        _eventDescription;

    private ImageView _eventImage;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_event_details);
        ViewCompat.setOnApplyWindowInsetsListener
        (
            findViewById(R.id.main), (v, insets) ->
            {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            }
        );

        // Initializes IDs
        _eventTitleText = findViewById(R.id.eventTitleText);
        _eventCategoryText = findViewById(R.id.eventCategoryText);
        _eventDateText = findViewById(R.id.eventDateText);
        _eventTimeText = findViewById(R.id.eventTimeText);
        _eventLocationText = findViewById(R.id.eventLocationText);
        _eventEntrancePrice = findViewById(R.id.eventEntrancePrice);
        _eventDescription = findViewById(R.id.eventDescription);
        _eventImage = findViewById(R.id.eventImage);

        // Fetches the event's information
        Event event = (Event) getIntent().getExtras().getSerializable("Event");
        _eventTitleText.setText(event.getTitle());
        _eventCategoryText.setText(event.getCategory());
        _eventDateText.setText(event.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        _eventTimeText.setText(event.getTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        _eventLocationText.setText(event.getLocation());
        _eventEntrancePrice.setText("Entrada: " + String.valueOf(event.getPrice()) + " €");
        _eventDescription.setText(event.getDescription());
    }
}
