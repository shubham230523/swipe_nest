package com.shubham.swipenest.story.homeScreen.fragments;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.github.chrisbanes.photoview.PhotoView;
import com.rtugeek.android.colorseekbar.ColorSeekBar;
import com.rtugeek.android.colorseekbar.OnColorChangeListener;
import com.shubham.swipenest.R;
import com.shubham.swipenest.story.homeScreen.fragments.preview.adapters.TypographyAdapter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class PickedMediaPreviewFragment extends Fragment implements View.OnTouchListener, OnClickListener {

    private FragmentPreviewListener listener;

    public static PickedMediaPreviewFragment newInstance(FragmentPreviewListener listener) {
        PickedMediaPreviewFragment fragment = new PickedMediaPreviewFragment();
        fragment.listener = listener;
        return fragment;
    }

    Uri imageUri, videoUri, outputFileUri;
    ImageView btnDeSelect, btnAddText, btnDeleteText;
    PhotoView pickedImage;
    ImageView btnAddToStory;
    VideoView videoView;
    MediaPlayer mediaPlayer;
    Runnable runnable;
    Handler handler;
    TextView tvUserTyped, tvDoneTyping;
    RelativeLayout rlPreviewImg;
    ConstraintLayout parentLayout;
    RecyclerView rvTypography;
    EditText editText;
    SeekBar textSizeSeekbar;
    ColorSeekBar colorSeekBar;
    int textColor = Color.WHITE;
    int textSize = 16;
    Typeface textTypeface;
    int lastSelectedTypographyPosition = 0;
    ImageView.ScaleType currentScaleType;
    Float restoredScale, scaleX , scaleY;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_media_preview, container, false);

        pickedImage = view.findViewById(R.id.ivImgPreview);
        btnDeSelect = view.findViewById(R.id.btnDeselect);
        btnAddToStory = view.findViewById(R.id.btnAddToStory);
        btnDeleteText = view.findViewById(R.id.btnDeleteText);
        videoView = view.findViewById(R.id.VideoPreview);
        btnAddText = view.findViewById(R.id.btnAddText);
        tvDoneTyping = view.findViewById(R.id.tvDoneTypingText);
        //tvDrag = view.findViewById(R.id.tvDrag);
        rlPreviewImg = view.findViewById(R.id.rl_preview_img);
        textSizeSeekbar = view.findViewById(R.id.fontSeekbar);
        textSizeSeekbar.setProgress(textSize);
        colorSeekBar = view.findViewById(R.id.color_seekbar);
        colorSeekBar.setColor(Color.WHITE);
        handler = new Handler();
        textTypeface = ResourcesCompat.getFont(requireContext(), R.font.caprasimo_regular);
        // typography recycler view
        rvTypography = view.findViewById(R.id.rvTypography);
        TypographyAdapter typographyAdapter = new TypographyAdapter(8, requireContext(), this);
        rvTypography.setAdapter(typographyAdapter);
        rvTypography.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));

        parentLayout = view.findViewById(R.id.parent);
        LinearLayout llColorSeekbar = view.findViewById(R.id.llColorSeekbar);

        parentLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                parentLayout.getWindowVisibleDisplayFrame(r);
                int screenHeight = parentLayout.getHeight();
                int keypadHeight = screenHeight - r.bottom-120;

                if (keypadHeight > screenHeight * 0.15) {
                    // Keyboard is visible
                    llColorSeekbar.setPadding(0, 0, 0, keypadHeight);
                    rvTypography.setVisibility(View.VISIBLE);
                    colorSeekBar.setVisibility(View.VISIBLE);
                    llColorSeekbar.setVisibility(View.VISIBLE);
                    pickedImage.setForeground(ContextCompat.getDrawable(requireContext(), R.drawable.black_blur_foreground));
                } else {
                    // Keyboard is hidden
                    llColorSeekbar.setPadding(0, 0, 0, 0);
                    llColorSeekbar.setVisibility(View.GONE);
                    rvTypography.setVisibility(View.GONE);
                    colorSeekBar.setVisibility(View.GONE);
                    pickedImage.setForeground(null);
                }
            }
        });


        if(getArguments()!=null){
            imageUri = getArguments().getParcelable("imageUri");
            videoUri = getArguments().getParcelable("videoUri");
            Log.d("FragmentPreview" , "imageUri got - " + imageUri);
            Log.d("FragmentPreview" , "videouri got - " + videoUri);
            if(imageUri!=null){
                btnAddText.setVisibility(View.VISIBLE);
                videoView.setVisibility(View.GONE);
                pickedImage.setVisibility(View.VISIBLE);
                Glide.with(this)
                        .load(imageUri)
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                Toast.makeText(requireContext(), "Failed to load image.", Toast.LENGTH_SHORT).show();
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                return false;
                            }
                        })
                        .into(pickedImage);
            }
            else if(videoUri!=null){
                pickedImage.setImageAlpha(0);
                Log.d("FragmentPreview", "inside videoUri " + videoUri);
                btnAddText.setVisibility(View.GONE);
                videoView.setVisibility(View.VISIBLE);
                videoView.setVideoURI(videoUri);
                videoView.setOnPreparedListener(mp -> {
                    mediaPlayer = mp;
                    // videoView.seekTo(currentPosition);
                    // play only the first 30 seconds
                    int duration = mediaPlayer.getDuration();
                    int playbackPosition = 30000;
                    if(duration > playbackPosition){
                        mediaPlayer.seekTo(playbackPosition);
                    }

                    videoView.start();

                    // stopping after 30 seconds
                    runnable = () -> {
                        if(videoView.isPlaying()){
                            videoView.stopPlayback();
                        }
                        Log.d("uris" , "skipping the story");
                        //videoView.skip();
                    };
                    handler.postDelayed(runnable, playbackPosition);
                    //videoView = true;
                });
            }
        }
        //tvDrag.setOnTouchListener(this);

        pickedImage.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View view, DragEvent dragEvent) {
                int action = dragEvent.getAction();
                switch (action) {
                    case DragEvent.ACTION_DRAG_STARTED:
                        // Ensure it's the TextView being dragged
                        if (dragEvent.getLocalState() == tvUserTyped) {
                            return true;
                        }
                        return false;

                    case DragEvent.ACTION_DRAG_ENTERED:
                        btnDeleteText.setVisibility(View.VISIBLE);
                        // Highlight the ImageView when TextView is dragged onto it
                        //pickedImage.setColorFilter(Color.LTGRAY);
                        return true;

                    case DragEvent.ACTION_DRAG_EXITED:

                        // Remove the highlight from the ImageView when TextView is dragged off it
                        pickedImage.clearColorFilter();
                        return true;

                    case DragEvent.ACTION_DROP:

                        Log.d("DeleteText" , "text dropped inside image listener");
                        Log.d("DeleteText" , "text drooped on view is " + view.toString());
                        // Handle the drop - update TextView position
                        float x = dragEvent.getX();
                        float y = dragEvent.getY();

                        tvUserTyped.setX(x - tvUserTyped.getWidth() / 2);
                        tvUserTyped.setY(y - tvUserTyped.getHeight() / 2);

                        // Remove the highlight from the ImageView
                        pickedImage.clearColorFilter();
                        tvUserTyped.setVisibility(View.VISIBLE);

                        boolean isDroppedOnDeleteIcon = areViewsOverlapping(btnDeleteText, tvUserTyped);
                        Log.d("DeleteText" , "isDroppedOnDeleteIcon" + isDroppedOnDeleteIcon);

                        if (isDroppedOnDeleteIcon) {
                            // Remove the dragged TextView
                            Log.d("preview" , "before id list remove ");
                            Log.d("preview" , "after id list remove ");
                            ViewGroup parentView = (ViewGroup) tvUserTyped.getParent();
                            parentView.removeView(tvUserTyped);
                            btnDeleteText.setVisibility(View.GONE);
                        }
                        return true;

                    case DragEvent.ACTION_DRAG_ENDED:
                        btnDeleteText.setVisibility(View.GONE);
                        // Remove the highlight from the ImageView when the drag operation ends
                        pickedImage.clearColorFilter();
                        tvUserTyped.setVisibility(View.VISIBLE);
                        return true;

                    default:
                        break;
                }
                return false;
            }
        });

        btnDeSelect.setOnClickListener(v ->{
            listener.onCancelClicked();
        });

        colorSeekBar.setOnColorChangeListener(new OnColorChangeListener() {
            @Override
            public void onColorChangeListener(int progress, int color) {
                textColor = color;
                if(editText!=null){
                    editText.setTextColor(color);
                }
            }
        });

        // adding a edit text first and after user presses enter, we are getting the text from edit text
        // and constructing a text view which can be dragged
        btnAddText.setOnClickListener(v -> {

            // creating the edit text
            editText = new EditText(requireContext());
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
            );
            layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL, R.id.rl_preview_img);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, R.id.rl_preview_img);
            editText.setLayoutParams(layoutParams);
            editText.setPadding(0, 500, 0, 0);
            editText.setImeOptions(EditorInfo.IME_ACTION_DONE);
            editText.setTextSize(textSize);
            editText.setTextColor(textColor);
            editText.setTypeface(textTypeface);
            editText.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.custom_edittext_bg));

            pickedImage.setDrawingCacheEnabled(true);
            Bitmap zoomedBitmap = Bitmap.createBitmap(pickedImage.getDrawingCache());
            rlPreviewImg.addView(editText);
            pickedImage.setDrawingCacheEnabled(false);
            pickedImage.setImageBitmap(zoomedBitmap);

            editText.setFocusableInTouchMode(true);
            editText.requestFocus();
            InputMethodManager inputMethodManager = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);

            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    String enteredText = editable.toString();
                    if(enteredText.isEmpty()){
                        tvDoneTyping.setVisibility(View.GONE);
                        textSizeSeekbar.setVisibility(View.GONE);
                        //rvTypography.setVisibility(View.GONE);
                    }else {
                        tvDoneTyping.setVisibility(View.VISIBLE);
                        textSizeSeekbar.setVisibility(View.VISIBLE);
                        //rvTypography.setVisibility(View.VISIBLE);
                    }
                }
            });

            pickedImage.setForeground(ContextCompat.getDrawable(requireContext(), R.drawable.black_blur_foreground));
        });

        tvDoneTyping.setOnClickListener(v -> {
            pickedImage.setForeground(null);
            if(editText!=null){
                // Get the entered text
                String enteredText = editText.getText().toString();

                // Remove the EditText from the RelativeLayout
                rlPreviewImg.removeView(editText);

                // Create a new TextView
                tvUserTyped = new TextView(requireContext());
                tvUserTyped.setText(enteredText);
                tvUserTyped.setTextSize(textSize);
                tvUserTyped.setTypeface(textTypeface);
                tvUserTyped.setTextColor(textColor);
                int id = View.generateViewId();
                tvUserTyped.setId(id);
                editText = null;

                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT
                );
                layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL, R.id.rl_preview_img);
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, R.id.rl_preview_img);
                tvUserTyped.setLayoutParams(layoutParams);
                layoutParams.setMargins(0, 500, 0, 0);
                //tvUserTyped.setPadding(0, 0, 0, 300);
                tvUserTyped.setFocusable(true);

                // Add the TextView to the RelativeLayout
                rlPreviewImg.addView(tvUserTyped);
                tvUserTyped.setOnTouchListener(PickedMediaPreviewFragment.this);
                tvDoneTyping.setVisibility(View.GONE);
                textSizeSeekbar.setVisibility(View.GONE);
                rvTypography.setVisibility(View.GONE);
            }
        });

        textSizeSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                textSize = i;
                if(editText!=null){
                    editText.setTextSize(i);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        btnAddToStory.setOnClickListener(v -> {
            if(imageUri!=null){

                // we are getting the zoomed image scaleX present inside imageview and then we are creating
                // a new bitmap from the scale and from original height and width which represents the zoomed bitmap
                // this bitmap is stored as a file in external directories of picture and then we are getting its
                // uri which is passed to onUriSelected method
                // the file represented by "imageUri" is a file in cache directory so we are not using the here

                // creating the bitmap
                pickedImage.setDrawingCacheEnabled(true);
                Bitmap zoomedBitmap = Bitmap.createBitmap(pickedImage.getDrawingCache());
                pickedImage.setDrawingCacheEnabled(false);
                int originalImageWidth = pickedImage.getWidth();
                int originalImageHeight = pickedImage.getHeight();

                float zoomScale = pickedImage.getScaleX(); // Assuming the x-axis and y-axis scales are equal
                int zoomedWidth = (int) (pickedImage.getWidth() * zoomScale);
                int zoomedHeight = (int) (pickedImage.getHeight() * zoomScale);

                Bitmap newBitmap = Bitmap.createBitmap(originalImageWidth, originalImageHeight, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(newBitmap);

                int left = (originalImageWidth - zoomedWidth) / 2;
                int top = (originalImageHeight - zoomedHeight) / 2;

                canvas.drawBitmap(zoomedBitmap, left, top, null);
                rlPreviewImg.draw(canvas);

                // creating the file
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                String imageFileName = "IMG_" + timeStamp + "_";

                File storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);

                FileOutputStream outputStream = null;

                try {
                    File zoomedFiled = File.createTempFile(imageFileName, ".jpg", storageDir);
                    outputStream = new FileOutputStream(zoomedFiled);
                    // writing zoomed image bitmap to file
                    newBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    outputStream.flush();
                    Uri uri = FileProvider.getUriForFile(requireContext(), "com.shubham.swipenest.fileprovider", zoomedFiled);
                    // sending the uri back
                    listener.onUriSelected(uri);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (outputStream != null) {
                            outputStream.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            else {
                listener.onUriSelected(videoUri);
            }
        });

        return view;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
            v.startDrag(null, shadowBuilder, v, 0);
            v.setVisibility(View.INVISIBLE);
            return true;
        } else {
            return false;
        }
    }

    private final ActivityResultLauncher<String[]> overlayImagesPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            result -> {
                boolean granted = true;
                for(Boolean per : result.values()){
                    if(!per){
                        granted = false;
                    }
                }
            }
    );

    @Override
    public void onClick(int position, Typeface font) {
        textTypeface = font;
        // adding the typeface to edit text if it is not null
        if(editText!=null){
            editText.setTypeface(font);
        }

        RecyclerView.LayoutManager layoutManager = rvTypography.getLayoutManager();
        View view = layoutManager.findViewByPosition(position);
        View lastSelectedView = layoutManager.findViewByPosition(lastSelectedTypographyPosition);

        // changing the background color of previous selected font to black and text color to white
        if(lastSelectedView != null){
            ImageView bg = lastSelectedView.findViewById(R.id.typography_bg);
            TextView text = lastSelectedView.findViewById(R.id.typography_text);
            if(bg!=null){
                bg.setImageResource(R.color.black);
            }
            if(text!=null){
                text.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
            }
        }

        // changing the bg color of selected font to white and text color to purple
        if(view != null){
            ImageView bg = view.findViewById(R.id.typography_bg);
            TextView text = view.findViewById(R.id.typography_text);
            if(bg!=null){
                bg.setImageResource(R.color.white);
            }
            if(text!=null){
                text.setTextColor(ContextCompat.getColor(requireContext(), R.color.purple_500));
            }
        }

        lastSelectedTypographyPosition = position;
    }

    private boolean areViewsOverlapping(View view1, View view2) {
        int[] view1Location = new int[2];
        view1.getLocationOnScreen(view1Location);
        Rect rect1 = new Rect(view1Location[0], view1Location[1],
                view1Location[0] + view1.getWidth(), view1Location[1] + view1.getHeight());

        int[] view2Location = new int[2];
        view2.getLocationOnScreen(view2Location);
        Rect rect2 = new Rect(view2Location[0], view2Location[1],
                view2Location[0] + view2.getWidth(), view2Location[1] + view2.getHeight());

        return Rect.intersects(rect1, rect2);
    }

    private String getFilePathFromUriByCreatingTempFile(Uri uri, String suffix) throws IOException {
        InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
        File tempFile = File.createTempFile("temp_" , suffix,  requireContext().getCacheDir());
        OutputStream outputStream1 = new FileOutputStream(tempFile);
        byte[] buffer = new byte[8 * 1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream1.write(buffer, 0, bytesRead);
        }
        inputStream.close();
        return tempFile.getAbsolutePath();
    }

    public static String createFileFromUri(Context context, Uri uri) throws IOException {
        // Get the file name from the URI
        String fileName = getFileName(context, uri);

        // Create a file in the external files directory
        File file = new File(context.getExternalFilesDir(null), fileName);

        // Copy the contents of the URI to the file
        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        OutputStream outputStream = new FileOutputStream(file);
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        outputStream.close();
        inputStream.close();

        // Return the path of the created file
        return file.getAbsolutePath();
    }

    private static String getFileName(Context context, Uri uri) {
        String fileName = null;
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            if (nameIndex != -1) {
                fileName = cursor.getString(nameIndex);
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return fileName;
    }

    public static File createVideoFile(Context context) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String videoFileName = "VIDEO_" + timeStamp + getFileExtension();
        File storageDir;
        File videoFile = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentResolver contentResolver = context.getContentResolver();
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES);
            contentValues.put(MediaStore.Video.Media.DISPLAY_NAME, videoFileName);
            contentValues.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");

            Uri videoUri = contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues);
            if (videoUri != null) {
                videoFile = getFileFromUri(context, videoUri);
            }
        } else {
            storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "YourAppName");

            if (!storageDir.exists()) {
                if (!storageDir.mkdirs()) {
                    return null;
                }
            }

            videoFile = new File(storageDir, videoFileName);
        }

        // Scan the media file to make it visible in the gallery app
        if (videoFile != null) {
            MediaScannerConnection.scanFile(context, new String[]{videoFile.getAbsolutePath()}, null, null);
        }

        return videoFile;
    }

    private static File getFileFromUri(Context context, Uri uri) {
        if (uri == null) {
            return null;
        }

        String[] projection = {MediaStore.Video.Media.DATA};
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            String filePath = cursor.getString(columnIndex);
            cursor.close();
            return new File(filePath);
        }
        return null;
    }

    private static String getFileExtension() {
        return ".mp4";
    }

}