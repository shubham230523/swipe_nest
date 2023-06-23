package com.shubham.swipenest.story;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Rational;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.*;
import androidx.camera.core.impl.ImageAnalysisConfig;
import androidx.camera.core.impl.ImageCaptureConfig;
import androidx.camera.core.impl.ImageOutputConfig;
import androidx.camera.core.impl.PreviewConfig;
import androidx.camera.core.impl.UseCaseConfig;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.video.MediaStoreOutputOptions;
import androidx.camera.video.Quality;
import androidx.camera.video.QualitySelector;
import androidx.camera.video.Recorder;
import androidx.camera.video.Recording;
import androidx.camera.video.VideoCapture;
import androidx.camera.video.VideoRecordEvent;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;

import java.io.File;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.google.common.util.concurrent.ListenableFuture;
import com.shubham.swipenest.R;
import com.shubham.swipenest.databinding.ActivityCameraBinding;

public class CameraActivity extends AppCompatActivity{

    private ActivityCameraBinding viewBinding;

    private ImageCapture imageCapture;
    private VideoCapture<Recorder> videoCapture;
    private Recording recording;
    private ExecutorService cameraExecutor;
    private CameraSelector currentCameraSelector;
    ProcessCameraProvider cameraProvider;
    Preview preview;
    private CameraControl cameraControl;


    private static final String TAG = "CameraXApp";
    private static final String FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS";
    private static final String[] REQUIRED_PERMISSIONS;

    static {
        List<String> permissionsList = new ArrayList<>();
        permissionsList.add(Manifest.permission.CAMERA);
        permissionsList.add(Manifest.permission.RECORD_AUDIO);

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            permissionsList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        REQUIRED_PERMISSIONS = permissionsList.toArray(new String[0]);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = ActivityCameraBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());


        // Request camera permissions
        if(allPermissionsGranted()){
            startCamera(CameraSelector.DEFAULT_BACK_CAMERA);
        }
        else {
            requestPermissions();
        }

        //setting listener for image capture and video recording button
        findViewById(R.id.image_capture_button).setOnClickListener( v -> {
            takePhoto();
        });

        findViewById(R.id.video_capture_button).setOnClickListener(v -> {
            captureVideo();
        });

        viewBinding.btnRotateCamera.setOnClickListener( v-> {
            switchCamera();
        });

