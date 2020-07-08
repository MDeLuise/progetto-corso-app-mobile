package com.example.foo.foodapp;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.RadioButton;
import android.support.v7.widget.SearchView;
import android.widget.Toast;

import com.example.foo.foodapp.database.AppDatabase;
import com.example.foo.foodapp.database.Category;
import com.example.foo.foodapp.database.Food;
import com.example.foo.foodapp.database.FoodDAO;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Class representing the main class of the application. In this class there are the 3 fragments
 */
public class MainActivity extends AppCompatActivity implements NotificationScheduler {

    private FoodsFragment _summaryFragment;
    private FavoritesFragment _favoritesFragment;
    private SettingsFragment _settingsFragment;
    private FoodDAO _db;
    private SharedPreferences _preferences;
    private PendingIntent _pendingIntent;
    private FloatingActionButton _fab;
    public DeactivableViewPager _viewPager;

    public static final String PREFS_NAME = BuildConfig.APPLICATION_ID + "_preferences";


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // link the XML layout
        setContentView(R.layout.activity_main);

        _preferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        linkDB();

        handleParameters();

        insertToolbarAndTabs();
        setupFloatingBottom();

        removeExpiredFoodIfFlagSetted();

        setupTabClickerListener();


        // if there are database problems uncomment above for only one run
        //getBaseContext().deleteDatabase(getString(R.string.database_name));


