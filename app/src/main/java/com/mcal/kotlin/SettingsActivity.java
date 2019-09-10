package com.mcal.kotlin;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.appcompat.widget.Toolbar;

import com.mcal.kotlin.data.NightMode;
import com.mcal.kotlin.model.BaseActivity;
import com.mcal.kotlin.module.Dialogs;
import com.mcal.kotlin.module.Offline;
import com.mcal.kotlin.utils.Utils;
import com.yarolegovich.mp.MaterialSwitchPreference;

import org.zeroturnaround.zip.commons.FileUtils;

import java.io.File;
import java.io.IOException;

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
                        progressDialog.setTitle(getString(R.string.downloading));
                        new Offline(this).execute();
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
                //recreate();
                Intent fullscreen_mode = getIntent();
                finish();
                startActivity(fullscreen_mode);
                break;
            case "night_mode":
                NightMode.setMode(NightMode.getCurrentMode());
                //recreate();
                Intent night_mode = getIntent();
                finish();
                startActivity(night_mode);
                break;
            case "grid_mode":
                setResult(RESULT_OK);
        }
    }
}
