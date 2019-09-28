package com.mcal.kotlin;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.BillingProcessor.IBillingHandler;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.mcal.kotlin.adapters.ListAdapter;
import com.mcal.kotlin.data.ListMode;
import com.mcal.kotlin.data.NightMode;
import com.mcal.kotlin.data.Preferences;
import com.mcal.kotlin.model.BaseActivity;
import com.mcal.kotlin.module.Ads;
import com.mcal.kotlin.module.AppUpdater;
import com.mcal.kotlin.module.Dialogs;
import com.mcal.kotlin.module.ListParser;
import com.mcal.kotlin.utils.Utils;
import com.mcal.kotlin.view.BookmarksFragment;
import com.mcal.kotlin.view.MainView;

import java.util.ArrayList;

import es.dmoral.toasty.Toasty;
import ru.svolf.melissa.MainMenuAdapter;
import ru.svolf.melissa.MainMenuItem;
import ru.svolf.melissa.MainMenuItems;
import ru.svolf.melissa.sheet.SweetContentDialog;

import static com.anjlab.android.iab.v3.Constants.BILLING_RESPONSE_RESULT_USER_CANCELED;
import static com.mcal.kotlin.data.Constants.IS_PREMIUM;
import static com.mcal.kotlin.data.Constants.LK;
import static com.mcal.kotlin.data.Constants.MORE_APPS;
import static com.mcal.kotlin.data.Constants.POSITION;
import static com.mcal.kotlin.data.Constants.PREMIUM;
import static com.mcal.kotlin.data.Constants.URL;
import static com.mcal.kotlin.data.Preferences.isOffline;

public class MainActivity extends BaseActivity implements MainView, SearchView.OnQueryTextListener, IBillingHandler {

    private LinearLayout adLayout;
    private BillingProcessor billing;
    private ListAdapter listAdapter;
    private Ads ads;
    private BottomSheetBehavior sheetBehavior;
    private boolean isAdsBlocked = false;
    private boolean isPremium;

