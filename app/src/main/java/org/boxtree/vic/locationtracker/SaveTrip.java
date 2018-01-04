package org.boxtree.vic.locationtracker;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import org.boxtree.vic.locationtracker.persistence.LocationTrackerDbHelper;
import org.boxtree.vic.locationtracker.vo.Trip;

import java.util.ArrayList;
import java.util.Calendar;

public class SaveTrip extends AppCompatActivity implements View.OnClickListener{

    private Trip mTrip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_trip);

        // get routes from intent and create new trip object
        Intent i = this.getIntent();
        ArrayList<Location> route = i.getParcelableArrayListExtra("Route");
        Log.d("SaveTrip", "with route " + route);
        mTrip = new Trip(route);

        Button saveButton = (Button) findViewById(R.id.saveTripButton);
        saveButton.setOnClickListener(this);

        ImageButton addPhotosButton = (ImageButton) findViewById(R.id.addPicturesButton);
        addPhotosButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.saveTripButton:

                saveTrip();

                Toast.makeText(this, "Saving Trip", Toast.LENGTH_LONG).show();

                Intent data = new Intent();
                setResult(RESULT_OK, data);

                finish();
                break;
            case R.id.addPicturesButton:
                Toast.makeText(this, "TODO implement picture saving", Toast.LENGTH_LONG).show();
                break;
            default:
                break;
        }
    }

    private void saveTrip() {
        EditText name = (EditText) findViewById(R.id.editTripName);
        mTrip.setName(name.getText().toString());

        EditText description = (EditText) findViewById(R.id.editDescription);
        mTrip.setDescription(description.getText().toString());

        mTrip.setDate(Calendar.getInstance().getTime()); // now

        // TODO photos


        LocationTrackerDbHelper dbh = new LocationTrackerDbHelper(getApplicationContext());

        dbh.insertNewTrip(mTrip);

    }


}
