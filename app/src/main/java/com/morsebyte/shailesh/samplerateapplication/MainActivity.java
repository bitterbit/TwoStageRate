package com.morsebyte.shailesh.samplerateapplication;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.morsebyte.shailesh.twostagerating.TwoStageRate;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onResume() {
        super.onResume();
        TwoStageRate.with(MainActivity.this).showIfMeetsConditions();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        initTwoStage();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TwoStageRate twoStageRate = TwoStageRate.with(MainActivity.this);
                twoStageRate.showRatePromptDialog();

                //startActivity(new Intent(MainActivity.this, EventActivity.class));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initTwoStage() {
        TwoStageRate twoStageRate = TwoStageRate.with(this);
        twoStageRate.setInstallDays(5).setEventsTimes(3).setLaunchTimes(5);
        twoStageRate.resetOnDismiss(true).resetOnFeedBackDeclined(true).resetOnRatingDeclined(true);
        twoStageRate.setShowAppIcon(true);

        twoStageRate.setFeedbackDialogEmptyText("Empty text");

        twoStageRate.setRatePromptTitle("Title!");
        twoStageRate.setRatePromptSubmitText("sub");
        twoStageRate.setFeedbackDialogDescription("desc");
        twoStageRate.setConfirmRateDialogTitle("titlell");


        Log.i("TAG", "initTwoStage");

        twoStageRate.setListener(new TwoStageRate.FeedbackListener() {
            @Override
            public void onRatePromptSubmit(float rating) {
                Log.i("MYTAG", "rate prompt submit");
            }

            @Override
            public void onNegativeFeedbackSubmit(float rating, String message) {
                Log.i("MYTAG", "on negative feedback " + rating + " " + message);
            }

            @Override
            public void onPositiveFeedbackSubmit(float rating) {
                Log.i("MYTAG", "on positive feedback! " + rating);
            }
         });


    }
}
