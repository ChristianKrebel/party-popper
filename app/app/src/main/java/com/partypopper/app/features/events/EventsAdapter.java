package com.partypopper.app.features.events;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.partypopper.app.R;
import com.partypopper.app.database.model.Event;

import static com.partypopper.app.utils.Constants.*;

import com.partypopper.app.features.dashboard.EventDetailActivity;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.RecyclerView;

public class EventsAdapter extends RecyclerView.Adapter<EventsViewHolder> {

    Activity activity;
    List<Event> modelList;
    Map<Event, String> eventsAndOrganizerNames;
    Context context;
    int rowLayout;

    public EventsAdapter(Activity activity,
                         List<Event> modelList,
                         Map<Event, String> eventsAndOrganizerNames,
                         Context context,
                         int rowLayout) {
        this.activity = activity;
        this.modelList = modelList;
        this.eventsAndOrganizerNames = eventsAndOrganizerNames;
        this.context = context;
        this.rowLayout = rowLayout;
    }

    @NonNull
    @Override
    public EventsViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, int viewType) {
        //inflate layout
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(rowLayout, parent, false);

        final EventsViewHolder viewHolder = new EventsViewHolder(itemView);

        //handle item clicks
        viewHolder.setOnClickListener(new EventsViewHolder.ClickListener() {
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
                intent.putExtra("eventUrl", event.getEventUrl());
                intent.putExtra("eventId", event.getEntityKey());

                ImageView mBannerIv = viewHolder.getMBannerIv();
                Drawable mBannerDrawable = mBannerIv.getDrawable();
                if (mBannerDrawable != null) {
                    Bitmap mBanner = ((BitmapDrawable) mBannerDrawable).getBitmap();
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    mBanner.compress(Bitmap.CompressFormat.PNG, COMPRESSION_QUALITY, stream);
                    byte[] bytes = stream.toByteArray();
                    intent.putExtra("image", bytes);
                }

                view.getContext().startActivity(intent);
            }

            @Override
            public void onItemLongClick(View view, int position) {
                // TODO
                Toast.makeText(activity,
                        modelList.get(position).getName(), Toast.LENGTH_SHORT).show();
            }
        });

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final EventsViewHolder holder, int position) {
        // Bind views / set data
        Event model = modelList.get(position);

        holder.getMTitleTv().setText(model.getName());
        Date date = model.getStartDate();

        holder.getMDateTv().setText(DateFormat.getDateInstance(DateFormat.SHORT).format(date));

        if (holder.getMOrganizerTv() != null) {     // not every layout has this view
            holder.getMOrganizerTv().setText(eventsAndOrganizerNames.get(model));
        }
        holder.getMVisitorCountTv().setText(model.getGoing() + " " + context.getString(R.string.attendees));

        Picasso.get()
                .load(model.getImage())
                .into(holder.getMBannerIv(), new com.squareup.picasso.Callback() {
            @Override
            public void onSuccess() {
                //when picture is loaded successfully
                Drawable unwrappedDrawable = holder.getMGradientFl().getBackground();
                final Drawable wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
                Drawable bannerDrawable = holder.getMBannerIv().getDrawable();

                Bitmap bannerBm = ((BitmapDrawable) bannerDrawable).getBitmap();
                Palette.from(bannerBm).generate(new Palette.PaletteAsyncListener() {
                    @Override
                    public void onGenerated(Palette palette) {
                        int imageColor = palette.getDarkMutedColor(Color.BLACK);
                        DrawableCompat.setTint(wrappedDrawable, imageColor);
                    }
                });
            }

            @Override
            public void onError(Exception ex) {
            }
        });

        Log.d("EventsAdapter", model.getName() + " - " + model.getOrganizer()
                + " - " + eventsAndOrganizerNames.get(model));
    }

    @Override
    public int getItemCount() {
        return modelList.size();
    }
}
