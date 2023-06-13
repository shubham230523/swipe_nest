package com.shubham.swipenest;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class YouDemoActivity1 extends AppCompatActivity {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        setContentView(R.layout.motion_24_youtube);
        Log.d("tag_you_tube_demo", "YouDemoActivity1 callled");
        MotionLayout motionLayout = findViewById(R.id.motionLayout);

        RecyclerView recyclerView = findViewById(R.id.recyclerview_front);
        recyclerView.setAdapter(new FrontPhotosAdapter());
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        int debugMode = getIntent().getBooleanExtra("showPaths", false) ? MotionLayout.DEBUG_SHOW_PATH : MotionLayout.DEBUG_SHOW_NONE;
        motionLayout.setDebugMode(debugMode);

    }
}