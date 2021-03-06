package solutis.com.br.babyberry;


import android.*;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.File;
import java.io.IOException;

import cafe.adriel.androidaudioconverter.AndroidAudioConverter;
import cafe.adriel.androidaudioconverter.model.AudioFormat;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import solutis.com.br.babyberry.solutis.com.br.babyberry.retrofit.ApiInterface;
import solutis.com.br.babyberry.solutis.com.br.babyberry.watson.TextToSpeechService;
import solutis.com.br.babyberry.solutis.com.br.babyberry.watson.TextToSpeechServiceResult;
import cafe.adriel.androidaudioconverter.callback.IConvertCallback;

public class MainActivity extends AppCompatActivity implements TextToSpeechServiceResult{

    private static final String TAG = "MainActivity";

    public static final String BASE_URL = "http://192.168.25.170:8080/";

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
        View someView = findViewById(R.id.linearlayout);

        // Find the root view
        View root = someView.getRootView();

        // Set the color
        root.setBackgroundColor(Color.rgb(255,219,249));


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

            for (String key : getIntent().getExtras().keySet()) {
                if (key.equals("Febre")) {
                    speakHelp("Use uma toalha úmida ou com uma bolsa térmica em temperatura mais fria no tronco e nos membros, utilize  água fria da torneira e mantenha-o assim até a chegada da emergência. Sempre verificando a temperatura da criança. ");
                } else if (key.equals("Apneia")){
                    speakHelp("Primeiro tente acordá-lo pegando no colo e chamando por ele. Se ele não voltar a respirar faça respiração boca a boca colocando a sua boca, expelindo o ar que está somente na sua boca dentro da boca e do nariz ao mesmo tempo. Como a face do bebê é pequena, sua boca aberta deve ser capaz de cobrir tanto o nariz como a boca do bebê. Não é preciso inspirar profundamente para oferecer muito ar para o bebê porque os pulmões dele são muito pequenos, por isso basta o ar que está dentro da sua boca.");
                }
            }
        }
        // [END handle_data_extras]

        Button logTokenButton = (Button) findViewById(R.id.logTokenButton);
        logTokenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get token
                FirebaseMessaging.getInstance().subscribeToTopic("news");

                String token = FirebaseInstanceId.getInstance().getToken();

                // Log and toast
                String msg = getString(R.string.msg_token_fmt, token);
                Log.d(TAG, msg);
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();

                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();

                ApiInterface apiService =
                        retrofit.create(ApiInterface.class);
                Call<Void> call = apiService.enviarToken(token);
                call.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        Toast.makeText(MainActivity.this, "Gravou", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        t.printStackTrace();
                        Toast.makeText(MainActivity.this, "Deu erro", Toast.LENGTH_SHORT).show();
                    }
                });
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
