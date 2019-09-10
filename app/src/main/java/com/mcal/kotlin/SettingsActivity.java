package com.mcal.kotlin;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.appcompat.widget.Toolbar;

import com.mcal.kotlin.data.NightMode;
import com.mcal.kotlin.model.BaseActivity;
import com.mcal.kotlin.module.Dialogs;
import com.mcal.kotlin.utils.Utils;
import com.yarolegovich.mp.MaterialSwitchPreference;

import org.zeroturnaround.zip.ZipUtil;
import org.zeroturnaround.zip.commons.FileUtils;

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

import static com.mcal.kotlin.data.Constants.DOWNLOAD_ZIP;
import static com.mcal.kotlin.data.Constants.IS_PREMIUM;

public class SettingsActivity extends BaseActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    private SharedPreferences prefs;
    private MaterialSwitchPreference offline;
    private boolean isVip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        isVip = getIntent().getBooleanExtra(IS_PREMIUM, false);
        offline = findViewById(R.id.offline);
    }

    @Override
    protected void onResume() {
        super.onResume();
        prefs.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        prefs.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
        switch (key) {
            case "offline":
                if (preferences.getBoolean(key, false) && isVip) {
                    if (Utils.isNetworkAvailable()) {
                        final ProgressDialog progressDialog = new ProgressDialog(this);
                        progressDialog.setTitle("Downloading...");
                        new Thread(){
                            @Override
                            public void run() {
                                super.run();
                                try {
                                    URL url = new URL(DOWNLOAD_ZIP);
                                    URLConnection connection = url.openConnection();

                                    progressDialog.setMax(connection.getContentLength());
                                    progressDialog.show();

                                    File offline = new File(getFilesDir(), "offline.zip");
                                    File resourcesDir = new File(getFilesDir(), "resources");

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
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                } catch (MalformedURLException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }.start();
                    } else {
                        Dialogs.noConnectionError(this);
                    }
                } else if (isVip) {
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                File resourcesDir = new File(getFilesDir(), "resources");
                                FileUtils.deleteDirectory(resourcesDir);
                            } catch (IOException ignored) {
                            }
                        }
                    });
                } else if (preferences.getBoolean(key, false)) {
                    offline.performClick();
                    Dialogs.show(this, getString(R.string.only_prem));
                }
                break;
            case "fullscreen_mode":
                recreate();
                break;
            case "night_mode":
                NightMode.setMode(NightMode.getCurrentMode());
                recreate();
                break;
            case "grid_mode":
                setResult(RESULT_OK);
        }
    }
}
