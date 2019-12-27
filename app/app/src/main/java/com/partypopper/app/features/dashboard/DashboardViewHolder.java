package com.partypopper.app.features.dashboard;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.partypopper.app.R;
import com.squareup.picasso.Picasso;

import androidx.recyclerview.widget.RecyclerView;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

public class DashboardViewHolder extends RecyclerView.ViewHolder {

    private View mView;
    @Getter
    @Setter
    private TextView mTitleTv, mDateTv, mOrganizerTv, mVisitorCountTv;
    @Getter
    @Setter
    private ImageView mBannerIv;
    private ClickListener mClickListener;

    public DashboardViewHolder(View itemView) {
        super(itemView);

        mView = itemView;


        // Item click
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mClickListener.onItemClick(v, getAdapterPosition());
            }
        });
        // Item long click
        itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mClickListener.onItemLongClick(v, getAdapterPosition());
                return true;
            }
        });

        // initialize views
        mBannerIv = mView.findViewById(R.id.rBannerIv);
        mTitleTv = mView.findViewById(R.id.rTitleTv);
        mDateTv = mView.findViewById(R.id.rDateTv);
        mOrganizerTv = mView.findViewById(R.id.rOrganizerTv);
        mVisitorCountTv = mView.findViewById(R.id.rVisitorCountTv);
    }


    // Interface for click listener
    public interface ClickListener {
        void onItemClick(View view, int position);
        void onItemLongClick(View view, int position);
    }

    public void setOnClickListener(DashboardViewHolder.ClickListener clickListener) {
        this.mClickListener = clickListener;
    }


}
