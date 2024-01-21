package com.vilisvit.things;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.MyViewHolder> {

    private final Context context;
    MyViewHolder holderParent;
    CustomAdapter parentRecyclerViewAdapter;
    RecyclerView recyclerView;
    private final ArrayList<String> ids, titles, descriptions, datetimes, notification_times, parent_ids;
    private  final ArrayList<Integer> priorities;
    private final ArrayList<Boolean> statuses;
    private String highlighted_element_id, highlighted_element_parent_id;

    private final int row_layout_id;

    CustomAdapter (Context context,
                   int row_layout_id,
                   MyViewHolder holderParent,
                   CustomAdapter parentRecyclerViewAdapter,
                   ArrayList<String> ids,
                   ArrayList<String> titles,
                   ArrayList<String> descriptions,
                   ArrayList<String> datetimes,
                   ArrayList<String> notification_times,
                   ArrayList<Integer> priorities,
                   ArrayList<Boolean> statuses,
                   ArrayList<String> parent_ids) {
        this.context = context;
        this.row_layout_id = row_layout_id;
        this.holderParent = holderParent;
        this.parentRecyclerViewAdapter = parentRecyclerViewAdapter;
        this.ids = ids;
        this.titles = titles;
        this.descriptions = descriptions;
        this.datetimes = datetimes;
        this.notification_times = notification_times;
        this.priorities = priorities;
        this.statuses = statuses;
        this.parent_ids = parent_ids;
    }

    CustomAdapter (Context context,
                   int row_layout_id,
                   MyViewHolder holderParent,
                   CustomAdapter parentRecyclerViewAdapter,
                   ArrayList<String> ids,
                   ArrayList<String> titles,
                   ArrayList<String> descriptions,
                   ArrayList<String> datetimes,
                   ArrayList<String> notification_times,
                   ArrayList<Integer> priorities,
                   ArrayList<Boolean> statuses,
                   ArrayList<String> parent_ids,
                   String highlighted_element_id) {
        this.context = context;
        this.row_layout_id = row_layout_id;
        this.holderParent = holderParent;
        this.parentRecyclerViewAdapter = parentRecyclerViewAdapter;
        this.ids = ids;
        this.titles = titles;
        this.descriptions = descriptions;
        this.datetimes = datetimes;
        this.notification_times = notification_times;
        this.priorities = priorities;
        this.statuses = statuses;
        this.parent_ids = parent_ids;
        this.highlighted_element_id = highlighted_element_id;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;

        if (highlighted_element_id != null && !ids.contains(highlighted_element_id)) {
            MyDatabaseHelper myDBHelper = new MyDatabaseHelper(context);
            highlighted_element_parent_id = myDBHelper.getParentId(highlighted_element_id);
            recyclerView.getLayoutManager().scrollToPosition(ids.indexOf(highlighted_element_parent_id));
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(row_layout_id, parent, false);
        return new MyViewHolder(view);
    }

    @SuppressLint("Range")
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int position) {
        if (statuses.get(position)) {
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
        holder.status_checkbox.setChecked(statuses.get(position));
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
                intent.putExtra("priority", priorities.get(position));
                intent.putExtra("status", statuses.get(position));
                intent.putExtra("parent_id", String.valueOf(parent_ids.get(position)));
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
                        } else if (item.getItemId() == R.id.create_child_option) {
                            Intent intent = new Intent(context, EditActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.putExtra("parent_id", String.valueOf(ids.get(position)));
                            context.startActivity(intent);
                            return true;
                        } else if (item.getItemId() == R.id.delete_children_option) {
                            confirmDialogDeletingChildren(position);
                            return true;
                        }
                        return false;
                    }
                });
                if (parentRecyclerViewAdapter == null) {
                    MyDatabaseHelper myDBHelper = new MyDatabaseHelper(context);
                    if (myDBHelper.countChildren(ids.get(position)) > 0) {
                        popupMenu.inflate(R.menu.on_item_click_menu);
                    } else {
                        popupMenu.inflate(R.menu.on_item_click_menu_no_subtasks);
                    }
                } else {
                    popupMenu.inflate(R.menu.on_children_click_menu);
                }
                popupMenu.show();
                return true;
            }
        });
        holder.status_checkbox.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                boolean isChecked = ((CheckBox)v).isChecked();
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
                            alarmHelper.startAlarm(alarmDatetime, String.valueOf(ids.get(position)), String.valueOf(titles.get(position)), String.valueOf(datetimes.get(position)).trim(), priorities.get(position));
                        }
                    }
                }

                if (parent_ids.get(position).equals("-1")) { //if is parent element
                    //(un)check all children
                    String[] children_ids = myDBHelper.getChildrenIds(ids.get(position)).toArray(new String[0]);
                    for (String child_id : children_ids) {
                        myDBHelper.updateStatus(child_id, isChecked);
                    }
                    CustomAdapter childAdapter = (CustomAdapter) holder.recycler_view_children.getAdapter();
                    Collections.fill(childAdapter.statuses, isChecked);
                    holder.recycler_view_children.getAdapter().notifyItemRangeChanged(0, myDBHelper.countChildren(ids.get(position)));
                } else {
                    //check all children of parent if all has the same status check parent to it
                    String[] children_ids = myDBHelper.getChildrenIds(parent_ids.get(position)).toArray(new String[0]);
                    boolean all_true = true;
                    for (String child_id : children_ids) {
                        if (!myDBHelper.getStatus(child_id)) {
                            all_true = false;
                        }
                    }
                    myDBHelper.updateStatus(parent_ids.get(position), all_true);
                    parentRecyclerViewAdapter.statuses.set(holderParent.getAdapterPosition(), all_true);
                    parentRecyclerViewAdapter.notifyItemChanged(holderParent.getAdapterPosition());
                }
            }
        });
        if (holder.expand_button != null) {
            holder.expand_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (holder.recycler_view_children.getVisibility() == View.VISIBLE) {
                        holder.expand_button.animate().rotation(180).setDuration(200).start();
                        holder.recycler_view_children.setVisibility(View.GONE);
                        notifyItemChanged(position);
                    } else {
                        holder.expand_button.animate().rotation(0).setDuration(200).start();
                        holder.recycler_view_children.setVisibility(View.VISIBLE);
                        notifyItemChanged(position);
                    }
                }
            });
        }

        if (ids.get(position).equals(highlighted_element_id)) {
            recyclerView.getLayoutManager().scrollToPosition(position);
            highlightItemView(holder.itemConstraintLayout);
        }

        if (ids.get(position).equals(highlighted_element_parent_id)) {
            if (holder.expand_button != null) {
                holder.expand_button.setRotation(180);
            } else {
                Log.e("error", "Expand button not found");
            }
            holder.recycler_view_children.setVisibility(View.VISIBLE);
        }

        switch (priorities.get(position)) {
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
        MyDatabaseHelper myDBHelper = new MyDatabaseHelper(context);
        if (holder.recycler_view_children != null) {
            if (myDBHelper.countChildren(String.valueOf(ids.get(position))) > 0) {
                ArrayList<String> children_ids = myDBHelper.getChildrenIds(String.valueOf(ids.get(position)));
                ArrayList<String> children_titles = new ArrayList<>();
                ArrayList<String> children_descriptions = new ArrayList<>();
                ArrayList<String> children_datetimes = new ArrayList<>();
                ArrayList<String> children_notification_times = new ArrayList<>();
                ArrayList<Integer> children_priorities = new ArrayList<>();
                ArrayList<Boolean> children_statuses = new ArrayList<>();
                ArrayList<String> children_parent_ids = new ArrayList<>();

                Cursor cursor = myDBHelper.readDataForIds(children_ids);
                cursor.moveToFirst();
                do {
                    children_titles.add(cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_TITLE)));
                    children_descriptions.add(cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_DESCRIPTION)));
                    children_datetimes.add(cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_DATETIME)));
                    children_notification_times.add(cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_NOTIFICATION_TIME)));
                    children_priorities.add(cursor.getInt(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_PRIORITY)));
                    children_statuses.add(cursor.getInt(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_STATUS)) > 0);
                    children_parent_ids.add(cursor.getString(cursor.getColumnIndex(MyDatabaseHelper.COLUMN_PARENT_ID)));
                } while (cursor.moveToNext());

                CustomAdapter childrenAdapter = new CustomAdapter(context, R.layout.recycle_view_row_child, holder, CustomAdapter.this, children_ids, children_titles, children_descriptions, children_datetimes, children_notification_times, children_priorities, children_statuses, children_parent_ids, highlighted_element_id);

                holder.recycler_view_children.setLayoutManager(new LinearLayoutManager(context));
                holder.recycler_view_children.setAdapter(childrenAdapter);
                ((SimpleItemAnimator) holder.recycler_view_children.getItemAnimator()).setSupportsChangeAnimations(false);
            } else {
                holder.recycler_view_children.setVisibility(View.GONE);
                holder.expand_button.setVisibility(View.GONE);
            }
        }
    }

    void confirmDialogDeleting (int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getResources().getString(R.string.delete_one_title) + " " + titles.get(position) + "?");
        builder.setMessage(context.getResources().getString(R.string.delete_one_message) + " " + titles.get(position) + "?");
        builder.setPositiveButton(context.getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
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
                parent_ids.remove(position);

                if (parentRecyclerViewAdapter != null) {
                    if (ids.size() == 0) {
                        holderParent.recycler_view_children.setVisibility(View.GONE);
                        holderParent.expand_button.setVisibility(View.GONE);
                    }
                    parentRecyclerViewAdapter.notifyItemChanged(holderParent.getAdapterPosition());
                }

                notifyItemRemoved(position);
                notifyItemRangeChanged(position, getItemCount());
            }
        });
        builder.setNegativeButton(context.getResources().getString(R.string.no), (dialog, which) -> {});
        builder.create().show();
    }

    void confirmDialogDeletingChildren (int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getResources().getString(R.string.delete_subtasks_title) + " " + titles.get(position) + "?");
        builder.setMessage(context.getResources().getString(R.string.delete_subtasks_message) + " " + titles.get(position) + "?");
        builder.setPositiveButton(context.getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MyDatabaseHelper myDB = new MyDatabaseHelper(context);
                myDB.deleteMultipleRows(myDB.getChildrenIds(String.valueOf(ids.get(position))));
                notifyItemChanged(position);
            }
        });
        builder.setNegativeButton(context.getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {}
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
        RecyclerView recycler_view_children;
        public ImageButton expand_button;
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
            recycler_view_children = itemView.findViewById(R.id.recyclerViewChildren);
            expand_button = itemView.findViewById(R.id.expandButton);
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
