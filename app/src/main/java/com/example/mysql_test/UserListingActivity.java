package com.example.mysql_test;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mysql_test.helper.CheckNetworkStatus;
import com.example.mysql_test.helper.HttpJsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class UserListingActivity extends AppCompatActivity {
    private static final String KEY_SUCCESS = "success";
    private static final String KEY_DATA = "data";
    private static final String KEY_USER_ID = "userID";
    private static final String KEY_USER_NAME = "userName";
    private static final String BASE_URL = "http://127.0.0.1/users/";
    private ArrayList<HashMap<String, String>> userList;
    private ListView userListView;
    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_listing);
        userListView = (ListView) findViewById(R.id.userList);
        new FetchUsersAsyncTask().execute();

    }

    /**
     * Fetches the list of movies from the server
     */
    private class FetchUsersAsyncTask extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //Display progress bar
            pDialog = new ProgressDialog(UserListingActivity.this);
            pDialog.setMessage("Loading users. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            HttpJsonParser httpJsonParser = new HttpJsonParser();
            JSONObject jsonObject = httpJsonParser.makeHttpRequest(
                    BASE_URL + "fetch_all_users.php", "GET", null);
            try {
                int success = jsonObject.getInt(KEY_SUCCESS);
                JSONArray users;
                if (success == 1) {
                    userList = new ArrayList<>();
                    users = jsonObject.getJSONArray(KEY_DATA);
                    //Iterate through the response and populate movies list
                    for (int i = 0; i < users.length(); i++) {
                        JSONObject user = users.getJSONObject(i);
                        Integer userID = user.getInt(KEY_USER_ID);
                        String userName = user.getString(KEY_USER_NAME);
                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put(KEY_USER_ID, userID.toString());
                        map.put(KEY_USER_NAME, userName);
                        userList.add(map);
                    }
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
                    populateUserList();
                }
            });
        }

    }

    /**
     * Updating parsed JSON data into ListView
     * */
    private void populateUserList() {
        ListAdapter adapter = new SimpleAdapter(
                UserListingActivity.this, userList,
                R.layout.list_item, new String[]{KEY_USER_ID,
                KEY_USER_NAME},
                new int[]{R.id.userID, R.id.userName});
        // updating listview
        userListView.setAdapter(adapter);
        //Call MovieUpdateDeleteActivity when a movie is clicked
        userListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //Check for network connectivity
                if (CheckNetworkStatus.isNetworkAvailable(getApplicationContext())) {
                    String userID = ((TextView) view.findViewById(R.id.userID))
                            .getText().toString();
                    Intent intent = new Intent(getApplicationContext(),
                            UserUpdateDeleteActivity.class);
                    intent.putExtra(KEY_USER_ID, userID);
                    startActivityForResult(intent, 20);

                } else {
                    Toast.makeText(UserListingActivity.this,
                            "Unable to connect to internet",
                            Toast.LENGTH_LONG).show();

                }


            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 20) {
            // If the result code is 20 that means that
            // the user has deleted/updated the movie.
            // So refresh the movie listing
            Intent intent = getIntent();
            finish();
            startActivity(intent);
        }
    }

}
