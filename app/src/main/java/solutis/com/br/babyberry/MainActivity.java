package solutis.com.br.babyberry;


import android.*;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.File;
import java.io.IOException;

import cafe.adriel.androidaudioconverter.AndroidAudioConverter;
import cafe.adriel.androidaudioconverter.model.AudioFormat;
import solutis.com.br.babyberry.solutis.com.br.babyberry.watson.TextToSpeechService;
import solutis.com.br.babyberry.solutis.com.br.babyberry.watson.TextToSpeechServiceResult;
import cafe.adriel.androidaudioconverter.callback.IConvertCallback;

public class MainActivity extends AppCompatActivity implements TextToSpeechServiceResult{

    private static final String TAG = "MainActivity";

    private TextToSpeechService textToSpeechService = new TextToSpeechService();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1);
        }
      if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.INTERNET) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.INTERNET},
                    1);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        // If a notification message is tapped, any data accompanying the notification
        // message is available in the intent extras. In this sample the launcher
        // intent is fired when the notification is tapped, so any accompanying data would
        // be handled here. If you want a different intent fired, set the click_action
        // field of the notification message to the desired intent. The launcher intent
        // is used when no click_action is specified.
        //
        // Handle possible data accompanying notification message.
        // [START handle_data_extras]
        if (getIntent().getExtras() != null) {
            speakHelp("Passo 1: Usar uma toalha úmida ou com uma bolsa térmica em temperatura mais fria no tronco e nos membros, utilize  água fria da torneira\n" +
                    "Passo 2: Mander esta ação verificando a temperatura da criança, até a chegada da emergência.\n" +
                    "\n");
            for (String key : getIntent().getExtras().keySet()) {
                Object value = getIntent().getExtras().get(key);
                Log.d(TAG, "Key: " + key + " Value: " + value);
            }
        }
        // [END handle_data_extras]

        Button subscribeButton = (Button) findViewById(R.id.subscribeButton);
        subscribeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // [START subscribe_topics]
                FirebaseMessaging.getInstance().subscribeToTopic("news");
                // [END subscribe_topics]

                // Log and toast
                String msg = getString(R.string.msg_subscribed);
                Log.d(TAG, msg);
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });

        Button logTokenButton = (Button) findViewById(R.id.logTokenButton);
        logTokenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get token
                String token = FirebaseInstanceId.getInstance().getToken();

                // Log and toast
                String msg = getString(R.string.msg_token_fmt, token);
                Log.d(TAG, msg);
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void speakHelp(String output) {
        System.out.println(output);
        TextToSpeechService textToSpeechService = new TextToSpeechService();
        textToSpeechService.setTextToSpeechServiceResult(this);
        textToSpeechService.execute(output, getExternalCacheDir().getAbsolutePath());
    }

    @Override
    public void processFinishAudio(String output) {
        System.out.println(output);
        playSound();
    }

    private void playSound() {
        File flacFile = new File(getExternalCacheDir().getAbsolutePath(), "babyberry.wav");

        IConvertCallback callback = new IConvertCallback() {
            @Override
            public void onSuccess(File convertedFile) {
                try {
                    Uri myUri = Uri.fromFile(convertedFile);
                    MediaPlayer mediaPlayer = new MediaPlayer();
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mediaPlayer.setDataSource(MainActivity.this, myUri);
                    mediaPlayer.prepare();
                    mediaPlayer.start();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(Exception error) {
                error.printStackTrace();
            }
        };
        AndroidAudioConverter.with(this)
                // Your current audio file
                .setFile(flacFile)

                // Your desired audio format
                .setFormat(AudioFormat.MP3)

                // An callback to know when conversion is finished
                .setCallback(callback)

                // Start conversion
                .convert();


    }
}
