package com.vilisvit.things;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import android.Manifest;
import android.widget.ToggleButton;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.color.MaterialColors;

import java.util.Calendar;
import java.util.Set;

public class EditActivity extends AppCompatActivity {

    EditText titleInput, descriptionInput, editDate, editTime;
    String id, title, description, date="", time="", notification_time, parent_id;
    int priority;
    boolean isThingCompleted;

    String selectedDateTime;
    MaterialButtonToggleGroup priorityToggleButton;
    Button saveButton;
    TextView notification_textView;
    AlarmManager alarmManager;

    final int POST_NOTIFICATIONS_REQUEST_CODE = 101;
    Spinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        if (getIntent().hasExtra("parent_id")) {
            parent_id = getIntent().getStringExtra("parent_id"); //necessary extra
        } else {
            Log.e("sus", "intent has no extra 'parent_id'");
        }

        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        titleInput = findViewById(R.id.editTextTitle);
        titleInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                titleInput.setBackgroundTintList(ColorStateList.valueOf(MaterialColors.getColor(titleInput, androidx.appcompat.R.attr.backgroundTint)));
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {}
        });
        descriptionInput = findViewById(R.id.editTextDescription);
        editDate = findViewById(R.id.datePicker);
        editDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int year, monthOfYear, dayOfMonth;
                try {
                    String fullDate = editDate.getText().toString();
                    year = Integer.parseInt(fullDate.split("-")[2]);
                    monthOfYear = Integer.parseInt(fullDate.split("-")[1]);
                    dayOfMonth = Integer.parseInt(fullDate.split("-")[0]);
                } catch (Exception e) {
                    final Calendar c = Calendar.getInstance();
                    year = c.get(Calendar.YEAR);
                    monthOfYear = c.get(Calendar.MONTH)+1;
                    dayOfMonth = c.get(Calendar.DAY_OF_MONTH);
                }
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        EditActivity.this,
                        R.style.CustomDateTimePickerDialogStyle,
                        new DatePickerDialog.OnDateSetListener() {
                            @SuppressLint({"SetTextI18n", "DefaultLocale"})
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                editDate.setText(String.format("%02d-%02d-%d", dayOfMonth, monthOfYear + 1, year));
                                if (!isThingCompleted) {  //status is true when the thing is completed
                                    notification_textView.setEnabled(true);
                                    spinner.setEnabled(true);
                                }
                            }
                        },
                        year, monthOfYear-1, dayOfMonth);
                datePickerDialog.show();
            }
        });
        editDate.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                PopupMenu popupMenu = new PopupMenu(EditActivity.this, editDate, Gravity.END);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId() == R.id.delete_date_or_time_option) {
                            editDate.setText("");
                            if (editTime.getText().toString().isEmpty()) {
                                notification_textView.setEnabled(false);
                                spinner.setEnabled(false);
                                spinner.setSelection(0);
                            }
                            return true;
                        }
                        return false;
                    }
                });
                popupMenu.inflate(R.menu.on_date_or_time_click_menu);
                popupMenu.show();
                return true;
            }
        });

        editTime = findViewById(R.id.timePicker);
        editTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int hour, minute;
                try {
                    String fullTime = editTime.getText().toString();
                    hour = Integer.parseInt(fullTime.split(":")[0]);
                    minute = Integer.parseInt(fullTime.split(":")[1]);
                } catch (Exception e) {
                    final Calendar c = Calendar.getInstance();
                    hour = c.get(Calendar.HOUR_OF_DAY);
                    minute = c.get(Calendar.MINUTE);
                }
                TimePickerDialog timePickerDialog = new TimePickerDialog(
                        EditActivity.this,
                        R.style.CustomDateTimePickerDialogStyle,
                        new TimePickerDialog.OnTimeSetListener() {
                            @SuppressLint({"SetTextI18n", "DefaultLocale"})
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                editTime.setText(String.format("%02d:%02d", hourOfDay, minute));
                                if (!isThingCompleted) {  //status is true when the thing is completed
                                    notification_textView.setEnabled(true);
                                    spinner.setEnabled(true);
                                }
                            }
                        },
                        hour, minute, true);
                timePickerDialog.show();
            }
        });
        editTime.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                PopupMenu popupMenu = new PopupMenu(EditActivity.this, editTime, Gravity.END);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId() == R.id.delete_date_or_time_option) {
                            editTime.setText("");
                            return true;
                        }
                        return false;
                    }
                });
                popupMenu.inflate(R.menu.on_date_or_time_click_menu);
                popupMenu.show();
                return true;
            }
        });

        saveButton = findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData();
            }
        });
        spinner = findViewById(R.id.notificationSpinner);
        notification_textView = findViewById(R.id.notificationTextView);

        notification_textView.setEnabled(false);
        spinner.setEnabled(false);
        spinner.setSelection(0);

        priorityToggleButton = findViewById(R.id.priorityToggleButton);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.notification_times_array,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        if (getIntent().hasExtra("id")) {
            getIntentData();
            if (!notification_time.equals(getResources().getStringArray(R.array.notification_times_array)[0])) {
                MyAlarmHelper alarmHelper = new MyAlarmHelper(this, alarmManager);
                alarmHelper.cancelAlarm(id, titleInput.getText().toString().trim(), selectedDateTime);
            }
        }
    }

    private boolean allFieldsAreCorrect() {
        if (titleInput.getText().toString().trim().isEmpty()){
            titleInput.setError("Title is required");
            titleInput.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.red)));
            return false;
        }
        return  true;
    }

    private void getIntentData () {
        if (getIntent().hasExtra("id")) {
            id = getIntent().getStringExtra("id");
            title = getIntent().getStringExtra("title");
            description = getIntent().getStringExtra("description");
            String datetime = getIntent().getStringExtra("datetime");
            priority = getIntent().getIntExtra("priority", 0);
            isThingCompleted = getIntent().getBooleanExtra("status", false);
            if (datetime != null && !datetime.isEmpty()) {
                date = datetime.split(" ")[0];
                try {
                    time = datetime.split(" ")[1];
                } catch (ArrayIndexOutOfBoundsException e) {
                    time = "";
                }
            }
            notification_time = getIntent().getStringExtra("notification_time");

            titleInput.setText(title);
            descriptionInput.setText(description);
            editDate.setText(date);
            editTime.setText(time);
            if (!date.isEmpty() && !time.isEmpty() && !isThingCompleted) {
                notification_textView.setEnabled(true);
                spinner.setEnabled(true);
            }
            ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) spinner.getAdapter();
            if (adapter != null) {
                int position = adapter.getPosition(notification_time);
                if (position != -1) {
                    spinner.setSelection(position);
                } else {
                    spinner.setSelection(0);
                }
            }
        } else {
            Toast.makeText(EditActivity.this, "Unable to get intent data", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveData() {
        MyDatabaseHelper myDBHelper = new MyDatabaseHelper(EditActivity.this);
        int checkedId = priorityToggleButton.getCheckedButtonId();
        if (checkedId == R.id.lowPriorityButton) {
            priority = 0;
        } else if (checkedId == R.id.mediumPriorityButton) {
            priority = 1;
        } else if (checkedId == R.id.highPriorityButton) {
            priority = 2;
        }
        if (editDate.getText().toString().isEmpty() && !editTime.getText().toString().isEmpty()) {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int monthOfYear = c.get(Calendar.MONTH)+1;
            int dayOfMonth = c.get(Calendar.DAY_OF_MONTH);
            editDate.setText(String.format("%02d-%02d-%d", dayOfMonth, monthOfYear, year));
        }
        selectedDateTime = (editDate.getText().toString() + " " + editTime.getText().toString()).trim();
        if (allFieldsAreCorrect()) {
            if (!spinner.getSelectedItem().toString().equals(getResources().getStringArray(R.array.notification_times_array)[0]) && !isThingCompleted) {
                if (permissionsGranted()) {

                    if (getIntent().hasExtra("id")) {
                        myDBHelper.updateData(id, titleInput.getText().toString().trim(), descriptionInput.getText().toString().trim(), selectedDateTime, spinner.getSelectedItem().toString(), priority);
                    } else {
                        id = String.valueOf(myDBHelper.addThing(titleInput.getText().toString().trim(), descriptionInput.getText().toString().trim(), selectedDateTime, spinner.getSelectedItem().toString(), priority, false, parent_id));
                    }

                    notification_time = spinner.getSelectedItem().toString();

                    MyAlarmHelper alarmHelper = new MyAlarmHelper(this, alarmManager);
                    Calendar alarmDatetime = alarmHelper.calculateAlarmDatetime(selectedDateTime, notification_time);
                    Calendar currentDatetime = Calendar.getInstance();
                    currentDatetime.add(Calendar.MINUTE, -1);
                    if (alarmDatetime.after(currentDatetime)) {
                        alarmHelper.startAlarm(alarmDatetime, id, titleInput.getText().toString().trim(), selectedDateTime);
                    }

                    Intent intent = new Intent(EditActivity.this, MainActivity.class);
                    startActivity(intent);

                } else {
                    grantPermissions();
                }
            } else {

                if (getIntent().hasExtra("id")) {
                    myDBHelper.updateData(id, titleInput.getText().toString().trim(), descriptionInput.getText().toString().trim(), selectedDateTime, spinner.getSelectedItem().toString(), priority);
                } else {
                    id = String.valueOf(myDBHelper.addThing(titleInput.getText().toString().trim(), descriptionInput.getText().toString().trim(), selectedDateTime, spinner.getSelectedItem().toString(), priority, false, parent_id));
                }
                Intent intent = new Intent(EditActivity.this, MainActivity.class);
                startActivity(intent);

            }
        }
    }


    private boolean permissionsGranted() {
        if (Build.VERSION.SDK_INT >= 31) {
            if (Build.VERSION.SDK_INT >= 33) {
                return (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED &&
                        alarmManager.canScheduleExactAlarms());
            } else {
                return alarmManager.canScheduleExactAlarms();
            }
        } else {
            return true;
        }
    }

    private void grantPermissions() {
        if (Build.VERSION.SDK_INT >= 31 && !alarmManager.canScheduleExactAlarms()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("To use notifications you should allow schedule alarm permission in settings")
                    .setTitle("Permission required")
                    .setCancelable(false)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                            Uri uri = Uri.fromParts("package", getPackageName(), null);
                            intent.setData(uri);
                            startActivity(intent);

                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            builder.show();
        }
        if (Build.VERSION.SDK_INT >= 33) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                Log.d("permissions", "Post notification permission already granted");
            } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.POST_NOTIFICATIONS)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("You should allow notification for this app")
                        .setTitle("Permission required")
                        .setCancelable(false)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(EditActivity.this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, POST_NOTIFICATIONS_REQUEST_CODE);
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                builder.show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, POST_NOTIFICATIONS_REQUEST_CODE);
            }
        }
    }
    /*
    @Override
    public void onRequestPermissionsResult (int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == POST_NOTIFICATIONS_REQUEST_CODE) {
                Log.d("permissions", "post notifications permission granted");
            }
        }
    }
    */
}