package com.mcal.kotlin.module;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.appcompat.widget.AppCompatTextView;

import com.google.android.material.textfield.TextInputLayout;
import com.mcal.kotlin.App;
import com.mcal.kotlin.BuildConfig;
import com.mcal.kotlin.R;

import org.w3c.dom.Document;

import java.net.URL;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import ru.svolf.melissa.sheet.SweetViewDialog;

public class AppUpdater extends AsyncTask<Void, Void, Void> {
    private String version_name;
    private int version_code;
    private String release_notes;
    private String download_link;
    @SuppressLint("StaticFieldLeak")
    private Context context;

    public AppUpdater(Context context) {
        this.context = context;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            URL url = new URL("https://mcal-llc.github.io/kt/config/update.xml");
            HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = dbf.newDocumentBuilder();
            Document doc = builder.parse(con.getInputStream());

            con.disconnect();

            version_name = doc.getElementsByTagName("version_name").item(0).getTextContent();
            version_code = Integer.parseInt(doc.getElementsByTagName("version_code").item(0).getTextContent());
            release_notes = doc.getElementsByTagName("release_notes").item(0).getTextContent();
            download_link = doc.getElementsByTagName("download_link").item(0).getTextContent();
        } catch (Exception ignored) {
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        try {
            if (version_code > BuildConfig.VERSION_CODE) {
                updateApp();
            }
        } catch (Exception ignored) {
        }
    }

    private void updateApp() {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        LinearLayout ll = new LinearLayout(context);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setPadding(40, 0, 40, 0);
        ll.setLayoutParams(layoutParams);
        final TextInputLayout til0 = new TextInputLayout(context);
        final AppCompatTextView message = new AppCompatTextView(context);
        message.setText(release_notes);
        til0.addView(message);
        ll.addView(til0);

        final SweetViewDialog dialog = new SweetViewDialog(context);
        dialog.setTitle(context.getString(R.string.version_available) + " " + version_name);
        dialog.setView(ll);
        dialog.setPositive(R.string.update, v1 -> {
            App.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(download_link)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            System.exit(0);
        });
        dialog.setCancelable(false);
        dialog.show();
    }
}