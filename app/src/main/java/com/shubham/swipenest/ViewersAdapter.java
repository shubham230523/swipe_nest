package com.shubham.swipenest;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ViewersAdapter extends RecyclerView.Adapter<ViewersAdapter.StoryViewersViewHolder> {

    private List<Viewers> viewsList;

    public ViewersAdapter(List<Viewers> viewsList) {
        this.viewsList = viewsList;
    }

    @NonNull
    @Override
    public StoryViewersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.story_view_rv_item,parent,false);
        return new StoryViewersViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StoryViewersViewHolder holder, int position) {
        Viewers user = viewsList.get(position);
        holder.username.setText(user.userName);
        holder.userImage.setImageResource(user.profileImg);
    }

    @Override
    public int getItemCount() {
        return viewsList.size();
    }

    public static class StoryViewersViewHolder extends RecyclerView.ViewHolder
    {
        TextView username;
        ImageView userImage;
        public StoryViewersViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.tvUserName);
            userImage = itemView.findViewById(R.id.ivUserImg);
        }
    }
}
