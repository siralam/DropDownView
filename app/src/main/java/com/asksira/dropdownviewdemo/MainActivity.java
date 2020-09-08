package com.asksira.dropdownviewdemo;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.asksira.dropdownview.DropDownView;
import com.asksira.dropdownview.OnDropDownSelectionListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    DropDownView dropDownView;
    RecyclerView recyclerView;
    JustAnAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dropDownView = findViewById(R.id.dropdownview);
        recyclerView = findViewById(R.id.recyclerview);

        adapter = new JustAnAdapter(this, generateList());
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(adapter);

        dropDownView.setDropDownListItem(generateFilterList());
        dropDownView.setOnSelectionListener(new OnDropDownSelectionListener() {
            @Override
            public void onItemSelected(DropDownView view, int position) {
                switch (position) {
                    case 1:
                        adapter.setStringList(generateOddList());
                        break;
                    case 2:
                        adapter.setStringList(generateEvenList());
                        break;
                    default:
                        adapter.setStringList(generateList());
                        break;
                }
            }
        });
    }

    private List<String> generateList () {
        List<String> list = new ArrayList<>();
        for (int i=1; i < 51; i++) {
            list.add(String.valueOf(i));
        }
        return list;
    }

    private List<String> generateOddList () {
        List<String> list = new ArrayList<>();
        for (int i=1; i < 51; i = i + 2) {
            list.add(String.valueOf(i));
        }
        return list;
    }

    private List<String> generateEvenList () {
        List<String> list = new ArrayList<>();
        for (int i=2; i < 51; i = i + 2) {
            list.add(String.valueOf(i));
        }
        return list;
    }

    private List<String> generateFilterList () {
        List<String> list = new ArrayList<>();
        list.add("All");
        list.add("odd");
        list.add("even");
        return list;
    }
}
