package com.liner.appmover;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AppAdapter extends RecyclerView.Adapter<AppAdapter.ViewHolder> {
    private List<AppHolder> appHolderList;
    private Context context;
    private ISelectionListener selectionListener;

    public AppAdapter(List<AppHolder> apps, Context context, ISelectionListener selectionListener) {
        this.appHolderList = apps;
        this.context = context;
        this.selectionListener = selectionListener;
    }

    @NonNull
    @Override
    public AppAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.app_item_layout, parent, false);
        return new AppAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final AppAdapter.ViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        final AppHolder item = appHolderList.get(position);
        holder.itemLayout.setBackgroundColor(item.isSelected() ? Color.parseColor("#CCCCCC") : Color.WHITE);
        holder.appIcon.setImageDrawable(AppHelper.getApplicationIcon(context, item.getAppPackageName()));
        holder.appName.setText(String.valueOf(item.getAppName()));
        if (item.isMovedToSystem()) {
            holder.appName.setTextColor(context.getColor(R.color.colorRed));
            holder.appPackageName.setTextColor(context.getColor(R.color.colorRedDark));
        } else {
            holder.appName.setTextColor(context.getColor(R.color.colorGreen));
            holder.appPackageName.setTextColor(context.getColor(R.color.colorGreenDark));
        }

        holder.appPackageName.setText(String.valueOf(item.getAppPackageName()));
        holder.itemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectionListener != null) {
                    clearSelection();
                    item.setSelected(true);
                    holder.itemLayout.setBackgroundColor(item.isSelected() ? Color.parseColor("#CCCCCC") : Color.WHITE);
                    selectionListener.onItemSelected(item);
                    notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return appHolderList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView appIcon;
        TextView appName;
        TextView appPackageName;
        LinearLayout itemLayout;


        ViewHolder(final View itemView) {
            super(itemView);
            itemLayout = itemView.findViewById(R.id.item_layout);
            appIcon = itemView.findViewById(R.id.appIcon);
            appName = itemView.findViewById(R.id.appName);
            appPackageName = itemView.findViewById(R.id.appPackageName);
        }
    }

    public interface ISelectionListener {
        void onItemSelected(AppHolder appHolder);
    }

    void clearSelection() {
        for (AppHolder item : appHolderList) {
            item.setSelected(false);
        }
    }

    List<AppHolder> getAppHolderList() {
        return appHolderList;
    }

}