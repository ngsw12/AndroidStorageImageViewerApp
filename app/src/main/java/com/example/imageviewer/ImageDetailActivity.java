package com.example.imageviewer;

import android.content.ContentUris;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.exifinterface.media.ExifInterface;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

public class ImageDetailActivity extends AppCompatActivity implements OnMapReadyCallback {

    private int imageId;
    private String timestamp;

    double[] latLong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_detail);

        getIntentData();
        ImageView imageView = findViewById(R.id.imageView);
        TextView timestampView = findViewById(R.id.timeStampTextView);
        TextView gpsTextView = findViewById(R.id.gpsTextView);
        Uri uri = ContentUris.withAppendedId(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imageId);
        // 位置情報
        if(Build.VERSION.SDK_INT >= 29) {
            uri = MediaStore.setRequireOriginal(uri);
        }
        try {
            InputStream stream = getContentResolver().openInputStream(uri);
            if (stream != null) {
                ExifInterface exifInterface = new ExifInterface(stream);
                double[] returnedLatLong = exifInterface.getLatLong();
                // If lat/long is null, fall back to the coordinates (0, 0).
                latLong = returnedLatLong != null ? returnedLatLong : new double[2];
                // Don't reuse the stream associated with
                // the instance of "ExifInterface".
                stream.close();
            } else {
                // Failed to load the stream, so return the coordinates (0, 0).
                latLong = new double[2];
            }
        } catch (FileNotFoundException fileNotFoundException) {
            Log.d("[DEBUG]", "fileNotFoundException : " + fileNotFoundException);
        } catch (IOException ioException) {
            Log.d("[DEBUG]", "ioException : " + ioException);
        }
        String latLongInfo = getString(R.string.timestamp_hint)
                + String.format(Locale.US, " : %f, %f", latLong[0], latLong[1]);
        imageView.setImageURI(uri);
        timestampView.setText(timestamp);
        gpsTextView.setText(latLongInfo);

        // map 表示
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_view);
        if(mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    // 一覧からimage ID とtimestampを取得
    void getIntentData() {
        if (getIntent().hasExtra("imageId")) {
            imageId = Integer.parseInt(getIntent().getStringExtra("imageId"));
            timestamp = getIntent().getStringExtra("timestamp");
        }
    }

    // Get a handle to the GoogleMap object and display marker.
    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng location = new LatLng(latLong[0], latLong[1]);
        googleMap.addMarker(new MarkerOptions()
                .position(location)
                .title("Marker"));
        // カメラ位置の移動、ズーム
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(location));
        googleMap.moveCamera(CameraUpdateFactory.zoomTo(17));
    }

}
