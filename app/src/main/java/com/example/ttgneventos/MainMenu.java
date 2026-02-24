package com.example.ttgneventos;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MainMenu extends AppCompatActivity
{
    // ID references
    private ImageButton _filterButton;

    private FirebaseFirestore db = null;

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

        // Initializes ID references
        _filterButton = findViewById(R.id.filterScreenButton);
        _filterButton.setOnClickListener(v -> startActivity(new Intent(this, Filters.class)));

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

        // Retrieves filters if they exist
        Filters.FilterObject filters;
        if(getIntent().getExtras() != null && getIntent().getExtras().containsKey("Filters"))
            filters = (Filters.FilterObject) getIntent().getExtras().getSerializable("Filters");
        else
            filters = null;

        // Retrieves the events from Firestore and updates the event display
        db = FirebaseFirestore.getInstance();
        db.collection("Events").get().addOnSuccessListener
        (
            queryDocumentSnapshots ->
            {
                List<Event> events = new ArrayList<>();

                for(DocumentSnapshot document : queryDocumentSnapshots.getDocuments())
                {
                    Event event = document.toObject(Event.class);
                    if(event == null) continue;

                    // Tries to apply filters
                    if(filters != null)
                    {
                        // Applies date filters
                        if
                        (
                            filters.getStartDate() != null && event.getDate().isBefore(filters.getStartDate()) ||
                            filters.getEndDate() != null && event.getDate().isAfter(filters.getEndDate())
                        ) continue;

                        // Applies search keywords filter
                        if(filters.getKeywords() != null)
                        {
                            boolean matchesAll = true;
                            String description = event.getDescription().toLowerCase();
                            for(String keyword : filters.getKeywords())
                            {
                                String lowerKeyword = keyword.toLowerCase();
                                String regex = "\\b" + java.util.regex.Pattern.quote(lowerKeyword) + "\\b";
                                Matcher matcher = Pattern.compile(regex).matcher(description);
                                if(!matcher.find())
                                {
                                    matchesAll = false;
                                    break;
                                }
                            }
                            if(!matchesAll) continue;
                        }
                    }

                    events.add(event);
                }

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
