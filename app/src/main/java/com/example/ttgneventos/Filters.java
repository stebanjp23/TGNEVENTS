package com.example.ttgneventos;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

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

import java.util.ArrayList;
import java.util.List;

public class Filters extends AppCompatActivity
{
    // ID references
    private LinearLayout
        _startDateField,
        _endDateField;

    private EditText _keywordField;
    private ImageButton _addKeywordButton;
    private RecyclerView _addedKeywordsList;

    // Tracks keywords
    private List<String> _keywords = new ArrayList<String>();
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.filters);
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
        _startDateField = findViewById(R.id.startDateField);
        _endDateField = findViewById(R.id.endDateField);
        _startDateField.setOnClickListener(v -> datePickDialog(_startDateField));
        _endDateField.setOnClickListener(v -> datePickDialog(_endDateField));

        _keywordField = findViewById(R.id.keywordField);
        _addKeywordButton = findViewById(R.id.addKeywordButton);
        _addedKeywordsList = findViewById(R.id.addedKeywordsList);
        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(this);
        layoutManager.setFlexDirection(FlexDirection.ROW);
        layoutManager.setFlexWrap(FlexWrap.WRAP);
        layoutManager.setJustifyContent(JustifyContent.FLEX_START);
        _addedKeywordsList.setLayoutManager(layoutManager);
        KeywordItemAdapter adapter = new KeywordItemAdapter(_keywords);
        _addedKeywordsList.setAdapter(adapter);
        _addKeywordButton.setOnClickListener
        (
            v ->
            {
                String newKeyword = _keywordField.getText().toString().trim();
                if(newKeyword.isEmpty()) return;
                _keywords.add(newKeyword);
                adapter.notifyItemInserted(_keywords.size() - 1);
                _keywordField.setText("");
            }
        );
    }

    private void datePickDialog(LinearLayout dateField)
    {
        // Prompts the user for a date
        DatePickerDialog datePickerDialog = new DatePickerDialog(this);
        datePickerDialog.show();

        // Retrieves the date selected by the user and updates the date field with the selected date
        datePickerDialog.setOnDateSetListener
        (
            (view, year, month, dayOfMonth) ->
            {
                TextView dateText = (TextView) dateField.getChildAt(0);
                dateText.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
            }
        );
    }
}
