package com.shubham.swipenest.story;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.shubham.swipenest.utils.OnClickListener;
import com.shubham.swipenest.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StoryViewAdapter extends RecyclerView.Adapter<StoryViewAdapter.StoryViewHolder> {

    OnClickListener onClickListener;
    private Boolean showSelfStoryGradient = false;
    List<String> usernameList = new ArrayList<>();
    public StoryViewAdapter(String[] usernameList, OnClickListener clickListener) {
        this.usernameList.addAll(Arrays.asList(usernameList));
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

        holder.username.setText(usernameList.get(position));
        if(position == 0){
            holder.profileAddStoryIcon.setVisibility(View.VISIBLE);
            if(showSelfStoryGradient){
                holder.imgProfile.setVisibility(View.GONE);
                holder.imgProfileStatus.setVisibility(View.VISIBLE);
            }
            holder.imgProfileStatus.setOnClickListener(v -> {
                onClickListener.onClick(0, false);
            });
            holder.profileAddStoryIcon.setOnClickListener(v -> {
                onClickListener.onClick(0, true);
            });
        }


    }

    public void applySelfStoryGradient(){
        if(!showSelfStoryGradient){
            showSelfStoryGradient = true;
            notifyItemChanged(0);
        }
    }

    @Override
    public int getItemCount() {
        return usernameList.size();
    }

    public static class StoryViewHolder extends RecyclerView.ViewHolder
    {
        TextView username;
        public ImageView imgProfile, profileAddStoryIcon, imgProfileStatus;
        public CardView imgCard;
        public StoryViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.username);
            profileAddStoryIcon = itemView.findViewById(R.id.profileStoryAddIcon);
            imgProfile = itemView.findViewById(R.id.imgProfile);
            imgCard = itemView.findViewById(R.id.imgCard);
            imgProfileStatus = itemView.findViewById(R.id.imgProfileStatus);
        }
    }

}