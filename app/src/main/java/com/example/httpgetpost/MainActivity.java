package com.example.httpgetpost;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {
    TextView textView;
    Handler handler;
    Timer voicePlayerTimer;
    MediaPlayer voiceMediaPlayer;
    ArrayList<Integer> voicePlaylist;
    PlayListBuilder playlist;
    int voicePlaylistCounter = 0;
    String currentNumber="";
    Context voiceContext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        voiceContext = this;
        textView = findViewById(R.id.textView);
        handler = new Handler();
        playlist = new PlayListBuilder();
        Thread thread = new Thread() {

            @Override
            public void run() {

                try {
                    while (!currentThread().isInterrupted()) {
                        Thread.sleep(1000);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                String queueNumber;
                                GetQueueNumberAsyncTask dl = new GetQueueNumberAsyncTask();
                                try{
                                    queueNumber = dl.execute("https://lineup.blueeyes.com.tw/counter.php?k=7b2007112425a79e08498ac36ff76f948945890c").get();
                                    if(!queueNumber.equals(currentNumber)) {
                                        textView.setText(queueNumber);
                                        voicePlaylist = playlist.buildPlaylist(Integer.parseInt(queueNumber), voiceContext);
                                        voiceMediaPlayer = MediaPlayer.create(voiceContext, voicePlaylist.get(0));
                                        voiceMediaPlayer.start();
                                        voicePlayerTimer = new Timer();
                                        if (voicePlaylist.size() > 1) {
                                            playNextVoice(); //first iteration
                                        }
                                        currentNumber = queueNumber;
                                        voicePlaylistCounter = 0;
                                    }
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

    public void playNextVoice() { //recursive function
        voicePlayerTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                voiceMediaPlayer.reset();
                try {
                    voiceMediaPlayer = MediaPlayer.create(MainActivity.this, voicePlaylist.get(++voicePlaylistCounter));
                    voiceMediaPlayer.start();
                }catch (IndexOutOfBoundsException e){
                    Log.i("There is a problem!", "Array out of bounds!");
                }
                if (voicePlaylist.size() > voicePlaylistCounter +1) { //stopping condition
                    playNextVoice();
                }
            }
        }, voiceMediaPlayer.getDuration());
    }

    @Override
    public void onDestroy() {
        if (voiceMediaPlayer.isPlaying())
            voiceMediaPlayer.stop();
        voiceMediaPlayer.release();
        voicePlayerTimer.cancel();
        super.onDestroy();
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