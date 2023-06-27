package com.shubham.swipenest.story.homeScreen.fragments.preview.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.fonts.Font;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.shubham.swipenest.R;
import com.shubham.swipenest.story.homeScreen.fragments.OnClickListener;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class TypographyAdapter extends RecyclerView.Adapter<TypographyAdapter.TypographyViewHolder> {
    Context mContext;
    List<Typeface> fontList;
    OnClickListener listener;
    int itemCount = 0;

    public TypographyAdapter(int itemCount, Context context, OnClickListener listener){
        this.listener = listener;
        this.itemCount = itemCount;
        mContext = context;
        fontList = new ArrayList<>();
        fontList.add(ResourcesCompat.getFont(mContext, R.font.caprasimo_regular));
        fontList.add(ResourcesCompat.getFont(mContext, R.font.caveat_regular));
        fontList.add(ResourcesCompat.getFont(mContext, R.font.changa_regular));
        fontList.add(ResourcesCompat.getFont(mContext, R.font.cookie_regular));
        fontList.add(ResourcesCompat.getFont(mContext, R.font.fasthand_regular));
        fontList.add(ResourcesCompat.getFont(mContext, R.font.montserrat_regular));
        fontList.add(ResourcesCompat.getFont(mContext, R.font.permanentmarker_regular));
        fontList.add(ResourcesCompat.getFont(mContext, R.font.ysabeau_sc_regular));
    }

    @NonNull
    @Override
    public TypographyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.font_item, parent, false);
        return new TypographyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TypographyViewHolder holder, int position) {
        if(position == 0){
            holder.typographyBg.setImageResource(R.color.white);
            holder.text.setTextColor(ContextCompat.getColor(holder.text.getContext(), R.color.purple_500));
        }
        holder.text.setTypeface(fontList.get(position));

        holder.typographyBg.setOnClickListener(v -> {
            listener.onClick(position, fontList.get(position));
        });
    }

    @Override
    public int getItemCount() {
        return itemCount;
    }

    public static class TypographyViewHolder extends RecyclerView.ViewHolder{

        ImageView typographyBg;
        TextView text;
        public TypographyViewHolder(@NonNull View itemView) {
            super(itemView);
            typographyBg = itemView.findViewById(R.id.typography_bg);
            text = itemView.findViewById(R.id.typography_text);
        }
    }
}
