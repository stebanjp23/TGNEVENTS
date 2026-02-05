package com.example.ttgneventos;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.security.KeyPair;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class MainMenu extends AppCompatActivity
{
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.main_menu);
        ViewCompat.setOnApplyWindowInsetsListener
        (
            findViewById(R.id.main), (v, insets) ->
            {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            }
        );

        // Initializes the event item display
        RecyclerView eventDisplay = findViewById(R.id.eventDisplay);
        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(this);
        layoutManager.setFlexDirection(FlexDirection.ROW);
        layoutManager.setFlexWrap(FlexWrap.WRAP);
        layoutManager.setJustifyContent(JustifyContent.FLEX_START);
        eventDisplay.setLayoutManager(layoutManager);

        List<Object> flattenedList = new ArrayList<>();
        EventItemAdapter adapter = new EventItemAdapter(flattenedList);
        eventDisplay.setAdapter(adapter);

        // Retrieves the events from the database
        db.collection("Events").get().addOnSuccessListener
        (
            queryDocumentSnapshots ->
            {
                List<Event> events = new ArrayList<>();
                for(DocumentSnapshot document : queryDocumentSnapshots.getDocuments())
                    events.add(document.toObject(Event.class));

                // Sorts events by proximity of date
                events.sort((e1, e2) -> e1.getDateTime().compareTo(e2.getDateTime()));
                Map<LocalDate, List<Event>> dates = new LinkedHashMap<>();
                for(Event event : events)
                {
                    LocalDate date = event.getDate();
                    if(!dates.containsKey(date)) dates.put(date, new ArrayList<>());
                    dates.get(date).add(event);
                }

                for(Map.Entry<LocalDate, List<Event>> entry : dates.entrySet())
                {
                    flattenedList.add(entry.getKey());
                    flattenedList.addAll(entry.getValue());
                }

                adapter.notifyDataSetChanged();
            }
        );
    }
}