    @Override
    public boolean onQueryTextSubmit(String p1) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String p1) {
        listAdapter.getFilter().filter(p1);
        return false;
    }

    @Override
    public void openLesson(String url, int position) {
        if (!isOffline() & !Utils.isNetworkAvailable()) {
            Dialogs.noConnectionError(this);
            return;
        }
        startActivityForResult(new Intent(this, LessonActivity.class)
                .putExtra(URL, url)
                .putExtra(POSITION, position), REQUEST_CODE_IS_READ);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sheetBehavior = BottomSheetBehavior.from(findViewById(R.id.bottomView));
        adLayout = findViewById(R.id.adLayout);

        RecyclerView lessons = (RecyclerView) getLayoutInflater().inflate(R.layout.recycler_view, null);

        if (ListMode.getCurrentMode().equals(ListMode.Mode.GRID)) {
            lessons.setLayoutManager(new GridLayoutManager(this, 3));
            lessons.setAdapter(listAdapter = new ListParser(this).getListAdapter());
            ((LinearLayout) findViewById(R.id.listContainer)).addView(lessons);
        } else {
            lessons.setLayoutManager(new LinearLayoutManager(this));
            lessons.setAdapter(listAdapter = new ListParser(this).getListAdapter());
            ((LinearLayout) findViewById(R.id.listContainer)).addView(lessons);
        }

        setupBottomSheet();

        ads = new Ads();
        billing = new BillingProcessor(this, LK, this);
        if (savedInstanceState == null)
            sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        new AppUpdater(this).execute();

        isPremium = getIntent().getBooleanExtra(IS_PREMIUM, false);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.continue_lesson).setVisible(Preferences.getBookmark() != null);
        return super.onPrepareOptionsMenu(menu);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        SearchView sv = (SearchView) menu.findItem(R.id.search).getActionView();
        sv.setOnQueryTextListener(this);
        ((MenuBuilder) menu).setOptionalIconsVisible(true);

        if (NightMode.getCurrentMode() == NightMode.Mode.DAY)
            menu.findItem(R.id.day_night).setIcon(R.drawable.ic_night);

        menu.findItem(R.id.search).setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                menu.findItem(R.id.continue_lesson).setVisible(false);
                menu.findItem(R.id.day_night).setVisible(false);
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                menu.findItem(R.id.continue_lesson).setVisible(true);
                menu.findItem(R.id.day_night).setVisible(true);
                return true;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.day_night:
                if (NightMode.getCurrentMode() == NightMode.Mode.DAY) {
                    NightMode.setMode(NightMode.Mode.NIGHT);
                    Preferences.setNightMode(true);
                } else {
                    NightMode.setMode(NightMode.Mode.DAY);
                    Preferences.setNightMode(false);
                }
                getDelegate().applyDayNight();
                break;
            case R.id.continue_lesson:
                if (isOffline() || Utils.isNetworkAvailable())
                    resumeLesson();
                else Dialogs.noConnectionError(this);
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (sheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED){
            sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else {
            if (time + 2000 > System.currentTimeMillis()) super.onBackPressed();
            else {
                Toasty.info(this, getString(R.string.press_back_once_more)).show();
                time = System.currentTimeMillis();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        billing.handleActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_IS_READ) {

            if (resultCode == RESULT_OK) {
                int position = data.getIntExtra(POSITION, 0);

                listAdapter.notifyItemChanged(position);
            }

            if (!Preferences.isRated()) Dialogs.rate(this);
            else if (!billing.isPurchased(PREMIUM)) {
                //ads.showInsAd();
            }
        }
    }

    private void resumeLesson() {
        startActivityForResult(new Intent(this, LessonActivity.class).
                putExtra(URL, Preferences.getBookmark()), REQUEST_CODE_IS_READ);
    }

    @Override
    public void onProductPurchased(@NonNull String productId, @Nullable TransactionDetails details) {
        Toasty.success(this, getString(R.string.p_a)).show();// premium_activated
        // FIXME: Рефрешнуть адаптер
    }

    @Override
    public void onPurchaseHistoryRestored() {

    }

    @Override
    public void onBillingError(int errorCode, @Nullable Throwable error) {
        if (errorCode == BILLING_RESPONSE_RESULT_USER_CANCELED) {
            Toasty.error(this, getString(R.string.purchase_canceled)).show();
            if (isAdsBlocked) System.exit(0);
        }
    }

    @Override
    public void onBillingInitialized() {
        if (!billing.isPurchased(PREMIUM)) {
            //adLayout.addView(ads.getBanner(this));
            ads.loadInterstitial(this);
            // FIXME: Рефрешнуть адаптер
            if (!billing.isPurchased(PREMIUM) & !ads.isAdsLoading()) {
                isAdsBlocked = true;
                adsBlocked();
            }
        }
    }

    public void adsBlocked() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.ads_blocked)
                .setPositiveButton(R.string.buy, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        billing.purchase(MainActivity.this, PREMIUM);
                    }
                })
                .setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        System.exit(0);
                    }
                })
                .setCancelable(false)
                .create().show();
    }

    private void setupBottomSheet(){
        //TextView caption = findViewById(R.id.caption);
        RecyclerView recycler = findViewById(R.id.list);

        ArrayList<MainMenuItem> menuItems = new ArrayList<>();

        //caption.setText(R.string.caption_lessons);

        menuItems.add(new MainMenuItem(R.drawable.bookmark, "#fdd835", getString(R.string.bookmarks), MainMenuItems.BOOKMARKS));
        menuItems.add(new MainMenuItem(R.drawable.settings, "#546e7a", getString(R.string.settings), MainMenuItems.SETTINGS));

        if (isPremium) {
            menuItems.add(new MainMenuItem(R.drawable.cash_multiple, "#43a047", getString(R.string.p), MainMenuItems.PREMIUM));
        }
        menuItems.add(new MainMenuItem(R.drawable.information, "#3949ab", getString(R.string.about), MainMenuItems.ABOUT));
        menuItems.add(new MainMenuItem(R.drawable.exit, "#e53935", getString(R.string.exit), MainMenuItems.EXIT));

        MainMenuAdapter adapter = new MainMenuAdapter(menuItems);
        adapter.setItemClickListener(new MainMenuAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(MainMenuItem menuItem, int position) {
                switch (menuItem.getAction()) {
                    case MainMenuItems.BOOKMARKS: {
                        new BookmarksFragment().show(getSupportFragmentManager(), null);
                        break;
                    }
                    case MainMenuItems.ABOUT: {
                        showAboutSheet();
                        break;
                    }
                    case MainMenuItems.SETTINGS: {
                        startActivityForResult(new Intent(MainActivity.this, SettingsActivity.class).putExtra(IS_PREMIUM, billing.isPurchased(PREMIUM)), REQUEST_CODE_SETTINGS);
                        break;
                    }
                    case MainMenuItems.EXIT: {
                        finish();
                        break;
                    }
                    case MainMenuItems.PREMIUM: {
                        billing.purchase(MainActivity.this, PREMIUM);
                        break;
                    }
                }
                sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });
        recycler.setAdapter(adapter);
    }

    private void showAboutSheet(){
        SweetContentDialog dialog = new SweetContentDialog(this);
        dialog.setTitle(getString(R.string.app_name) + " v." + BuildConfig.VERSION_NAME);
        dialog.setMessage(R.string.copyright);
        dialog.setPositive(R.drawable.star, getString(R.string.rate), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dialogs.rate(MainActivity.this);
            }
        });
        dialog.setNegative(R.drawable.google_play, getString(R.string.more_apps), new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(MORE_APPS)));
            }
        });
        dialog.show();

    }
}
