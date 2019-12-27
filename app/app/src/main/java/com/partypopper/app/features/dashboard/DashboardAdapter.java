package com.partypopper.app.features.dashboard;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.partypopper.app.R;
import com.partypopper.app.database.model.Event;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class DashboardAdapter extends RecyclerView.Adapter<DashboardViewHolder> {

    private final int COMPRESSION_QUALITY = 98;

    DashboardActivity dashboardActivity;
    List<Event> modelList;
    Context context;

    public DashboardAdapter(DashboardActivity dashboardActivity, List<Event> modelList, Context context) {
        this.dashboardActivity = dashboardActivity;
        this.modelList = modelList;
        this.context = context;
    }

    @NonNull
    @Override
    public DashboardViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, int viewType) {
        //inflate layout
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_dashboard, parent, false);

        final DashboardViewHolder viewHolder = new DashboardViewHolder(itemView);

        //handle item clicks
        viewHolder.setOnClickListener(new DashboardViewHolder.ClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Event event = modelList.get(position);

                Intent intent = new Intent(view.getContext(), EventDetailActivity.class);
                intent.putExtra("name", event.getName());
                intent.putExtra("description", event.getDescription());
                intent.putExtra("endDate", event.getEndDate());
                intent.putExtra("going", event.getGoing());
                intent.putExtra("organizer", event.getOrganizer());
                intent.putExtra("startDate", event.getStartDate());

                ImageView mBannerIv = viewHolder.getMBannerIv();
                Drawable mBannerDrawable = mBannerIv.getDrawable();
                Bitmap mBanner = ((BitmapDrawable) mBannerDrawable).getBitmap();
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                mBanner.compress(Bitmap.CompressFormat.PNG, COMPRESSION_QUALITY, stream);
                byte[] bytes = stream.toByteArray();
                intent.putExtra("image", bytes);

                view.getContext().startActivity(intent);
            }

            @Override
            public void onItemLongClick(View view, int position) {
                // TODO
                Toast.makeText(dashboardActivity,
                        position + " - " + modelList.get(position).getName(), Toast.LENGTH_SHORT).show();
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
        holder.getMVisitorCountTv().setText(modelList.get(position).getGoing() + " " + context.getString(R.string.attendees));

        Picasso.get().load(modelList.get(position).getImage()).into(holder.getMBannerIv());
    }

    @Override
    public int getItemCount() {
        return modelList.size();
    }
}
