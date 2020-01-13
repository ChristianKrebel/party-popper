package com.partypopper.app.features.organizer.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RatingBar;

import com.partypopper.app.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.appcompat.app.AlertDialog;

/**
 * Class of a Dialog with a RatingBar to give a rating.
 */
public class OrganizerRateDialog extends AppCompatDialogFragment {

    private RatingBar ratingBar;
    private float ratingValue;
    private OrganizerRateDialogListener listener;

    /**
     * Builds and returns the dialog.
     *
     * @param savedInstanceState
     * @return
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_organizer_rate, null);

        builder.setView(view)
                .setTitle(getString(R.string.organizer_rate_dialog_title))
                .setNegativeButton(getString(R.string.dialog_negative_button), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton(getString(R.string.organizer_rate_dialog_positive_button), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ratingValue = ratingBar.getRating();
                        listener.applyRating(ratingValue);
                    }
                });

        final AlertDialog dialog = builder.create();

        ratingBar = view.findViewById(R.id.doOrganizerRb);

        return dialog;
    }

    /**
     * Attaches the dialog.
     *
     * @param context
     */
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            listener = (OrganizerRateDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() +
                    " must implement OrganizerRateDialogListener" );
        }

    }

    /**
     * Listener-interface for what to do when rated.
     */
    public interface OrganizerRateDialogListener {
        void applyRating(float rating);
    }
}
