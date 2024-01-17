package com.vilisvit.things;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    public TextView noData;
    private FloatingActionButton floatingActionButton;

    private MyDatabaseHelper myDatabaseHelper;
    private ArrayList<String> ids, datetimes_added, titles, descriptions, datetimes, notification_times;
    private ArrayList<Boolean> statuses;
    private CustomAdapter customAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(MainActivity.this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recycler_view);
        noData=findViewById(R.id.noDataTextview);
        floatingActionButton = findViewById(R.id.add_button);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, EditActivity.class);
                startActivity(intent);
            }
        });

        myDatabaseHelper = new MyDatabaseHelper(MainActivity.this);
        ids = new ArrayList<>();
        datetimes_added = new ArrayList<>();
        titles = new ArrayList<>();
        descriptions = new ArrayList<>();
        datetimes = new ArrayList<>();
        notification_times = new ArrayList<>();
        statuses = new ArrayList<>();

        storeDataInArrays();

        if (getIntent().hasExtra("highlighted_element_id")) {
            customAdapter = new CustomAdapter(MainActivity.this, ids, titles, descriptions, datetimes, notification_times, statuses, getIntent().getStringExtra("highlighted_element_id"));
        } else {
            customAdapter = new CustomAdapter(MainActivity.this, ids, titles, descriptions, datetimes, notification_times, statuses);
        }

        recyclerView.setAdapter(customAdapter);
        recyclerView.setLayoutManager( new LinearLayoutManager(MainActivity.this));

        if (getIntent().hasExtra("highlighted_element_id")) {
            recyclerView.getLayoutManager().scrollToPosition(ids.indexOf(getIntent().getStringExtra("highlighted_element_id")));
        }
    }

    @SuppressLint("Range")
    private void storeDataInArrays() {
        Cursor cursor = myDatabaseHelper.readAllData();
        if (cursor.getCount() == 0) {
            noData.setVisibility(View.VISIBLE);
        } else {
            noData.setVisibility(View.GONE);
            while (cursor.moveToNext()) {
                ids.add(cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_ID)));
                datetimes_added.add(cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_DATETIME_ADDED)));
                titles.add(cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_TITLE)));
                descriptions.add(cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_DESCRIPTION)));
                datetimes.add(cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_DATETIME)));
                notification_times.add(cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_NOTIFICATION_TIME)));
                statuses.add(cursor.getInt(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_STATUS))>0);
            }
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
            builder.setTitle("Delete all?");
            builder.setMessage("Are you sure you want to delete all things?");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    customAdapter.deleteAllData();
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            builder.create().show();
        }
        return super.onOptionsItemSelected(item);
    }
}