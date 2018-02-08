package fr.enssat.berrou_couillard.interfaces_riches;

import android.app.ProgressDialog;
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

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

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
    private MapView mMapView;
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    private static final String JSON_LAT="lat";
    private static final String JSON_LNG="lng";
    private static final String JSON_LABEL="label";
    private static final String JSON_TIMESTAMP="timestamp";

    //    private Point mWayPoint;
    private ProgressDialog mProgress;

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

        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mMapView = findViewById(R.id.mapview);
        mMapView.onCreate(mapViewBundle);

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

        mProgress= new ProgressDialog(this);
        // Chargement des boutons pour les chapitres
        try {
            initChapters();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        initMap();
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

   @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle != null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }
        mMapView.onSaveInstanceState(mapViewBundle);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mMapView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    private void initMap() {
        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                try {
                    long lat = 0;
                    long lng = 0;
                    String label = "";
                    int timestamp = 0;
                    JSONArray mWaypoints = jObject.getJSONArray("Waypoints");
                    for (int i = 0; i < mWaypoints.length(); i++) {
                        lat = mWaypoints.getJSONObject(i).getLong(JSON_LAT);
                        lng = mWaypoints.getJSONObject(i).getLong(JSON_LNG);
                        label = mWaypoints.getJSONObject(i).getString(JSON_LABEL);
                        timestamp = mWaypoints.getJSONObject(i).getInt(JSON_TIMESTAMP);
                        Marker marker = googleMap.addMarker(new MarkerOptions()
                                .position(new LatLng(lat, lng))
                                .title(label));
                        marker.setTag(timestamp);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        int timestamp = (int) marker.getTag();
                        vidView.seekTo(timestamp * 1000);
                        myWebViewClient.shouldOverrideUrlLoading(browser,"https://en.wikipedia.org/wiki/"+marker.getTitle());
                        return false;
                    }
                });
            }
        });
    }

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
            return false;
        }
    }

/*
    private int setProgress() {
        int position = vidView.getCurrentPosition();
        int duration = vidView.getDuration();
        if (mProgress != null) {
            if (duration > 0) {
                // use long to avoid overflow
                long pos = 1000L * position / duration;
                mProgress.setProgress((int) pos);
            }
            int percent = vidView.getBufferPercentage();
            mProgress.setSecondaryProgress(percent * 10);
            return position;
        }
    }



    private final Runnable mShowProgress = new Runnable() { @Override
        public void run() {
            int pos = setProgress();
            if (!mDragging && mShowing && vidView.isPlaying()) {
                postDelayed(mShowProgress, 1000 - (pos % 1000)); }
        }
    };
*/


}
