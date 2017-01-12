package com.sam_chordas.android.stockhawk.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

import com.db.chart.model.LineSet;
import com.db.chart.view.ChartView;
import com.db.chart.view.LineChartView;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;

public class StockDetailsActivity extends AppCompatActivity {
    private LineChartView lineChartView;
    public static final String TAG = "StockDetailsActivity";

    public ArrayList historicalHigh;

    private String id;
    private String symbol;
    private String bid_price;
    private String percent_change;
    private String change;
    public ArrayList<String> labels = new ArrayList<>();
    public ArrayList date;

    public Boolean isConnected;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ConnectivityManager cm =
                (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
     isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        if (isConnected == false) {
            Toast.makeText(getApplicationContext(), getString(R.string.string_no_internet), Toast.LENGTH_LONG).show();
        }

else {
            setContentView(R.layout.activity_line_graph);
            lineChartView = (LineChartView) findViewById(R.id.linechart);
            historicalHigh = new ArrayList();
            labels = new ArrayList<>();
            date = new ArrayList();


            if (savedInstanceState == null) {
                id = getIntent().getStringExtra(QuoteColumns._ID);
                symbol = getIntent().getStringExtra(QuoteColumns.SYMBOL);

                new AsyncHttpTask().execute(this.getURL());
            } else {
                id = savedInstanceState.getString(QuoteColumns._ID);
                symbol = savedInstanceState.getString(QuoteColumns.SYMBOL);

                historicalHigh = savedInstanceState.getParcelableArrayList(getString(R.string.Historial_High));
                date = savedInstanceState.getParcelableArrayList(getString(R.string.string_date));

                setupLineChart();
            }
            getLabels();
        }

    }
    public void onSaveInstanceState(Bundle savedInstanceState) {

        savedInstanceState.putString(QuoteColumns.SYMBOL, symbol);
        savedInstanceState.putParcelableArrayList(getString(R.string.Historial_High), historicalHigh);
        savedInstanceState.putParcelableArrayList(getString(R.string.string_date), date);
        super.onSaveInstanceState(savedInstanceState);

    }

    public String getURL(){

        StringBuilder urlStringBuilder = new StringBuilder();
        getLabels();
        try{
            // Base URL for the Yahoo query
            urlStringBuilder.append("https://query.yahooapis.com/v1/public/yql?q=");
             urlStringBuilder.append(URLEncoder.encode("select * from yahoo.finance.historicaldata where symbol=\"", "UTF-8"));
            urlStringBuilder.append(URLEncoder.encode(symbol+"\"", "UTF-8"));


            urlStringBuilder.append(URLEncoder.encode("and startDate = " , "UTF-8"));
            urlStringBuilder.append(URLEncoder.encode("\""+labels.get(0)+"\"", "UTF-8"));
            //urlStringBuilder.append(URLEncoder.encode("\"2015-07-15\"", "UTF-8"));
            urlStringBuilder.append(URLEncoder.encode(" and endDate = ", "UTF-8"));
            urlStringBuilder.append(URLEncoder.encode("\""+labels.get(labels.size()-1)+"\"", "UTF-8"));
            //urlStringBuilder.append(URLEncoder.encode("\"2016-07-15\"", "UTF-8"));

            urlStringBuilder.append("&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&callback=");
            Log.d(TAG+"getLabels, url=", urlStringBuilder.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return urlStringBuilder.toString();

    }


    private void setupLineChart(){
        final LineSet dataset = new LineSet();

        float min = Float.parseFloat((String)historicalHigh.get(0));
        float max = 0;

        for(int i = 0;i<historicalHigh.size();i++){
            float value = Float.parseFloat((String)historicalHigh.get(i));
            String currentDate = (String) date.get(i);

            dataset.addPoint((String)date.get(i), value);

            if(value < min){
                min = value;
            }
            if(value>max){
                max=value;
            }

        }

        dataset.setColor(getResources().getColor(R.color.material_red_700))
                .setThickness(5)
                .setSmooth(true)
                .beginAt(1);
        lineChartView.setBackgroundColor(Color.GRAY);
        lineChartView.setContentDescription(symbol+ R.string.historical_data_chart);

        //lineChartView.setBackgroundColor(R.color.common_google_signin_btn_text_light);
        lineChartView.addData(dataset);
        lineChartView.setYAxis(true);
        lineChartView.setXAxis(false);

        int temp = Math.round((max-min)/5);
        lineChartView.setAxisBorderValues(Math.round(min)-5, Math.round(max)+5);
        lineChartView.setStep(temp);

        lineChartView.setGrid(ChartView.GridType.FULL, new Paint(Color.GRAY));

        lineChartView.show();
    }

    public void restoreActionBar() {

        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(symbol.toUpperCase());


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my_stocks, menu);
     if(isConnected==true) {
         restoreActionBar();
         return true;
     }
        else
         return false;
    }

    public class AsyncHttpTask extends AsyncTask<String, Void, Integer> {

        @Override
        protected Integer doInBackground(String... params) {
            int result = 0;
            try {
                // Create Apache HttpClient
                HttpClient httpclient = new DefaultHttpClient();
                HttpResponse httpResponse = httpclient.execute(new HttpGet(params[0]));
                int statusCode = httpResponse.getStatusLine().getStatusCode();

                if (statusCode == 200) {
                    String response = parseToString(httpResponse.getEntity().getContent());
                    parseJSON(response);
                    result = 1; // Successful
                } else {
                    result = 0; //"Failed
                }
            } catch (Exception e) {
                Log.d(TAG, e.getLocalizedMessage());
            }
            //Log.d(TAG+"AsyncHttpTask", Integer.toString(result));
            return result;
        }
        protected void onPostExecute(Integer result) {
            // Download complete. Let us update UI

            if (result == 1) {
                setupLineChart();


                Log.d(TAG,"onPostExecute");

            } else {
                Log.d(TAG, "Failed to get data");

            }

        }
    }
    private String parseToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        String result = "";
        while ((line = bufferedReader.readLine()) != null) {
            result += line;
        }

        // Close stream
        if (null != inputStream) {
            inputStream.close();
        }
        return result;
    }
    private void parseJSON(String inputString) {
        Log.d(TAG, "parseJSON()");

        try {
            JSONObject jsonObject = new JSONObject(inputString);
            JSONObject jsonQuery = jsonObject.optJSONObject("query");
            JSONObject jsonResults= jsonQuery.optJSONObject("results");
            JSONArray quote = jsonResults.optJSONArray("quote");
            Log.d(TAG, quote.toString());
            Log.d(TAG, Integer.toString(quote.length()));
            for(int i=0;i<quote.length();i++){
                JSONObject dailyQuote = quote.getJSONObject(i);
                //Log.d(TAG, dailyQuote.getString("High"));
                historicalHigh.add(dailyQuote.getString("High"));
                date.add(dailyQuote.getString("Date"));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
    public ArrayList<String> getLabels() {

        for(int i = 12; i >=0; i--) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Calendar pastCalendar = Calendar.getInstance();
            pastCalendar.add(Calendar.MONTH, +-i);
            String label = dateFormat.format(pastCalendar.getTime());
            labels.add(label);
            Log.d(TAG, label);

        }

        return labels;
    }

}
