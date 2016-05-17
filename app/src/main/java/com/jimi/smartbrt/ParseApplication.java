package com.jimi.smartbrt;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseObject;

/**
 * Created by gidimo on 30/04/2016.
 */
public class ParseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Parse.initialize(this, "o9KLa0HRC5lE2VVL3nKSBDL2gyta5i5Cz1fjNRUQ", "JRFRkur90hHB0oQMp2AIpNVe7pZISoEmaTm2ErHF");

        ParseObject testObject = new ParseObject("TestObject");
        testObject.put("foo", "bar");
        testObject.saveInBackground();
    }
}
