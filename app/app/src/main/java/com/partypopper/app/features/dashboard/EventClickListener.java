package com.partypopper.app.features.dashboard;

import android.view.View;

interface EventClickListener {
    void onItemClick(View view, int position);
    void onItemLongClick(View view, int position);
}
