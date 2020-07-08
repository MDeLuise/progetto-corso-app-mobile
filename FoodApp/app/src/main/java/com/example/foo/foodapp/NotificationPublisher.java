package com.example.foo.foodapp;

import android.app.Notification;
import android.app.PendingIntent;
import android.arch.persistence.room.Room;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.example.foo.foodapp.database.AppDatabase;
import com.example.foo.foodapp.database.Food;
import com.example.foo.foodapp.database.FoodDAO;

import java.util.Calendar;
import java.util.Date;
import java.util.List;


/**
 * Class the implements the receiver of the notifications
 */
public class NotificationPublisher extends BroadcastReceiver {
    private Context _context;
    private SharedPreferences _preferences;



    @Override
    public void onReceive(Context context, Intent intent) {
        _preferences = context.getSharedPreferences(MainActivity.PREFS_NAME,
                context.MODE_PRIVATE);
        _context = context;

        // check needed because for an android studio bug, if not checked, the notification is
        // displayed twice (https://stackoverflow.com/a/24433269)
        if (intent.getStringExtra("source") == null ||
                !intent.getStringExtra("source").equals("app_notification")) {
            return;
        }

        if (_preferences.getBoolean("notificationSwitch", false)) {

            // try-catch needed because user can enter empty string in the field
            int notificationDay = 1;

            try {
                notificationDay = Integer.parseInt(_preferences.getString(
                        "notificationDays", "1"));
            } catch(NumberFormatException e) {}


            Intent notificationIntent = new Intent(context, MainActivity.class);
            PendingIntent contentIntent = PendingIntent.getActivity(
                    context, 0, notificationIntent, 0);



            NotificationCompat.Builder builder = new NotificationCompat.Builder(_context, "CHANNEL_ID")
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setContentTitle(_context.getString(R.string.notification_title))
                    .setContentText(context.getString(R.string.notification_message)
                            .replace("NUM", "" + calcExpiringFoods())
                            .replace("DAYS", "" + notificationDay))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(contentIntent);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(_context);
            Notification notification = builder.build();
            notification.flags |= Notification.FLAG_AUTO_CANCEL;

            notificationManager.notify(createID(), notification);
        }
    }


    /**
     * Calculate how many foods'll expire in chosen days
     * @return how many foods
     */
    private int calcExpiringFoods() {
        FoodDAO db = Room.databaseBuilder(_context, AppDatabase.class,
                _context.getString(R.string.database_name))
                .allowMainThreadQueries()
                .build().getFoodDAO();


        // try-catch needed because user can enter empty string in the field
        int dayBeforeExpiringToGetNotification = 1;

        try {
            dayBeforeExpiringToGetNotification = Integer.parseInt(_preferences.getString(
                    "notificationDays", "1"));
        } catch(NumberFormatException e) {}


        Calendar now = Calendar.getInstance();
        now.setTime(new Date());
        now.add(Calendar.DAY_OF_MONTH, dayBeforeExpiringToGetNotification);

        List<Food> foods = db.getVisibleFoods();
        int expiringFoodNum = 0;
        for (Food food: foods) {
            if (food.getExpirationDate().compareTo(now.getTime()) <= 0) {
                expiringFoodNum++;
            }
        }

        return expiringFoodNum;
    }


    /**
     * Create the unique id needed for the notifications
     * @return unique id
     */
    private int createID(){
       /*
        Date now = new Date();
        int id = Integer.parseInt(new SimpleDateFormat("ddHHmmss",  Locale.US).format(now));
        return id;
        */
       return (int) System.currentTimeMillis();
    }





}
