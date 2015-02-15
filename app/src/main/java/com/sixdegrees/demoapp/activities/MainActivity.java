package com.sixdegrees.demoapp.activities;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Contacts;
import android.provider.ContactsContract;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.contacts.common.util.ViewUtil;
import com.sixdegrees.demoapp.R;
import com.sixdegrees.demoapp.util.PrefUtils;
import com.thrivecom.ringcaptcha.RingcaptchaApplication;
import com.thrivecom.ringcaptcha.RingcaptchaApplicationHandler;
import com.thrivecom.ringcaptcha.RingcaptchaVerification;


public class MainActivity extends ActionBarActivity {

    private static final String API_KEY = "lufo4ofa3umy6o2e9ojo";
    private static final String API_SECRET = "4icu2e5o7ojo2ibepi4y";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PrefUtils.init(this);

        // Check if the EULA has been accepted; if not, show it.
        if (!PrefUtils.isTosAccepted(this)) {
            Intent intent = new Intent(this, WelcomeActivity.class);
            startActivity(intent);
            finish();
        }

        if (PrefUtils.isPhoneVerified(this).equals("")){
            RingcaptchaApplication.verifyPhoneNumber(getApplicationContext(), API_KEY, API_SECRET, new RingcaptchaApplicationHandler() {
                private static final String TAG = "Ringcaptcha verification";
                @Override
                public void onSuccess(RingcaptchaVerification rcObj) {
                    Log.i(TAG, "success");
                    String pn = rcObj.getPhoneNumber();
                    PrefUtils.setPhoneVerified(getParent(), pn);

                }

                @Override
                public void onCancel() {
                    Log.i(TAG, "cancel");
                    finish();
                }
            });
            finish();
        }

        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }


    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        private static final String TAG = PlaceholderFragment.class.getSimpleName();

        public static final int PICK_CONTACT = 1;
        private ImageButton btnContacts;
        private TextView txtContacts1;
        private TextView txtContacts2;

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            final View floatingActionButtonContainer = rootView.findViewById(R.id.floating_action_button_container);
            ViewUtil.setupFloatingActionButton(floatingActionButtonContainer, getResources());
            btnContacts = (ImageButton) rootView.findViewById(R.id.floating_action_button);
            txtContacts1 = (TextView) rootView.findViewById(R.id.txt_contacts_name);
            txtContacts2 = (TextView) rootView.findViewById(R.id.txt_contacts_number);
            btnContacts.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                    startActivityForResult(intent, PICK_CONTACT);
                }
            });

            return rootView;
        }

        @Override
        public void onActivityResult(int reqCode, int resultCode, Intent data) {
            super.onActivityResult(reqCode, resultCode, data);
            String contactID = null;
            switch (reqCode) {
                case (PICK_CONTACT):
                    if (resultCode == Activity.RESULT_OK) {
                        Uri uri = data.getData();
                        Cursor cursorID = getActivity().getContentResolver().query(uri,
                                new String[]{ContactsContract.Contacts._ID},
                                null, null, null);

                        if (cursorID.moveToFirst()) {

                            contactID = cursorID.getString(cursorID.getColumnIndex(ContactsContract.Contacts._ID));
                        }
                        cursorID.close();
                        Log.d(TAG, "Contact ID: " + contactID);
                        // Using the contact ID now we will get contact phone number
                        Cursor c = getActivity().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER},
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? AND " +
                                        ContactsContract.CommonDataKinds.Phone.TYPE + " = " +
                                        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE,
                                new String[]{contactID},
                                null);

                        if (c.moveToFirst()) {
                            String name = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                            String number = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            txtContacts1.setText(name + " : " + number);
                        }
                        c.close();
                    }
                    break;
            }
        }
    }
}
