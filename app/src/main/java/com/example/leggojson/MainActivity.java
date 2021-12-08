package com.example.leggojson;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.helper.widget.Layer;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, View.OnClickListener {
    EditText eInput;
    Button changes;
    Spinner s1;
    Spinner s2;
    Spinner s3;
    String vs1, vs2, vs3;
    int counter;
    double inputValue;
    TextView tOutput,tList,conversionRateFor;
    Layer c;

    private final String TAG = MainActivity.class.getSimpleName();
    String[][] conversionMatrix = new String[7][2];
    double rateToEur;
    double rateFromEur;
    double output;

    // URL to get contacts JSON
    String url = "http://data.fixer.io/api/latest?access_key=da2ba6072e65af8a125eb9f91e7a56ce";

    ArrayList<HashMap<String, String>> contactList;

    //method for get the location and set the default currency
    private void getLocation(){
        TelephonyManager tm = (TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);
        String countryCodeValue = tm.getNetworkCountryIso();
        //Log.d("TAG",countryCodeValue);
        switch (countryCodeValue){
            case "se":
                s1.setSelection(2);
                break;
            case "us":
                s1.setSelection(3);
                s3.setSelection(3);
                break;
            case "gb":
                s1.setSelection(4);
                break;
            case "cn":
                s1.setSelection(5);
                break;
            case "jp":
                s1.setSelection(6);
                break;
            case "kr":
                s1.setSelection(7);
                break;
            default:
                s1.setSelection(1);
        }




    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        contactList = new ArrayList<>();
        new GetRates().execute();
        counter = 0;
        c = findViewById(R.id.c);
        changes = findViewById(R.id.changeView);
        eInput = findViewById(R.id.in);
        tOutput = findViewById(R.id.out);
        tList= findViewById(R.id.textView3);
        conversionRateFor = findViewById(R.id.textView4);
        s1 = findViewById(R.id.spinner);
        s2 = findViewById(R.id.spinner2);
        s3 = findViewById(R.id.spinner3);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.currencies, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        s1.setAdapter(adapter);
        s2.setAdapter(adapter);
        s3.setAdapter(adapter);
        s1.setOnItemSelectedListener(this);
        s2.setOnItemSelectedListener(this);
        s3.setOnItemSelectedListener(this);
        //set default spinner currency
        getLocation();

        changes.setOnClickListener(this);
        eInput.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                vs1 = s1.getSelectedItem().toString();
                vs2 = s2.getSelectedItem().toString();
                for (String[] strings : conversionMatrix) {
                    if (strings[0].compareTo(vs1) == 0)
                        rateToEur = Double.parseDouble(strings[1]);
                }
                for (String[] strings : conversionMatrix) {
                    if (strings[0].compareTo(vs2) == 0)
                        rateFromEur = Double.parseDouble(strings[1]);
                }
            }

            @SuppressLint("DefaultLocale")
            public void afterTextChanged(Editable s) {
                if (eInput.getText().length() < 1) {
                    tOutput.setText("");
                    return;
                }
                inputValue = Double.parseDouble(eInput.getText().toString());
                output = (inputValue / rateToEur) * rateFromEur;
                tOutput.setText(String.format("%.4f",output));

            }


        });

    }

    @Override
    public void onClick(View v) {
        if (counter == 0) {
            s3.setVisibility(View.VISIBLE);
            tList.setVisibility(View.VISIBLE);
            conversionRateFor.setVisibility(View.VISIBLE);
            c.setVisibility(View.GONE);
            counter = 1;

        } else {
            s3.setVisibility(View.GONE);
            tList.setVisibility(View.GONE);
            conversionRateFor.setVisibility(View.GONE);
            c.setVisibility(View.VISIBLE);
            counter = 0;
        }
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (eInput.getText().length() < 1) {
            tOutput.setText("");
            return;
        }
        vs1 = s1.getSelectedItem().toString();
        vs2 = s2.getSelectedItem().toString();
        vs3 = s3.getSelectedItem().toString();
        for (String[] strings : conversionMatrix) {
            if (strings[0].compareTo(vs1) == 0)
                rateToEur = Double.parseDouble(strings[1]);
        }
        for (String[] strings : conversionMatrix) {
            if (strings[0].compareTo(vs2) == 0)
                rateFromEur = Double.parseDouble(strings[1]);
        }
        inputValue = Double.parseDouble(eInput.getText().toString());
        output = (inputValue / rateToEur) * rateFromEur;
        tOutput.setText(String.format("%.4f",output));

        getListRate();

    }



    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    /**
     * Async task class to get json by making HTTP call
     */
    private class GetRates extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();
            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(url);
            Log.e(TAG, "Response from url: " + jsonStr);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);
                    JSONObject rates = jsonObj.getJSONObject("rates");
                    Double rateEURDouble = rates.getDouble("EUR");
                    Double rateSEKDouble = rates.getDouble("SEK");
                    Double rateUSDDouble = rates.getDouble("USD");
                    Double rateGBPDouble = rates.getDouble("GBP");
                    Double rateCNYDouble = rates.getDouble("CNY");
                    Double rateJPYDouble = rates.getDouble("JPY");
                    Double rateKRWDouble = rates.getDouble("KRW");
                    conversionMatrix[0][0] = "EUR";
                    conversionMatrix[1][0] = "SEK";
                    conversionMatrix[2][0] = "USD";
                    conversionMatrix[3][0] = "GBP";
                    conversionMatrix[4][0] = "CNY";
                    conversionMatrix[5][0] = "JPY";
                    conversionMatrix[6][0] = "KRW";
                    conversionMatrix[0][1] = rateEURDouble.toString();
                    conversionMatrix[1][1] = rateSEKDouble.toString();
                    conversionMatrix[2][1] = rateUSDDouble.toString();
                    conversionMatrix[3][1] = rateGBPDouble.toString();
                    conversionMatrix[4][1] = rateCNYDouble.toString();
                    conversionMatrix[5][1] = rateJPYDouble.toString();
                    conversionMatrix[6][1] = rateKRWDouble.toString();

                    //Log.d("DEGUB", conversionMatrix[5][1]);

                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(),
                            "Json parsing error: " + e.getMessage(),
                            Toast.LENGTH_LONG)
                            .show());

                }
            } else {
                Log.e(TAG, "Couldn't get json from server.");
                runOnUiThread(
                        () -> Toast.makeText(getApplicationContext(),
                                "Couldn't get json from server. Check LogCat for possible errors!",
                                Toast.LENGTH_LONG)
                                .show());
            }
            vs3 = s3.getSelectedItem().toString();

            getListRate();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

        }

    }

    public void getListRate(){
        switch (vs3) {
            case "SEK":
                tList.setText("");
                for (String[] matrix : conversionMatrix) { //row
                    for (int k = 0; k < conversionMatrix[0].length; k++) { //column
                        if (k > 0) {
                            double base = Double.parseDouble(conversionMatrix[1][1]);
                            double tot = (Double.parseDouble(matrix[k])) / base;
                            tList.append(tot + "");
                        } else {
                            tList.append(matrix[k] + "  ");
                        }
                    }
                    tList.append("\n");
                }
                break;
            case "USD":
                tList.setText("");
                for (String[] matrix : conversionMatrix) { //row
                    for (int k = 0; k < conversionMatrix[0].length; k++) { //column
                        if (k > 0) {
                            double base = Double.parseDouble(conversionMatrix[2][1]);
                            double tot = (Double.parseDouble(matrix[k])) / base;
                            tList.append(tot + "");
                        } else {
                            tList.append(matrix[k] + "  ");
                        }
                    }
                    tList.append("\n");
                }
                break;
            case "GBP":
                tList.setText("");
                for (String[] matrix : conversionMatrix) { //row
                    for (int k = 0; k < conversionMatrix[0].length; k++) { //column
                        if (k > 0) {
                            double base = Double.parseDouble(conversionMatrix[3][1]);
                            double tot = (Double.parseDouble(matrix[k])) / base;
                            tList.append(tot + "");
                        } else {
                            tList.append(matrix[k] + "  ");
                        }
                    }
                    tList.append("\n");
                }
                break;
            case "CNY":
                tList.setText("");
                for (String[] matrix : conversionMatrix) { //row
                    for (int k = 0; k < conversionMatrix[0].length; k++) { //column
                        if (k > 0) {
                            double base = Double.parseDouble(conversionMatrix[4][1]);
                            double tot = (Double.parseDouble(matrix[k])) / base;
                            tList.append(tot + "");
                        } else {
                            tList.append(matrix[k] + "  ");
                        }
                    }
                    tList.append("\n");
                }
                break;
            case "JPY":
                tList.setText("");
                for (String[] matrix : conversionMatrix) { //row
                    for (int k = 0; k < conversionMatrix[0].length; k++) { //column
                        if (k > 0) {
                            double base = Double.parseDouble(conversionMatrix[5][1]);
                            double tot = (Double.parseDouble(matrix[k])) / base;
                            tList.append(tot + "");
                        } else {
                            tList.append(matrix[k] + "  ");
                        }
                    }
                    tList.append("\n");
                }
                break;
            case "KRW":
                tList.setText("");
                for (String[] matrix : conversionMatrix) { //row
                    for (int k = 0; k < conversionMatrix[0].length; k++) { //column
                        if (k > 0) {
                            double base = Double.parseDouble(conversionMatrix[6][1]);
                            double tot = (Double.parseDouble(matrix[k])) / base;
                            tList.append(tot + "");
                        } else {
                            tList.append(matrix[k] + "  ");
                        }
                    }
                    tList.append("\n");
                }
                break;
            default:
                tList.setText("");
                for (String[] matrix : conversionMatrix) {
                    for (int k = 0; k < conversionMatrix[0].length; k++) {
                        tList.append(matrix[k] + "  ");
                    }
                    tList.append("\n");
                }
        }
    }
}
