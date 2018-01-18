package fr.enssat.berrou_couillard.interfaces_riches;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.VideoView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    private VideoView vidView;
    private MediaController vidControl;
    private JSONObject jObject;
    private WebView browser;
    private MyWebViewClient myWebViewClient = new MyWebViewClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Lecture du fichier Json pour obtenir les informations
        // plus rapidement et simplement dans la variable jObject
        readFile();
        // Webview
        browser = (WebView) findViewById(R.id.webView);
        browser.setWebViewClient(myWebViewClient);
        browser.getSettings().setJavaScriptEnabled(true);

        // Vidéo view
        vidView = (VideoView)findViewById(R.id.videoView);
        // URL de la vidéo pour la lancer et de la webView
        try {
            JSONObject jFilm = jObject.getJSONObject("Film");
            vidView.setVideoURI(Uri.parse(jFilm.getString("file_url")));
            myWebViewClient.shouldOverrideUrlLoading(browser, jFilm.getString("synopsis_url"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        vidView.start();
        // Activer Vidéo Control
        vidControl = new MediaController(this);
        vidControl.setAnchorView(vidView);
        vidView.setMediaController(vidControl);
        // Chargement des boutons pour les chapitres
        try {
            initChapters();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void readFile() {
        InputStream inputStream = getResources().openRawResource(R.raw.chapters);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        int ctr;
        try {
            ctr = inputStream.read();
            while (ctr != -1) {
                byteArrayOutputStream.write(ctr);
                ctr = inputStream.read();
            }
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            jObject = new JSONObject(byteArrayOutputStream.toString());
        } catch (Exception e) {
        e.printStackTrace();
        }
    }

    private void initChapters() throws JSONException {
            JSONArray jArray = jObject.getJSONArray("Chapters");
            int pos = 0;
            String title = "";
            LinearLayout chapters = (LinearLayout)findViewById(R.id.chapters);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            for (int i = 0; i < jArray.length(); i++) {
                pos = jArray.getJSONObject(i).getInt("pos");
                title = jArray.getJSONObject(i).getString("title");
                Button button = new Button(this);
                // ajoute l'information pour l'endroit où se situer dans la vidéo
                button.setTag(pos);
                button.setText(title);
                button.setLayoutParams(layoutParams);
                button.setOnClickListener(chaptersListener);
                chapters.addView(button);
            }
    }

    private View.OnClickListener chaptersListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            vidView.seekTo((int)v.getTag()*1000);
            JSONArray jArray = null;
            try {
                jArray = jObject.getJSONArray("Keywords");
                for (int i = 0; i < jArray.length(); i++) {
                    if ((int) v.getTag() == jArray.getJSONObject(i).getInt("pos")) {
                        JSONArray data = jArray.getJSONObject(i).getJSONArray("data");
                        myWebViewClient.shouldOverrideUrlLoading(browser, data.getJSONObject(0).getString("url"));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * Classe customisée pour la webView du layout
     */
    private class MyWebViewClient extends WebViewClient {
        /**
         * Méthode qui évite de naviguer sur la WebView comme on le souhaite.
         * Permet d'éviter de naviguer sur d'autre url excepté celles du film dans le fichier chapters.json
         * @param view
         * @param url
         * @return boolean True ou False si nous devons changer l'url courante de la WebView
         */
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }
}
