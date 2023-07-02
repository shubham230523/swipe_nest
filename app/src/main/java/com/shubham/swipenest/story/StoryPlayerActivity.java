package com.shubham.swipenest.story;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.shubham.swipenest.R;
import com.shubham.swipenest.model.Viewers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import jp.shts.android.storiesprogressview.StoriesProgressView;

public class StoryPlayerActivity extends AppCompatActivity implements StoriesProgressView.StoriesListener {

    BottomSheetBehavior<View> bottomSheetBehavior;
    private GestureDetector gestureDetector;

    private List<Uri> MediaUriList = new ArrayList<>();
    public StoryPlayerActivity(List<Uri> list){
        MediaUriList.clear();
        MediaUriList.addAll(list);
    }

    public StoryPlayerActivity() {}

    private final String[] usernames = {"Person", "Person", "Person"};
    private final String[] storyTimes = {"15hr Ago", "8hr Ago", "9hr Ago"};
    private final String[] likeCounts = {"22K", "257", "6.8K"};
    private final String[] storyText = {"Tasty chocolate rolls", "Beautiful bird", "Amazing city"};
    private final List<Boolean> isImageList = new ArrayList<>();
    private Boolean isCallBackPosted = false;
    private int currentPosition = 0;

    long pressTime = 0L;
    long limit = 500L;

    private StoriesProgressView storiesProgressView;
    private ImageView image;
    private VideoView storyVideoView;
    View bottomSheet;
    private MediaPlayer mediaPlayer;
    private Handler handler;
    private Runnable runnable;

    private CircleImageView profileImage;
    private TextView usernameTV;
    private TextView storyTimeTV;
    private TextView likeCountTV;
    private TextView storyTTV;
    private Button btnBottomSheet;
    private ImageView btnStoryClose;
    private List<Viewers> viewsList = new ArrayList<>();
    private int halfExpandedHeight;
    private int screenHeight;

    private int counter = 0;

    private final View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:

                    // on action down when we press our screen
                    // the story will pause for specific time.
                    pressTime = System.currentTimeMillis();

                    // on below line we are pausing our indicator.
                    storiesProgressView.pause();
                    return false;
                case MotionEvent.ACTION_UP:

                    // in action up case when user do not touches
                    // screen this method will skip to next image.
                    long now = System.currentTimeMillis();

                    // on below line we are resuming our progress bar for status.
                    storiesProgressView.resume();

