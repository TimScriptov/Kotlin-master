package com.mcal.kotlin.module;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RatingBar;

import androidx.appcompat.app.AlertDialog;

import com.mcal.kotlin.App;
import com.mcal.kotlin.R;
import com.mcal.kotlin.data.Constants;
import com.mcal.kotlin.data.Preferences;

import ru.svolf.melissa.sheet.SweetViewDialog;

import static com.mcal.kotlin.data.Constants.RATE;

public class Dialogs {
    public static void noConnectionError(Context c) {
        new AlertDialog.Builder(c)
                .setTitle(R.string.error)
                .setMessage(R.string.no_connection)
                .setPositiveButton(android.R.string.ok, null)
                .setCancelable(false)
                .create().show();
    }

    public static void show(Context c, String text) {
        new AlertDialog.Builder(c)
                .setMessage(text)
                .setPositiveButton(android.R.string.ok, null)
                .create().show();
    }

    public static void rate(final Context context) {
        View v = LayoutInflater.from(context).inflate(R.layout.rate, null);
        final RatingBar ratingBar = v.findViewById(R.id.rating_bar);

        SweetViewDialog dialog = new SweetViewDialog(context);
                dialog.setTitle(R.string.rate);
                dialog.setView(v);
                dialog.setPositive(R.string.rate, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (ratingBar.getRating() > 3) {
                            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(RATE)));
                            Preferences.setRated();
                        } else {
                            App.toast(R.string.thanks);
                            App.preferences.edit().putBoolean(Constants.IS_RATED, true).apply();
                        }
                    }
                });
                dialog.show();
    }

    public static void error(Context c, String text) {
        new AlertDialog.Builder(c)
                .setMessage(text)
                .setPositiveButton(android.R.string.ok, null)
                .setCancelable(true)
                .create().show();
    }
}