package com.example.ttgneventos.model;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.ttgneventos.R;
import com.example.ttgneventos.pojo.Event;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private ImageButton
        _btnBack,
        _favouriteButton;


    private FirebaseAuth _auth = null;
    private FirebaseFirestore _db = null;
    private boolean _isFavourite = false;

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

        _auth = FirebaseAuth.getInstance();
        _db = FirebaseFirestore.getInstance();

        // Initializes IDs
        _eventTitleText = findViewById(R.id.eventTitleText);
        _eventCategoryText = findViewById(R.id.eventCategoryText);
        _eventDateText = findViewById(R.id.eventDateText);
        _eventTimeText = findViewById(R.id.eventTimeText);
        _eventLocationText = findViewById(R.id.eventLocationText);
        _eventEntrancePrice = findViewById(R.id.eventEntrancePrice);
        _eventDescription = findViewById(R.id.eventDescription);
        _eventImage = findViewById(R.id.eventImage);
        _btnBack = findViewById(R.id.btnBack);
        _favouriteButton = findViewById(R.id.favouriteButton);

        _btnBack.setOnClickListener(v -> finish());

        // Fetches the event's information
        Event event = (Event) getIntent().getExtras().getSerializable("Event");
        _eventTitleText.setText(event.getTitle());
        _eventCategoryText.setText(event.getCategory());
        _eventDateText.setText(event.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        _eventTimeText.setText(event.getTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        _eventLocationText.setText(event.getLocation());
        _eventEntrancePrice.setText("Entrada: " + event.getPrice() + " €");
        _eventDescription.setText(event.getDescription());

        // Checks if the user has this event marked as favourite
        String userId = _auth.getCurrentUser().getUid();
        DocumentReference userReference = _db.collection("Usuarios").document(userId);
        userReference.get().addOnSuccessListener
        (
            documentSnapshot ->
            {
                if(documentSnapshot.exists())
                {
                    List<String> favourites = (List<String>) documentSnapshot.get("favourites");
                    if(favourites != null && favourites.contains(event.getId()))
                    {
                        _isFavourite = true;
                        _favouriteButton.setImageResource(R.drawable.icon_favourite_full);
                    }
                    else
                    {
                        _isFavourite = false;
                        _favouriteButton.setImageResource(R.drawable.icon_favourite_empty);
                    }
                }
            }
        );

        _favouriteButton.setOnClickListener
        (
            v ->
            {
                // Determines if the user is adding or removing from the favourite list based on the event's current state
                FieldValue operation = _isFavourite ? FieldValue.arrayRemove(event.getId()) : FieldValue.arrayUnion(event.getId());

                Map<String, Object> updates = new HashMap<>();
                updates.put("favourites", operation);

                userReference.set(updates, SetOptions.merge())
                    .addOnSuccessListener
                    (
                        unused ->
                        {
                            // Flips the state and updates the favourite button icon
                            _isFavourite = !_isFavourite;
                            if(_isFavourite)
                            {
                                _favouriteButton.setImageResource(R.drawable.icon_favourite_full);
                                Toast.makeText(this, "Evento añadido a favoritos", Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                _favouriteButton.setImageResource(R.drawable.icon_favourite_empty);
                                Toast.makeText(this, "Evento eliminado de favoritos", Toast.LENGTH_SHORT).show();
                            }
                        }
                    );
            }
        );
    }
}
