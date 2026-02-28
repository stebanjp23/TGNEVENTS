package com.example.ttgneventos.model;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ttgneventos.R;
import com.example.ttgneventos.pojo.Event;
import com.example.ttgneventos.recyclerviewadapters.EventItemAdapter;
import com.example.ttgneventos.util.IniciarMenu;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class Favourites extends AppCompatActivity
{
    private FirebaseAuth _auth = null;
    private FirebaseFirestore _db = null;


    private List<Object> _flattenedList = new ArrayList<>();
    private List<String> _lastFavourites = new ArrayList<>();
    private EventItemAdapter _adapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_favourites);

        Toolbar toolbar = findViewById(R.id.toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);

        NavigationView navView = findViewById(R.id.nav_view);

        Menu menu = navView.getMenu();
        MenuItem itemAdmin = menu.findItem(R.id.administracion);
        itemAdmin.setVisible(getIntent().getBooleanExtra("Es_admin", false));

        IniciarMenu.setupDrawer(this, drawer, navView, toolbar, getIntent().getBooleanExtra("Es_admin", false));
        IniciarMenu.actualizarEmailEnHeader(navView);

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

        _flattenedList = new ArrayList<>();
        _adapter = new EventItemAdapter(_flattenedList, getIntent().getBooleanExtra("Es_admin", false));
        eventDisplay.setAdapter(_adapter);

        // Retrieves the users favourite event IDs and subsequently retrieves all events to filter them out
        _auth = FirebaseAuth.getInstance();
        _db = FirebaseFirestore.getInstance();
        _db.collection("Usuarios").document(_auth.getCurrentUser().getUid()).get().addOnSuccessListener
        (
            documentSnapshot ->
            {
                List<String> favourites = (List<String>) documentSnapshot.get("favourites");
                if(favourites == null || favourites.isEmpty())
                {
                    Toast.makeText(this, "No tienes eventos favoritos", Toast.LENGTH_LONG).show();
                    return;
                }
                _lastFavourites.clear();
                _lastFavourites.addAll(favourites);
                listFavouriteEvents(favourites);
            }
        );
    }

    @Override
    protected void onRestart()
    {
        super.onRestart();

        // Checks if the user removed a favourite event and updates the list
        _db.collection("Usuarios").document(_auth.getCurrentUser().getUid()).get().addOnSuccessListener
        (
            documentSnapshot ->
            {
                List<String> favourites = (List<String>) documentSnapshot.get("favourites");
                if(favourites == null || favourites.isEmpty())
                {
                    Toast.makeText(this, "No tienes eventos favoritos", Toast.LENGTH_LONG).show();
                    _flattenedList.clear();
                    _lastFavourites.clear();
                    _adapter.notifyDataSetChanged();
                    return;
                }

                if(favourites.size() != _lastFavourites.size())
                {
                    _lastFavourites.clear();
                    _lastFavourites.addAll(favourites);
                    listFavouriteEvents(favourites);
                }
            }
        );
    }

    private void listFavouriteEvents(List<String> favourites)
    {
        _flattenedList.clear();

        // Retrieves the events from Firestore and updates the event display
        _db.collection("Events").get().addOnSuccessListener
        (
            queryDocumentSnapshots ->
            {
                List<Event> events = new ArrayList<>();

                for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments())
                {
                    Event event = document.toObject(Event.class);
                    if(event == null || !favourites.contains(document.getId())) continue;
                    event.setId(document.getId());
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
                    _flattenedList.add(entry.getKey());
                    _flattenedList.addAll(entry.getValue());
                }

                _adapter.notifyDataSetChanged();
            }
        );
    }
}