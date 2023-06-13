package com.shubham.swipenest;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;

public class FrontPhotosAdapter extends RecyclerView.Adapter<YouTubeDemoViewHolder> {
    @NonNull
    @Override
    public YouTubeDemoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(viewType, parent, false);

        switch (viewType) {
            case R.layout.motion_24_recyclerview_expanded_text_header:
                return new YouTubeDemoViewHolder.TextHeaderViewHolder(itemView);
            case R.layout.motion_24_recyclerview_expanded_text_description:
                return new YouTubeDemoViewHolder.TextDescriptionViewHolder(itemView);
            case R.layout.motion_24_recyclerview_expanded_row:
                return new YouTubeDemoViewHolder.CatRowViewHolder(itemView);
            default:
                throw new IllegalStateException("Unknown viewType " + viewType);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull YouTubeDemoViewHolder holder, int position) {
        if (holder instanceof YouTubeDemoViewHolder.CatRowViewHolder) {
            YouTubeDemoViewHolder.CatRowViewHolder catRowViewHolder = (YouTubeDemoViewHolder.CatRowViewHolder) holder;
            int imagePosition = position - 2;
            catRowViewHolder.textView.setText("cat_n_" + imagePosition);
            Glide.with(catRowViewHolder.imageView)
                    .load(Cats.catImages[imagePosition])
                    .into(catRowViewHolder.imageView);
        }
    }

    @Override
    public int getItemCount() {
        return Cats.catImages.length + 2; // For text header and text description
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return R.layout.motion_24_recyclerview_expanded_text_header;
        } else if (position == 1) {
            return R.layout.motion_24_recyclerview_expanded_text_description;
        } else {
            return R.layout.motion_24_recyclerview_expanded_row;
        }
    }
}