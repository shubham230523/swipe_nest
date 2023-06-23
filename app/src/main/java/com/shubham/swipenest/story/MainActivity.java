package com.shubham.swipenest.story;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
//
//import com.lassi.common.utils.KeyUtils;
//import com.lassi.data.media.MiMedia;
//import com.lassi.domain.media.LassiOption;
//import com.lassi.domain.media.MediaType;
//import com.lassi.presentation.builder.Lassi;
//import com.lassi.presentation.cropper.CropImageView;

import com.shubham.swipenest.story.homeScreen.fragments.FragmentPreviewListener;
import com.shubham.swipenest.story.homeScreen.fragments.HomeScreenStoriesFragment;
import com.shubham.swipenest.story.homeScreen.fragments.PickedMediaPreviewFragment;
import com.shubham.swipenest.utils.OnClickListener;
import com.shubham.swipenest.R;
import com.shubham.swipenest.utils.StreamUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements OnClickListener, FragmentPreviewListener {

    RecyclerView storyViewRV;
    ImageView plusIcon;
    String[] usernameList = {"Shubham","Omkar","Vikas","Akash","Tushar"};
    String[] mimeTypes = {"image/*", "video/*"};
    List<Uri> selectedMediaUris = new ArrayList<>();
    Intent intent;
    String[] permissions;
    File imageFile;
    File videoFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        HomeScreenStoriesFragment fragment = HomeScreenStoriesFragment.newInstance(this);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment, "storiesFragment")
                .addToBackStack("storiesFragment")
                .commit();

//        storyViewRV = findViewById(R.id.storyViewRV);
//        storyViewRV.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));
//        storyViewRV.setAdapter(new StoryViewAdapter(usernameList, this));

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
                    else if(imageFile!=null){
                        Uri uri = FileProvider.getUriForFile(MainActivity.this, "com.shubham.swipenest.fileprovider", imageFile);
                        selectedMediaUris.add(uri);
                    }
                    else if(videoFile!=null){
                        Uri uri = FileProvider.getUriForFile(MainActivity.this, "com.shubham.swipenest.fileprovider", videoFile);
                        selectedMediaUris.add(uri);
                    }
                }
            }
    );

    private final ActivityResultLauncher<String[]> permissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            result -> {
                Boolean cameraPermissionGranted = result.get(Manifest.permission.CAMERA);
                if(cameraPermissionGranted!=null && cameraPermissionGranted){
                    receiveData.launch(intent);
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

    @Override
    public void onClick(int position, StoryViewAdapter.StoryViewHolder viewHolder) {
        if(position == 0){
            if(selectedMediaUris.isEmpty()){
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Select a option");
                View customView = LayoutInflater.from(this).inflate(R.layout.story_alert_dialog, null);
                builder.setView(customView);

                AlertDialog dialog = builder.create();
                dialog.show();

                customView.findViewById(R.id.llCaptureImage).setOnClickListener( v -> {
                    intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    Uri uri = null;
                    try {
                        imageFile = createImageFile();
                        uri = FileProvider.getUriForFile(MainActivity.this, "com.shubham.swipenest.fileprovider", imageFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                    if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED){
                        permissionLauncher.launch(new String[]{Manifest.permission.CAMERA});
                    }
                    else receiveData.launch(intent);
                    viewHolder.plusIcon.setVisibility(View.INVISIBLE);
                    dialog.dismiss();
                });

                customView.findViewById(R.id.llSelectMedia).setOnClickListener( v -> {
                    intent = new Intent(Intent.ACTION_PICK);
                    intent.setType("image/* video/*");
                    receiveData.launch(intent);
                    Toast.makeText(this, "Select image clicked", Toast.LENGTH_SHORT).show();
                    viewHolder.plusIcon.setVisibility(View.INVISIBLE);
                    dialog.dismiss();
                });
            }
            else {
                Intent intent = new Intent(getApplicationContext(), StoryPlayerActivity.class);
                intent.putParcelableArrayListExtra("uriList", new ArrayList<>(selectedMediaUris));
                startActivity(intent);
            }
        }
    }
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    private File createVideoFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "VIDEO_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_MOVIES);
        return File.createTempFile(imageFileName, ".mp4", storageDir);
    }

    @Override
    public void onUriSelected(Uri uri) {
        getSupportFragmentManager().popBackStack();
        HomeScreenStoriesFragment fragment = (HomeScreenStoriesFragment) getSupportFragmentManager().findFragmentByTag("storiesFragment");
        if (fragment != null) {
            fragment.addToUriList(uri);
        }
    }

    @Override
    public void onReplaceFragmentRequest(Uri uri, Boolean isVideo) {
        PickedMediaPreviewFragment fragment = PickedMediaPreviewFragment.newInstance(this);
        Bundle bundle = new Bundle();
        if(isVideo){
            bundle.putParcelable("videoUri", uri);
        }
        else  {
            bundle.putParcelable("imageUri" , uri);
        }
        fragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment, "previewFragment")
                .addToBackStack("previewFragment")
                .commit();
    }

    @Override
    public void onCancelClicked() {
        getSupportFragmentManager().popBackStack();
    }
}