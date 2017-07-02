package solutis.com.br.babyberry.solutis.com.br.babyberry.watson;

import android.os.AsyncTask;

import com.ibm.watson.developer_cloud.text_to_speech.v1.TextToSpeech;
import com.ibm.watson.developer_cloud.text_to_speech.v1.model.AudioFormat;
import com.ibm.watson.developer_cloud.text_to_speech.v1.model.Voice;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by flavia.silva on 17/03/2017.
 */

public class TextToSpeechService extends AsyncTask<String, Void, String> {
    private TextToSpeechServiceResult textToSpeechServiceResult;

    @Override
    protected String doInBackground(String... params) {
        TextToSpeech service = new TextToSpeech();
        service.setUsernameAndPassword("32cc598e-b279-4987-8684-42ec44d875e2", "Qnd0lPksQHgJ");

        try {
            String text = params[0];
            InputStream stream = service.synthesize(text, Voice.PT_ISABELA,
                    AudioFormat.WAV).execute();
            //InputStream in = WaveUtils.reWriteWaveHeader(stream);

            File file = new File(params[1], "babyberry.wav");

            OutputStream out = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = stream.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
            out.close();
            stream.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return "Executed";
    }

    @Override
    protected void onPostExecute(String result) {
        textToSpeechServiceResult.processFinishAudio(result);
    }

    @Override
    protected void onPreExecute() {}

    @Override
    protected void onProgressUpdate(Void... values) {}

    public TextToSpeechServiceResult getTextToSpeechServiceResult() {
        return textToSpeechServiceResult;
    }

    public void setTextToSpeechServiceResult(TextToSpeechServiceResult textToSpeechServiceResult) {
        this.textToSpeechServiceResult = textToSpeechServiceResult;
    }
}
