package com.example.imageviewer;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSION = 1000;

    // 画像リスト
    private final List<Integer> imageIds = new ArrayList<>();
    private final List<String> timeStamps = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 権限の確認
        checkPermission();

        // ストレージの画像一覧取得
        Uri queryUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        List<Bitmap> thumbnailList = new ArrayList<>();

        int idIndex, timeStampIndex;
        ContentResolver contentResolver = getContentResolver();
        try (Cursor cursor = contentResolver.query(
                queryUri,null,null, null,null)) {
            // queryのそれぞれ何番目か
            idIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
            timeStampIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED);

            while (cursor.moveToNext()) {
                // IDをリストに格納
                imageIds.add(cursor.getInt(idIndex));

                // IDからサムネイル取得
                Bitmap thumbnail = MediaStore.Images.Thumbnails.getThumbnail(
                        contentResolver, cursor.getLong(idIndex),
                        MediaStore.Images.Thumbnails.MICRO_KIND, null);
                thumbnailList.add(thumbnail);

                /* Added in API level 29 から */
                /* getThumbnailはdeprecatedだけど普通に使えるのでそのまま使う */
                // Bitmap thumbnail = contentResolver.loadThumbnail(bmpUri, new Size(96, 96), null);

                Date formatDate = new Date(cursor.getInt(timeStampIndex) * 1000L);
                SimpleDateFormat sdf = new SimpleDateFormat(getString(R.string.timestamp_format), Locale.US);
                String formatTime = sdf.format(formatDate);
                timeStamps.add(formatTime);

            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("[DEBUG]", "Exception : " + e);
        }
        RecyclerView recyclerView = findViewById(R.id.recycler_view_image_list);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        RecyclerView.LayoutManager rLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(rLayoutManager);

        // divider 境界線
        RecyclerView.ItemDecoration itemDecoration =
                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(itemDecoration);

        // ImageListAdapter
        ImageListAdapter adapter = new ImageListAdapter(timeStamps, thumbnailList);
        recyclerView.setAdapter(adapter);
        // lambda式。 new View.OnClickListener() を呼んで onClickを Overrideしてる
        adapter.setOnItemClickListener(view -> {
            int line = ((ImageListAdapter) adapter).getLine();
            Intent intentImageDetail = new Intent(MainActivity.this, ImageDetailActivity.class);
            intentImageDetail.putExtra("imageId", imageIds.get(line).toString());
            intentImageDetail.putExtra("timestamp", timeStamps.get(line));
            startActivity(intentImageDetail);
        });
    }

    // 権限の確認
    private void checkPermission() {
        List<String> permissions = new ArrayList<>();
        permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        // API 29 からメディアファイルのexif情報に対して権限が必要
        if(Build.VERSION.SDK_INT >= 29) {
            permissions.add(Manifest.permission.ACCESS_MEDIA_LOCATION);
        }

        List<String> deniedList = new ArrayList<>();
        for (String permission : permissions) {
            if (!(ContextCompat.checkSelfPermission(this, permission)
                    == PackageManager.PERMISSION_GRANTED)) {
                deniedList.add(permission);
            }
        }
        if (deniedList.size() > 0) {
            Log.d("[DEBUG]",  "Denied : " + deniedList);
            ActivityCompat.requestPermissions(
                    this, permissions.toArray(
                            new String[0]), REQUEST_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted. Continue the action or workflow
                // in your app.
                Log.d("[DEBUG]", "Permission is granted.");
                finish();
                startActivity(getIntent());
            } else {
                // Explain to the user that the feature is unavailable because
                // the features requires a permission that the user has denied.
                // At the same time, respect the user's decision. Don't link to
                // system settings in an effort to convince the user to change
                // their decision.
                Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_LONG).show();
                this.finishAndRemoveTask();
            }
        }
        // Other 'case' lines to check for other
        // permissions this app might request.
    }
}
