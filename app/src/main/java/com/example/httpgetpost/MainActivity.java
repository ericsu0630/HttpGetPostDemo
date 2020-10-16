package com.example.httpgetpost;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {
    TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.textView);
        Thread thread = new Thread() {

            @Override
            public void run() {

                try {
                    while (!currentThread().isInterrupted()) {
                        Thread.sleep(1000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String queueNumber;
                                GetQueueNumberAsyncTask dl = new GetQueueNumberAsyncTask();
                                try{
                                    queueNumber = dl.execute("https://lineup.blueeyes.com.tw/counter.php?k=7b2007112425a79e08498ac36ff76f948945890c").get();
                                    textView.setText(queueNumber);
                                }catch(Exception e){
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
    }

    public class GetQueueNumberAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            String returnValue;
            try{
                URL url = new URL(strings[0]);
                HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
                con.setRequestMethod("POST");

                // For POST only - START
                con.setDoOutput(true);
                DataOutputStream os = new DataOutputStream(con.getOutputStream());
                os.write("key=get".getBytes());
                os.flush();
                os.close();
                // For POST only - END

                //For POST request
                int responseCode = con.getResponseCode(); //get response code
                Log.i("POST Response Code",String.valueOf(responseCode));

                if (responseCode == HttpURLConnection.HTTP_OK) { //success
                    BufferedReader in = new BufferedReader(new InputStreamReader(
                            con.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder(); //get POST response

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    returnValue = String.valueOf(response);
                } else {
                    returnValue = "POST request failed";
                }
            }catch(Exception e){
                e.printStackTrace();
                returnValue = "URL Connection failed";
            }
            return returnValue;
        }
    }
}