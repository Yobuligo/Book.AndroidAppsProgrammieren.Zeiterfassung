package com.yobuligo.zeiterfassung;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.yobuligo.zeiterfassung.adapter.DemoDataAdapter;

public class DemoActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private DemoDataAdapter demoDataAdapter;
    private DemoData demoData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);

        demoData = new DemoData();
        demoData.addName("Max");
        demoData.addName("Fritz");
        demoData.addName("Charlie");

        recyclerView = findViewById(R.id.DemoDataList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        demoDataAdapter = new DemoDataAdapter(demoData);
        recyclerView.setAdapter(demoDataAdapter);
    }
}
