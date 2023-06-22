package com.shubham.swipenest.story.homeScreen.fragments;

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
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.shubham.swipenest.R;
import com.shubham.swipenest.story.MainActivity;
import com.shubham.swipenest.story.StoryPlayerActivity;
import com.shubham.swipenest.story.StoryViewAdapter;
import com.shubham.swipenest.utils.OnClickListener;
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

public class HomeScreenStoriesFragment extends Fragment implements OnClickListener {

    RecyclerView storyViewRV;
    ImageView addStoryIcon;
    String[] usernameList = {"Shubham","Omkar","Vikas","Akash","Tushar"};
    String[] mimeTypes = {"image/*", "video/*"};
    List<Uri> selectedMediaUris = new ArrayList<>();
    Intent intent;
    String[] permissions;
    File imageFile;
    File videoFile;
    FragmentPreviewListener listener;


    public static HomeScreenStoriesFragment newInstance(FragmentPreviewListener listener){
        HomeScreenStoriesFragment fragment = new HomeScreenStoriesFragment();
        fragment.listener = listener;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stories, container, false);
        storyViewRV = view.findViewById(R.id.storyViewRV);
        storyViewRV.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false));
        storyViewRV.setAdapter(new StoryViewAdapter(usernameList, this));
        addStoryIcon = view.findViewById(R.id.icAddStory);

        addStoryIcon.setOnClickListener( v1 -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Select a option");
            View customView = LayoutInflater.from(requireContext()).inflate(R.layout.story_alert_dialog, null);
            builder.setView(customView);

            AlertDialog dialog = builder.create();
            dialog.show();

            customView.findViewById(R.id.llCaptureImage).setOnClickListener( v -> {
                intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                //intent.addFlags(Intent.FLAG_FULLSCREEN);
                Uri uri = null;
                try {
                    imageFile = createImageFile();
                    uri = FileProvider.getUriForFile(requireContext(), "com.shubham.swipenest.fileprovider", imageFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                if(ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
                    permissionLauncher.launch(new String[]{Manifest.permission.CAMERA});
                }
                else receiveData.launch(intent);
                dialog.dismiss();
            });

            customView.findViewById(R.id.llRecordVideo).setOnClickListener( v -> {
                intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                if(intent.resolveActivity(requireContext().getPackageManager()) != null){
                    Uri uri = null;
                    try {
                        videoFile = createVideoFile();
                        uri = FileProvider.getUriForFile(requireContext(), "com.shubham.swipenest.fileprovider", videoFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 30);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                    if(ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED){
                        permissionLauncher.launch(new String[]{Manifest.permission.CAMERA});
                    }
                    else {
                        receiveData.launch(intent);
                    }
                }else Toast.makeText(requireContext(), "No camera app found", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });

            customView.findViewById(R.id.llSelectMedia).setOnClickListener( v -> {
                //Uri initialFolderUri = Uri.parse("file:///storage/emulated/0/MyFolder");
                intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/* video/*");
                //intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, initialFolderUri);
                receiveData.launch(intent);
                Toast.makeText(requireContext(), "Select image clicked", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });
        });
        return view;
    }

    @Override
    public void onClick(int position, StoryViewAdapter.StoryViewHolder viewHolder) {
        if(position == 0 && !selectedMediaUris.isEmpty()){
            Intent intent = new Intent(requireContext(), StoryPlayerActivity.class);
            intent.putParcelableArrayListExtra("uriList", new ArrayList<>(selectedMediaUris));
            startActivity(intent);
        }
    }

    private final ActivityResultLauncher<Intent> receiveData = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    selectedMediaUris.clear();
                    if(result.getResultCode() == Activity.RESULT_OK && imageFile!=null){
                        Uri uri = FileProvider.getUriForFile(requireContext(), "com.shubham.swipenest.fileprovider", imageFile);
                        selectedMediaUris.add(uri);
                    }
                    else if(result.getResultCode() == Activity.RESULT_OK && videoFile!=null){
                        Uri uri = FileProvider.getUriForFile(requireContext(), "com.shubham.swipenest.fileprovider", videoFile);
                        selectedMediaUris.add(uri);
                    }
                    else if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Intent intent = result.getData();
                        ClipData clipData = intent.getClipData();
                        if (clipData != null && clipData.getItemCount()>1) {
                            // Multiple items selected
                            int count = clipData.getItemCount();
                            for (int i = 0; i < count; i++) {
                                Uri mediaUri = clipData.getItemAt(i).getUri();
                                try {
                                    ParcelFileDescriptor parcelFileDescriptor = requireContext().getContentResolver().openFileDescriptor(mediaUri, "r", null);
                                    if(parcelFileDescriptor!=null){
                                        FileInputStream inputStream = new FileInputStream(parcelFileDescriptor.getFileDescriptor());
                                        String fileName = getFileNameFromUri(requireContext(), mediaUri);
                                        File file = new File(requireContext().getCacheDir(), fileName);
                                        FileOutputStream outputStream = new FileOutputStream(file);
                                        StreamUtils.copyStream(inputStream, outputStream);
                                        parcelFileDescriptor.close();
                                        Uri uri = FileProvider.getUriForFile(requireContext(), "com.shubham.swipenest.fileprovider", file);
                                        selectedMediaUris.add(uri);
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        else if (intent.getData() != null) {
                            // Single item selected
                            Uri mediaUri = intent.getData();

                            try {
                                ParcelFileDescriptor parcelFileDescriptor = requireContext().getContentResolver().openFileDescriptor(mediaUri, "r", null);
                                if(parcelFileDescriptor!=null){
                                    FileInputStream inputStream = new FileInputStream(parcelFileDescriptor.getFileDescriptor());
                                    String fileName = getFileNameFromUri(requireContext(), mediaUri);
                                    File file = new File(requireContext().getCacheDir(), fileName);
                                    FileOutputStream outputStream = new FileOutputStream(file);
                                    StreamUtils.copyStream(inputStream, outputStream);
                                    parcelFileDescriptor.close();
                                    Uri uri = FileProvider.getUriForFile(requireContext(), "com.shubham.swipenest.fileprovider", file);
                                    //selectedMediaUris.add(uri);
                                    String mimeType = requireContext().getContentResolver().getType(uri);
                                    boolean isVideo = mimeType != null && mimeType.startsWith("video/");
                                    Log.d("isVIdeo " , "is video " + isVideo);
                                    Log.d("isVIdeo " , "memeType  " + mimeType);
                                    listener.onReplaceFragmentRequest(uri, isVideo);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
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

    public void addToUriList(Uri uri){
        selectedMediaUris.add(uri);
    }
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";
        File storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    private File createVideoFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "VIDEO_" + timeStamp + "_";
        File storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_MOVIES);
        return File.createTempFile(imageFileName, ".mp4", storageDir);
    }

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
