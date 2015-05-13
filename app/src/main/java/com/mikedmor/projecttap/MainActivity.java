package com.mikedmor.projecttap;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity {

    // Progress Dialog
    private ProgressDialog pDialog;

    JSONParser jsonParser = new JSONParser();
    // User JSONArray
    JSONArray User = null;

    CheckBox rememberMe;
    EditText userid;
    EditText upassword;
    TextView status_message;
    ServerCheck initial = new ServerCheck();

    // url to create new product
    private static String url_get_login = "http://www.mikedmor.com/ProjectTap/get_login_details.php";
    public static final String PREFS_NAME = "PTPrefsFile";
    // JSON Node names
    private static final String TAG_SUCCESS = "success";

    /** Called when the user clicks the Send button */
    public void registerAccount(View view) {
        // build the intent
        Intent intent = new Intent(this, RegistrationActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userid = (EditText) findViewById(R.id.userid);
        upassword = (EditText) findViewById(R.id.password);
        rememberMe = (CheckBox) findViewById(R.id.rememberMe);
        status_message = (TextView) findViewById(R.id.status_msg);

        Button login_btn = (Button) findViewById(R.id.login_btn);
        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                checkLogin(v);
            }
        });

        //Create status message from saved string value
        if(getIntent().hasExtra("status")) {
            String status_msg_value = getIntent().getExtras().getString("status");
            if(status_msg_value.equals("1")){
                //successfull (change color of status to green
                status_message.setTextColor(Color.parseColor("#4CAF50"));
            }else{
                //unsuccessfull (change color of status to red
                status_message.setTextColor(Color.parseColor("#F44336"));
            }
            String status_msg_string = getIntent().getExtras().getString("status_msg");
            status_message.setText(status_msg_string);
        }

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        boolean autologin = settings.getBoolean("rememberMe", false);
        String PR_username = settings.getString("PR_username", "");
        String PR_password = settings.getString("PR_password", "");

        //update the textbox's to reflect the PREFS
        userid.setText(PR_username);
        upassword.setText(PR_password);
        rememberMe.setChecked(autologin);
        initial.execute();

    }

    @Override
    protected void onStop(){
        super.onStop();

        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("rememberMe", rememberMe.isChecked());
        if(rememberMe.isChecked()) {
            editor.putString("PR_username", userid.getText().toString());
            editor.putString("PR_password", upassword.getText().toString());
        }
        // Commit the edits!
        editor.apply();
    }

    public void autoLogin(boolean autologin){
        if(autologin){
            new AccountLogin().execute();
        }
    }

    public void checkLogin(View v){
        //TODO: Write login script
        if(userid.getText().toString().equals("")) {
            //update textviews to show error message in hint!!
            userid.setText("");
            upassword.setText("");
            userid.setHint("Can't login without a username!");
        }else if(upassword.getText().toString().equals("")) {
            //update textviews to show error message in hint!!
            upassword.setText("");
            upassword.setHint("Must provide password!");
        }else{
                // creating new product in background thread
                if(initial.getServerReachable()) {
                    new AccountLogin().execute();
                }else{
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Not Online!")
                            .setMessage("We're sorry, but it doesn't seem like your connected to the internet. Please connect to the internet and try again.")
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int whichButton) {

                                }
                            }).setCancelable(false).show();
                }
            }
        }

    class ServerCheck extends AsyncTask<String, String, String> {

        Boolean ServerReachable = false;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Checking Internet...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }
        public Boolean getServerReachable(){
            return ServerReachable;
        }

        protected String doInBackground(String... args) {
            try {
                URL myUrl = new URL("http://www.mikedmor.com");
                URLConnection connection = myUrl.openConnection();
                connection.setConnectTimeout(1000);
                connection.connect();
                ServerReachable=true;
            } catch (Exception e) {
                // Handle your exceptions
                Log.d("IT DIDNT WORK", e.toString());
            }
            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog once done
            pDialog.dismiss();
            if(!ServerReachable){
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Not Online!")
                        .setMessage("We're sorry, but it doesn't seem like your connected to the internet. Please connect to the internet and try again.")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {

                            }
                        }).setCancelable(false).show();
            }else{
                autoLogin(rememberMe.isChecked());
            }
        }
    }

    /**
     * Background Async Task to login to accounts
     * */
    class AccountLogin extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Logging on...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        /**
         * Creating product
         * */
        protected String doInBackground(String... args) {
            String username = userid.getText().toString();
            String password = upassword.getText().toString();

            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("username", username));
            params.add(new BasicNameValuePair("password", password));

            // getting JSON Object
            // Note that create product url accepts POST method
            JSONObject json = jsonParser.makeHttpRequest(url_get_login,
                    "POST", params);

            // check log cat fro response
            //Log.d("Create Response", json.toString());

            // check for success tag
            try {
                int success;
                if(initial.getServerReachable()){
                    success = json.getInt(TAG_SUCCESS);
                }else{
                    success = 0;
                    throw new JSONException("Couldn't reach the internet");
                }


                if (success == 1) {
                        // successfully created account
                        //Setup our test data
                    String aJsonString = json.getString("account");
                    String status_msg = "Login Successful! Bet you got excited for a moment. Check back after the next update.";
                        //Setup the bundle that will be passed
                    //Bundle b = new Bundle();
                    //b.putString("status",String.valueOf(success));
                    //b.putString("status_msg", status_msg);
                        //Setup the Intent that will start the next Activity
                    //Intent nextActivity = new Intent(RegistrationActivity.this, MainActivity.class);
                        //Assumes this references this instance of Activity A
                    //nextActivity.putExtras(b);

                    //RegistrationActivity.this.startActivity(nextActivity);

                    //finish();

                        // successfully logged on
                        //Check if the user wants their login details saved for autologin

                        //Store user data for use in application later
                    Bundle b = new Bundle();
                    b.putString("status",String.valueOf(success));
                    b.putString("status_msg", status_msg);
                    b.putString("user_data", aJsonString);

                    User = json.getJSONArray("account");

                    // looping through All Products
                    JSONObject c = User.getJSONObject(0);

                    // Storing each json item in variable
                    String pid = c.getString("pid");
                    String uname = c.getString("username");
                    String uemail = c.getString("email");
                    String ubirthday = c.getString("birthday");


                    Log.d("JObject: ", aJsonString);
                        //Setup the Intent that will start the next Activity
                    //if(uname.equals("mikedmor")){
                        Intent nextActivity = new Intent(MainActivity.this, HomeActivity.class);
                        nextActivity.putExtras(b);
                        MainActivity.this.startActivity(nextActivity);
                    //}else {
                    //    Intent nextActivity = new Intent(MainActivity.this, MainActivity.class);
                    //    nextActivity.putExtras(b);
                    //    MainActivity.this.startActivity(nextActivity);
                    //}
                        //Assumes this references this instance of Activity A

                    finish();

                } else {
                    // something went wrong with the account login
                    //get json message
                    String aJsonString = json.getString("message");
                    //Update the status_msg to reflect the error from the server
                    Bundle b = new Bundle();
                    b.putString("status",String.valueOf(success));
                    b.putString("status_msg", aJsonString);
                    //Setup the Intent that will start the next Activity
                    Intent nextActivity = new Intent(MainActivity.this, MainActivity.class);
                    //Assumes this references this instance of Activity A
                    nextActivity.putExtras(b);

                    MainActivity.this.startActivity(nextActivity);

                    finish();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog once done
            pDialog.dismiss();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
