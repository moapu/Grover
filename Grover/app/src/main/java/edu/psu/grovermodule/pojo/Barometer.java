package edu.psu.grovermodule.pojo;

import android.content.ContentValues;

import edu.psu.grovermodule.database.DatabaseSetup;
import edu.psu.grovermodule.database.ISqlInsertHandler;

/**
 * This class holds setters and getters for the
 * barometer pressure sensor
 */
public class Barometer implements ISqlInsertHandler {
    private float _barometricPressure;

    public Barometer() {}

    public Barometer(float pressure) {
        this._barometricPressure = pressure;
    }

    public float getPressure() {
        return this._barometricPressure;
    }

    public void setPressure(float barometric) {
        this._barometricPressure = barometric;
    }

    @Override
    public ContentValues columnValues() {
        ContentValues values = new ContentValues();
        values.put(DatabaseSetup.BARO_PRESSURE, this._barometricPressure);
        values.put(DatabaseSetup.TIME_STAMP, String.valueOf(new java.util.Date()));
        return values;
    }

    @Override
    public String tableName() {
        return DatabaseSetup.TABLE_BARO;
    }
}
