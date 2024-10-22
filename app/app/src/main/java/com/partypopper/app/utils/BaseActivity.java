package com.partypopper.app.utils;

import android.animation.ValueAnimator;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import lombok.Getter;
import lombok.Setter;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.functions.FirebaseFunctions;
import com.partypopper.app.R;

import java.io.IOException;
import java.util.List;

import static com.partypopper.app.utils.Constants.STANDARD_ANIMATION_DURATION;

/**
 * Tools for every Activity.
 * This Activity should be extended instead of AppCompatActivity.
 */
public abstract class BaseActivity extends AppCompatActivity {

    public static final FirebaseFunctions mFunctions;
    public static final FirebaseAuth mAuth;
    public static final FirebaseFirestore mFireStore;
    @Setter
    @Getter
    protected static boolean isOrganizer = false;

    static {
        mFunctions = FirebaseFunctions.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mFireStore = FirebaseFirestore.getInstance();
    }

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        hideProgressDialog();
    }

    /**
     * Shows a ProgressDialog.
     *
     * @param text
     */
    public void showProgressDialog(String text) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(text);
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    /**
     * Hides a ProgressDialog.
     */
    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    /**
     * Shows a Snackbar.
     *
     * @param text text to show
     */
    public void showText(String text) {
        Snackbar.make(findViewById(android.R.id.content), text, Snackbar.LENGTH_LONG).show();
    }

    /**
     * Change the value of a color
     * @param color
     * @param factor 1.0 = unchanged
     * @return
     */
    @ColorInt
    public int changeValueOfColor(@ColorInt int color, float factor) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= factor;
        return Color.HSVToColor(hsv);
    }

    /**
     * Copies text to the clipboard
     * @param label explaining the text
     * @param text
     */
    public static void copyTextToClipboard(String label, CharSequence text, Context context) {
        ClipboardManager manager = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText(label, text);
        manager.setPrimaryClip(clipData);
    }

    /**
     * Share data via a share sheet
     * @param type
     * @param title
     * @param text
     */
    public void share(String type, String title, String text) {
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, text);
        sendIntent.setType(type);

        // (Optional) Here we're setting the title of the content
        sendIntent.putExtra(Intent.EXTRA_TITLE, title);

        // Show the Sharesheet
        startActivity(Intent.createChooser(sendIntent, null));
    }

    /**
     * Opens an url in the default browser app
     * @param url
     */
    public void openUrl(String url) {
        try {
            Uri uri = Uri.parse(url); // missing 'http://' will cause crashed
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        } catch (Exception e) {
            showText(getString(R.string.wrong_url));
        }

    }

    /**
     * Sets an offset for the appbar with CollapsingToolbarLayout
     * (how much of the imageView is shown) and animates it
     * @param offset
     */
    public void setAppBarOffset(int offset, final AppBarLayout appBarLayout) {
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams();
        final AppBarLayout.Behavior behavior = (AppBarLayout.Behavior) params.getBehavior();
        if (behavior != null) {
            ValueAnimator valueAnimator = ValueAnimator.ofInt();
            valueAnimator.setInterpolator(new DecelerateInterpolator());
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    behavior.setTopAndBottomOffset((Integer) animation.getAnimatedValue());
                    appBarLayout.requestLayout();
                }
            });
            valueAnimator.setIntValues(behavior.getTopAndBottomOffset(), -offset);
            valueAnimator.setDuration(STANDARD_ANIMATION_DURATION);
            valueAnimator.start();
        }
    }

    /**
     * returns a Geopoint with latitude and longtitude coordinates, which matches the given address
     * @param strAddress passed Address which shall be represented as latitude and longtitude
     * @return GeoPoint
     */
    public LatLng getLocationFromAddress(Context context, String strAddress) {
        Geocoder coder = new Geocoder(context);
        List<Address> address;
        LatLng p1 = null;
        try {
            // May throw an IOException
            address = coder.getFromLocationName(strAddress, 5);
            if (address == null || address.size() == 0) {
                return null;
            } else {
                Address location = address.get(0);
                p1 = new LatLng(location.getLatitude(), location.getLongitude());
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return p1;
    }
}
