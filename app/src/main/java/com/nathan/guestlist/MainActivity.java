package com.nathan.guestlist;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    EditText mNewGuestNameInput;
    Button mAddGuestButton;
    Button mClearButton;
    TextView mGuestList;

    ArrayList<String> mGuests;

    private static final String TAG = "API_ACTIVITY";

    private static final String BASE_API_URL = "";
    private static final String API_KEY = "";

    private static final String GUEST_LIST_KEY = "guest-list-bundle-key";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNewGuestNameInput = findViewById(R.id.new_guest_input);
        mAddGuestButton = findViewById(R.id.add_guest_button);
        mClearButton = findViewById(R.id.clear_button);
        mGuestList = findViewById(R.id.list_of_guests);

        getAllGuests();

        //updateGuestList();

        mAddGuestButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String newGuestName = mNewGuestNameInput.getText().toString();
                if (newGuestName.trim().isEmpty()) {
                    Toast.makeText(MainActivity.this, "Enter a name", Toast.LENGTH_LONG).show();
                    return;
                }
//                mGuests.add(newGuestName);
                mNewGuestNameInput.setText("");
//                updateGuestList();
                addGuest(newGuestName);
            }
        });
        mClearButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //clearGuestList();
                deleteAllGuests();
                Toast.makeText(MainActivity.this, "Guestlist Cleared", Toast.LENGTH_LONG).show();
            }
        });

    }

    @Override
    public void onSaveInstanceState(Bundle outBundle){
        super.onSaveInstanceState(outBundle);
        outBundle.putStringArrayList(GUEST_LIST_KEY, mGuests);
    }

    private void updateGuestList(){
        StringBuilder builder = new StringBuilder();
        for (String guest: mGuests) {
            builder.append(guest);
            builder.append("\n");
        }

        String list = builder.toString();
        mGuestList.setText(list);
    }

    private void getAllGuests() {

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonArrayRequest jsonRequest = new JsonArrayRequest(Request.Method.GET, BASE_API_URL, null, new Response.Listener<JSONArray>() {

            @Override
            public void onResponse(JSONArray response) {
                Log.i(TAG, "Received all guests JSON" + response);
                try {
                    ArrayList<String> guests = new ArrayList<>();
                    for (int i = 0; i < response.length(); i++) {
                        JSONObject ob = response.getJSONObject(i);
                        String name = ob.getString("name");
                        guests.add(name);
                    }

                    mGuests = guests;
                    updateGuestList();

                } catch (JSONException e) {
                    Log.e(TAG, "Get all guests, error processing JSON" + response, e);
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Error fetching all guests", error);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put("guestlist-key", API_KEY);
                return params;
            }
        };

        queue.add(jsonRequest);
    }


    private void addGuest(String name) {

        final String guestName = name;
        RequestQueue queue = Volley.newRequestQueue(this);
        JSONObject newGuest = new JSONObject();

        try {
            newGuest.put("name", name);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating json for new guest name", e);
            return;
        }


        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, BASE_API_URL, newGuest, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.i(TAG, "Added guest " + guestName  + "  and received response " + response);
                getAllGuests();
            }
        },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error adding new guest", error);
                    }
                }) {

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put("guestlist-key", API_KEY);
                return params;
            }
        };

        queue.add(request);
    }


    private void deleteAllGuests() {

        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest request = new StringRequest(Request.Method.DELETE, BASE_API_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                mGuests.clear();
                Log.d(TAG, "All guests deleted.");
                updateGuestList();

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Error deleting all guests", error);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put("guestlist-key", API_KEY);
                return params;
            }
        };

        queue.add(request);

    }
}
