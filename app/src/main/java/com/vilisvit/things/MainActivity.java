package com.vilisvit.things;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    public TextView noData;

    private MyDatabaseHelper myDatabaseHelper;
    private ArrayList<String> ids, titles, descriptions, datetimes, notification_times, parent_ids;
    private ArrayList<Integer> priorities;
    private ArrayList<Boolean> statuses;
    private CustomAdapter customAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(MainActivity.this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        noData=findViewById(R.id.noDataTextview);
        FloatingActionButton floatingActionButton = findViewById(R.id.add_button);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, EditActivity.class);
                intent.putExtra("parent_id", "-1");
                startActivity(intent);
            }
        });

        myDatabaseHelper = new MyDatabaseHelper(MainActivity.this);
        ids = new ArrayList<>();
        titles = new ArrayList<>();
        descriptions = new ArrayList<>();
        datetimes = new ArrayList<>();
        notification_times = new ArrayList<>();
        priorities = new ArrayList<>();
        statuses = new ArrayList<>();
        parent_ids = new ArrayList<>();

        storeDataInArrays();

        if (getIntent().hasExtra("highlighted_element_id")) {
            customAdapter = new CustomAdapter(MainActivity.this, R.layout.recycle_view_row, null, null,  ids, titles, descriptions, datetimes, notification_times, priorities, statuses, parent_ids, getIntent().getStringExtra("highlighted_element_id"));
        } else {
            customAdapter = new CustomAdapter(MainActivity.this, R.layout.recycle_view_row, null, null, ids, titles, descriptions, datetimes, notification_times, priorities, statuses, parent_ids);
        }

        recyclerView.setLayoutManager( new LinearLayoutManager(MainActivity.this));
        recyclerView.setAdapter(customAdapter);
        ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
    }

    @SuppressLint("Range")
    private void storeDataInArrays() {
        Cursor cursor = myDatabaseHelper.readAllData();
        if (cursor.getCount() == 0) {
            noData.setVisibility(View.VISIBLE);
        } else {
            noData.setVisibility(View.GONE);
            cursor.moveToFirst();
            do {
                if (cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_PARENT_ID)).equals("-1")) {
                    ids.add(cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_ID)));
                    titles.add(cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_TITLE)));
                    descriptions.add(cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_DESCRIPTION)));
                    datetimes.add(cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_DATETIME)));
                    notification_times.add(cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_NOTIFICATION_TIME)));
                    priorities.add(cursor.getInt(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_PRIORITY)));
                    statuses.add(cursor.getInt(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_STATUS)) > 0);
                    parent_ids.add(cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_PARENT_ID)));
                }
            } while (cursor.moveToNext());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.delete_all_option) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getResources().getString(R.string.delete_all_title));
            builder.setMessage(getResources().getString(R.string.delete_all_message));
            builder.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    customAdapter.deleteAllData();
                }
            });
            builder.setNegativeButton(getResources().getString(R.string.no), (dialog, which) -> {

            });
            builder.create().show();
        }
        return super.onOptionsItemSelected(item);
    }
}