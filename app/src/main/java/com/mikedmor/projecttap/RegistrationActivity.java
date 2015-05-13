package com.mikedmor.projecttap;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import com.mikedmor.projecttap.DatePickerFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class RegistrationActivity extends ActionBarActivity {

    public void closeReg(View view) {
        // close registration dialog
        finishActivity(0);
    }

    public void showDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");

    }

    // Progress Dialog
    private ProgressDialog pDialog;

    JSONParser jsonParser = new JSONParser();


    EditText regUID;
    EditText regPASS;
    EditText regConPASS;
    EditText regemail;
    EditText regbirth;

    // url to create new product
    private static String url_create_account = "http://www.mikedmor.com/ProjectTap/create_account.php";

    // JSON Node names
    private static final String TAG_SUCCESS = "success";

    public static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        regUID = (EditText) findViewById(R.id.regUID);
        regPASS = (EditText) findViewById(R.id.regPASS);
        regConPASS = (EditText) findViewById(R.id.regConPASS);
        regemail = (EditText) findViewById(R.id.regemail);
        regbirth = (EditText) findViewById(R.id.regbirth);

        regbirth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(v);
            }
        });
        Button signupBtn = (Button) findViewById(R.id.signup);

        // button click event
        signupBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                //do some error checking on the fields before talking to server (note: same checking will also be done serverside)
                doFormCheck(view);
            }
        });
    }

    public void doFormCheck(View view){
        //TODO: Update serverside code to reflect the same FormCheck, just incase we miss something clientside
        if (regUID.getText().toString().equals("")){
            //update textviews to show error message in hint!!
            regUID.setText("");
            regPASS.setText("");
            regConPASS.setText("");
            regUID.setHint("Username is Required!");
        }else if(regUID.getText().toString().length() < 4) {
            //update textviews to show error message in hint!!
            regUID.setText("");
            regPASS.setText("");
            regConPASS.setText("");
            regUID.setHint("Username not long enough!");
        }else if(regUID.getText().toString().length() > 23) {
            //update textviews to show error message in hint!!
            regUID.setText("");
            regPASS.setText("");
            regConPASS.setText("");
            regUID.setHint("Username too long!");
        }else if(regPASS.getText().toString().equals("")) {
            regPASS.setText("");
            regConPASS.setText("");
            regPASS.setHint("Password is Required!");
        }else if(regConPASS.getText().toString().equals("")){
            regConPASS.setText("");
            regConPASS.setHint("Confirm Password is Required!");
        }else if(!regPASS.getText().toString().equals(regConPASS.getText().toString())) {
            regPASS.setText("");
            regConPASS.setText("");
            regPASS.setHint("Password don't match!");
        }else if(regPASS.getText().toString().length() < 4) {
            regPASS.setText("");
            regConPASS.setText("");
            regPASS.setHint("Password too short!");
        }else if(regPASS.getText().toString().length() > 50){
            regPASS.setText("");
            regConPASS.setText("");
            regPASS.setHint("Password too long!");
        }else if(!isValidEmail(regemail.getText().toString()) && !regemail.getText().toString().equals("")) {
            //update textviews to show error message in hint!!
            regemail.setText("");
            regPASS.setText("");
            regConPASS.setText("");
            regemail.setHint("Email provided is not valid!");
        }else{
            if(regemail.getText().toString().equals("")) {
                //Display dialog warning that they will not be able to recover passwords without an email
                new AlertDialog.Builder(RegistrationActivity.this)
                        .setTitle("Are you sure?")
                        .setMessage("If you don't provide an email you wont be able to reset your password if you forget it (You'll be able to provide it later)!")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                new CreateNewAccount().execute();
                            }})
                        .setNegativeButton(android.R.string.cancel, null).show();
            }else {
                // creating new product in background thread
                new CreateNewAccount().execute();
            }
        }
    }

    /**
     * Background Async Task to Create new account
     * */
    class CreateNewAccount extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(RegistrationActivity.this);
            pDialog.setMessage("Creating Account..");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        /**
         * Creating product
         * */
        protected String doInBackground(String... args) {
            String username = regUID.getText().toString();
            String password = regPASS.getText().toString();
            String password2 = regConPASS.getText().toString();
            String email = regemail.getText().toString();
            String birthday = regbirth.getText().toString();

            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("username", username));
            params.add(new BasicNameValuePair("password", password));
            params.add(new BasicNameValuePair("password_repeat", password2));
            if(!email.equals("")) {
                params.add(new BasicNameValuePair("email", email));
            }
            if(!birthday.equals("")) {
                params.add(new BasicNameValuePair("birthday", birthday));
            }

            // getting JSON Object
            // Note that create product url accepts POST method
            JSONObject json = jsonParser.makeHttpRequest(url_create_account,
                    "POST", params);

            // check log cat fro response
            Log.d("Create Response", json.toString());

            // check for success tag
            try {
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {
                    // successfully created account
                    //Setup our test data
                    String status_msg = "Account Successfully Created!";
                    //Setup the bundle that will be passed
                    Bundle b = new Bundle();
                    b.putString("status",String.valueOf(success));
                    b.putString("status_msg", status_msg);
                    //Setup the Intent that will start the next Activity
                    Intent nextActivity = new Intent(RegistrationActivity.this, MainActivity.class);
                    //Assumes this references this instance of Activity A
                    nextActivity.putExtras(b);

                    RegistrationActivity.this.startActivity(nextActivity);

                    finish();
                } else {
                    // something went wrong with the account registartion
                    //get json message
                    String aJsonString = json.getString("message");
                    //Setup the bundle that will be passed
                    Bundle b = new Bundle();
                    b.putString("status",String.valueOf(success));
                    b.putString("status_msg", aJsonString);
                    //Setup the Intent that will start the next Activity
                    Intent nextActivity = new Intent(RegistrationActivity.this, MainActivity.class);
                    //Assumes this references this instance of Activity A
                    nextActivity.putExtras(b);

                    RegistrationActivity.this.startActivity(nextActivity);

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
        //getMenuInflater().inflate(R.menu.menu_registration, menu);
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

    public static boolean isEmailValid(String email) {
        boolean isValid = false;

        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";

        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        if (matcher.matches()) {
            isValid = true;
        }
        return isValid;
    }
}