        cameraExecutor = Executors.newSingleThreadExecutor();
    }

    private void startCamera(CameraSelector cameraSelector) {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                // Used to bind the lifecycle of cameras to the lifecycle owner
                cameraProvider = cameraProviderFuture.get();

                // Preview
                preview = new Preview.Builder().build();
                preview.setSurfaceProvider(viewBinding.viewFinder.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder().build();
                Recorder recorder = new Recorder.Builder()
                        .setQualitySelector(QualitySelector.from(Quality.HIGHEST)).build();
                videoCapture = VideoCapture.withOutput(recorder);

                // Select back camera as a default
                currentCameraSelector = cameraSelector;

                // Unbind use cases before rebinding
                cameraProvider.unbindAll();

                // Bind use cases to camera
                 Camera camera  = cameraProvider.bindToLifecycle(this, currentCameraSelector, preview, imageCapture, videoCapture);
                // camera.getCameraControl().setZoomRatio(2.0f);

            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Use case binding failed", e);
            }

        }, ContextCompat.getMainExecutor(this));
    }


    private void takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        Log.d(TAG, "takePhotocalled");
        ImageCapture imageCapture = this.imageCapture;
        if (imageCapture == null) {
            return;
        }

        // Create time stamped name and MediaStore entry
        String name = new SimpleDateFormat(FILENAME_FORMAT, Locale.US)
                .format(System.currentTimeMillis());
        File file = new File(getExternalMediaDirs()[0], name + ".jpg");
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image");
        }

        // Create output options object which contains file + metadata
        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(
                getContentResolver(),
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues)
                .build();

        Log.d(TAG, "before takPicture call");
        // Set up image capture listener, which is triggered after photo has been taken
        imageCapture.takePicture(
                outputOptions,
                cameraExecutor,
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onError(ImageCaptureException exc) {
                        Log.e(TAG, "Photo capture failed: " + exc.getMessage(), exc);
                    }

                    @Override
                    public void onImageSaved(ImageCapture.OutputFileResults output) {
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("CameraActivity", "Media");
                        resultIntent.setData(output.getSavedUri());
                        setResult(RESULT_OK, resultIntent);
                        finish();
                    }
                }
        );
    }


    private void captureVideo() {
        VideoCapture<Recorder> videoCapture = this.videoCapture;
        if (videoCapture == null) {
            return;
        }

        viewBinding.videoCaptureButton.setEnabled(false);

        Recording curRecording = recording;
        if (curRecording != null) {
            // Stop the current recording session.
            curRecording.stop();
            recording = null;
            return;
        }

        // Create and start a new recording session.
        String name = new SimpleDateFormat(FILENAME_FORMAT, Locale.US)
                .format(System.currentTimeMillis());
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            contentValues.put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/CameraX-Video");
        }

        MediaStoreOutputOptions mediaStoreOutputOptions = new MediaStoreOutputOptions
                .Builder(getContentResolver(), MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
                .setContentValues(contentValues)
                .build();

        if (PermissionChecker.checkSelfPermission(CameraActivity.this,
                Manifest.permission.RECORD_AUDIO) ==
                PermissionChecker.PERMISSION_GRANTED
        ) {
            recording = videoCapture.getOutput()
                    .prepareRecording(CameraActivity.this, mediaStoreOutputOptions)
                    .withAudioEnabled()
            .start(ContextCompat.getMainExecutor(CameraActivity.this), recordEvent -> {
                if (recordEvent instanceof VideoRecordEvent.Start) {
                    viewBinding.videoCaptureButton.setText(getString(R.string.stop_capture));
                    viewBinding.videoCaptureButton.setEnabled(true);
                } else if (recordEvent instanceof VideoRecordEvent.Finalize) {
                    VideoRecordEvent.Finalize finalizeEvent = (VideoRecordEvent.Finalize) recordEvent;
                    if (!finalizeEvent.hasError()) {

                        String msg = "Video stored at " +
                                finalizeEvent.getOutputResults().getOutputUri();

                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();

                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("CameraActivity", "Media");
                        resultIntent.setData(finalizeEvent.getOutputResults().getOutputUri());
                        setResult(RESULT_OK, resultIntent);
                        finish();

                    } else {
                        recording.close();
                        recording = null;
                        Log.e(TAG, "Video capture ends with error: " +
                                finalizeEvent.getError());
                    }
                    viewBinding.videoCaptureButton.setText(getString(R.string.start_capture));
                    viewBinding.videoCaptureButton.setEnabled(true);
                }
            });
        }
        else {
            recording = videoCapture.getOutput()
                    .prepareRecording(CameraActivity.this, mediaStoreOutputOptions)
            .start(ContextCompat.getMainExecutor(CameraActivity.this), recordEvent -> {
                if (recordEvent instanceof VideoRecordEvent.Start) {
                    viewBinding.videoCaptureButton.setText(getString(R.string.stop_capture));
                    viewBinding.videoCaptureButton.setEnabled(true);
                } else if (recordEvent instanceof VideoRecordEvent.Finalize) {
                    VideoRecordEvent.Finalize finalizeEvent = (VideoRecordEvent.Finalize) recordEvent;
                    if (!finalizeEvent.hasError()) {
                        String msg = "Video stored at " +
                                finalizeEvent.getOutputResults().getOutputUri();

                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();

                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("CameraActivity", "Media");
                        resultIntent.setData(finalizeEvent.getOutputResults().getOutputUri());
                        setResult(RESULT_OK, resultIntent);
                        finish();
                    } else {
                        recording.close();
                        recording = null;
                        Log.e(TAG, "Video capture ends with error: " +
                                finalizeEvent.getError());
                    }
                    viewBinding.videoCaptureButton.setText(getString(R.string.start_capture));
                    viewBinding.videoCaptureButton.setEnabled(true);
                }
            });
        }
    }

    private void switchCamera() {
        if (currentCameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
            currentCameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;
        } else {
            currentCameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
        }
        startCamera(currentCameraSelector);
    }



    private void requestPermissions() {
        activityResultLauncher.launch(REQUIRED_PERMISSIONS);
    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private final ActivityResultLauncher<String[]> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            new ActivityResultCallback<Map<String, Boolean>>() {
                @Override
                public void onActivityResult(Map<String, Boolean> permissions) {
                    // Handle Permission granted/rejected
                    boolean permissionGranted = true;
                    for (Map.Entry<String, Boolean> entry : permissions.entrySet()) {
                        String permission = entry.getKey();
                        boolean granted = entry.getValue();
                        if (Arrays.asList(REQUIRED_PERMISSIONS).contains(permission) && !granted) {
                            permissionGranted = false;
                            break;
                        }
                    }
                    if (!permissionGranted) {
                        Toast.makeText(CameraActivity.this, "Permission request denied", Toast.LENGTH_SHORT).show();
                    } else {
                        startCamera(CameraSelector.DEFAULT_BACK_CAMERA);
                    }
                }
            });


    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }
}