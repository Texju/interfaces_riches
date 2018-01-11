package fr.enssat.berrou_couillard.interfaces_riches;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.VideoView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    private VideoView vidView;
    private MediaController vidControl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Vidéo view
        vidView = (VideoView)findViewById(R.id.videoView);
        // Par défault on met la première vidéo
        vidView.setVideoURI();
        vidView.start();
        // Activer Vidéo Control
        vidControl = new MediaController(this);
        vidControl.setAnchorView(vidView);
        vidView.setMediaController(vidControl);
    }

    private void initChapters(){
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
            JSONObject jObject = new JSONObject(byteArrayOutputStream.toString());
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
                button.setTag(pos);
                button.setText(title);
                button.setLayoutParams(layoutParams);
                button.setOnClickListener(chaptersListener);
                chapters.addView(button);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private View.OnClickListener chaptersListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final int position = (int)v.getTag();
            vidView.seekTo(position);
        }
    };

}
