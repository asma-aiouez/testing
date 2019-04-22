package com.example.mysql_test;


import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.app.AlertDialog;

import com.example.mysql_test.helper.CheckNetworkStatus;
import com.example.mysql_test.helper.HttpJsonParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class UserUpdateDeleteActivity extends AppCompatActivity {
    private static String STRING_EMPTY = "";
    private static final String KEY_SUCCESS = "success";
    private static final String KEY_DATA = "data";
    private static final String KEY_USER_ID = "userID";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_PASSWORD = "password";
    private static final String BASE_URL = "http://10.225.121.175/users/";
    private String userID;
    private String userName;
    private String password;
    private EditText userNameEditText;
    private EditText userIDEditText;
    private EditText passwordEditText;

    private Button deleteButton;
    private Button updateButton;
    private int success;
    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_update_delete);
        //Intent intent = new Intent(getApplicationContext());
        Intent intent = getIntent();
        userNameEditText = (EditText) findViewById(R.id.txtUserNameUpdate);
        userIDEditText = (EditText) findViewById(R.id.txtUserIDUpdate);
        passwordEditText = (EditText) findViewById(R.id.txtPasswordUpdate);


        userID = intent.getStringExtra(KEY_USER_ID);
        userName = intent.getStringExtra(KEY_USER_NAME);
        password = intent.getStringExtra(KEY_PASSWORD);
        Log.w("myApp", "USER ID here "+userID+userName+password);
        Log.w("myApp", "USER ID "+userID);
        new FetchuserDetailsAsyncTask().execute();
        deleteButton = (Button) findViewById(R.id.btnDelete);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmDelete();
            }
        });
        updateButton = (Button) findViewById(R.id.btnUpdate);
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (CheckNetworkStatus.isNetworkAvailable(getApplicationContext())) {
                    UpdateUser();

                } else {
                    Toast.makeText(UserUpdateDeleteActivity.this,
                            "Unable to connect to internet",
                            Toast.LENGTH_LONG).show();

                }

            }
        });


    }

    /**
     * Fetches single user details from the server
     */
    private class FetchuserDetailsAsyncTask extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //Display progress bar
            pDialog = new ProgressDialog(UserUpdateDeleteActivity.this);
            pDialog.setMessage("Loading user Details. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            HttpJsonParser httpJsonParser = new HttpJsonParser();
            Map<String, String> httpParams = new HashMap<>();
            httpParams.put(KEY_USER_ID, userID);
            Log.w("myApp", "httpParams "+httpParams);

            JSONObject jsonObject = httpJsonParser.makeHttpRequest(
                    BASE_URL + "get_user_details.php", "GET", httpParams);
            try {
                Log.w("myApp", "inting obj");
                int success = jsonObject.getInt(KEY_SUCCESS);
                Log.w("myApp", "success in update uset  "+success);
                JSONObject user;
                if (success == 1) {
                    //Parse the JSON response
                    user = jsonObject.getJSONObject(KEY_DATA);
                    userName = user.getString(KEY_USER_NAME);
                    userID = user.getString(KEY_USER_ID);
                    password = user.getString(KEY_PASSWORD);


                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String result) {
            pDialog.dismiss();
            runOnUiThread(new Runnable() {
                public void run() {
                    //Populate the Edit Texts once the network activity is finished executing
                    userNameEditText.setText(userName);
                    userIDEditText.setText(userID);
                    passwordEditText.setText(password);

                }
            });
        }


    }

    /**
     * Displays an alert dialogue to confirm the deletion
     */
    private void confirmDelete() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                UserUpdateDeleteActivity.this);
        alertDialogBuilder.setMessage("Are you sure, you want to delete this user?");
        alertDialogBuilder.setPositiveButton("Delete",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        if (CheckNetworkStatus.isNetworkAvailable(getApplicationContext())) {
                            //If the user confirms deletion, execute DeleteuserAsyncTask
                            new DeleteuserAsyncTask().execute();
                        } else {
                            Toast.makeText(UserUpdateDeleteActivity.this,
                                    "Unable to connect to internet",
                                    Toast.LENGTH_LONG).show();

                        }
                    }
                });

        alertDialogBuilder.setNegativeButton("Cancel", null);

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    /**
     * AsyncTask to delete a user
     */
    private class DeleteuserAsyncTask extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //Display progress bar
            pDialog = new ProgressDialog(UserUpdateDeleteActivity.this);
            pDialog.setMessage("Deleting user. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            HttpJsonParser httpJsonParser = new HttpJsonParser();
            Map<String, String> httpParams = new HashMap<>();
            //Set user_id parameter in request
            httpParams.put(KEY_USER_ID, userID);
            Log.w("myApp", "deleting this id "+httpParams);
            JSONObject jsonObject = httpJsonParser.makeHttpRequest(
                    BASE_URL + "delete_user.php", "GET", httpParams);
            try {
                success = jsonObject.getInt(KEY_SUCCESS);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String result) {
            pDialog.dismiss();
            runOnUiThread(new Runnable() {
                public void run() {
                    if (success == 1) {
                        //Display success message
                        Toast.makeText(UserUpdateDeleteActivity.this,
                                "user Deleted", Toast.LENGTH_LONG).show();
                        Intent i = getIntent();
                        //send result code 20 to notify about user deletion
                        setResult(20, i);
                        finish();

                    } else {
                        Toast.makeText(UserUpdateDeleteActivity.this,
                                "Some error occurred while deleting user",
                                Toast.LENGTH_LONG).show();

                    }
                }
            });
        }
    }

    /**
     * Checks whether all files are filled. If so then calls UpdateuserAsyncTask.
     * Otherwise displays Toast message informing one or more fields left empty
     */
    private void UpdateUser() {


        if (!STRING_EMPTY.equals(userNameEditText.getText().toString()) &&
                !STRING_EMPTY.equals(userIDEditText.getText().toString()) &&
                !STRING_EMPTY.equals(passwordEditText.getText().toString())) {

            userName = userNameEditText.getText().toString();
            userID = userIDEditText.getText().toString();
            password = passwordEditText.getText().toString();
            new UpdateUserAsyncTask().execute();
        } else {
            Toast.makeText(UserUpdateDeleteActivity.this,
                    "One or more fields left empty!",
                    Toast.LENGTH_LONG).show();

        }


    }
    /**
     * AsyncTask for updating a user details
     */

    private class UpdateUserAsyncTask extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //Display progress bar
            pDialog = new ProgressDialog(UserUpdateDeleteActivity.this);
            pDialog.setMessage("Updating user. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            HttpJsonParser httpJsonParser = new HttpJsonParser();
            Map<String, String> httpParams = new HashMap<>();
            //Populating request parameters
            httpParams.put(KEY_USER_ID, userID);
            httpParams.put(KEY_USER_NAME, userName);
            httpParams.put(KEY_PASSWORD, password);
            Log.w("myApp", "YOUR UPDATED PARAMS "+httpParams);
            JSONObject jsonObject = httpJsonParser.makeHttpRequest(
                    BASE_URL + "update_user.php", "GET", httpParams);
            try {
                success = jsonObject.getInt(KEY_SUCCESS);
                Log.w("myApp", "YOUR json "+jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String result) {
            pDialog.dismiss();
            runOnUiThread(new Runnable() {
                public void run() {
                    if (success == 1) {
                        //Display success message
                        Toast.makeText(UserUpdateDeleteActivity.this,
                                "User Updated", Toast.LENGTH_LONG).show();
                        Intent i = getIntent();
                        //send result code 20 to notify about user update
                        setResult(20, i);
                        finish();

                    } else {
                        Toast.makeText(UserUpdateDeleteActivity.this,
                                "Some error occurred while updating user",
                                Toast.LENGTH_LONG).show();

                    }
                }
            });
        }
    }
}