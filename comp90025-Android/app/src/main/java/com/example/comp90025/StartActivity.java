//Author: Shaobo Wang
//shaobow@student.unimelb.edu.au

package com.example.comp90025;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.comp90025.util.Logger;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class StartActivity extends AppCompatActivity {
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference databaseReference = database.getReference();
    private FirebaseAuth auth;
    public static String[] finalResult = new String[100];
    public static int count= 0;

    private Button mBtnRecord, mBtnHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        mBtnRecord = findViewById(R.id.btn_recorder);
        mBtnHistory = findViewById(R.id.btn_history);
        auth = FirebaseAuth.getInstance();
        //use auth to retrieve the data from this username
        final FirebaseUser fu = auth.getCurrentUser();
        DatabaseReference hist = databaseReference.child("user").child(fu.getUid().trim());
        hist.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // analysis the json string to string list
                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                    String address = ds.child("userid").getValue(String.class);
                    String name = ds.child("result").getValue(String.class);
                    String[] list = name.split("],");
                    list[0] = list[0].replaceAll("\\[","");
                    String[] list1 = list[0].split(",");
                    int ppl = list1.length;
                    list[2] = list[2].replaceAll("\\[","");
                    String[] list2 = list[2].split(",");
                    String totalTime = list2[list2.length-1];
                    finalResult[count] = "Userid : " + address + ", Total people: " + ppl + ", Total Time: " + totalTime;
                    count++;
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(StartActivity.this, "cannot gain the data",Toast.LENGTH_LONG).show();
            }


        });
        setListeners();
    }

    private void setListeners() {
        OnClick onClick = new OnClick();
        mBtnRecord.setOnClickListener(onClick);
        mBtnHistory.setOnClickListener(onClick);
    }

    private class OnClick implements View.OnClickListener{

        public void onClick(View v) {
            Intent intent = null;
            switch ((v.getId())) {
                case R.id.btn_recorder:
                // switch to recorder_Activity
                    intent = new Intent(StartActivity.this,RecordActivity.class);
                    startActivity(intent);
                    break;
                case R.id.btn_history:
                    intent = new Intent(StartActivity.this,HistoryActivity.class);
                    startActivity(intent);
                    break;


            }
        }
    }
}
