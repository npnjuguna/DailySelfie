package com.njuguna.dailyselfie.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.njuguna.dailyselfie.R;

public class SelfieDetailActivity extends AppCompatActivity {

    public static final String ARG_SELECTED_SELFIE_ID = "selected_selfie_id";

    public static void startForAction(Context context, String intentAction, Long selectedSelfieId) {
        Intent intent = new Intent(context, SelfieDetailActivity.class);
        if (null != selectedSelfieId) intent.putExtra(ARG_SELECTED_SELFIE_ID, selectedSelfieId.longValue());
        intent.setAction(intentAction);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selfie_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        String intentAction = getIntent().getAction();
        final long rowID = getIntent().getLongExtra(ARG_SELECTED_SELFIE_ID, 0);
        if (savedInstanceState == null) {
            if ((intentAction.equals(Intent.ACTION_INSERT)) && (rowID == 0)) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.container, SelfieDetailEditFragment.newInstance(intentAction, null))
                        .commit();
            } else if ((intentAction.equals(Intent.ACTION_EDIT)) && (rowID > 0)) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.container, SelfieDetailEditFragment.newInstance(intentAction, rowID))
                        .commit();
            } else if ((intentAction.equals(Intent.ACTION_VIEW)) && (rowID > 0)) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.container, SelfieDetailEditFragment.newInstance(intentAction, rowID))
                        .commit();
            }

        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SelfieDetailActivity.startForAction(SelfieDetailActivity.this, Intent.ACTION_EDIT, rowID);
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (!intentAction.equals(Intent.ACTION_VIEW)) fab.setVisibility(View.GONE);

    }

}
