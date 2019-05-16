package edu.psu.grovermodule.pojo;

import android.content.ContentValues;

import edu.psu.grovermodule.database.DatabaseSetup;
import edu.psu.grovermodule.database.ISqlInsertHandler;

public class Temp implements ISqlInsertHandler {
    private float _temperature;

    public Temp() {}

    public Temp(float temperature) {
        this._temperature = temperature;
    }

    public float getTemperature() {
        return _temperature;
    }

    public void setTemperature(float temperature) {
        this._temperature = temperature;
    }

    @Override
    public ContentValues columnValues() {
        ContentValues values = new ContentValues();
        values.put(DatabaseSetup.TEMPERATURE_VALUE, this._temperature);
        values.put(DatabaseSetup.TIME_STAMP, String.valueOf(new java.util.Date()));
        return values;
    }

    @Override
    public String tableName() {
        return DatabaseSetup.TABLE_TEMPERATURE;
    }
}
