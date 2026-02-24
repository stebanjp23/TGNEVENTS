package com.example.ttgneventos.recyclerviewadapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ttgneventos.R;

import java.util.List;

public final class KeywordItemAdapter extends RecyclerView.Adapter<KeywordItemAdapter.KeywordItem>
{
    private final List<String> _keywords;
    public KeywordItemAdapter(List<String> keywords) { _keywords = keywords; }

    @NonNull
    @Override
    public KeywordItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        return new KeywordItem(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapterview_keyword_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull KeywordItem holder, int position)
    {
        holder.getKeywordText().setText(_keywords.get(position));
    }

    @Override
    public int getItemCount()
    {
        return _keywords.size();
    }

    public final class KeywordItem extends RecyclerView.ViewHolder
    {
        private TextView _keywordText;
        private ImageButton _deleteKeywordButton;

        public KeywordItem(@NonNull View itemView)
        {
            super(itemView);

            _keywordText = itemView.findViewById(R.id.keywordText);
            _deleteKeywordButton = itemView.findViewById(R.id.deleteKeywordButton);

            _deleteKeywordButton.setOnClickListener
            (
                v ->
                {
                    _keywords.remove(getBindingAdapterPosition());
                    notifyItemRemoved(getBindingAdapterPosition());
                }
            );
        }

        // Getters & setters
        public TextView getKeywordText() { return _keywordText; }
        public ImageButton getDeleteKeywordButton() { return _deleteKeywordButton; }
    }
}
