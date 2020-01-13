package com.partypopper.app.features.events;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.partypopper.app.R;

import androidx.recyclerview.widget.RecyclerView;
import lombok.Getter;
import lombok.Setter;

/**
 * The ViewHolder for an event. It initializes the views.
 */
public class EventsViewHolder extends RecyclerView.ViewHolder {

    private View mView;
    @Getter
    @Setter
    private TextView mTitleTv, mDateTv, mOrganizerTv, mVisitorCountTv;
    @Getter
    @Setter
    private ImageView mBannerIv;
    @Getter
    @Setter
    private FrameLayout mGradientFl;
    private ClickListener mClickListener;

    /**
     * The constructor.
     * Initializes views and sets an onClickListener.
     *
     * @param itemView
     */
    public EventsViewHolder(View itemView) {
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
        mGradientFl = mView.findViewById(R.id.rGradientFl);
    }


    /**
     * Interface for click listener.
     */
    public interface ClickListener {
        void onItemClick(View view, int position);
        void onItemLongClick(View view, int position);
    }

    /** Sets the ViewHolder's click listener
     *
     * @param clickListener
     */
    public void setOnClickListener(EventsViewHolder.ClickListener clickListener) {
        this.mClickListener = clickListener;
    }


}
