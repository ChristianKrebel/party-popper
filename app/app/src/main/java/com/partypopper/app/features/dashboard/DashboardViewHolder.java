package com.partypopper.app.features.dashboard;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.partypopper.app.R;
import com.squareup.picasso.Picasso;

import androidx.recyclerview.widget.RecyclerView;

public class DashboardViewHolder extends RecyclerView.ViewHolder {

    private View mView;
    private EventClickListener mEventClickListener;

    public DashboardViewHolder(View itemView) {
        super(itemView);

        mView = itemView;

        // Item click
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEventClickListener.onItemClick(v, getAdapterPosition());
            }
        });
        // Item long click
        itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mEventClickListener.onItemLongClick(v, getAdapterPosition());
                return true;
            }
        });
    }

    public void setDetails(String title, String date, String image, String organizer, long visitorCount) {
        // Views
        ImageView mBannerIv = mView.findViewById(R.id.rBannerIv);
        TextView mTitleTv = mView.findViewById(R.id.rTitleTv);
        TextView mDateTv = mView.findViewById(R.id.rDateTv);
        TextView mOrganizerTv = mView.findViewById(R.id.rOrganizerTv);
        TextView mVisitorCountTv = mView.findViewById(R.id.rVisitorCountTv);

        // set data to views
        Picasso.get().load(image).into(mBannerIv);

        mTitleTv.setText(title);
        mDateTv.setText(date);
        mOrganizerTv.setText(organizer);
        mVisitorCountTv.setText(Long.toString(visitorCount) + " Attendees");
    }

    public void setOnClickListener(EventClickListener eventClickListener) {
        this.mEventClickListener = eventClickListener;
    }
}
