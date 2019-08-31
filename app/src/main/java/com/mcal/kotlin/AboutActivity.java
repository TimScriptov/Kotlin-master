package com.mcal.kotlin;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.mcal.kotlin.model.BaseActivity;
import com.mcal.kotlin.module.Dialogs;

import static com.mcal.kotlin.data.Constants.MORE_APPS;

public class AboutActivity extends BaseActivity implements OnClickListener {
    @Override
    public void onClick(View p1) {
        switch (p1.getId()) {
            case R.id.rateApp:
                Dialogs.rate(this);
                break;
            case R.id.moreApps:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(MORE_APPS)));
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        try {
            ((TextView) findViewById(R.id.version)).append(" " + getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_META_DATA).versionName);
        } catch (PackageManager.NameNotFoundException e) {
        }

        findViewById(R.id.rateApp).setOnClickListener(this);
        findViewById(R.id.moreApps).setOnClickListener(this);
    }
}
