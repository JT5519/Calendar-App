package com.example.kalnir_naya;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kalnir_naya.Events;
import com.example.kalnir_naya.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
/*Creates the list view of events that is displayed
when a day is clicked. It can be refreshed on certain
events such as an event being deleted or edited*/
public class EventRecyclerAdapter extends
        RecyclerView.Adapter<EventRecyclerAdapter.MyViewHolder> {

    Context context;
    ArrayList<Events> arrayList;
    DBOpenHelper dbOpenHelper;
    //TextView textView;
    EditText editText;
    AlertDialog alertDialog;
    Calendar datecalendar,timecalendar;
    public EventRecyclerAdapter(Context context, ArrayList<Events> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate
                (R.layout.show_events_rowlayout,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
        final Events events = arrayList.get(position);
        holder.Event.setText(events.getEVENT());
        holder.DateTxt.setText(events.getDATE());
        holder.Time.setText(events.getTIME());
        if(isAlarmed(events.getDATE(),events.getEVENT(),events.getTIME())){
            holder.setAlarm.setImageResource(R.drawable.ic_action_notification_on);

        }else{
            holder.setAlarm.setImageResource(R.drawable.ic_action_notification_off);
        }


        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isAlarmed(events.getDATE(),events.getEVENT(),events.getTIME())){
                    cancelAlarm(getRequestCode(events.getDATE(),events.getEVENT(),
                            events.getTIME()));
                }
                deleteCalendarEvent(events.getEVENT(),events.getDATE(),events.getTIME());
                arrayList.remove(position);
                notifyDataSetChanged();
            }
        });

        holder.setAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isAlarmed(events.getDATE(),events.getEVENT(),events.getTIME())){
                    holder.setAlarm.setImageResource(R.drawable.ic_action_notification_off);
                    cancelAlarm(getRequestCode(events.getDATE(),events.getEVENT(),
                            events.getTIME()));
                    updateEvent(events.getDATE(),events.getEVENT(),events.getTIME(),
                            "off");
                    notifyDataSetChanged();

                }else {
                    holder.setAlarm.setImageResource(R.drawable.ic_action_notification_on);
                    datecalendar = Calendar.getInstance();
                    datecalendar.setTime(ConvertStringToDate(events.getDATE()));
                    Log.d("events.getDATE(): ",events.getDATE());
                    final int alarmYear = datecalendar.get(Calendar.YEAR);
                    Log.d("alarmYear: ", String.valueOf(alarmYear));
                    final int alarmMonth = datecalendar.get(Calendar.MONTH);
                    Log.d("alarmMonth: ", String.valueOf(alarmMonth));
                    final int alarmDay  = datecalendar.get(Calendar.DAY_OF_MONTH);
                    Log.d("alarmDay: ", String.valueOf(alarmDay));
                    timecalendar = Calendar.getInstance();
                    timecalendar.setTime(ConvertStringToTime(events.getTIME()));
                    Log.d("events.getTIME()",events.getTIME()+"*");
                    int aH = timecalendar.get(Calendar.HOUR_OF_DAY);
                    String s = events.getTIME();
                    if(s.charAt(s.length()-2)=='P')
                        //Log.d("Print P please", String.valueOf(s.charAt(s.length()-2)));
                        aH+=12;
                    final int alarmHour = aH;
                    //Log.d("alarmHour: ", String.valueOf(alarmHour));
                    final int alarmMinute = timecalendar.get(Calendar.MINUTE);
                    //Log.d("alarmMinute: ", String.valueOf(alarmMinute));

                    Calendar alarmCalendar = Calendar.getInstance();
                    alarmCalendar.set(alarmYear,alarmMonth,alarmDay,alarmHour,alarmMinute,0);
                    setAlarm(alarmCalendar,events.getEVENT(),events.getTIME(),
                    getRequestCode(events.getDATE(),events.getEVENT(),events.getTIME()));
                    updateEvent(events.getDATE(),events.getEVENT(),events.getTIME(),
                            "on");
                    notifyDataSetChanged();
                }
            }
        });

        alertDialog = new AlertDialog.Builder(context).create();
        editText = new EditText(context);
        alertDialog.setTitle("Edit the event");
        alertDialog.setView(editText);
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "CONFIRM EVENT CHANGE",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        editCalendarEvent(editText.getText().toString(),events.getEVENT(),
                                events.getDATE(),events.getTIME());
                        events.setEVENT(editText.getText().toString());
                        notifyDataSetChanged();
                    }
                });
        holder.Event.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editText.setText(holder.Event.getText());
                alertDialog.show();
            }
        });

        holder.Time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int hours = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);
                TimePickerDialog timePickerDialog = new TimePickerDialog(
                        context, R.style.Theme_AppCompat_Dialog,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                Calendar c = Calendar.getInstance();
                                c.set(Calendar.HOUR_OF_DAY,hourOfDay);
                                c.set(Calendar.MINUTE,minute);
                                c.setTimeZone(TimeZone.getDefault());
                                SimpleDateFormat hformat = new SimpleDateFormat(
                                        "K:mm a",Locale.ENGLISH);
                                String event_Time = hformat.format(c.getTime());
                                editTime(events.getEVENT(),
                                        events.getDATE(),events.getTIME(),event_Time);
                                events.setTIME(event_Time);
                                notifyDataSetChanged();
                                if(isAlarmed(events.getDATE(),events.getEVENT(),events.getTIME())){
                                    cancelAlarm(getRequestCode(events.getDATE(),events.getEVENT(),
                                            events.getTIME()));

                                    datecalendar = Calendar.getInstance();
                                    datecalendar.setTime(ConvertStringToDate(events.getDATE()));
                                    final int alarmYear = datecalendar.get(Calendar.YEAR);
                                    final int alarmMonth = datecalendar.get(Calendar.MONTH);
                                    final int alarmDay  = datecalendar.get(Calendar.DAY_OF_MONTH);
                                    timecalendar = Calendar.getInstance();
                                    timecalendar.setTime(ConvertStringToTime(events.getTIME()));

                                    int aH = timecalendar.get(Calendar.HOUR_OF_DAY);
                                    String s = events.getTIME();
                                    char c1 = s.charAt(s.length() - 2);
                                    if(c1 =='P')
                                        //Log.d("Print P please again", String.valueOf(c1));
                                    aH+=12;
                                    final int alarmHour = aH;
                                    //Log.d("alarmHour: ", String.valueOf(alarmHour));
                                    final int alarmMinute = timecalendar.get(Calendar.MINUTE);

                                    Calendar alarmCalendar = Calendar.getInstance();
                                    alarmCalendar.set(alarmYear,alarmMonth,alarmDay,alarmHour,alarmMinute,0);
                                    setAlarm(alarmCalendar,events.getEVENT(),events.getTIME(),
                                            getRequestCode(events.getDATE(),events.getEVENT(),events.getTIME()));
                                }
                            }
                        },hours,minute,false);
                timePickerDialog.show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        TextView DateTxt,Event,Time;
        Button delete;
        ImageButton setAlarm;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            DateTxt = itemView.findViewById(R.id.eventdate);
            Event = itemView.findViewById(R.id.eventname);
            Time = itemView.findViewById(R.id.eventtime);
            delete = itemView.findViewById(R.id.delete);
            setAlarm = itemView.findViewById(R.id.alarmmeBtn);
        }
    }

    private Date ConvertStringToDate(String eventDate){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        Date date = null;
        try{
            date = format.parse(eventDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    private Date ConvertStringToTime(String eventDate){
        SimpleDateFormat format = new SimpleDateFormat("kk:mm", Locale.ENGLISH);
        Date date = null;
        try{
            date = format.parse(eventDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    private void deleteCalendarEvent(String event,String date,String time){
        dbOpenHelper = new DBOpenHelper(context);
        SQLiteDatabase database = dbOpenHelper.getWritableDatabase();
        dbOpenHelper.deleteEvent(event,date,time,database);
        dbOpenHelper.close();
    }

    private boolean isAlarmed(String date,String event,String time)
    {
        boolean alarmed = false;
        dbOpenHelper = new DBOpenHelper(context);
        SQLiteDatabase database = dbOpenHelper.getReadableDatabase();
        Cursor cursor = dbOpenHelper.ReadIDEvents(date,event,time,database);
        while(cursor.moveToNext()){
            String notify = cursor.getString(cursor.getColumnIndex(DBStructure.Notify));
            if(notify.equals("on")){
                alarmed = true;
            }else{
                alarmed = false;
            }
        }
        cursor.close();
        dbOpenHelper.close();
        return alarmed;
    }

    private void setAlarm(Calendar calendar,String event, String time, int RequestCode)
    {
        Intent intent = new Intent(context.getApplicationContext(),AlarmReceiver.class);
        intent.putExtra("event",event);
        intent.putExtra("time",time);
        intent.putExtra("id",RequestCode);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,RequestCode,
                intent,PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmManager = (AlarmManager) context.getApplicationContext()
                .getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),pendingIntent);
    }

    private void cancelAlarm(int RequestCode)
    {
        Intent intent = new Intent(context.getApplicationContext(),AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,RequestCode,
                intent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmManager = (AlarmManager) context.getApplicationContext()
                .getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();
    }

    private int getRequestCode(String date,String event,String time){
        int code = 0;
        dbOpenHelper = new DBOpenHelper(context);
        SQLiteDatabase database = dbOpenHelper.getReadableDatabase();
        Cursor cursor = dbOpenHelper.ReadIDEvents(date,event,time,database);
        while(cursor.moveToNext()){
            code = cursor.getInt(cursor.getColumnIndex(DBStructure.ID));

        }
        cursor.close();
        dbOpenHelper.close();
        return code;
    }

    private void updateEvent(String date,String event,String time, String notify)
    {
        dbOpenHelper = new DBOpenHelper(context);
        SQLiteDatabase database = dbOpenHelper.getWritableDatabase();
        dbOpenHelper.updateEvent(date,event,time,notify,database);
        dbOpenHelper.close();
    }

    private void editCalendarEvent(String eventNew,String eventOld,
                                   String date,String time){
        dbOpenHelper = new DBOpenHelper(context);
        SQLiteDatabase database = dbOpenHelper.getWritableDatabase();
        dbOpenHelper.editEvent(eventNew,eventOld,date,time,database);
        dbOpenHelper.close();
    }

    private void editTime(String event, String date,String timeOld,String timeNew){
        dbOpenHelper = new DBOpenHelper(context);
        SQLiteDatabase database = dbOpenHelper.getWritableDatabase();
        dbOpenHelper.editTime(event,date,timeOld,timeNew,database);
        dbOpenHelper.close();
    }

}

