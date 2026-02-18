package com.example.ttgneventos;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.FlexboxLayoutManager;

import java.time.LocalDate;
import java.util.List;

public final class EventItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
    private List<Object> _events;

    public EventItemAdapter(List<Object> events) { _events = events; }

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
            return new EventItem(LayoutInflater.from(parent.getContext()).inflate(R.layout.event_item, parent, false));
        else
            return new EventHeaderText(LayoutInflater.from(parent.getContext()).inflate(R.layout.event_date_header, parent, false));
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
            flexboxLp.setFlexBasisPercent(0.50f);
            flexboxLp.setFlexGrow(0.0f);
        }
        else
        {
            EventHeaderText eventHeaderText = (EventHeaderText) holder;

            LocalDate eventDate = (LocalDate) _events.get(position);
            String dateString = eventDate.getDayOfMonth() + " de " + eventDate.getMonth() + " del " + eventDate.getYear();

            eventHeaderText.getHeaderText().setText(dateString);

            ViewGroup.LayoutParams lp = eventHeaderText.itemView.getLayoutParams();
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
            _eventItemTitle = itemView.findViewById(R.id.eventItemTitle);
            _eventItemDescription = itemView.findViewById(R.id.eventItemDescription);
            _eventItemTime = itemView.findViewById(R.id.eventItemTime);
            _eventItemImage = itemView.findViewById(R.id.eventItemImage);
        }

        // Getters
        public TextView getEventItemTitle() { return _eventItemTitle; }
        public TextView getEventItemDescription() { return _eventItemDescription; }
        public TextView getEventItemTime() { return _eventItemTime; }
        public ImageView getEventItemImage() { return _eventItemImage; }
    }

    public static final class EventHeaderText extends RecyclerView.ViewHolder
    {
        // ID references
        private final TextView _headerText;

        // Constructor
        public EventHeaderText(View itemView)
        {
            super(itemView);

            // Initialize IDs
            _headerText = itemView.findViewById(R.id.headerText);
        }

        // Getters
        public TextView getHeaderText() { return _headerText; }
    }
}
