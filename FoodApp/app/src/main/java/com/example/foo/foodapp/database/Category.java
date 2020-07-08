package com.example.foo.foodapp.database;

/**
 * enumeration representing the food categories implemented in the app
 */
public enum Category {


    // the int value is necessary because this enum has to be stored in the database so its
    // values are converted to int
    CASEARI(0), CARNE(1), PESCE(2), BEVANDA(3), DOLCI(4), FRUTTA(5), VERDURA(6), PANETTERIA(7), ALTRO(8);
    private int _value;


    Category(int value) { _value = value; }


    public int getValue() { return _value; }


    public static Category getName(int value) {
        for (Category cName : Category.values()) {
            if (cName._value == value)
                return cName;
        }
        return null;
    }


}
