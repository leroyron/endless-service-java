package com.nassosaic.tolo.endless;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Build;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public final class MainActivity extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);
        this.setTitle("Endless Service");

        final Button startButton = (Button) findViewById(R.id.btnStartService);
        startButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Utils.log("START THE FOREGROUND SERVICE ON DEMAND");
                actionOnService(Actions.START);
            }
        });

        final Button stopButton = (Button) findViewById(R.id.btnStopService);
        stopButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Utils.log("STOP THE FOREGROUND SERVICE ON DEMAND");
                actionOnService(Actions.STOP);
            }
        });
    }

    private final void actionOnService(Actions action) {
        if (ServiceTracker.getServiceState(this) == ServiceState.STOPPED && action == Actions.STOP) return;

            Intent intent = new Intent(this, EndlessService.class);
            intent.setAction(action.name());

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Utils.log("Starting the service in >=26 Mode");
                this.startForegroundService(intent);
                return;
            }

            Utils.log("Starting the service in < 26 Mode");
            this.startService(intent);

    }
}
