package com.kpthink.temperatureconverter;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kpthink.temperatureconverter.utils.ConversionUtils;
import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    EditText etValue;
    Spinner spFrom, spTo;
    ImageButton btnSwap;
    Button btnConvert, btnClear, btnCopy;
    TextView tvResult, tvFormula, tvExport, tvClearAll;
    DecimalFormat df = new DecimalFormat("#.###");

    RecyclerView rvHistory;
    HistoryAdapter adapter;
    List<HistoryItem> historyList = new ArrayList<>();

    private static final String PREFS = "temp_prefs";
    private static final String KEY_HISTORY = "history_json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_main);

            // find views (null-checks below)
            etValue = findViewById(R.id.etValue);
            spFrom = findViewById(R.id.spFrom);
            spTo = findViewById(R.id.spTo);
            btnSwap = findViewById(R.id.btnSwap);
            btnConvert = findViewById(R.id.btnConvert);
            btnClear = findViewById(R.id.btnClear);
            btnCopy = findViewById(R.id.btnCopy);
            tvResult = findViewById(R.id.tvResult);
            tvFormula = findViewById(R.id.tvFormula);
            tvExport = findViewById(R.id.tvExport);
            tvClearAll = findViewById(R.id.tvClearAll);
            rvHistory = findViewById(R.id.rvHistory);

            // quick null check so we fail loudly but gracefully
            if (etValue == null || spFrom == null || spTo == null || btnConvert == null || tvResult == null || rvHistory == null) {
                Toast.makeText(this, "UI elements missing — check activity_main.xml IDs", Toast.LENGTH_LONG).show();
                android.util.Log.e("TC", "Missing UI elements. Verify IDs in activity_main.xml");
                return;
            }

            // spinner setup
            String[] units = {"Celsius", "Fahrenheit", "Kelvin"};
            ArrayAdapter<String> adapterSpinner = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, units);
            adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spFrom.setAdapter(adapterSpinner);
            spTo.setAdapter(adapterSpinner);

            // history RecyclerView
            rvHistory.setLayoutManager(new LinearLayoutManager(this));
            adapter = new HistoryAdapter(this, historyList);
            rvHistory.setAdapter(adapter);

            // safe load
            loadHistorySafe();

            // listeners
            etValue.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    doConvert();
                    return true;
                }
                return false;
            });

            btnConvert.setOnClickListener(v -> {
                try { doConvert(); }
                catch (Exception ex) {
                    android.util.Log.e("TC", "convert error", ex);
                    Toast.makeText(MainActivity.this, "Conversion error: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

            btnClear.setOnClickListener(v -> {
                etValue.setText("");
                tvResult.setText("---");
                tvFormula.setText("");
            });

            btnCopy.setOnClickListener(v -> {
                String text = tvResult.getText().toString();
                if (text.isEmpty() || text.equals("---")) {
                    Toast.makeText(this, "No result to copy", Toast.LENGTH_SHORT).show();
                    return;
                }
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Converted value", text);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show();
            });

            btnSwap.setOnClickListener(v -> {
                int fromPos = spFrom.getSelectedItemPosition();
                int toPos = spTo.getSelectedItemPosition();
                spFrom.setSelection(toPos);
                spTo.setSelection(fromPos);
                doConvert();
            });

            tvClearAll.setOnClickListener(v -> {
                historyList.clear();
                saveHistory();
                adapter.notifyDataSetChanged();
            });

            tvExport.setOnClickListener(v -> {
                if (historyList.isEmpty()) {
                    Toast.makeText(this, "No history to export", Toast.LENGTH_SHORT).show();
                    return;
                }
                StringBuilder sb = new StringBuilder();
                sb.append("conversion,timestamp\n");
                for (HistoryItem hi : historyList) {
                    sb.append("\"").append(hi.conversionText.replace("\"","'")).append("\",").append(hi.timestamp).append("\n");
                }
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("text/csv");
                share.putExtra(Intent.EXTRA_SUBJECT, "Temperature conversions");
                share.putExtra(Intent.EXTRA_TEXT, sb.toString());
                startActivity(Intent.createChooser(share, "Export conversions"));
            });

        } catch (Exception e) {
            android.util.Log.e("TC", "Fatal error in onCreate", e);
            Toast.makeText(this, "Startup error: " + e.getClass().getSimpleName() + " — check Logcat", Toast.LENGTH_LONG).show();
        }
    }

    private void doConvert() {
        String input = etValue.getText().toString().trim();
        if (input.isEmpty()) {
            etValue.setError("Enter a number");
            return;
        }
        double value;
        try { value = Double.parseDouble(input); }
        catch (NumberFormatException ex) {
            etValue.setError("Invalid number");
            return;
        }

        String from = spFrom.getSelectedItem().toString();
        String to = spTo.getSelectedItem().toString();
        double result = ConversionUtils.convert(value, from, to);
        String formatted = df.format(result) + " °" + unitSymbol(to);
        tvResult.setText(formatted);
        tvFormula.setText(ConversionUtils.formula(from, to));
        tvResult.announceForAccessibility(tvResult.getText());

        String conversionText = df.format(value) + " °" + unitSymbol(from) + " → " + df.format(result) + " °" + unitSymbol(to);
        HistoryItem hi = new HistoryItem(conversionText, System.currentTimeMillis());
        historyList.add(0, hi);
        if (historyList.size() > 50) historyList.remove(historyList.size()-1);
        adapter.notifyDataSetChanged();
        saveHistory();
    }

    private String unitSymbol(String unit) {
        switch (unit) {
            case "Celsius": return "C";
            case "Fahrenheit": return "F";
            case "Kelvin": return "K";
            default: return "";
        }
    }

    private void saveHistory() {
        try {
            Gson gson = new Gson();
            String json = gson.toJson(historyList);
            getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString(KEY_HISTORY, json).apply();
        } catch (Exception e) {
            android.util.Log.e("TC", "saveHistory error", e);
        }
    }

    private void loadHistorySafe() {
        try {
            String json = getSharedPreferences(PREFS, MODE_PRIVATE).getString(KEY_HISTORY, null);
            if (json == null) return;
            Gson gson = new Gson();
            Type listType = new TypeToken<List<HistoryItem>>(){}.getType();
            List<HistoryItem> saved = gson.fromJson(json, listType);
            if (saved != null) {
                historyList.clear();
                historyList.addAll(saved);
                if (adapter != null) adapter.notifyDataSetChanged();
            }
        } catch (Exception e) {
            android.util.Log.e("TC", "Failed to load history JSON", e);
            getSharedPreferences(PREFS, MODE_PRIVATE).edit().remove(KEY_HISTORY).apply();
        }
    }
}
