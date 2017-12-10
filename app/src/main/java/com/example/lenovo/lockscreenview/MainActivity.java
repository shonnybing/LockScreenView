package com.example.lenovo.lockscreenview;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LockScreenViewGroup lockScreenViewGroup = (LockScreenViewGroup) findViewById(R.id.lockScreenViewGroup);
        int[] answers = {1,2,3,5,7,8,9};
        lockScreenViewGroup.setAnswers(answers);
    }
}
