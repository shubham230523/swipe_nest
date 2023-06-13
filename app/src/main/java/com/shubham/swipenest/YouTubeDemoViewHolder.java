package com.shubham.swipenest;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public abstract class YouTubeDemoViewHolder extends RecyclerView.ViewHolder {
    public YouTubeDemoViewHolder(View itemView) {
        super(itemView);
    }

    public static class TextHeaderViewHolder extends YouTubeDemoViewHolder {
        public TextHeaderViewHolder(View itemView) {
            super(itemView);
        }
    }

    public static class TextDescriptionViewHolder extends YouTubeDemoViewHolder {
        public TextDescriptionViewHolder(View itemView) {
            super(itemView);
        }
    }

    public static class CatRowViewHolder extends YouTubeDemoViewHolder {
        public ImageView imageView;
        public TextView textView;

        public CatRowViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_row);
            textView = itemView.findViewById(R.id.text_row);
        }
    }
}