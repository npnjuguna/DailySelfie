package com.njuguna.dailyselfie.ui;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.njuguna.dailyselfie.R;
import com.njuguna.dailyselfie.app.SelfieApplication;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, SelfiesFragment.OnSelfiesFragmentInteractionListener {

    public static final String EXTRA_EXIT_APPLICATION = "exit_application";

    private TextView mFullName, mUserName;
    private ImageButton mLogin;
    private SelfieApplication mSelfieApplication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SelfieDetailActivity.startForAction(MainActivity.this, Intent.ACTION_INSERT, null);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mFullName = (TextView) findViewById(R.id.full_name);
        mUserName = (TextView) findViewById(R.id.user_name);
        mLogin = (ImageButton) findViewById(R.id.login);

        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            }
        });

        mSelfieApplication = (SelfieApplication) getApplication();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (null == mSelfieApplication.getUser()) {
            LoginActivity.startActionCreateNewSession(this);
        } else {
            mFullName.setText(mSelfieApplication.getUser().getFullname());
            mUserName.setText(mSelfieApplication.getUser().getEmail());
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_selfies:
                break;
            case R.id.nav_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            default:
                Toast.makeText(getApplicationContext(), "Coming soon...", Toast.LENGTH_LONG).show();
                break;
        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (intent.getBooleanExtra(EXTRA_EXIT_APPLICATION, false)) {
            finish();
        }
    }

    @Override
    public void onSelfiesFragmentInteraction(long rowID) {
        Intent intent = new Intent(this, SelfieDetailActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        intent.putExtra(SelfieDetailActivity.ARG_SELECTED_SELFIE_ID, rowID);
        startActivity(intent);
    }
}
