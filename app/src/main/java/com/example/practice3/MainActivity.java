package com.example.practice3;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class MainActivity extends AppCompatActivity implements BitmapCallback {

    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;
    EditText edImgUrl;
    Button btnDownload;
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // পরিচয় করিয়ে দেওয়া হয়েছে ।
        edImgUrl = findViewById(R.id.edImgUrl);
        btnDownload = findViewById(R.id.btnDownload);
        imageView = findViewById(R.id.imageView);

        // button এর onClick লেখা হয়েছে ।
        btnDownload.setOnClickListener(v -> {
            String imgUrl = edImgUrl.getText().toString();
            if (!imgUrl.isEmpty()) {
                if (isStoragePermissionGranted()) {
                    ImageDownloader downloader = new ImageDownloader(MainActivity.this, this);
                    downloader.execute(imgUrl);
                }
            } else {
                edImgUrl.setError("Enter Url");
            }

        });

    } // onCreate method end here ============

    @Override
    public void onBitmapDownloaded(Bitmap bitmap) {
        if (isStoragePermissionGranted()) {
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
                saveImage(bitmap);
            }
        }
    }

    private boolean isStoragePermissionGranted() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            int READ_MEDIA_IMAGES = ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_IMAGES);
            int POST_NOTIFICATIONS = ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS);

            List<String> listPermissionsNeeded = new ArrayList<>();
            if (READ_MEDIA_IMAGES != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(android.Manifest.permission.READ_MEDIA_IMAGES);
            }

            if (POST_NOTIFICATIONS != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(android.Manifest.permission.POST_NOTIFICATIONS);
            }

            if (!listPermissionsNeeded.isEmpty()) {
                ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
                return false;
            }

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int WRITE_EXTERNAL_STORAGE = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);

            List<String> listPermissionsNeeded = new ArrayList<>();
            if (WRITE_EXTERNAL_STORAGE != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }

            if (!listPermissionsNeeded.isEmpty()) {
                ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
                return false;
            }
        }

        return true;

    } // isStoragePermissionGranted end here ==================

    private void saveImage(Bitmap finalBitmap) {
        String folderName = "YT Thumbnail";
        String root = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES).toString();

        File myDir = new File(root + "/" + folderName);
        myDir.mkdirs();

        Random generator = new Random();

        int n = 10000;
        n = generator.nextInt(n);
        String fname = "Image-" + n + ".jpg";
        File file = new File(myDir, fname);
        if (file.exists()) file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
            Toast.makeText(getApplicationContext(), "Download Done!", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Log.d("storageException", e.toString());
            e.printStackTrace();
        }
        // Tell the media scanner about the new file so that it is
        // immediately available to the user.
        MediaScannerConnection.scanFile(this, new String[]{file.toString()}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("ExternalStorage", "Scanned " + path + ":");
                        Log.i("ExternalStorage", "-> uri=" + uri);
                    }
                });

    } // saveImage end here ============

} // public class end here ==================