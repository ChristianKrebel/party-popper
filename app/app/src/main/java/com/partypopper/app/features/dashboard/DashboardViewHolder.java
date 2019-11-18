package com.partypopper.app.features.dashboard;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.partypopper.app.R;
import com.squareup.picasso.Picasso;

import androidx.recyclerview.widget.RecyclerView;

public class DashboardViewHolder extends RecyclerView.ViewHolder {

    View mView;

    public DashboardViewHolder(View itemView) {
        super(itemView);

        mView = itemView;
    }

    public void setDetails(String title, String date, long visitorCount, String image) {
        // Views
        ImageView mBannerIv = mView.findViewById(R.id.rBannerIv);
        TextView mTitleTv = mView.findViewById(R.id.rTitleTv);
        TextView mDateTv = mView.findViewById(R.id.rDateTv);
        TextView mVisitorCountTv = mView.findViewById(R.id.rVisitorCountTv);

        // set data to views
        Picasso.get().load(image).into(mBannerIv);

        mTitleTv.setText(title);
        mDateTv.setText(date);
        mVisitorCountTv.setText(Long.toString(visitorCount) + " Attendees");
    }
}
