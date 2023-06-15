package com.shubham.swipenest;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
//
//import com.lassi.common.utils.KeyUtils;
//import com.lassi.data.media.MiMedia;
//import com.lassi.domain.media.LassiOption;
//import com.lassi.domain.media.MediaType;
//import com.lassi.presentation.builder.Lassi;
//import com.lassi.presentation.cropper.CropImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import pl.aprilapps.easyphotopicker.ChooserType;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;
import pl.aprilapps.easyphotopicker.MediaFile;
import pl.aprilapps.easyphotopicker.MediaSource;

public class MainActivity extends AppCompatActivity {

    RecyclerView storyViewRV;
    String[] usernameList = {"Shubham","Omkar","Vikas","Akash","Tushar"};
    String[] mimeTypes = {"image/*", "video/*"};
    List<Uri> selectedMediaUris = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        storyViewRV = findViewById(R.id.storyViewRV);
        storyViewRV.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));
        storyViewRV.setAdapter(new StoryViewAdapter(usernameList));

        // self story
        View selfStoryView = findViewById(R.id.self_story);
        ImageView plusIcon = selfStoryView.findViewById(R.id.imageView);
        plusIcon.setVisibility(View.VISIBLE);
        selfStoryView.findViewById(R.id.frameLayout).setOnClickListener(view -> {
            if(plusIcon.getVisibility() == View.VISIBLE){
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*"); // Set the general type to allow any file type
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes); // Specify the allowed MIME types
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                receiveData.launch(intent);
                plusIcon.setVisibility(View.INVISIBLE);
            }
            else {
                Intent intent = new Intent(view.getContext(), StoryPlayerActivity.class);
                intent.putParcelableArrayListExtra("uriList", new ArrayList<>(selectedMediaUris));
                startActivity(intent);
            }
        });
    }

    private final ActivityResultLauncher<Intent> receiveData = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    selectedMediaUris.clear();
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Intent intent = result.getData();
                        ClipData clipData = intent.getClipData();
                        if (clipData != null) {
                            // Multiple items selected
                            int count = clipData.getItemCount();
                            for (int i = 0; i < count; i++) {
                                Uri mediaUri = clipData.getItemAt(i).getUri();
                                try {
                                    ParcelFileDescriptor parcelFileDescriptor = getContentResolver().openFileDescriptor(mediaUri, "r", null);
                                    if(parcelFileDescriptor!=null){
                                        FileInputStream inputStream = new FileInputStream(parcelFileDescriptor.getFileDescriptor());
                                        String fileName = getFileNameFromUri(MainActivity.this, mediaUri);
                                        File file = new File(MainActivity.this.getCacheDir(), fileName);
                                        FileOutputStream outputStream = new FileOutputStream(file);
                                        StreamUtils.copyStream(inputStream, outputStream);
                                        parcelFileDescriptor.close();
                                        Uri uri = FileProvider.getUriForFile(MainActivity.this, "com.shubham.swipenest.fileprovider", file);
                                        selectedMediaUris.add(uri);
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            Log.d("uris", selectedMediaUris.toString());
                        } else if (intent.getData() != null) {
                            // Single item selected
                            Uri mediaUri = intent.getData();
                            try {
                                ParcelFileDescriptor parcelFileDescriptor = getContentResolver().openFileDescriptor(mediaUri, "r", null);
                                if(parcelFileDescriptor!=null){
                                    FileInputStream inputStream = new FileInputStream(parcelFileDescriptor.getFileDescriptor());
                                    String fileName = getFileNameFromUri(MainActivity.this, mediaUri);
                                    File file = new File(MainActivity.this.getCacheDir(), fileName);
                                    FileOutputStream outputStream = new FileOutputStream(file);
                                    StreamUtils.copyStream(inputStream, outputStream);
                                    parcelFileDescriptor.close();
                                    Uri uri = FileProvider.getUriForFile(MainActivity.this, "com.shubham.swipenest.fileprovider", file);
                                    selectedMediaUris.add(uri);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
    );

    public static String getFileNameFromUri(Context context, Uri uri) {
        String fileName = null;
        if (uri.getScheme().equals("content")) {
            ContentResolver contentResolver = context.getContentResolver();
            Cursor cursor = contentResolver.query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (nameIndex != -1) {
                    fileName = cursor.getString(nameIndex);
                }
                cursor.close();
            }
        } else if (uri.getScheme().equals("file")) {
            fileName = uri.getLastPathSegment();
        }
        return fileName;
    }
}