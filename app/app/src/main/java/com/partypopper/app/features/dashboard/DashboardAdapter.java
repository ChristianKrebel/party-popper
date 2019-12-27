package com.partypopper.app.features.dashboard;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.partypopper.app.R;
import com.partypopper.app.database.model.Event;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class DashboardAdapter extends RecyclerView.Adapter<DashboardViewHolder> {

    DashboardActivity dashboardActivity;
    List<Event> modelList;

    public DashboardAdapter(DashboardActivity dashboardActivity, List<Event> modelList) {
        this.dashboardActivity = dashboardActivity;
        this.modelList = modelList;
    }

    @NonNull
    @Override
    public DashboardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate layout
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_dashboard, parent, false);

        DashboardViewHolder viewHolder = new DashboardViewHolder(itemView);

        //handle item clicks
        viewHolder.setOnClickListener(new DashboardViewHolder.ClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                // TODO
                Toast.makeText(dashboardActivity,
                        position + " - " + modelList.get(position).getName(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onItemLongClick(View view, int position) {
                // TODO
            }
        });

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull DashboardViewHolder holder, int position) {
        // Bind views / set data
        holder.getMTitleTv().setText(modelList.get(position).getName());
        Date date = modelList.get(position).getStartDate();
        holder.getMDateTv().setText(DateFormat.getDateInstance(DateFormat.SHORT).format(date));
        holder.getMOrganizerTv().setText((modelList.get(position).getOrganizer()));
        holder.getMVisitorCountTv().setText(modelList.get(position).getGoing() + " " + R.string.attendees);

        Picasso.get().load(modelList.get(position).getImage()).into(holder.getMBannerIv());
    }

    @Override
    public int getItemCount() {
        return modelList.size();
    }
}
