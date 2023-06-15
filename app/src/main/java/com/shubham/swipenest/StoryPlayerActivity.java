package com.shubham.swipenest;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import jp.shts.android.storiesprogressview.StoriesProgressView;

public class StoryPlayerActivity extends AppCompatActivity implements StoriesProgressView.StoriesListener {

//    private final int[] ImageURls = {
//            R.drawable.chocolates,
//            R.drawable.bird,
//            R.drawable.football
//    };

    BottomSheetBehavior<View> bottomSheetBehavior;

    private List<Uri> ImageURls = new ArrayList<>();
    public StoryPlayerActivity(List<Uri> list){
        ImageURls.clear();
        ImageURls.addAll(list);
    }

    public StoryPlayerActivity() {}

    private final String[] usernames = {"Person", "Person", "Person"};
    private final String[] storyTimes = {"15hr Ago", "8hr Ago", "9hr Ago"};
    private final String[] likeCounts = {"22K", "257", "6.8K"};
    private final String[] storyText = {"Tasty chocolate rolls", "Beautiful bird", "Amazing city"};

    long pressTime = 0L;
    long limit = 500L;

    private StoriesProgressView storiesProgressView;
    private ImageView image;

    private CircleImageView profileImage;
    private TextView usernameTV;
    private TextView storyTimeTV;
    private TextView likeCountTV;
    private TextView storyTTV;
    private Button btnBottomSheet;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_story_player);

        View bottomSheet = findViewById(R.id.bottomSheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        btnBottomSheet = findViewById(R.id.btnBottomSheet);

        bottomSheet.setOnTouchListener(new View.OnTouchListener() {
            private float startY;
            private boolean isMovingUp;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.d("bottomSheetTouch", "onTouch called");
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startY = event.getRawY();
                        isMovingUp = false;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        float y = event.getRawY();
                        if (y < startY) {
                            // Swiping up
                            if (!isMovingUp) {
                                isMovingUp = true;
                                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                            }
                        } else if (y > startY) {
                            // Swiping down
                            if (isMovingUp) {
                                isMovingUp = false;
                                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                            }
                        }
                        break;
                }
                return false;
            }
        });

        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {

            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        btnBottomSheet.setOnClickListener( view -> {
            Log.d("uris" , "btnBottomSheet clicked");
            if(bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED){
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
            else bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        });

        List<Uri> list = getIntent().getParcelableArrayListExtra("uriList");
        ImageURls.clear();
        ImageURls.addAll(list);
        Log.d("uris", "uri list size in story player activity " + ImageURls.size());
        // inside in create method below line is use to make a full screen.

        // on below line we are initializing our variables.
        storiesProgressView = (StoriesProgressView) findViewById(R.id.stories);

        // on below line we are setting the total count for our stories.
        storiesProgressView.setStoriesCount(ImageURls.size());

        // on below line we are setting story duration for each story.
        storiesProgressView.setStoryDuration(3000L);

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
        likeCountTV = findViewById(R.id.likeCountTV);
        storyTTV = findViewById(R.id.storyTTV);
        btnBottomSheet = findViewById(R.id.btnBottomSheet);
        btnBottomSheet.setOnClickListener(view -> {

        });

        // on below line we are setting image to our image view.
        glideImage(ImageURls.get(counter),usernames[0],storyTimes[0],likeCounts[0],storyText[0]);

        // below is the view for going to the previous story.
        // initializing our previous view.
        View reverse = findViewById(R.id.reverse);

        // adding on click listener for our reverse view.
        reverse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // inside on click we are
                // reversing our progress view.
                storiesProgressView.reverse();
            }
        });

        // on below line we are calling a set on touch
        // listener method to move towards previous image.
        reverse.setOnTouchListener(onTouchListener);

        // on below line we are initializing
        // view to skip a specific story.
        View skip = findViewById(R.id.skip);
        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // inside on click we are
                // skipping the story progress view.
                storiesProgressView.skip();
            }
        });
        // on below line we are calling a set on touch
        // listener method to move to next story.
        skip.setOnTouchListener(onTouchListener);
    }

    @Override
    public void onNext() {
        // this method is called when we move
        // to next progress view of story.
        glideImage(ImageURls.get(++counter),usernames[0],storyTimes[0],likeCounts[0],storyText[0]);
    }

    @Override
    public void onPrev() {

        // this method id called when we move to previous story.
        // on below line we are decreasing our counter
        if ((counter - 1) < 0) return;
        glideImage(ImageURls.get(--counter),usernames[0],storyTimes[0],likeCounts[0],storyText[0]);

        // on below line we are setting image to image view
    }

    @Override
    public void onComplete() {
        // when the stories are completed this method is called.
        // in this method we are moving back to initial main activity.
        Intent i = new Intent(StoryPlayerActivity.this, MainActivity.class);
        startActivity(i);
        finish();
    }

    @Override
    protected void onDestroy() {
        // in on destroy method we are destroying
        // our stories progress view.
        storiesProgressView.destroy();
        super.onDestroy();
    }

    private void glideImage(Uri URL, String username, String time, String like, String storyText)
    {
        Glide.with(this)
                .load(URL)
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

        usernameTV.setText(username);
        storyTimeTV.setText(time);
        likeCountTV.setText(like);
        storyTTV.setText(storyText);

    }

}