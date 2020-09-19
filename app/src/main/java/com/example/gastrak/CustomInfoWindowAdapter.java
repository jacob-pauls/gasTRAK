package com.example.gastrak;

// Jacob Pauls
// 300273666
// File Name: CustomInfoWindowAdapter.java

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter{

    private final View window;
    private Context currentContext;

    public CustomInfoWindowAdapter(Context context) {
        currentContext = context;
        window = LayoutInflater.from(context).inflate(R.layout.custom_info_window, null);
    }

    // setting text to the view
    private void loadText(Marker marker, View view) {
        // hold title values
        String title = marker.getTitle();
        TextView infoTitle = (TextView) view.findViewById(R.id.title);

        // set the heading if not null
        if (!title.equals("")) {
            infoTitle.setText(title);
        }

        // hold snippet values
        String snippet = marker.getSnippet();
        TextView infoSnippet = (TextView) view.findViewById(R.id.snippet);

        // set the snippet if not null
        if (!snippet.equals("")) {
            infoSnippet.setText(snippet);
            // add DB prices here (maybe?)
            //infoSnippet.append("APPENDED");
        }
    }

    @Override
    public View getInfoWindow(Marker marker) {
        loadText(marker, window);
        return window;
    }

    @Override
    public View getInfoContents(Marker marker) {
        loadText(marker, window);
        return window;
    }
}
