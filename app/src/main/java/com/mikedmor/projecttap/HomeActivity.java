package com.mikedmor.projecttap;

import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.view.PagerAdapter;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;


public class HomeActivity extends AppCompatActivity {


    SectionsPagerAdapter mSectionsPagerAdapter;

    ViewPager mViewPager;

    // Progress Dialog
    private ProgressDialog pDialog;

    JSONParser jsonParser = new JSONParser();
    // User JSONArray

    // JSON Node names
    private static final String TAG_SUCCESS = "success";

    ServerCheck initial = new ServerCheck();

    String user_PID;
    String user_data;
    static String profile_name;

    //variables for pages
    boolean is_conpage = true;
    boolean is_chatpage = false;
    boolean is_profilepage = false;

    MenuItem mManualAddFriend;



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.connection_menu, menu);
        // Get dynamic menu item
        mManualAddFriend = menu.findItem(R.id.action_Manual_Friend);
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initial.execute();

        if(getIntent().hasExtra("user_data")) {

            user_PID = getIntent().getExtras().getString("user_data").substring(1,getIntent().getExtras().getString("user_data").length()-1);
            Map<String,String> map = new HashMap<>();
            try {
                JSONObject jsonObject = new JSONObject(user_PID);
                Iterator keys = jsonObject.keys();
                while (keys.hasNext()) {
                    String key = (String) keys.next();
                    map.put(key, jsonObject.getString(key));
                }
                System.out.println(map);// this map will contain your json stuff
            } catch (JSONException e) {
                e.printStackTrace();
            }

            profile_name = map.get("username");

            //USE HTTP to get stored account data to be used in HomeActivity


            //if(status_msg_value.equals("1")){
            //successfull (change color of status to green
            //status_message.setTextColor(Color.parseColor("#4CAF50"));
            //}else{
            //unsuccessfull (change color of status to red
            //status_message.setTextColor(Color.parseColor("#F44336"));
            //}
            //String status_msg_string = getIntent().getExtras().getString("status_msg");
            //status_message.setText(status_msg_string);
        }

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mViewPager.setOnPageChangeListener(mSectionsPagerAdapter);
        setTitle(R.string.title_section1);

        RelativeLayout ll = (RelativeLayout) mViewPager.findViewById(R.id.ChatID);
        ll.setVisibility(View.INVISIBLE);



    }

    public void setCurrentItem (int item, boolean smoothScroll) {
        mViewPager.setCurrentItem(item, smoothScroll);
    }


    class ServerCheck extends AsyncTask<String, String, String> {

        Boolean ServerReachable = false;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(HomeActivity.this);
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
                new AlertDialog.Builder(HomeActivity.this)
                        .setTitle("Not Online!")
                        .setMessage("We're sorry, but it doesn't seem like your connected to the internet. Please connect to the internet and try again.")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                Intent nextActivity = new Intent(HomeActivity.this, MainActivity.class);
                                HomeActivity.this.startActivity(nextActivity);
                            }
                        }).setCancelable(false).show();
            }
        }
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter implements ViewPager.OnPageChangeListener {

        private Fragment mCurrentFragment;

        public Fragment ConFrag;
        public Fragment PFrag;
        public Fragment ChatFrag;

        public Fragment getCurrentFragment() {
            return mCurrentFragment;
        }

        @Override
        public void onPageSelected(int position) {
            setTitle(getPageTitle(position));
            switch(position){
                case 0:
                    is_conpage=true;
                    is_chatpage=false;
                    is_profilepage=false;
                    break;
                case 1:
                    is_conpage=false;
                    is_chatpage=true;
                    is_profilepage=false;
                    break;
                case 2:
                    is_conpage=false;
                    is_chatpage=false;
                    is_profilepage=true;
                    break;
            }
            //Log.d("is_conpage: ", Boolean.toString(is_conpage));
            //Log.d("is_chatpage: ", Boolean.toString(is_chatpage));
            //Log.d("is_profilepage: ", Boolean.toString(is_profilepage));
            mManualAddFriend.setVisible(is_conpage);

        }

        @Override
        public void onPageScrolled(int a,float b, int c){

        }

        @Override
        public void onPageScrollStateChanged(int a){

        }

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }


        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch (position) {
                case 0:
                    ConFrag = connectionsFragment.newInstance(position + 1);
                    return ConFrag;
                case 1:
                    ChatFrag = chatFragment.newInstance(position + 1);
                    return ChatFrag;
                case 2:
                    PFrag = profileFragment.newInstance(position + 1);
                    return PFrag;

            }
            return null;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.title_section1);
                case 1:
                    return getString(R.string.title_section2);
                case 2:
                    return getString(R.string.title_section3);
            }
            return null;
        }




        //this is called when notifyDataSetChanged() is called
        @Override
        public int getItemPosition(Object object) {
            // refresh all fragments when data set changed
            return PagerAdapter.POSITION_NONE;
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            if (getCurrentFragment() != object) {
                mCurrentFragment = ((Fragment) object);
            }
            super.setPrimaryItem(container, position, object);

        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class connectionsFragment extends Fragment {
        View rootView;
        ListView lv;
        /**
         *
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";
        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */

        public static connectionsFragment newInstance(int sectionNumber) {
            connectionsFragment fragment = new connectionsFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public connectionsFragment() {
            //super();
        }



        public boolean setuplist(){
            lv = (ListView)rootView.findViewById(R.id.conList);
            String[] cowid = {"Custom Name 1","Connection Username 1",
                    "Custom Name 2","Connection Username 2",
                    "Custom Name 3","Connection Username 3",
                    "Custom Name 4","Connection Username 4",
                    "Custom Name 5","Connection Username 5",
                    "Custom Name 6","Connection Username 6",
                    "Custom Name 7","Connection Username 7",
                    "Custom Name 8","Connection Username 8"};


            ArrayList<HashMap<String, String>> items = new ArrayList<>();
            HashMap<String, String> listItem;
            for(int i=0;i<cowid.length;i=i+2){
                listItem = new HashMap<>();
                listItem.put("item", cowid[i]);
                listItem.put("subitem", cowid[i+1]);
                items.add(listItem);
            }
            SimpleAdapter adapter = new SimpleAdapter(getActivity(), items, R.layout.con_list,
                                                        new String[]{"item", "subitem"},
                                                        new int[]{R.id.Itemname, R.id.ItemSubname});
            lv.setAdapter(adapter);
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                    // TODO Auto-generated method stub
                    //int itm=arg0.getItemAtPosition(arg2);
                    ((HomeActivity)getActivity()).setCurrentItem (1, true);
                }
            });

            return true;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            rootView = inflater.inflate(R.layout.connections, container, false);
            setuplist();
            return rootView;
        }
    }



    /**
     * A placeholder fragment containing a simple view.
     */
    public static class profileFragment extends Fragment {

        public TextView ProfileName;

        public TextView getProfileName(){
            return ProfileName;
        }
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";
        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static profileFragment newInstance(int sectionNumber) {
            profileFragment fragment = new profileFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public profileFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            if (container == null) {
                return null;
            }

            RelativeLayout ll = (RelativeLayout)inflater.inflate(R.layout.profile, container, false);
            ProfileName = (TextView) ll.findViewById(R.id.ProfileName);

            ProfileName.setText(profile_name+"'s Profile");


            return ll;
            //return inflater.inflate(R.layout.profile, container, false);
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class chatFragment extends Fragment {
        //static Fragment storedFrag;
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";
        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static chatFragment newInstance(int sectionNumber) {
            chatFragment fragment = new chatFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            //storedFrag = fragment;
            return fragment;
        }

        public chatFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.chat, container, false);
        }
    }

    /**
     * Background Async Task to get user account data
     * */
    class AccountLogin extends AsyncTask<String, String, String> {
        // url to create new product
        private String url_get_account = "http://www.mikedmor.com/ProjectTap/get_account_details.php";
        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(HomeActivity.this);
            pDialog.setMessage("Getting Account...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        /**
         * Creating product
         * */
        protected String doInBackground(String... args) {
            String upid = user_PID;

            // Building Parameters
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("upid", upid));

            // getting JSON Object
            // Note that create product url accepts POST method
            JSONObject json = jsonParser.makeHttpRequest(url_get_account,
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
                    user_data = json.getString("account");
                    //update ui to show user data


                } else {
                    // something went wrong with the account login
                    //get json message
                    String aJsonString = json.getString("message");
                    //Update the status_msg to reflect the error from the server
                    Bundle b = new Bundle();
                    b.putString("status",String.valueOf(success));
                    b.putString("status_msg", aJsonString);
                    //Setup the Intent that will start the next Activity
                    Intent nextActivity = new Intent(HomeActivity.this, MainActivity.class);
                    //Assumes this references this instance of Activity A
                    nextActivity.putExtras(b);

                    HomeActivity.this.startActivity(nextActivity);

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

}

