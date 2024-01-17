package com.vilisvit.things;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.Calendar;

public class MyAlarmHelper {
    Context context;
    AlarmManager alarmManager;
    MyAlarmHelper (Context context, AlarmManager alarmManager) {
        this.context=context;
        this.alarmManager=alarmManager;
    }

    public Calendar calculateAlarmDatetime(String datetime, String delay) {

        Calendar alarmDatetime = Calendar.getInstance();
        String setDate = datetime.split(" ")[0];
        int year = Integer.parseInt(setDate.split("-")[2]);
        int month = Integer.parseInt(setDate.split("-")[1]);
        int day = Integer.parseInt(setDate.split("-")[0]);
        int hour, minute;
        try {
            String setTime;
            setTime = datetime.split(" ")[1];
            hour = Integer.parseInt(setTime.split(":")[0]);
            minute = Integer.parseInt(setTime.split(":")[1]);
        } catch (ArrayIndexOutOfBoundsException e) {
            hour = 0;
            minute = 0;
        }
        alarmDatetime.set(Calendar.YEAR, year);
        alarmDatetime.set(Calendar.MONTH, month - 1);
        alarmDatetime.set(Calendar.DAY_OF_MONTH, day);
        alarmDatetime.set(Calendar.HOUR_OF_DAY, hour);
        alarmDatetime.set(Calendar.MINUTE, minute);
        alarmDatetime.set(Calendar.SECOND, 0);
        /*
        switch (delay) {
            case "in 1 hour":
                alarmDatetime.add(Calendar.HOUR_OF_DAY, -1);
                break;
            case "in 6 hours":
                alarmDatetime.add(Calendar.HOUR_OF_DAY, -6);
                break;
            case "in 1 day":
                alarmDatetime.add(Calendar.DAY_OF_MONTH, -1);
                break;
            case "in 3 days":
                alarmDatetime.add(Calendar.DAY_OF_MONTH, -3);
                break;
            case "in 1 week":
                alarmDatetime.add(Calendar.WEEK_OF_YEAR, -1);
                break;
            case "in 1 month":
                alarmDatetime.add(Calendar.MONTH, -1);
                break;

        }
        */
        //Toast.makeText(context, "Notification will be shown at: " + alarmDatetime.getTime(), Toast.LENGTH_SHORT).show();
        return alarmDatetime;
    }

    public void startAlarm(Calendar calendar, String id, String title, String datetime) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("id", id);
        intent.putExtra("title", title);
        intent.putExtra("datetime", datetime);

        Log.d("intent_extra", "put extra - id: " + id + ", title: " + title + ", datetime: " + datetime);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, Integer.parseInt(id), intent, PendingIntent.FLAG_MUTABLE);
        if (Build.VERSION.SDK_INT >= 31 && alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            Log.d("alarm", "alarm successfully set to:" + calendar.getTime());
        } else {
            Toast.makeText(context, "Unable to setExact() alarm", Toast.LENGTH_SHORT).show();
        }
    }
    public void cancelAlarm(String id, String title, String datetime) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("id", id);
        intent.putExtra("title", title);
        intent.putExtra("datetime", datetime);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, Integer.parseInt(id), intent, PendingIntent.FLAG_MUTABLE);
        try {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
            Log.d("alarm", "alarm successfully canceled");
        } catch (Exception e) {
            Toast.makeText(context, "Unable to cancel alarm", Toast.LENGTH_SHORT).show();
        }
    }
}
