package com.shubham.swipenest.story.homeScreen.fragments;

import android.graphics.drawable.Drawable;
import android.media.Image;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
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
import com.shubham.swipenest.story.StoryPlayerActivity;

public class PickedMediaPreviewFragment extends Fragment {

    private FragmentPreviewListener listener;

    public static PickedMediaPreviewFragment newInstance(FragmentPreviewListener listener) {
        PickedMediaPreviewFragment fragment = new PickedMediaPreviewFragment();
        fragment.listener = listener;
        return fragment;
    }

    Uri imageUri, videoUri;
    ImageView pickedImage;
    ImageView btnDeSelect;
    ImageView btnAddToStory;
    VideoView videoView;
    MediaPlayer mediaPlayer;
    Runnable runnable;
    Handler handler;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_media_preview, container, false);

        pickedImage = view.findViewById(R.id.ivImgPreview);
        btnDeSelect = view.findViewById(R.id.btnDeselect);
        btnAddToStory = view.findViewById(R.id.btnAddToStory);
        videoView = view.findViewById(R.id.VideoPreview);
        handler = new Handler();

        if(getArguments()!=null){
            imageUri = getArguments().getParcelable("imageUri");
            videoUri = getArguments().getParcelable("videoUri");
            Log.d("FragmentPreview" , "imageUri got - " + imageUri);
            Log.d("FragmentPreview" , "videouri got - " + videoUri);
            if(imageUri!=null){
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
                pickedImage.setVisibility(View.GONE);
                videoView.setVisibility(View.VISIBLE);
                videoView.setVideoURI(videoUri);
                videoView.start();

                videoView.setOnPreparedListener( mp -> {
                    mediaPlayer = mp;
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
                    };
                    handler.postDelayed(runnable, playbackPosition);
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
        return view;
    }
}
