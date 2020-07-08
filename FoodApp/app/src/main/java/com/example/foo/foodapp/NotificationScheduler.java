package com.example.foo.foodapp;

/**
 * Interface which should be implemented in the app activity which manage the notifications.
 * It's not explicitly used in the MainActivity, but it's a useful remainder of which function(s)
 * should be exist in the API
 */
public interface NotificationScheduler {

    void scheduleNotification();

}
