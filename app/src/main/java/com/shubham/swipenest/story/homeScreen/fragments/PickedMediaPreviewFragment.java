package com.shubham.swipenest.story.homeScreen.fragments;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.DragEvent;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.shubham.swipenest.R;
import com.shubham.swipenest.story.MainActivity;
import com.shubham.swipenest.story.StoryPlayerActivity;

public class PickedMediaPreviewFragment extends Fragment implements View.OnTouchListener{

    private FragmentPreviewListener listener;

    public static PickedMediaPreviewFragment newInstance(FragmentPreviewListener listener) {
        PickedMediaPreviewFragment fragment = new PickedMediaPreviewFragment();
        fragment.listener = listener;
        return fragment;
    }

    Uri imageUri, videoUri;
    ImageView pickedImage;
    ImageView btnDeSelect, btnAddText;
    ImageView btnAddToStory;
    VideoView videoView;
    MediaPlayer mediaPlayer;
    Runnable runnable;
    Handler handler;
    TextView tvDrag, tvUserTyped, tvDoneTyping;
    RelativeLayout rlPreviewImg;
    private int offsetX, offsetY;
    EditText editText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_media_preview, container, false);

        pickedImage = view.findViewById(R.id.ivImgPreview);
        btnDeSelect = view.findViewById(R.id.btnDeselect);
        btnAddToStory = view.findViewById(R.id.btnAddToStory);
        videoView = view.findViewById(R.id.VideoPreview);
        btnAddText = view.findViewById(R.id.btnAddText);
        tvDoneTyping = view.findViewById(R.id.tvDoneTypingText);
        //tvDrag = view.findViewById(R.id.tvDrag);
        rlPreviewImg = view.findViewById(R.id.rl_preview_img);

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
                        // Highlight the ImageView when TextView is dragged onto it
                        //pickedImage.setColorFilter(Color.LTGRAY);
                        return true;

                    case DragEvent.ACTION_DRAG_EXITED:
                        // Remove the highlight from the ImageView when TextView is dragged off it
                        pickedImage.clearColorFilter();
                        return true;

                    case DragEvent.ACTION_DROP:
                        // Handle the drop - update TextView position
                        float x = dragEvent.getX();
                        float y = dragEvent.getY();

                        tvUserTyped.setX(x - tvUserTyped.getWidth() / 2);
                        tvUserTyped.setY(y - tvUserTyped.getHeight() / 2);

                        // Remove the highlight from the ImageView
                        pickedImage.clearColorFilter();
                        tvUserTyped.setVisibility(View.VISIBLE);
                        return true;

                    case DragEvent.ACTION_DRAG_ENDED:
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

        handler = new Handler();

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
                Log.d("FragmentPreview", "inside videoUri " + videoUri);

                pickedImage.setVisibility(View.GONE);
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

        btnDeSelect.setOnClickListener(v ->{
            listener.onCancelClicked();
        });

        btnAddToStory.setOnClickListener(v -> {
            if(imageUri!=null){
                listener.onUriSelected(imageUri);
            }
            else listener.onUriSelected(videoUri);
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
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, R.id.rl_preview_img);
            editText.setLayoutParams(layoutParams);
            editText.setImeOptions(EditorInfo.IME_ACTION_DONE);
            rlPreviewImg.addView(editText);
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
                    }else {
                        tvDoneTyping.setVisibility(View.VISIBLE);
                    }
                }
            });
        });

        tvDoneTyping.setOnClickListener(v -> {
            if(editText!=null){
                // Get the entered text
                String enteredText = editText.getText().toString();

                // Remove the EditText from the RelativeLayout
                rlPreviewImg.removeView(editText);

                // Create a new TextView
                tvUserTyped = new TextView(requireContext());
                tvUserTyped.setText(enteredText);

                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT
                );
                layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, R.id.rl_preview_img);

                tvUserTyped.setLayoutParams(layoutParams);
                tvUserTyped.setFocusable(true);
                tvUserTyped.setTypeface(Typeface.DEFAULT_BOLD);
                //tvUserTyped.setFocusedByDefault(true);

                // Add the TextView to the RelativeLayout
                rlPreviewImg.addView(tvUserTyped);
                tvUserTyped.setOnTouchListener(PickedMediaPreviewFragment.this);
                tvDoneTyping.setVisibility(View.GONE);
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
}
