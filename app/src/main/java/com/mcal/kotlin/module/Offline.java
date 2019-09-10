package com.mcal.kotlin.module;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;

import androidx.appcompat.app.AlertDialog;

import com.mcal.kotlin.SettingsActivity;
import com.mcal.kotlin.data.Preferences;

import org.zeroturnaround.zip.ZipUtil;
import org.zeroturnaround.zip.commons.FileUtilsV2_2;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import es.dmoral.toasty.Toasty;

import static com.mcal.kotlin.data.Constants.DOWNLOAD_ZIP;

public class Offline extends AsyncTask<Void, Integer, Boolean> {
    private SettingsActivity settingsActivity;
    private ProgressDialog progressDialog;

    public Offline(SettingsActivity settingsActivity) {
        this.settingsActivity = settingsActivity;
        progressDialog = new ProgressDialog(settingsActivity);
    }

    private void deleteResources() {
        try {
            File resourcesDir = new File(settingsActivity.getPackageName(), "resources");
            FileUtilsV2_2.deleteDirectory(resourcesDir);
        } catch (IOException e) {
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (Preferences.isOffline()) {
            new AlertDialog.Builder(settingsActivity)
                    .setMessage("Отключить оффлайн и удалить ресурсы?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();

                            execute(new Runnable() {
                                @Override
                                public void run() {
                                    deleteResources();
                                }
                            });

                            Preferences.setOffline(false);
                            Toasty.warning(settingsActivity, "Ресурсы удалены").show();
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            cancel(true);
                        }
                    })
                    .setCancelable(false)
                    .show();
        } else {
            progressDialog.setTitle("Загрузка");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setCancelable(false);
            progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, settingsActivity.getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    cancel(true);
                    dialogInterface.dismiss();
                    Preferences.setOffline(false);
                }
            });
            progressDialog.show();
        }
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        try {
            try {
                URL url = new URL(DOWNLOAD_ZIP);
                URLConnection connection = url.openConnection();

                progressDialog.setMax(connection.getContentLength());
                progressDialog.show();

                File offline = new File(settingsActivity.getFilesDir(), "offline.zip");
                File resourcesDir = new File(settingsActivity.getFilesDir(), "resources");

                InputStream inputStream = new BufferedInputStream(connection.getInputStream());
                OutputStream outputStream = new FileOutputStream(offline);

                int count;
                byte[] data = new byte[1024];

                while ((count = inputStream.read(data)) != -1) {
                    progressDialog.incrementProgressBy(count);
                    outputStream.write(data, 0, count);
                }
                inputStream.close();
                outputStream.flush();

                ZipUtil.unpack(offline, resourcesDir);
                offline.delete();
                progressDialog.dismiss();
                return true;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);

        if (values[0] == -1) {
            progressDialog.setTitle("Распаковка");
        } else progressDialog.setProgress(values[0]);
    }

    @Override
    protected void onPostExecute(Boolean bool) {
        super.onPostExecute(bool);

        if (bool) {
            progressDialog.dismiss();
            Preferences.setOffline(true);
            Toasty.success(settingsActivity, "Оффлайн активирован").show();
        }
    }
}
