package com.vilisvit.things;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.MyViewHolder> {

    private final Context context;
    private final ArrayList ids, titles, descriptions, datetimes, notification_times, priorities, statuses;
    private String highlighted_element_id;

    CustomAdapter (Context context,
                   ArrayList ids,
                   ArrayList titles,
                   ArrayList descriptions,
                   ArrayList datetimes,
                   ArrayList notification_times,
                   ArrayList priorities,
                   ArrayList statuses) {
        this.context = context;
        this.ids = ids;
        this.titles = titles;
        this.descriptions = descriptions;
        this.datetimes = datetimes;
        this.notification_times = notification_times;
        this.priorities = priorities;
        this.statuses = statuses;
    }

    CustomAdapter (Context context,
                   ArrayList ids,
                   ArrayList titles,
                   ArrayList descriptions,
                   ArrayList datetimes,
                   ArrayList notification_times,
                   ArrayList statuses,
                   ArrayList priorities,
                   String highlighted_element_id) {
        this.context = context;
        this.ids = ids;
        this.titles = titles;
        this.descriptions = descriptions;
        this.datetimes = datetimes;
        this.notification_times = notification_times;
        this.priorities = priorities;
        this.statuses = statuses;
        this.highlighted_element_id = highlighted_element_id;
    }



    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.recycle_view_row, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int position) {
        if ((Boolean) statuses.get(position)) {
            holder.title_text.setEnabled(false);
            holder.description_text.setEnabled(false);
            holder.datetime_text.setEnabled(false);
            holder.dateStaticText.setEnabled(false);
        } else {
            holder.title_text.setEnabled(true);
            holder.description_text.setEnabled(true);
            holder.datetime_text.setEnabled(true);
            holder.dateStaticText.setEnabled(true);
        }

        holder.title_text.setText(String.valueOf(titles.get(position)));
        holder.description_text.setText(String.valueOf(descriptions.get(position)).replaceAll("\\n", " "));
        holder.datetime_text.setText(String.valueOf(datetimes.get(position)));
        if (String.valueOf(datetimes.get(position)).trim().isEmpty()) {
            holder.datesTimesConstraintLayout.setVisibility(View.GONE);
        }
        holder.status_checkbox.setChecked((Boolean) statuses.get(position));
        holder.mainLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, EditActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("id", String.valueOf(ids.get(position)));
                intent.putExtra("title", String.valueOf(titles.get(position)));
                intent.putExtra("description", String.valueOf(descriptions.get(position)));
                intent.putExtra("datetime", String.valueOf(datetimes.get(position)).trim());
                intent.putExtra("notification_time", String.valueOf(notification_times.get(position)));
                intent.putExtra("priority", (Integer) priorities.get(position));
                intent.putExtra("status", (Boolean) statuses.get(position));
                context.startActivity(intent);
            }
        });
        holder.mainLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                PopupMenu popupMenu = new PopupMenu(context, holder.mainLayout, Gravity.END);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId() == R.id.delete_option) {
                            confirmDialogDeleting(position);
                            return true;
                        }
                        return false;
                    }
                });
                popupMenu.inflate(R.menu.on_item_click_menu);
                popupMenu.show();
                return true;
            }
        });
        holder.status_checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MyDatabaseHelper myDBHelper = new MyDatabaseHelper(context);
                myDBHelper.updateStatus(String.valueOf(ids.get(position)), isChecked);
                statuses.set(position, isChecked);
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                MyAlarmHelper alarmHelper = new MyAlarmHelper(context, alarmManager);
                if (isChecked) {
                    holder.title_text.setEnabled(false);
                    holder.description_text.setEnabled(false);
                    holder.datetime_text.setEnabled(false);
                    holder.dateStaticText.setEnabled(false);
                    if (!String.valueOf(notification_times.get(position)).equals(context.getResources().getStringArray(R.array.notification_times_array)[0])) {
                        alarmHelper.cancelAlarm(String.valueOf(ids.get(position)), String.valueOf(titles.get(position)), String.valueOf(datetimes.get(position)).trim());
                    }
                } else {
                    holder.title_text.setEnabled(true);
                    holder.description_text.setEnabled(true);
                    holder.datetime_text.setEnabled(true);
                    holder.dateStaticText.setEnabled(true);
                    if (!String.valueOf(notification_times.get(position)).equals(context.getResources().getStringArray(R.array.notification_times_array)[0])) {
                        Calendar alarmDatetime = alarmHelper.calculateAlarmDatetime(String.valueOf(datetimes.get(position)).trim(), String.valueOf(notification_times.get(position)));
                        Calendar currentDatetime = Calendar.getInstance();
                        currentDatetime.add(Calendar.MINUTE, -1);
                        if (alarmDatetime.after(currentDatetime)) {
                            alarmHelper.startAlarm(alarmDatetime, String.valueOf(ids.get(position)), String.valueOf(titles.get(position)), String.valueOf(datetimes.get(position)).trim());
                        }
                    }
                }
            }
        });
        if (ids.get(position).equals(highlighted_element_id)) {
            highlightItemView(holder.itemConstraintLayout);
        }
        switch ((int) priorities.get(position)) {
            case 0:
                holder.priority_indication_line.setBackgroundColor(ContextCompat.getColor(context, R.color.low_priority));
                break;
            case 1:
                holder.priority_indication_line.setBackgroundColor(ContextCompat.getColor(context, R.color.medium_priority));
                break;
            case 2:
                holder.priority_indication_line.setBackgroundColor(ContextCompat.getColor(context, R.color.high_priority));
                break;
        }
    }

    void confirmDialogDeleting (int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Delete " + titles.get(position) + "?");
        builder.setMessage("Are you sure you want to delete " + titles.get(position) + "?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MyDatabaseHelper myDB = new MyDatabaseHelper(context);
                myDB.deleteOneRow(String.valueOf(ids.get(position)));

                ids.remove(position);
                titles.remove(position);
                descriptions.remove(position);
                datetimes.remove(position);
                notification_times.remove(position);
                statuses.remove(position);

                notifyItemRemoved(position);
                notifyItemRangeChanged(position, getItemCount());
                if (getItemCount() == 0) {
                    MainActivity mainActivity = (MainActivity) context;
                    mainActivity.noData.setVisibility(View.VISIBLE);
                }
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.create().show();
    }

    @Override
    public int getItemCount() {
        return ids.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        TextView title_text, description_text, datetime_text, dateStaticText;
        View priority_indication_line;
        CheckBox status_checkbox;
        LinearLayout mainLayout;
        ConstraintLayout itemConstraintLayout, datesTimesConstraintLayout;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            title_text = itemView.findViewById(R.id.titleText);
            description_text = itemView.findViewById(R.id.descriptionText);
            datetime_text = itemView.findViewById(R.id.deadlineDateText);
            priority_indication_line = itemView.findViewById(R.id.priorityIndicationLine);
            status_checkbox = itemView.findViewById(R.id.checkBox);
            dateStaticText = itemView.findViewById(R.id.dateStaticText);
            mainLayout = itemView.findViewById(R.id.mainLayout);
            itemConstraintLayout = itemView.findViewById(R.id.itemConstraintLayout);
            datesTimesConstraintLayout = itemView.findViewById(R.id.datesTimesConstraintLayout);
        }
    }

    private void highlightItemView(View mainLayout) {
        Animator animation = AnimatorInflater.loadAnimator(context, R.animator.recycleview_row_highlight_animator);
        animation.setTarget(mainLayout);
        animation.start();
    }

    public void deleteAllData () {
        MyDatabaseHelper myDBHelper = new MyDatabaseHelper(context);
        myDBHelper.deleteAllData();
        notifyItemRangeRemoved(0, getItemCount());
        ids.clear();
        titles.clear();
        descriptions.clear();
        datetimes.clear();
        notification_times.clear();
        priorities.clear();
        statuses.clear();
        MainActivity mainActivity = (MainActivity) context;
        mainActivity.noData.setVisibility(View.VISIBLE);
    }
}
