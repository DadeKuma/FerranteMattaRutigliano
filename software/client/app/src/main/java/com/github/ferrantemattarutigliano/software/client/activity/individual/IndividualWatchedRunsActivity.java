package com.github.ferrantemattarutigliano.software.client.activity.individual;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.ferrantemattarutigliano.software.client.R;
import com.github.ferrantemattarutigliano.software.client.model.RunDTO;
import com.github.ferrantemattarutigliano.software.client.presenter.IndividualWatchedRunsPresenter;
import com.github.ferrantemattarutigliano.software.client.util.LoadingScreen;
import com.github.ferrantemattarutigliano.software.client.view.IndividualWatchedRunsView;

import java.sql.Date;
import java.sql.Time;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Collection;

public class IndividualWatchedRunsActivity extends AppCompatActivity implements IndividualWatchedRunsView {
    private LoadingScreen loadingScreen;
    private AlertDialog.Builder dialogFactory;
    private IndividualWatchedRunsPresenter individualWatchedRunsPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_individual_watched_runs);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //show back button on toolbar
        individualWatchedRunsPresenter = new IndividualWatchedRunsPresenter(this);
        dialogFactory = new AlertDialog.Builder(this);

        ViewGroup layout = findViewById(R.id.layout_individual_watched_runs);
        loadingScreen = new LoadingScreen(layout, "Getting data...");
        loadingScreen.show();
        individualWatchedRunsPresenter.doFetchRun();
    }

    @Override //finish activity if back button is clicked
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void noWatchedRuns() {
        ViewGroup container = findViewById(R.id.container_watched_runs);
        TextView textView = new TextView(getApplicationContext());
        CharSequence text = "No run watched yet!";
        textView.setText(text);
        container.addView(textView);
        loadingScreen.hide();
    }

    @Override
    public void onRunFetch(Collection<RunDTO> output) {
        ViewGroup container = findViewById(R.id.container_watched_runs);
        container.removeAllViews();
        for (RunDTO runDTO : output) {
            LinearLayout linearLayout = new LinearLayout(getApplicationContext());
            linearLayout.setOrientation(LinearLayout.HORIZONTAL);
            //create title
            TextView titleView = new TextView(getApplicationContext());
            titleView.setText(runDTO.getTitle());
            linearLayout.addView(titleView);
            //create buttons
            final Long runId = runDTO.getId();
            Button watchButton = new Button(getApplicationContext());
            CharSequence watchText = "Watch";
            final Date runDate = runDTO.getDate();
            final Time runTime = runDTO.getTime();
            watchButton.setText(watchText);
            watchButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    java.util.Date date = new java.util.Date();
                    Date currentDate = new Date(date.getTime());
                    Time currentTime = new Time(date.getTime());
                    boolean isRunStarted = runDate.before(currentDate) ||
                            (runDate.equals(currentDate) && runTime.before(currentTime));
                    if(!isRunStarted){
                        dialogFactory.setTitle("Run not started")
                                .setMessage("This run is not started yet. Wait until:" +
                                        "\n" + runDate + "\n" + runTime)
                                .setPositiveButton("Okay", null)
                                .show();
                    }
                    else {
                        loadingScreen.show();
                        //todo add subscription to the run and change activity in the map
                    }
                }
            });
            Button unwatchButton = new Button(getApplicationContext());
            CharSequence unwatchText = "Unwatch";
            unwatchButton.setText(unwatchText);
            unwatchButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    loadingScreen.show();
                    individualWatchedRunsPresenter.unwatchRun(runId);
                }
            });
            linearLayout.addView(watchButton);
            linearLayout.addView(unwatchButton);
            container.addView(linearLayout);
        }
        loadingScreen.hide();
    }

    @Override
    public void onRunUnwatch(String message) {
        dialogFactory.setTitle("Run unwatched")
                    .setMessage(message)
                    .setPositiveButton("Okay", null)
                    .show();
        individualWatchedRunsPresenter.doFetchRun();
    }
}