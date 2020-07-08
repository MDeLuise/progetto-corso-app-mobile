package com.example.foo.foodapp;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.arch.persistence.room.Room;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.foo.foodapp.database.AppDatabase;
import com.example.foo.foodapp.database.Category;
import com.example.foo.foodapp.database.Favorite;
import com.example.foo.foodapp.database.Food;
import com.example.foo.foodapp.database.FoodDAO;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


/**
 * Class with double personality, it is:
 * 1) an Activity linked with "add element layout";
 * 2) a View.OnClickListener, because so can run the barcode scanner and retrieval information from
 *    that.
 */
public class ManageElementActivity extends AppCompatActivity implements View.OnClickListener {

    private FoodDAO _db;
    private IntentIntegrator _codeScanner;
    private boolean _insertOldFoodAllowed = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        linkXmlAndSetupToolbar();
        setupBarcodeBottom();
        linkDatabase();
        setupCategorySpinner(); // must be called before retrieveOptionalParameters()
        retrieveOptionalParameters();
        setupAddButton();
        setupAutocompleteTextView();
        setupBarcodeScanner();
        setupExpirationDateTextView();
        setupTextChangeListeners();
    }


    /**
     * Fill the fields with optional parameters passed to this view (from "favorite" tab)
     */
    private void retrieveOptionalParameters() {

        if (getIntent().hasExtra("nameToAdd")) {
            ((AutoCompleteTextView) findViewById(R.id.enteredNameOfFood)).setText(
                    getIntent().getStringExtra("nameToAdd"));
        }

        if (getIntent().hasExtra("priceToAdd")) {
            ((EditText) findViewById(R.id.enteredPriceOfFood)).setText(
                    getIntent().getStringExtra("priceToAdd")
                            .replaceAll(".0$", ""));
        }

        if (getIntent().hasExtra("categoryToAdd")) {
            ((Spinner) findViewById(R.id.categorySpinner)).setSelection(
                    (Category.valueOf(getIntent().getStringExtra("categoryToAdd"))).getValue());
        }

        if (getIntent().hasExtra("expirationDateToAdd")) {
            ((EditText) findViewById(R.id.expirationDatePicked)).setText(
                    getIntent().getStringExtra("expirationDateToAdd"));
        }

        if (getIntent().hasExtra("supplyToAdd")) {
            ((EditText) findViewById(R.id.enteredQuantityOfFood)).setText(
                    getIntent().getStringExtra("supplyToAdd"));
        }

        if (getIntent().hasExtra("actionIsToEdit")) {
            ((Button) findViewById(R.id.doActionFoodButton)).setText(getString(R.string.modify_food));
        }

        if (getIntent().getBooleanExtra("noFavoriteSwitch", false)) {
            findViewById(R.id.addToFavoriteSwitch).setVisibility(View.GONE);
            findViewById(R.id.addToFavoriteLabel).setVisibility(View.GONE);
        }

        if (getIntent().getBooleanExtra("oldFoodInserted", false)) {
            _insertOldFoodAllowed = true;
        }

        if (getIntent().getBooleanExtra("editFavorite", false)) {
            ((Button) findViewById(R.id.doActionFoodButton)).setText(getString(R.string.modify_fav));

            findViewById(R.id.input_layout_quantity).setVisibility(View.GONE);
            findViewById(R.id.enteredQuantityOfFood).setVisibility(View.GONE);

            findViewById(R.id.expirationDatePicked).setVisibility(View.GONE);
            findViewById(R.id.input_layout_expirationDate).setVisibility(View.GONE);

            findViewById(R.id.addToFavoriteSwitch).setVisibility(View.GONE);
            findViewById(R.id.addToFavoriteLabel).setVisibility(View.GONE);

            findViewById(R.id.barcodeButton).setVisibility(View.GONE);

        } else if (getIntent().getBooleanExtra("favoriteInsertion", false)) {
            ((Button) findViewById(R.id.doActionFoodButton)).setText(getString(R.string.add_fav));

            findViewById(R.id.input_layout_quantity).setVisibility(View.GONE);
            findViewById(R.id.enteredQuantityOfFood).setVisibility(View.GONE);

            findViewById(R.id.expirationDatePicked).setVisibility(View.GONE);
            findViewById(R.id.input_layout_expirationDate).setVisibility(View.GONE);

            findViewById(R.id.addToFavoriteSwitch).setVisibility(View.GONE);
            findViewById(R.id.addToFavoriteLabel).setVisibility(View.GONE);

            findViewById(R.id.barcodeButton).setVisibility(View.GONE);
        }
    }


    /**
     * Setup the click on expiration date EditText
     */
    private void setupExpirationDateTextView() {
        EditText foo = findViewById(R.id.expirationDatePicked);
        foo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDate(v);
            }
        });
    }


    /**
     * Setup the spinner used to choose the food's category
     */
    public void setupCategorySpinner() {
        Category[] allCategory = Category.values();
        List<String> allCategoryNames = new ArrayList<>(allCategory.length);

        for (Category category: allCategory)
            allCategoryNames.add(category.name());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getBaseContext(), android.R.layout.simple_spinner_item, allCategoryNames);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner spinner = findViewById(R.id.categorySpinner);
        spinner.setAdapter(adapter);

        spinner.setSelection(adapter.getPosition(Category.ALTRO.name()));
    }


    /**
     * Setup the autocomplete text for "name" in the layout
     */
    private void setupAutocompleteTextView() {
        List<Food> foods = _db.getFoodsOrderedById();
        final List<Food> suggestedFoodForAutocomplete = new ArrayList<>();
        final List<String> dbNames = new ArrayList<>();
        for (int i=0; i < foods.size(); i++) {
            if (!isContainedInStringList(dbNames, foods.get(i).getName())) {
                dbNames.add(foods.get(i).getName());
                suggestedFoodForAutocomplete.add(foods.get(i));
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<> (
                this, android.R.layout.select_dialog_item, dbNames);
        AutoCompleteTextView autoCompleteName = findViewById(R.id.enteredNameOfFood);
        autoCompleteName.setThreshold(1);
        autoCompleteName.setAdapter(adapter);
        autoCompleteName.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) findViewById(R.id.enteredPriceOfFood)).setText(
                        ("" + suggestedFoodForAutocomplete.get(position).getPrice())
                                .replaceAll(".0$", ""));
                ((Spinner) findViewById(R.id.categorySpinner)).setSelection(
                        suggestedFoodForAutocomplete.get(position).getCategory().getValue());
            }
        });
    }


    /**
     * Needed because list.contains doesn't work on string
     * (it test if str1 == str2 not if str1.equals(str2))
     */
    private boolean isContainedInStringList(List<String> list, String str) {
        for (String el: list) {
            if (el.equals(str))
                    return true;
        }
        return false;
    }


    /**
     * Link the action to the "add food" button
     */
    private void setupAddButton() {
        Button finishButton = findViewById(R.id.doActionFoodButton);
        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validateName() && validateQuantity() && validateExpirationDate() && validatePrice()) {
                    Intent intentBundle = new Intent(ManageElementActivity.this, MainActivity.class);
                    Bundle bundle = new Bundle();

                    if (!getIntent().hasExtra("actionIsToEdit")) {
                        addFoodInDB();
                        bundle.putBoolean("moreFoodInDB", true);
                    } else {
                        editFoodInDB();
                        bundle.putBoolean("editedFoodInDB", true);
                    }

                    intentBundle.putExtras(bundle);
                    startActivity(intentBundle);

                } else if (getIntent().getBooleanExtra("editFavorite", false) &&
                        validateName() && validatePrice()) {
                    Bundle bundle = new Bundle();
                    Intent intentBundle = new Intent(ManageElementActivity.this, MainActivity.class);

                    editFavoriteInDB();

                    bundle.putBoolean("editedFavoriteInDB", true);
                    intentBundle.putExtras(bundle);
                    startActivity(intentBundle);

                } else if (getIntent().getBooleanExtra("favoriteInsertion", false) &&
                        validateName() && validatePrice()) {
                Bundle bundle = new Bundle();
                Intent intentBundle = new Intent(ManageElementActivity.this, MainActivity.class);

                editFavoriteInDB();

                bundle.putBoolean("moreFavoriteInDB", true);
                intentBundle.putExtras(bundle);
                startActivity(intentBundle);
            }
            }
        });
    }


    /**
     * Save the new food (that will be created from fields) in the database
     */
    private void addFoodInDB() {
        Food foodToAdd = new Food();
        foodToAdd.setName(((AutoCompleteTextView)
                findViewById(R.id.enteredNameOfFood)).getText().toString());
        foodToAdd.setSupply(Integer.parseInt
                (((EditText) findViewById(R.id.enteredQuantityOfFood)).getText().toString()));
        foodToAdd.setPrice(Double.parseDouble
                (((EditText) findViewById(R.id.enteredPriceOfFood)).getText().toString()));
        foodToAdd.setVisibility(true);

        String chosenDate = ((TextView) findViewById(R.id.expirationDatePicked)).getText().toString();
        try {
            foodToAdd.setExpirationDate(
                    new SimpleDateFormat("dd/MM/yyyy", Locale.ITALIAN).parse(chosenDate));
        } catch (ParseException e) {
            foodToAdd.setExpirationDate(new Date());
        }
        foodToAdd.setCategory(Category.valueOf(
                ((Spinner) findViewById(R.id.categorySpinner)).getSelectedItem().toString()));

        String barCode = ((TextView) findViewById(R.id.enteredBarcodeOfFood)).getText().toString();
        if (!barCode.isEmpty()) {
            foodToAdd.setBarcode(barCode);
        }

        if (((Switch) findViewById(R.id.addToFavoriteSwitch)).isChecked()) {
            _db.insert(new Favorite(foodToAdd));
        }

        _db.insert(foodToAdd);
    }


    private void addFavoriteInDB() {
        Favorite favoriteToAdd = new Favorite();
        favoriteToAdd.setName(((AutoCompleteTextView)
                findViewById(R.id.enteredNameOfFood)).getText().toString());
        favoriteToAdd.setPrice(Double.parseDouble
                (((EditText) findViewById(R.id.enteredPriceOfFood)).getText().toString()));
        favoriteToAdd.setCategory(Category.valueOf(
                ((Spinner) findViewById(R.id.categorySpinner)).getSelectedItem().toString()));

        _db.insert(favoriteToAdd);
    }


    /**
     * Remove the food that is modified, and add new inserted food
     */
    private void editFoodInDB() {
        _db.hideFoodById(getIntent().getIntExtra("actionIsToEdit", -1));
        addFoodInDB();
    }


    /**
     * Remove the food that is modified, and add new inserted food
     */
    private void editFavoriteInDB() {
        _db.deleteFavoriteById(getIntent().getIntExtra("actionIsToEdit", -1));
        addFavoriteInDB();
    }


    /**
     * Called when the button "select date" is pressed
     */
    public void setDate(View view) {
        showDialog(999);
    }


    /**
     * called by setDate method, it open the date picker
     */
    @Override
    protected Dialog onCreateDialog(int id) {
        if (id == 999) {
            Calendar calendar = Calendar.getInstance();
            // change the "3" below to change DatePickerDialog theme and style
            // (it can also be deleted from parameters)
            return new DatePickerDialog(this, 3,
                    myDateListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
        }
        return null;
    }


    /**
     * date picker object open by onCreateDialog method
     */
    private DatePickerDialog.OnDateSetListener myDateListener = new
            DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker arg0,
                                      int arg1, int arg2, int arg3) {
                    showDate(arg1, arg2 + 1, arg3); // +1 necessary
                }
            };


    /**
     * Called by the object myDateListener, set the text of the label expirationDatePicker
     * with the chosen date
     */
    private void showDate(int year, int month, int day) {
        TextView pickerDate = findViewById(R.id.expirationDatePicked);
        pickerDate.setText(new StringBuilder().append(day).append("/")
                .append(month).append("/").append(year));
    }


    /**
     * Link the action to the "scan" button
     */
    private void setupBarcodeBottom() {
        ImageButton barCodeButton = findViewById(R.id.barcodeButton);
        barCodeButton.setOnClickListener(this);
    }


    /**
     * Link the database to this class
     */
    private void linkDatabase() {
        AppDatabase database = Room.databaseBuilder(getBaseContext(), AppDatabase.class,
                getString(R.string.database_name))
                .allowMainThreadQueries()
                .build();

        _db = database.getFoodDAO();
    }


    /**
     * Link the XML layout and setup the toolbar
     */
    private void linkXmlAndSetupToolbar() {
        setContentView(R.layout.activity_add_element);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // if "true" show back button
    }


    /**
     * Setup the listeners linked to the fields, they give a feedback if the linked field
     * is not compile correctly
     */
    private void setupTextChangeListeners() {
        TextView editableQuantity = findViewById(R.id.enteredQuantityOfFood);
        TextView editablePrice = findViewById(R.id.enteredPriceOfFood);
        TextView editableExpirationDate = findViewById(R.id.expirationDatePicked);
        TextView editableName = findViewById(R.id.enteredNameOfFood);

        editableQuantity.addTextChangedListener(new MyTextWatcher(editableQuantity));
        editablePrice.addTextChangedListener(new MyTextWatcher(editablePrice));
        editableExpirationDate.addTextChangedListener(new MyTextWatcher(editableExpirationDate));
        editableName.addTextChangedListener(new MyTextWatcher(editableName));
    }




    /**
     * Inner class used to handle the checks necessary for the fields to be correct
     */
    private class MyTextWatcher implements TextWatcher {

        private View _view;


        private MyTextWatcher(View view) { _view = view; }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}


        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}


        @Override
        public void afterTextChanged(Editable editable) {
            switch (_view.getId()) {
                case R.id.enteredQuantityOfFood:
                    validateQuantity();
                    break;
                case R.id.enteredPriceOfFood:
                    validatePrice();
                    break;
                case R.id.enteredNameOfFood:
                    validateName();
                    break;
                case R.id.expirationDatePicked:
                    validateExpirationDate();
                    break;
            }
        }
    }


    /**
     * Check if the field quantity is correct
     */
    private boolean validateQuantity() {

        TextView editableQuantity = findViewById(R.id.enteredQuantityOfFood);

        if (editableQuantity.getText().length() >= 7) {
            editableQuantity.setError(getString(R.string.too_much_char_error));
            requestFocus(editableQuantity);
            return false;
        }

        try {
            int insertedQuantity = Integer.parseInt(editableQuantity.getText().toString());

            if (insertedQuantity <= 0)
                throw new NumberFormatException();

        } catch (NumberFormatException e) {
            editableQuantity.setError(getString(R.string.quantity_error));
            requestFocus(editableQuantity);
            return false;
        }

        editableQuantity.setError(null);
        return true;
    }


    /**
     * Check if the field price is correct
     */
    private boolean validatePrice() {

        TextView editablePrice = findViewById(R.id.enteredPriceOfFood);

        if (editablePrice.getText().length() >= 8) {
            editablePrice.setError(getString(R.string.too_much_char_error));
            requestFocus(editablePrice);
            return false;
        }

        try {
            double insertedPrice = Double.parseDouble(editablePrice.getText().toString());

            if (insertedPrice < 0)
                throw new NumberFormatException();

        } catch (NumberFormatException e) {
            editablePrice.setError(getString(R.string.price_error));
            requestFocus(editablePrice);
            return false;
        }

        editablePrice.setError(null);
        return true;
    }


    /**
     * Check if the field name is correct
     */
    private boolean validateName() {

        TextView editableName = findViewById(R.id.enteredNameOfFood);

        if (editableName.getText().toString().isEmpty()) {
            editableName.setError(getString(R.string.name_error));
            requestFocus(editableName);
            return false;
        }

        editableName.setError(null);
        return true;
    }


    /**
     * Check if the field expiration date is correct
     */
    private boolean validateExpirationDate() {

        TextView editableExpirationDate = findViewById(R.id.expirationDatePicked);
        String chosenDateString = editableExpirationDate.getText().toString();
        Date chosenDate = new Date();

        try {
            chosenDate = new SimpleDateFormat("dd/MM/yyyy", Locale.ITALIAN).parse(chosenDateString);
        } catch (java.text.ParseException e) {}

        if (chosenDateString.isEmpty()) {
            editableExpirationDate.setError(getString(R.string.date_error));
            requestFocus(editableExpirationDate);
            return false;
        } else if (!_insertOldFoodAllowed && chosenDate.compareTo(new Date()) < 0) {
            editableExpirationDate.setError(getString(R.string.date_past_error));
            requestFocus(editableExpirationDate);
            return false;
        }

        editableExpirationDate.setError(null);
        return true;
    }


    /**
     * Active the focus on the given view
     */
    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }


    /**
     * Implements what happen when the user click on back button device
     */
    @Override
    public void onBackPressed() {
        startActivity(new Intent(ManageElementActivity.this, MainActivity.class));
    }



    /**
     * =============================================================================================
     * ========= BELOW THERE ARE THE View.OnClickListener interface implementation functions =======
     * =============================================================================================
     */


    /**
     * Initialize the barcode scanner
     */
    private void setupBarcodeScanner() {
        _codeScanner = new IntentIntegrator(this);
        _codeScanner.setPrompt(getString(R.string.barcode_instructions));
    }


    /**
     * Handle the scanner result
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {

            if (result.getContents() == null) {
                Toast.makeText(this, getString(R.string.barcode_not_ok), Toast.LENGTH_LONG).show();

            } else {
                fillFields(result.getContents());
                Toast.makeText(this, getString(R.string.barcode_ok), Toast.LENGTH_LONG).show();
            }

        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    /**
     * Start the scanner
     */
    @Override
    public void onClick(View view) { _codeScanner.initiateScan(); }


    /**
     * Fill the field with the food stored in the db that match with the given barcode
     */
    private void fillFields(String barcode) {
        TextView scanCodeLabel = findViewById(R.id.enteredBarcodeOfFood);
        scanCodeLabel.setText(barcode);

        List<Food> foods = _db.getFoods();
        for (Food food: foods) {
            if (food.getBarcode() != null && food.getBarcode().equals(barcode)) {
                TextView nameLabel = findViewById(R.id.enteredNameOfFood);
                nameLabel.setText(food.getName());

                TextView priceLabel = findViewById(R.id.enteredPriceOfFood);
                priceLabel.setText("" + food.getPrice());

                ((Spinner) findViewById(R.id.categorySpinner)).setSelection(
                        food.getCategory().getValue());
            }
        }
    }


}
