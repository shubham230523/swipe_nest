package com.shubham.swipenest.story.homeScreen.fragments;

import android.net.Uri;

public interface FragmentPreviewListener {
    void onUriSelected(Uri uri);
    void onReplaceFragmentRequest(Uri uri, Boolean isVideo);
    void onCancelClicked();
}
