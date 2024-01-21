package com.vilisvit.things;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
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

        if (delay.equals(context.getResources().getString(R.string.spinner_option_in_1_hour))) {
            alarmDatetime.add(Calendar.HOUR_OF_DAY, -1);
        } else if (delay.equals(context.getResources().getString(R.string.spinner_option_in_6_hours))) {
            alarmDatetime.add(Calendar.HOUR_OF_DAY, -6);
        } else if (delay.equals(context.getResources().getString(R.string.spinner_option_in_1_day))) {
            alarmDatetime.add(Calendar.DAY_OF_MONTH, -1);
        } else if (delay.equals(context.getResources().getString(R.string.spinner_option_in_3_days))) {
            alarmDatetime.add(Calendar.DAY_OF_MONTH, -3);
        } else if (delay.equals(context.getResources().getString(R.string.spinner_option_in_1_week))) {
            alarmDatetime.add(Calendar.WEEK_OF_YEAR, -1);
        } else if (delay.equals(context.getResources().getString(R.string.spinner_option_in_1_month))) {
            alarmDatetime.add(Calendar.MONTH, -1);
        }

        //Toast.makeText(context, "Notification will be shown at: " + alarmDatetime.getTime(), Toast.LENGTH_SHORT).show();
        return alarmDatetime;
    }

    public void startAlarm(Calendar calendar, String id, String title, String datetime, int priority) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("id", id);
        intent.putExtra("title", title);
        intent.putExtra("datetime", datetime);
        intent.putExtra("priority", priority);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, Integer.parseInt(id), intent, PendingIntent.FLAG_MUTABLE);
        if (Build.VERSION.SDK_INT < 31 || alarmManager.canScheduleExactAlarms()) {
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
