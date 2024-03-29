package com.shubham.swipenest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.motion_24_youtube);
        Log.d("tag_main_activity", "main activity called");
        MotionLayout motionLayout = findViewById(R.id.motionLayout);

        RecyclerView recyclerView = findViewById(R.id.recyclerview_front);
        recyclerView.setAdapter(new FrontPhotosAdapter());
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        int debugMode = getIntent().getBooleanExtra("showPaths", false) ? MotionLayout.DEBUG_SHOW_PATH : MotionLayout.DEBUG_SHOW_NONE;
        motionLayout.setDebugMode(debugMode);

        ViewPager2 viewPager2 = findViewById(R.id.view_pager);
        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        viewPager2.setAdapter(adapter);
    }
}