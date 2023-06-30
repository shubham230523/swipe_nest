package com.shubham.swipenest.story;

import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.shubham.swipenest.R;
import com.shubham.swipenest.story.homeScreen.fragments.FragmentPreviewListener;
import com.shubham.swipenest.story.homeScreen.fragments.HomeScreenStoriesFragment;
import com.shubham.swipenest.story.homeScreen.fragments.PickedMediaPreviewFragment;

public class MainActivity extends AppCompatActivity implements  FragmentPreviewListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        HomeScreenStoriesFragment fragment = HomeScreenStoriesFragment.newInstance(this);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment, "storiesFragment")
                .addToBackStack("storiesFragment")
                .commit();

    }

    @Override
    public void onUriSelected(Uri uri) {
        getSupportFragmentManager().popBackStack();
        HomeScreenStoriesFragment fragment = (HomeScreenStoriesFragment) getSupportFragmentManager().findFragmentByTag("storiesFragment");
        if (fragment != null) {
            fragment.addToUriList(uri);
        }
    }

    @Override
    public void onReplaceFragmentRequest(Uri uri, Boolean isVideo) {
        PickedMediaPreviewFragment fragment = PickedMediaPreviewFragment.newInstance(this);
        Bundle bundle = new Bundle();
        if(isVideo){
            bundle.putParcelable("videoUri", uri);
        }
        else  {
            bundle.putParcelable("imageUri" , uri);
        }
        fragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment, "previewFragment")
                .addToBackStack("previewFragment")
                .commit();
    }

    @Override
    public void onCancelClicked() {
        getSupportFragmentManager().popBackStack();
    }
}