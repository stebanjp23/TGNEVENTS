package com.example.ttgneventos.recyclerviewadapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ttgneventos.model.EventDetails;
import com.example.ttgneventos.pojo.Event;
import com.example.ttgneventos.R;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.material.card.MaterialCardView;

import java.time.LocalDate;
import java.util.List;

public final class EventItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
    private List<Object> _events;
    private boolean _isAdmin;

    public EventItemAdapter(List<Object> events, boolean isAdmin) { _events = events; _isAdmin = isAdmin; }

    // Gets the type of view to display
    private static final int TYPE_EVENT_ITEM = 0;
    private static final int TYPE_DATE_HEADER = 1;
    @Override
    public int getItemViewType(int position)
    {
        if(_events.get(position) instanceof Event)
            return TYPE_EVENT_ITEM;
        else
            return TYPE_DATE_HEADER;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        if(viewType == TYPE_EVENT_ITEM)
            return new EventItem(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapterview_event_item, parent, false));
        else
            return new EventDateHeaderItem(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapterview_event_date_header_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position)
    {
        if(_events.get(position) instanceof Event)
        {
            EventItem eventItem = (EventItem) holder;

            Event event = (Event) _events.get(position);

            eventItem.getEventItemTitle().setText(event.getTitle());
            eventItem.getEventItemDescription().setText(event.getDescription());
            eventItem.getEventItemTime().setText(event.getTime().toString());

            ViewGroup.LayoutParams lp = eventItem.itemView.getLayoutParams();
            FlexboxLayoutManager.LayoutParams flexboxLp = (FlexboxLayoutManager.LayoutParams) lp;
            flexboxLp.setFlexBasisPercent(0.45f);
            flexboxLp.setFlexGrow(0.0f);

            eventItem.getEventItemCard().setOnClickListener
            (
                v ->
                {
                    Intent eventDetails = new Intent(v.getContext(), EventDetails.class);
                    eventDetails.putExtra("Event", event);
                    eventDetails.putExtra("Es_admin", _isAdmin);
                    v.getContext().startActivity(eventDetails);
                }
            );
        }
        else
        {
            EventDateHeaderItem eventDateHeaderItem = (EventDateHeaderItem) holder;

            LocalDate eventDate = (LocalDate) _events.get(position);

            String[] spanishMonths = {"enero", "febrero", "marzo", "abril", "mayo", "junio", "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"};
            String dateString = eventDate.getDayOfMonth() + " de " + spanishMonths[eventDate.getMonthValue() - 1] + " del " + eventDate.getYear();

            eventDateHeaderItem.getHeaderText().setText(dateString);

            ViewGroup.LayoutParams lp = eventDateHeaderItem.itemView.getLayoutParams();
            FlexboxLayoutManager.LayoutParams flexboxLp = (FlexboxLayoutManager.LayoutParams) lp;
            flexboxLp.setFlexBasisPercent(1f);
            flexboxLp.setFlexGrow(0.0f);
        }
    }

    @Override
    public int getItemCount() { return _events.size(); }

    public static final class EventItem extends RecyclerView.ViewHolder
    {
        // ID references
        private final MaterialCardView _eventItemCard;

        private final TextView
            _eventItemTitle,
            _eventItemDescription,
            _eventItemTime;

        private final ImageView _eventItemImage;

        // Constructor
        public EventItem(View itemView)
        {
            super(itemView);

            // Initialize IDs
            _eventItemCard = itemView.findViewById(R.id.eventItemCard);
            _eventItemTitle = itemView.findViewById(R.id.eventItemTitle);
            _eventItemDescription = itemView.findViewById(R.id.eventItemDescription);
            _eventItemTime = itemView.findViewById(R.id.eventItemTime);
            _eventItemImage = itemView.findViewById(R.id.eventItemImage);
        }

        // Getters
        public MaterialCardView getEventItemCard() { return _eventItemCard; }
        public TextView getEventItemTitle() { return _eventItemTitle; }
        public TextView getEventItemDescription() { return _eventItemDescription; }
        public TextView getEventItemTime() { return _eventItemTime; }
        public ImageView getEventItemImage() { return _eventItemImage; }
    }

    public static final class EventDateHeaderItem extends RecyclerView.ViewHolder
    {
        // ID references
        private final TextView _headerText;

        // Constructor
        public EventDateHeaderItem(View itemView)
        {
            super(itemView);

            // Initialize IDs
            _headerText = itemView.findViewById(R.id.headerText);
        }

        // Getters
        public TextView getHeaderText() { return _headerText; }
    }
}
