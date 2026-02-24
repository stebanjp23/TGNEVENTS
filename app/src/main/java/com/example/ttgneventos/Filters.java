package com.example.ttgneventos;

import android.app.DatePickerDialog;
import android.content.Intent;
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
import com.google.type.DateTime;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public final class Filters extends AppCompatActivity
{
    // ID references
    private LinearLayout
        _startDateField,
        _endDateField;

    private EditText _keywordField;
    private ImageButton _addKeywordButton;
    private RecyclerView _addedKeywordsList;

    private ImageButton _filterSearchButton;

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

        _filterSearchButton = findViewById(R.id.filterSearchButton);
        _filterSearchButton.setOnClickListener
        (
            v ->
            {
                // Sends the filters to the main menu
                Intent mainMenu = new Intent(this, MainMenu.class);

                TextView startDateTextView = (TextView) _startDateField.getChildAt(0);
                TextView endDateTextView = (TextView) _endDateField.getChildAt(0);
                String startString = startDateTextView.getText().toString();
                String endString = endDateTextView.getText().toString();
                LocalDate startDate = parseLocalDate(startString);
                LocalDate endDate = parseLocalDate(endString);

                List<String> keywords = _keywords.isEmpty() ? null : new ArrayList<>(_keywords);

                mainMenu.putExtra("Filters", new FilterObject(startDate, endDate, keywords));
                startActivity(mainMenu);
            }
        );
    }

    private LocalDate parseLocalDate(String dateString)
    {
        try
        {
            return LocalDate.parse(dateString, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }
        catch(Exception exception)
        {
            return null;
        }
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
                dateText.setText(LocalDate.of(year, month + 1, dayOfMonth).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            }
        );
    }

    public static class FilterObject implements Serializable
    {
        // Fields
        private final LocalDate _startDate;
        private final LocalDate _endDate;
        private final List<String> _keywords;

        // Constructor
        public FilterObject(LocalDate startDate, LocalDate endDate, List<String> keywords)
        {
            _startDate = startDate;
            _endDate = endDate;
            _keywords = keywords;
        }

        // Getters
        public LocalDate getStartDate() { return _startDate; }
        public LocalDate getEndDate() { return _endDate; }
        public List<String> getKeywords() { return _keywords; }
    }
}
