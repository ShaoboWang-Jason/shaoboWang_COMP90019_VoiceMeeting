//Author: Shaobo Wang
//shaobow@student.unimelb.edu.au

package com.example.comp90025;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button mainreg;
    private Button mainlogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainreg = findViewById(R.id.mainreg);
        mainlogin = findViewById(R.id.mainlogin);
        setLisener();
    }

    private void setLisener() {

        // turn to different class
        mainlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent;
                intent = new Intent(MainActivity.this,LoginActivity.class);
                startActivity(intent);
            }
        });

        mainreg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent;
                intent = new Intent(MainActivity.this,RegisterActivity.class);
                startActivity(intent);
            }
        });
    }
}
