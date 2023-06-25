package com.shubham.swipenest.story;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Rational;
import android.util.Size;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
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

import com.google.android.material.progressindicator.CircularProgressIndicator;
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
    private Boolean isVideoRecordingStarted = false;
    private CircularProgressIndicator circularProgressIndicator;
    private CountDownTimer countDownTimer;


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

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = ActivityCameraBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());

        circularProgressIndicator = viewBinding.videoProgress;

        // Request camera permissions
        if(allPermissionsGranted()){
            startCamera(CameraSelector.DEFAULT_BACK_CAMERA);
        }
        else {
            requestPermissions();
        }


        //setting listener for image capture and video recording
        viewBinding.captureMedia.setOnClickListener( v -> {
            takePhoto();
        });

        // if the user is holding the capture button then video recording is started
        viewBinding.captureMedia.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                isVideoRecordingStarted = true;
                viewBinding.captureMedia.setImageResource(R.drawable.ic_camera_capture);
                circularProgressIndicator.setTrackColor(ContextCompat.getColor(CameraActivity.this, R.color.darkYello));
                captureVideo();
                startCountdownTimer();
                return true;
            }
        });

        // on touch listener is added to detect user capture button release action
        // here if the isVideoRecordingStarted boolean is set then it means video was started and we have
        // to stop it and save it
        viewBinding.captureMedia.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_UP && isVideoRecordingStarted){
                    captureVideo();
                    circularProgressIndicator.setTrackColor(ContextCompat.getColor(CameraActivity.this, R.color.transparentBlack));
                    viewBinding.captureMedia.setBackgroundResource(R.drawable.ic_default_camera_capture);
                    circularProgressIndicator.setProgress(0);
                    if(countDownTimer!=null){
                        countDownTimer.cancel();
                    }
                    isVideoRecordingStarted = false;
                }
                return false;
            }
        });

        viewBinding.btnRotateCamera.setOnClickListener( v-> {
            switchCamera();
        });

        cameraExecutor = Executors.newSingleThreadExecutor();
    }

    private void startCountdownTimer() {
        countDownTimer = new CountDownTimer(30000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // Calculate the progress value based on the remaining time
                int progress = (int) (((30000 - millisUntilFinished) / 30000.0) * 100);

                // Update the progress indicator
                circularProgressIndicator.setProgressCompat(progress, true);
            }

            @Override
            public void onFinish() {
                captureVideo();
                circularProgressIndicator.setProgress(0);
                viewBinding.captureMedia.setBackgroundResource(R.drawable.ic_default_camera_capture);
                circularProgressIndicator.setTrackColor(ContextCompat.getColor(CameraActivity.this, R.color.transparentBlack));
                // Countdown timer finished, handle any desired actions
            }
        };

        countDownTimer.start();
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

                imageCapture = new ImageCapture.Builder()
                        .setTargetResolution(new Size(1080, 1920)).build();
                Recorder recorder = new Recorder.Builder()
                        .setQualitySelector(QualitySelector.from(Quality.FHD)).build();
                videoCapture = VideoCapture.withOutput(recorder);

                // Select back camera as a default
                currentCameraSelector = cameraSelector;

                // Unbind use cases before rebinding
                cameraProvider.unbindAll();

                // Bind use cases to camera
                 cameraProvider.bindToLifecycle(this, currentCameraSelector, preview, imageCapture, videoCapture);

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
//                    viewBinding.videoCaptureButton.setText(getString(R.string.stop_capture));
//                    viewBinding.videoCaptureButton.setEnabled(true);
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
//                    viewBinding.videoCaptureButton.setText(getString(R.string.start_capture));
//                    viewBinding.videoCaptureButton.setEnabled(true);
                }
            });
        }
        else {
            recording = videoCapture.getOutput()
                    .prepareRecording(CameraActivity.this, mediaStoreOutputOptions)
            .start(ContextCompat.getMainExecutor(CameraActivity.this), recordEvent -> {
                if (recordEvent instanceof VideoRecordEvent.Start) {
//                    viewBinding.videoCaptureButton.setText(getString(R.string.stop_capture));
//                    viewBinding.videoCaptureButton.setEnabled(true);
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
//                    viewBinding.videoCaptureButton.setText(getString(R.string.start_capture));
//                    viewBinding.videoCaptureButton.setEnabled(true);
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