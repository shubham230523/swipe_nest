package com.shubham.swipenest;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class StoryViewAdapter extends RecyclerView.Adapter<StoryViewAdapter.StoryViewHolder> {

    OnClickListener onClickListener;
    String[] usernameList;
    public StoryViewAdapter(String[] usernameList, OnClickListener clickListener) {
        this.usernameList = usernameList;
        this.onClickListener = clickListener;
    }

//    private final String[] ImageURls = {"https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/11.png",
////            "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/41.png",
////            "https://images.pexels.com/photos/799443/pexels-photo-799443.jpeg"};
////
////    private final String[] usernames = {"Siddharth Singh", "Mika Rami", "Sokata Ryuk"};
////    private final String[] storyTimes = {"15hr Ago", "8hr Ago", "9hr Ago"};
////    private final String[] likeCounts = {"22K", "257", "6.8K"};
////    private final String[] storyText = {"New Pokemon now live!", "Gather tonight for the latest event by AC/DC", "People around the world are crazy!"};


    @NonNull
    @Override
    public StoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.story_item_layout,parent,false);
        return new StoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StoryViewHolder holder, int position) {
        holder.username.setText(usernameList[position]);
        if(position == 0) holder.plusIcon.setVisibility(View.VISIBLE);
        holder.frameLayout.setOnClickListener(view -> {
            onClickListener.onClick(position, holder);
        });
    }

    @Override
    public int getItemCount() {
        return usernameList.length;
    }

    public static class StoryViewHolder extends RecyclerView.ViewHolder
    {
        TextView username;
        ImageView plusIcon;
        FrameLayout frameLayout;
        public StoryViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.username);
            frameLayout = itemView.findViewById(R.id.frameLayout);
            plusIcon = itemView.findViewById(R.id.imageView);
        }
    }

}