        /*
         * to insert in the database test foods uncomment this (to comment in production)
         * WARNING: this function is executed EVERY time the Main Activity is showed, so if the follow is
         * uncommented, it can not be 0 elements in the view
         */
        //insertTestEntryInDB();

    }


    @Override
    protected void onStop() {
        super.onStop();
        _preferences.edit().remove("filter").apply();
    }


    /**
     * Schedule a notification in the future (only if installed version of android is greater
     * or equals then Oreo)
     */
    public void scheduleNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            if (_preferences.getInt("nextNotificationTime", -1) !=
                    _preferences.getInt("timePicker", 540)) {

                createNotificationChannel();

                Intent intent = new Intent(MainActivity.this, NotificationPublisher.class);
                intent.putExtra("source", "app_notification"); // needed because if not, notification is fired twice
                PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this,
                        0, intent, 0);


                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

                if (_pendingIntent != null) {
                    alarmManager.cancel(pendingIntent);
                }

                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                        getNextNotificationTime(),
                        AlarmManager.INTERVAL_DAY,
                        pendingIntent);


                _pendingIntent = pendingIntent;
            }

        }
    }


    /**
     * Calculate how much time is left to schedule the first notification from now
     * @return millisecond from now
     */
    private long getNextNotificationTime() {
        int selectedNotifyTime = _preferences.getInt("timePicker", 540);

        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(new Date());
        int hours = calendar.get(Calendar.HOUR_OF_DAY);
        int minutes = calendar.get(Calendar.MINUTE);

        int minutesAfterMidnight = (hours * 60) + minutes;

        int nextNotificationTime = selectedNotifyTime - minutesAfterMidnight;

        if (nextNotificationTime < 0) {
            nextNotificationTime += 1440; // one day in future
        }

        return nextNotificationTime * 60 * 100; // because minutes -> milliseconds
    }


    /**
     * Link database to the class
     */
    private void linkDB() {
        _db = Room.databaseBuilder(getBaseContext(), AppDatabase.class, getString(R.string.database_name))
                .allowMainThreadQueries()
                .build().getFoodDAO();
    }



    /**
     * Insert test foods in the database
     */
    private void insertTestEntryInDB() {
        if (_db.getVisibleFoods().size() == 0) {
            for (int i = 0; i < 5; i++) {
                Food testFood = new Food();
                testFood.setVisibility(true);
                testFood.setPrice(42);
                testFood.setExpirationDate(new Date());
                testFood.setSupply(42);
                testFood.setName("cibo prova");
                testFood.setCategory(Category.ALTRO);
                _db.insert(testFood);
            }
        }
    }


    /**
     * If ManageElementActivity insert new element in database, then notify the user
     */
    private void handleParameters() {
        if (getIntent() != null && getIntent().getExtras() != null) {
            Bundle extraBundle = getIntent().getExtras();

            if (extraBundle.containsKey("moreFoodInDB") && extraBundle.getBoolean("moreFoodInDB")) {
                Toast.makeText(getApplicationContext(),
                        getString(R.string.new_food_added), Toast.LENGTH_SHORT).show();

            } else if (extraBundle.containsKey("editedFoodInDB") && extraBundle.getBoolean("editedFoodInDB")) {
                Toast.makeText(getApplicationContext(),
                        getString(R.string.modified_food), Toast.LENGTH_SHORT).show();
            }

            if (extraBundle.containsKey("moreFavoriteInDB") && extraBundle.getBoolean("moreFavoriteInDB")) {
                Toast.makeText(getApplicationContext(),
                        getString(R.string.favorite_added), Toast.LENGTH_SHORT).show();
                _preferences.edit().putBoolean("switchToFav", true).apply();

            } else if (extraBundle.containsKey("editedFavoriteInDB") && extraBundle.getBoolean("editedFavoriteInDB")) {
                Toast.makeText(getApplicationContext(),
                        getString(R.string.favorite_modified), Toast.LENGTH_SHORT).show();
                _preferences.edit().putBoolean("switchToFav", true).apply();
            }
        }
    }


    /**
     * Initialize the floating button
     */
    private void setupFloatingBottom() {
        _fab = findViewById(R.id.addFoodFloatingButton);
        addFoodActionToFab();

        _summaryFragment.manageFloatingButton(_fab);
        _favoritesFragment.manageFloatingButton(_fab);
    }


    /**
     * Insert the app toolbar and the tabs below that
     */
    private void insertToolbarAndTabs() {
        // insert toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false); // if "true" show back button

        // insert tabs below toolbar
        _viewPager = findViewById(R.id.viewpager);
        setupViewPager(_viewPager);
        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(_viewPager);
    }


    /**
     * Setup the toolbar
     */
    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        _summaryFragment = new FoodsFragment();
        adapter.addFragment(_summaryFragment, getString(R.string.first_tab_name));

        _favoritesFragment = new FavoritesFragment();
        _favoritesFragment.linkFoodFragment(_summaryFragment);
        adapter.addFragment(_favoritesFragment, getString(R.string.second_tab_name));

        _settingsFragment = new SettingsFragment();
        _settingsFragment.linkNotificationScheduler(this);
        adapter.addFragment(_settingsFragment, getString(R.string.third_tab_name));

        viewPager.setAdapter(adapter);
    }


    /**
     * Link the appbar to the menu menu/app_bar_menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.app_bar_menu, menu);
        displayOptionsForSearching(menu);
        return true;
    }


    /**
     * Link actions to the menu buttons
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.sortButton) { // case of sort button pressed
            displayOptionsForSorting();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }


    /**
     * If the sort/search button is pressed in the "settings tab", than activate tab "summary tab"
     * before doing anything
     */
    private void activeOtherTabIfNeeded() {
        TabLayout tabLayout = findViewById(R.id.tabs);
        int selectedTab = tabLayout.getSelectedTabPosition();
        if (selectedTab == 2) {
            TabLayout.Tab tab = tabLayout.getTabAt(0);
            tab.select();
        }
    }


    /**
     * Implements what happen when the user click on a tab
     * (in addition to the usual behavior of changing view)
     */
    private void setupTabClickerListener() {
        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    _fab.show();
                    addFoodActionToFab();
                } else if (tab.getPosition() == 1) {
                    _fab.show();
                    addFavoriteActionToFab();
                } else {
                    _fab.hide();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                AppBarLayout appBarLayout = findViewById(R.id.appbar);

                switch (tab.getPosition()) {
                    case 0:
                        _summaryFragment.getRView().getLayoutManager().smoothScrollToPosition(
                                _summaryFragment.getRView(),new RecyclerView.State(), 0);

                        appBarLayout.setExpanded(true);
                        break;
                    case 1:
                        _favoritesFragment.getRView().getLayoutManager().smoothScrollToPosition(
                                _favoritesFragment.getRView(),new RecyclerView.State(), 0);

                        appBarLayout.setExpanded(true);
                        break;
                    case 2:
                        _settingsFragment.getListView().getLayoutManager().smoothScrollToPosition(
                                _settingsFragment.getRView(),new RecyclerView.State(), 0);

                        appBarLayout.setExpanded(true);
                        break;
                }
            }
        });
    }


    /**
     * Link the "add new food" action to the floating button
     */
    private void addFoodActionToFab() {
        _fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(), ManageElementActivity.class);
                intent.putExtra("oldFoodInserted",
                        _preferences.getBoolean("oldFoodInserted", false));
                startActivity(intent);
            }});
    }


    /**
     * Link the "add new favorite" action to the floating button
     */
    private void addFavoriteActionToFab() {
        _fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(), ManageElementActivity.class);
                intent.putExtra("favoriteInsertion", true);
                startActivity(intent);
            }
        });
    }


    /**
     * Creates the alert dialog when sort button is pressed
     *
     * After MANY failed attempts, I made the function works following this link:
     * http://android.pcsalt.com/create-alertdialog-with-custom-layout-using-xml-layout/
     */
    private void displayOptionsForSorting() {

        activeOtherTabIfNeeded();

        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.sort_elements, null);

        final RadioButton rb2 = alertLayout.findViewById(R.id.radioButton2);
        final RadioButton rb3 = alertLayout.findViewById(R.id.radioButton3);
        final RadioButton rb5 = alertLayout.findViewById(R.id.radioButton5);

        // preselect the chosen radio buttons for sorting parameter
        switch(_preferences.getString("sortOption","NAME")) {
            case "NAME":
                rb2.setChecked(true);
                break;
            case "PRICE":
                rb3.setChecked(true);
                break;
        }

        // preselect the chosen radio buttons for sorting order
        if(!_preferences.getBoolean("orderOption",true)) {
            rb5.setChecked(true);
        }


        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setView(alertLayout);


        alert.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        alert.setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                // retrieve sorting option
                SortingParam chosenOption = SortingParam.DATE;
                boolean chosenOrder = true;

                if (rb2.isChecked()) {
                    chosenOption = SortingParam.NAME;
                } else if (rb3.isChecked()) {
                    chosenOption = SortingParam.PRICE;
                }

                if (rb5.isChecked()) {
                    chosenOrder = false;
                }


                // save new preferences
                SharedPreferences.Editor editor = _preferences.edit();
                editor.putString("sortOption", chosenOption.toString());
                editor.putBoolean("orderOption", chosenOrder);
                editor.apply();


                // update the view
                dialog.dismiss(); // this MUST be done BEFORE the below command
                _summaryFragment.onActivityCreated(new Bundle());
                _favoritesFragment.onActivityCreated(new Bundle());
            }
        });


        AlertDialog dialog = alert.create();
        dialog.show();
    }


    /**
     * Handle the actions when search function is activated
     */
    private void displayOptionsForSearching(Menu menu) {

        final MenuItem myActionMenuItem = menu.findItem(R.id.searchButton);
        SearchView searchView = (SearchView) myActionMenuItem.getActionView();
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

        searchView.setOnSearchClickListener(new SearchView.OnClickListener() {
            @Override
            public void onClick(View v) { activeOtherTabIfNeeded(); }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                activeOtherTabIfNeeded();

                // remove below comment to achieve this:
                // when the user click on "ok" button during search, the filtered list is removed and
                // it's seen the entire list
                /*
                // remove search preferences
                SharedPreferences preferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                preferences.edit().remove("filter").apply();

                _summaryFragment.onActivityCreated(new Bundle());
                */

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                activeOtherTabIfNeeded();

                // save new search text
                SharedPreferences preferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                preferences.edit().putString("filter", newText).apply();

                _summaryFragment.onActivityCreated(new Bundle());
                _favoritesFragment.onActivityCreated(new Bundle());

                return false;
            }
        });
    }


    /**
     * Remove expired food if the user select it in the preferences
     */
    private void removeExpiredFoodIfFlagSetted() {
        int removedFoods = 0;
        if (_preferences.getBoolean("removeOldFoodSwitch", false)) {

            // try-catch needed because user can enter empty string in the field
            int removeOldFood = 1;

            try {
                removeOldFood = Integer.parseInt(_preferences.getString(
                        "RemoveOldDays", "1"));
            } catch(NumberFormatException e) {}

            Calendar calendar = Calendar.getInstance();

            for (Food food: _db.getVisibleFoods()) {
                calendar.setTime(food.getExpirationDate());
                calendar.add(Calendar.DATE, removeOldFood);
                if ((new Date()).compareTo(calendar.getTime()) >= 0) {
                    _db.hideFoodById(food.getId());
                    removedFoods++;
                }
            }
        }

        if (removedFoods > 0 && _preferences.getBoolean("notificationAboutAutoRemoveFood",
                false)) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle(getString(R.string.warning))
                    .setMessage(getString(R.string.old_foods_removed)
                            .replace("NUM", "" + removedFoods))
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    }).show();
        }


    }


    /**
     * Needed for the notifications
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "CHANNEL_ID";
            String description = "Channel description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("CHANNEL_ID", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


    /**
     * Implements what happen when the user click on back button device
     */
    @Override
    public void onBackPressed() {}








    /**
     * Inner class used to show list of items in the RecyclerView
     */
    class ViewPagerAdapter extends FragmentPagerAdapter {

        private final List<Fragment> _mFragmentList = new ArrayList<>();
        private final List<String> _mFragmentTitleList = new ArrayList<>();


        ViewPagerAdapter(FragmentManager manager) { super(manager); }


        @Override
        public Fragment getItem(int position) { return _mFragmentList.get(position); }


        @Override
        public int getCount() { return _mFragmentList.size(); }


        void addFragment(Fragment fragment, String title) {
            _mFragmentList.add(fragment);
            _mFragmentTitleList.add(title);
        }


        @Override
        public CharSequence getPageTitle(int position) { return _mFragmentTitleList.get(position); }

    }



}
