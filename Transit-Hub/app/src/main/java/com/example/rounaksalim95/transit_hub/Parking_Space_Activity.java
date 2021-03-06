package com.example.rounaksalim95.transit_hub;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;


public class Parking_Space_Activity extends AppCompatActivity {

    private TableLayout mTableLayout;

    // JSONObject that holds the JSON data for the garage
    private JSONObject garage;

    // JSONObject that holds the JSON data for the floor
    private JSONObject floor;

    //JSONArray that holds all the parking data
    private JSONArray data;

    // JSONArray that holds the JSON data for the parking slots on the floor
    private JSONArray parkingSlots;

    // Number of parking slots on the floor
    private int numberOfSlots;

    // Drawables set to available and unavailable images
    private Drawable available;
    private Drawable unavailable;

    // Integers that hold the floorID and garageID received from Floor_Activity
    private int floorID, garageID;

    // Holds garage name
    private String garageName;

    // Holds floor name (number)
    private int floorName;

    // Default value used while extracting id from intent
    private final int DEFAULT_VALUE = -1;

    // Preserves a link to the menu
    private Menu optionsMenu;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parking_space);

        mTableLayout= (TableLayout) findViewById(R.id.mainTable);

        Intent intent = getIntent();
        // System.out.println("This is the floor Json: " + intent.getStringExtra("floor"));

        // Get the resources from the drawable
        available = ResourcesCompat.getDrawable(getResources(), R.drawable.available, null);
        unavailable = ResourcesCompat.getDrawable(getResources(), R.drawable.unavailable, null);

        // Set bounds for the drawables
        available.setBounds(0, 0, 135, 135);
        unavailable.setBounds(0, 0, 135, 135);

        // Extract the garage and floor names
        garageName = intent.getStringExtra("garageName");
        floorName = intent.getIntExtra("floorName", DEFAULT_VALUE);

        try {
            /*floor = new JSONObject(intent.getStringExtra("floor"));*/
            data = new JSONArray(intent.getStringExtra("data"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Parse the raw JSON data to extract garages
        data = Floor_Activity.parseGarageData(data);

        // Get the JSON data for the appropriate garage
        try {
            garage = Floor_Activity.getCorrectGarage(data, garageName);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // In case the garage whose parking slots we are viewing is removed
        if (garage == null) {
            startActivity(new Intent(this, Start.class));
            Toast.makeText(this, "This garage has been removed!", Toast.LENGTH_SHORT).show();

        }

        // Get the JSON data for the appropriate floor
        try {
            floor = getCorrectFloor(garage.getJSONArray("floors"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (floor == null) {
            Intent floorIntent = new Intent(this, Floor_Activity.class);
            intent.putExtra("garages", data.toString());
            intent.putExtra("garageName", garageName);
            startActivity(floorIntent);
            Toast.makeText(this, "This floor hsa been removed!", Toast.LENGTH_SHORT).show();

        } else {
            try {
                displayParkingSlots();
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }


    @Override
    protected void onStart() {
        super.onStart();

        // Store our shared preferences
        SharedPreferences sp = getSharedPreferences("ACTIVE", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(this.getLocalClassName(), true);
        editor.putBoolean("Start", false);
        editor.putBoolean("Floor_Activity", false);
        editor.putString("garageName", garageName);
        editor.putInt("floorName", floorName);
        editor.apply();
    }


    @Override
    protected void onStop() {
        super.onStop();

        /*// Store our shared preferences
        SharedPreferences sp = getSharedPreferences("ACTIVE", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(this.getLocalClassName(), false);
        editor.apply();*/
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Store our shared preferences
        SharedPreferences sp = getSharedPreferences("ACTIVE", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(this.getLocalClassName(), false);
        editor.apply();
    }

    /**
     * Method that displays all the parking slots and their availability status
     * @throws JSONException
     */
    private void displayParkingSlots() throws JSONException {

        // Get the JSON data for the parking slots
        parkingSlots = floor.getJSONArray("parkingSpots");

        // Place holders for the text that has to go into the TextViews
        String lhsString, rhsString;

        // Holds parkingSlotID
        int parkingSlotID;

        // HashMap used for mapping parkingSlotID to object index
        HashMap<Integer, Integer> translation = new HashMap<>();

        if (parkingSlots != null) {

            // Loop through slots and map parkingSlotID to corresponding object index
            for (int i = 0; i < parkingSlots.length(); ++i) {
                parkingSlotID = parkingSlots.getJSONObject(i).getInt("parkingSpotID");
                translation.put(parkingSlotID, i);
            }

            // Loop through all the parking slots and display their availability
            numberOfSlots = parkingSlots.length();
            for (int i = 1; i <= numberOfSlots; i+=2) {

                // Create a table row to add to the TableLayout
                TableRow row = new TableRow(this);

                // Declare the layoutParams for the table row and textviews
                TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT);
                TableRow.LayoutParams tvlp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
                row.setLayoutParams(lp);

                TextView lhs = new TextView(this);
                TextView rhs = new TextView(this);
                lhs.setLayoutParams(tvlp);
                rhs.setLayoutParams(tvlp);

                // Set the size of the text in the text field
                lhs.setTextSize(22);
                rhs.setTextSize(22);

                // Check whether there are an odd number of parking slots
                // so that only the left side is printed out
                if (numberOfSlots % 2 != 0 && i >= numberOfSlots) {

                    lhsString = "Parking spot " + i + " : ";
                    lhs.setText(lhsString);

                    // Set the drawable to the right of the text
                    lhs.setCompoundDrawables(null, null,
                            getDrawable(parkingSlots.getJSONObject(translation.get(i))), null);

                } else {
                    lhsString = "Parking spot " + i + " : ";
                    rhsString = "Parking spot " + (i + 1) + " : ";
                    lhs.setText(lhsString);
                    rhs.setText(rhsString);

                    // Set the drawable to the right of the text
                    lhs.setCompoundDrawables(null, null,
                            getDrawable(parkingSlots.getJSONObject(translation.get(i))), null);

                    rhs.setCompoundDrawables(null, null,
                            getDrawable(parkingSlots.getJSONObject(translation.get(i + 1))), null);
                }

                // Add the views to the row
                row.addView(lhs);
                row.addView(rhs);

                // Add the row to the TableLayout
                mTableLayout.addView(row);
            }
        } else {

            // Throw a toast indicating that there are not parking slots on this floor
            // (if that ever happens)
            Toast.makeText(this, "Surprisingly there are no parking slots on this floor",
                    Toast.LENGTH_LONG).show();
        }
    }


    /**
     * Gets the correct floor matching by name
     * @param floors JSONArray containing floor JSONObjects
     * @return JSONObject of appropriate floor
     */
    private JSONObject getCorrectFloor(JSONArray floors) throws JSONException {
        for (int i = 0; i < floors.length(); ++i) {
            if (floors.getJSONObject(i).getInt("floorNumber") == floorName) {
                return floors.getJSONObject(i);
            }
        }
        return null;
    }


    /**
     * Sets the drawable to the appropriate image bases on the availability of the parking slots
     * @param slot The JSONObject that contains information about the parking slot
     * @return Returns the appropriate drawable
     */
    private Drawable getDrawable(JSONObject slot) throws JSONException {
        if (slot.getString("Availability").equals("true")) {
            return available;
        } else {
            return unavailable;
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, Floor_Activity.class);
        intent.putExtra("garageName", garageName);
        intent.putExtra("garages", data.toString());
        startActivity(intent);
    }
}
