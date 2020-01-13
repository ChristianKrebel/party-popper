package com.partypopper.app.features.events;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.partypopper.app.R;
import com.partypopper.app.database.model.Event;


import com.partypopper.app.features.eventDetail.EventDetailActivity;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.RecyclerView;

/**
 * The adapter to control events for a RecyclerView
 */
public class EventsAdapter extends RecyclerView.Adapter<EventsViewHolder> {

    private Activity activity;
    private List<Event> modelList;
    private Map<Event, String> eventsAndOrganizerNames;
    private Context context;
    private int rowLayout;

    /**
     * Constructor.
     *
     * @param activity
     * @param modelList events list
     * @param eventsAndOrganizerNames map with events and its organizer's names
     * @param context
     * @param rowLayout
     */
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

    /**
     * Creates a ViewHolder, inflates the row layout and implements
     * how clicks are being handled.
     *
     * @param parent
     * @param viewType
     * @return
     */
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
                intent.putExtra("location", new LatLng(event.getLocation().getLatitude(), event.getLocation().getLongitude()));

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

    /**
     * Binds a ViewHolder. The data for its views are being retrieved
     * from the model and then set.
     *
     * @param holder
     * @param position
     */
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

    /**
     * Returns the amount of items in the model list.
     *
     * @return
     */
    @Override
    public int getItemCount() {
        return modelList.size();
    }
}