                    // on below line we are returning if the limit < now - presstime
                    return limit < now - pressTime;
            }
            return false;
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_story_player);

        bottomSheet = findViewById(R.id.bottomSheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        screenHeight = getResources().getDisplayMetrics().heightPixels;
        halfExpandedHeight = screenHeight / 2;

        bottomSheetBehavior.setPeekHeight(0);
        //collapseBottomSheet();
//        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        RecyclerView rvViewers = bottomSheet.findViewById(R.id.rvStoryViewers);
        viewsList.add(new Viewers(R.drawable.person, "user1"));
        viewsList.add(new Viewers(R.drawable.person, "user2"));
        viewsList.add(new Viewers(R.drawable.person, "user3"));
        viewsList.add(new Viewers(R.drawable.person, "user4"));
        viewsList.add(new Viewers(R.drawable.person, "user5"));
        viewsList.add(new Viewers(R.drawable.person, "user6"));
        viewsList.add(new Viewers(R.drawable.person, "user7"));
        viewsList.add(new Viewers(R.drawable.person, "user8"));
        viewsList.add(new Viewers(R.drawable.person, "user9"));
        viewsList.add(new Viewers(R.drawable.person, "user10"));
        ViewersAdapter viewsAdapter = new ViewersAdapter(viewsList);
        rvViewers.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        rvViewers.setAdapter(viewsAdapter);

        image = findViewById(R.id.image);
        storyVideoView = findViewById(R.id.story_video_view);
        btnStoryClose = findViewById(R.id.btnStoryClose);
        handler = new Handler();

        btnStoryClose.setOnClickListener(view -> {
            finish();
        });

        int initialWidth = image.getWidth(); // Initial width of the ImageView
        int initialHeight = image.getHeight(); // Initial height of the ImageView
        int targetWidth = 500; // Target width of the ImageView
        int targetHeight = 500; // Target height of the ImageView

        ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
        animator.setDuration(1000);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float fraction = animation.getAnimatedFraction();

                // Interpolate the width and height based on the fraction
                int newWidth = (int) (initialWidth + (targetWidth - initialWidth) * fraction);
                int newHeight = (int) (initialHeight + (targetHeight - initialHeight) * fraction);

                // Update the size of the ImageView
                image.getLayoutParams().width = newWidth;
                image.getLayoutParams().height = newHeight;
                image.requestLayout();
            }
        });

        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if(newState == BottomSheetBehavior.STATE_EXPANDED){
                    storiesProgressView.pause();
                    if(storyVideoView!=null && storyVideoView.isPlaying()){
                        storyVideoView.pause();
                        currentPosition = storyVideoView.getCurrentPosition();
                    }
                }
                else if(newState == BottomSheetBehavior.STATE_COLLAPSED){
                    storiesProgressView.resume();
                    if(storyVideoView!=null && !storyVideoView.isPlaying()){
                        storyVideoView.start();
                    }
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {


            @Override
            public void onShowPress(@NonNull MotionEvent e) {
                storiesProgressView.pause();
                if(storyVideoView!=null && storyVideoView.isPlaying()){
                    storyVideoView.pause();
                }
                super.onShowPress(e);
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                float xCoordinate = e.getX();
                float storyWidth = image.getWidth();
                if(xCoordinate < storyWidth/3){
                    storiesProgressView.reverse();
                    collapseBottomSheet();
//                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
                else if(xCoordinate > 3*storyWidth/4){
                    storiesProgressView.skip();
                    collapseBottomSheet();
//                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
                return super.onSingleTapConfirmed(e);
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                float deltaX = e2.getX() - e1.getX();
                float deltaY = e2.getY() - e1.getY();

                if (Math.abs(deltaX) > Math.abs(deltaY)) {
                    // Horizontal swipe
                    if (deltaX > 0) {
                        // Swipe to the right
                        // Handle going to the previous story
                        return true;
                    } else {
                        // Swipe to the left
                        // Handle going to the next story
                        return true;
                    }
                } else {
                    // Vertical swipe
                    if (deltaY > 0) {
                        // Swipe down
                        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                            // Handle closing the bottom sheet and resuming the story playback
                            collapseBottomSheet();
//                            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                            storiesProgressView.resume();
                            if(storyVideoView!=null && !storyVideoView.isPlaying()){
                                storyVideoView.start();
                            }
                            return true;
                        }
                    } else {
                        // Swipe up
                        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                            // Handle opening the bottom sheet and pausing the story playback
                            expandBottomSheet();
//                            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                            //animator.start();
                            storiesProgressView.pause();
                            if(storyVideoView!=null && storyVideoView.isPlaying()){
                                storyVideoView.pause();
                            }
                            return true;
                        }
                    }
                }

                return false;
            }
        });

        // bottom sheet delete icon
        ImageView btnDeleteStory = bottomSheet.findViewById(R.id.btnDelete);
        btnDeleteStory.setOnClickListener(view -> {
            Intent i = new Intent(StoryPlayerActivity.this, MainActivity.class);
            startActivity(i);
            finish();
        });

        List<Uri> list = getIntent().getParcelableArrayListExtra("uriList");
        MediaUriList.clear();
        MediaUriList.addAll(list);
        Log.d("uris", "uri list size in story player activity " + MediaUriList.size());
        Log.d("uris" , "video uri is " + MediaUriList.get(0));


        long [] durationList = new long[MediaUriList.size()];
        for(int i = 0; i<MediaUriList.size(); i++){
            Uri uri = MediaUriList.get(i);
            String[] uriList = uri.toString().split("\\.");
            String mimeType = this.getContentResolver().getType(uri);
            boolean isVideo = mimeType != null && mimeType.startsWith("video/");
            if(
                    uriList[uriList.length-1].equals("mp4")
                            || uriList[uriList.length-1].equals("avi")
                            || uriList[uriList.length-1].equals("mkv")
                            || uriList[uriList.length-1].equals("mov")
                            || uriList[uriList.length-1].equals("wmv")
                            || uriList[uriList.length-1].equals("flv")
                            || uriList[uriList.length-1].equals("webm") || isVideo
            ){
                isImageList.add(false);
                try {
                    long duration = getVideoDuration(uri);
                    Log.d("uris", "duration from getVideoDurationMethod " + duration);
                    if(duration > 30000){
                        durationList[i] = 30000;
                    }
                    else durationList[i] = duration;
                } catch (IOException e) {
                    e.printStackTrace();
                    durationList[i] = 3000L;
                }
            }
            else {
                isImageList.add(true);
                durationList[i] = 3000L;
            }
        }

        // on below line we are initializing our variables.
        storiesProgressView = (StoriesProgressView) findViewById(R.id.stories);

        // on below line we are setting the total count for our stories.
        storiesProgressView.setStoriesCount(MediaUriList.size());

        for(long i : durationList){
            Log.d("uris" , "duration is " + i);
        }
        // on below line we are setting story duration for each story.
        storiesProgressView.setStoriesCountWithDurations(durationList);

        // on below line we are calling a method for set
        // on story listener and passing context to it.
        storiesProgressView.setStoriesListener(this);

        // below line is use to start stories progress bar.
        storiesProgressView.startStories(counter);

        // initializing our image view.
        image = (ImageView) findViewById(R.id.image);

        profileImage = findViewById(R.id.profile_image);
        usernameTV = findViewById(R.id.usernameTV);
        storyTimeTV = findViewById(R.id.storyTimeTV);

        // on below line we are setting image to our image view.
        setUpStory(MediaUriList.get(counter), isImageList.get(counter), usernames[0],storyTimes[0],likeCounts[0],storyText[0]);

    }

    @Override
    public void onNext() {
        // this method is called when we move
        // to next progress view of story.
        currentPosition = 0;
        if(isCallBackPosted){
            handler.removeCallbacks(runnable);
            isCallBackPosted = false;
        }
       //storyVideoView.stopPlayback();
        setUpStory(MediaUriList.get(++counter), isImageList.get(counter), usernames[0],storyTimes[0],likeCounts[0],storyText[0]);
    }

    @Override
    public void onPrev() {
        currentPosition = 0;
        if(isCallBackPosted){
            handler.removeCallbacks(runnable);
            isCallBackPosted = false;
        }
        // this method id called when we move to previous story.
        // on below line we are decreasing our counter
        if ((counter - 1) < 0) return;
        setUpStory(MediaUriList.get(--counter), isImageList.get(counter),  usernames[0],storyTimes[0],likeCounts[0],storyText[0]);

        // on below line we are setting image to image view
    }

    @Override
    public void onComplete() {
        currentPosition = 0;
        if(isCallBackPosted){
            handler.removeCallbacks(runnable);
            isCallBackPosted = false;
        }
        // when the stories are completed this method is called.
        // in this method we are moving back to initial main activity.
        finish();
    }

    @Override
    protected void onDestroy() {
        if(isCallBackPosted){
            handler.removeCallbacks(runnable);
            isCallBackPosted = false;
        }
        // in on destroy method we are destroying
        // our stories progress view.
        storiesProgressView.destroy();
        super.onDestroy();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_UP && bottomSheetBehavior.getState()!=BottomSheetBehavior.STATE_EXPANDED){
            storiesProgressView.resume();
            if(storyVideoView!=null && !storyVideoView.isPlaying()){
                storyVideoView.start();
            }
        }
        return gestureDetector.onTouchEvent(event);
    }

    private void setUpStory(Uri uri, Boolean isImage, String username, String time, String like, String storyText)
    {

        if(!isImage) {
            Log.d("uris" , " uri check inside video " + uri);
            image.setVisibility(View.GONE);
            storyVideoView.setVisibility(View.VISIBLE);
            storyVideoView.setVideoURI(uri);
            storyVideoView.setOnPreparedListener(mp -> {
                mediaPlayer = mp;
                storyVideoView.seekTo(currentPosition);
                // play only the first 30 seconds
                int duration = mediaPlayer.getDuration();
                int playbackPosition = 30000;
                if(duration > playbackPosition){
                    mediaPlayer.seekTo(playbackPosition);
                }

                storyVideoView.start();

                // stopping after 30 seconds
                runnable = () -> {
                    if(storyVideoView.isPlaying()){
                        storyVideoView.stopPlayback();
                    }
                    Log.d("uris" , "skipping the story");
                    storiesProgressView.skip();
                };
                handler.postDelayed(runnable, playbackPosition);
                isCallBackPosted = true;
            });
        }
        else {
            storyVideoView.setVisibility(View.GONE);
            image.setVisibility(View.VISIBLE);
            Log.d("uris" , "uri check inside image " + uri);
            Glide.with(this)
                    .load(uri)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            Toast.makeText(StoryPlayerActivity.this, "Failed to load image.", Toast.LENGTH_SHORT).show();
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            return false;
                        }
                    })
                    .into(image);
        }
    }

    private Long getVideoDuration(Uri uri) throws IOException {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(getApplicationContext(), uri);

            // Get the duration in milliseconds
            String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);

            // Convert duration from milliseconds to seconds
            long durationInMillis = Long.parseLong(duration);

            return durationInMillis;

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            retriever.release();
        }

        return 0L;
    }

    private void expandBottomSheet() {
        ValueAnimator animator = ValueAnimator.ofInt(0, halfExpandedHeight);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setDuration(300);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                setBottomSheetHeight(value);
            }
        });
        animator.start();
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    private void collapseBottomSheet() {
        ValueAnimator animator = ValueAnimator.ofInt(halfExpandedHeight, 0);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setDuration(300);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                setBottomSheetHeight(value);
            }
        });
        animator.start();
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    private void setBottomSheetHeight(int height) {
        ViewGroup.LayoutParams layoutParams = bottomSheet.getLayoutParams();
        layoutParams.height = height;
        bottomSheet.setLayoutParams(layoutParams);
    }
}