package com.robot_partner.camera_button;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.robot_partner.camera_button.R.layout.activity_main);
        Button btn = (Button) findViewById(com.robot_partner.camera_button.R.id.btn_start);
        btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                startService(new Intent(MainActivity.this, CameraButtonService.class));
            }

        });
        btn = (Button) findViewById(com.robot_partner.camera_button.R.id.btn_stop);
        btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                stopService(new Intent(MainActivity.this, CameraButtonService.class));
            }

        });
    }
}
