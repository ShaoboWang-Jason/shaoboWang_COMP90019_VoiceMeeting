//Author: Shaobo Wang
//shaobow@student.unimelb.edu.au

package com.example.comp90025;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity {

    private ListView listview;


    private StartActivity startActivity = new StartActivity();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        listview = findViewById(R.id.listview);

        // use listview to show the history
        final ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < startActivity.count; ++i) {
            list.add(startActivity.finalResult[i]);
        }
        ArrayAdapter adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                list);
        listview.setAdapter(adapter);
    }
}
