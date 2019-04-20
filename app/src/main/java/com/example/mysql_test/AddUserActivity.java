package com.example.mysql_test;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.mysql_test.helper.CheckNetworkStatus;
import com.example.mysql_test.helper.HttpJsonParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AddUserActivity extends AppCompatActivity {
    private static final String KEY_SUCCESS = "success";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_USER_ID = "userID";
    private static final String BASE_URL = "http://127.0.0.1/users/";
    private static String STRING_EMPTY = "";
    private EditText userNameEditText;
    private EditText passwordEditText;
    private EditText userIDEditText;
    private String userName;
    private String password;
    private String userID;
    private Button addButton;
    private int success;
    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);
        userNameEditText = (EditText) findViewById(R.id.txtUserNameAdd);
        passwordEditText = (EditText) findViewById(R.id.txtPasswordAdd);
        userIDEditText = (EditText) findViewById(R.id.txtUserIDAdd);
        addButton = (Button) findViewById(R.id.btnAdd);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (CheckNetworkStatus.isNetworkAvailable(getApplicationContext())) {
                    AddUser();
                } else {
                    Toast.makeText(AddUserActivity.this,
                            "Unable to connect to internet",
                            Toast.LENGTH_LONG).show();

                }

            }
        });

    }

    /**
     * Checks whether all files are filled. If so then calls AddMovieAsyncTask.
     * Otherwise displays Toast message informing one or more fields left empty
     */
    private void AddUser() {
        if (!STRING_EMPTY.equals(userNameEditText.getText().toString()) &&
                !STRING_EMPTY.equals(passwordEditText.getText().toString()) &&
                !STRING_EMPTY.equals(userIDEditText.getText().toString())) {

            userName = userNameEditText.getText().toString();
            password = passwordEditText.getText().toString();
            userID = userIDEditText.getText().toString();
            new AddUserAsyncTask().execute();
        } else {
            Toast.makeText(AddUserActivity.this,
                    "One or more fields left empty!",
                    Toast.LENGTH_LONG).show();

        }


    }

    /**
     * AsyncTask for adding a movie
     */
    private class AddUserAsyncTask extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //Display proggress bar
            pDialog = new ProgressDialog(AddUserActivity.this);
            pDialog.setMessage("Adding User. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            HttpJsonParser httpJsonParser = new HttpJsonParser();
            Map<String, String> httpParams = new HashMap<>();
            //Populating request parameters
            httpParams.put(KEY_USER_NAME, userName);
            httpParams.put(KEY_PASSWORD, password);
            httpParams.put(KEY_USER_ID, userID);
            JSONObject jsonObject = httpJsonParser.makeHttpRequest(
                    BASE_URL + "add_user.php", "POST", httpParams);
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
                        Toast.makeText(AddUserActivity.this,
                                "User Added", Toast.LENGTH_LONG).show();
                        Intent i = getIntent();
                        //send result code 20 to notify about movie update
                        setResult(20, i);
                        //Finish ths activity and go back to listing activity
                        finish();

                    } else {
                        Toast.makeText(AddUserActivity.this,
                                "Some error occurred while adding user",
                                Toast.LENGTH_LONG).show();

                    }
                }
            });
        }
    }
}