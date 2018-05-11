package com.yourstrulyssj.smartgaskit;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.yourstrulyssj.smartgaskit.app.AppConfig;
import com.yourstrulyssj.smartgaskit.app.AppController;
import com.yourstrulyssj.smartgaskit.helper.SQLiteHandler;
import com.yourstrulyssj.smartgaskit.helper.SessionManager;
import com.android.volley.Request.Method;
import com.android.volley.Response;

import org.json.JSONException;
import org.json.JSONObject;


import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private EditText firstName;
    private EditText lastName;
    private EditText vname;
    private EditText vno;
    private Button btnOk;
    private ProgressDialog pDialog;
    private SessionManager session;
    private SQLiteHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firstName = (EditText) findViewById(R.id.firstName);
        lastName = (EditText) findViewById(R.id.lastName);
        vname = (EditText) findViewById(R.id.vendorName);
        vno = (EditText) findViewById(R.id.vendorContact);
        btnOk = (Button) findViewById(R.id.btnOk);

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // Session manager
        session = new SessionManager(getApplicationContext());

        // SQLite database handler
        db = new SQLiteHandler(getApplicationContext());


        // Check if user is already logged in or not
        if (session.isLoggedIn()) {
            // User is already logged in. Take him to main activity
            Intent intent = new Intent(MainActivity.this,
                    Main2Activity.class);
            startActivity(intent);
            finish();
        }

        // Register Button Click event
        btnOk.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                String firstname = firstName.getText().toString().trim();
                String lastname = lastName.getText().toString().trim();
                String vendorname = vname.getText().toString().trim();
                String vendorcontact = vno.getText().toString().trim();

                if (!firstname.isEmpty() && !lastname.isEmpty() && !vendorname.isEmpty() && !vendorcontact.isEmpty()) {
                    registerUser(firstname, lastname,vendorname, vendorcontact);
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Please enter your details!", Toast.LENGTH_LONG)
                            .show();
                }

            }
        });

        // Link to Login Screen



    }

    private void registerUser(final String firstname, final String lastname,
                              final String vendorname, final String vendorcontact) {
        // Tag used to cancel the request
        String tag_string_req = "req_register";

        pDialog.setMessage("Registering ...");
        showDialog();

        StringRequest strReq = new StringRequest(Method.POST,
                AppConfig.URL_REGISTER, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Register Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    if (!error) {
                        // User successfully stored in MySQL
                        // Now store the user in sqlite
                        String uid = jObj.getString("uid");

                        JSONObject user = jObj.getJSONObject("user");
                        String firstname = user.getString("firstname");
                        String lastname = user.getString("lastname");
                        String vname = user.getString("vname");
                        String vno = user.getString("vno");
                        String created_at = user
                                .getString("created_at");

                        // Inserting row in users table
                        db.addUser(firstname, lastname, vname, vno, created_at);

                        Toast.makeText(getApplicationContext(), "User successfully registered. Try login now!", Toast.LENGTH_LONG).show();

                        // Launch login activity
                        Intent intent = new Intent(
                                MainActivity.this,
                                Main2Activity.class);
                        startActivity(intent);
                        finish();
                    } else {

                        // Error occurred in registration. Get the error
                        // message
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Registration Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();

            }

        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting params to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("firstname", firstname);
                params.put("lastname", lastname);
                params.put("vname", vendorname);
                params.put("vno", vendorcontact);

                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

}
