package com.sixdegrees.demoapp.activities;

import android.app.Activity;
import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.matesnetwork.callverification.Cognalys;
import com.sixdegrees.demoapp.R;
import com.matesnetwork.interfaces.VerificationListner;
import com.sixdegrees.demoapp.util.PrefUtils;

/**
 * Created by Macs on 15/02/2015.
 */
public class VerifyNumberActivity extends Activity {
    private TextView timertv = null;
    private EditText phoneNumbTv = null;
    private CountDownTimer countDownTimer;
    private RelativeLayout timerLayout;
    private static String APP_ID="";
    private static String ACCESS_TOKEN="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_verify_number);
        phoneNumbTv = (EditText) findViewById(R.id.ph_et);
        timertv = (TextView) findViewById(R.id.timer_tv);
        TextView country_code_tv = (TextView) findViewById(R.id.country_code_tv);
        country_code_tv.setText(Cognalys.getCountryCode(getApplicationContext()));
        timerLayout = (RelativeLayout) findViewById(R.id.timer_rl);

        findViewById(R.id.verifybutton).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if (!TextUtils.isEmpty(phoneNumbTv.getText().toString())) {
                            verify();
                        }else{
                            Toast.makeText(getApplicationContext(), "Please enter your phone number to verify", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }

    private void verify() {
        timerLayout.setVisibility(View.VISIBLE);
        countDownTimer = new CountDownTimer(60000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                timertv.setText("" + millisUntilFinished / 1000);
            }

            @Override
            public void onFinish() {
                timerLayout.setVisibility(View.GONE);
            }

        };
        countDownTimer.start();

        Cognalys.verifyMobileNumber(VerifyNumberActivity.this, ACCESS_TOKEN, APP_ID, phoneNumbTv.getText().toString(), new VerificationListner() {

                    @Override
                    public void onVerificationStarted() {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void onVerificationSuccess() {
                        countDownTimer.cancel();
                        timerLayout.setVisibility(View.GONE);
                        PrefUtils.setPhoneVerified(VerifyNumberActivity.this, phoneNumbTv.getText().toString());
                        showAlert("Your number has been verified\n\nThanks!!",true);

                    }

                    @Override
                    public void onVerificationFailed(ArrayList<String> errorList) {
                        countDownTimer.cancel();
                        timerLayout.setVisibility(View.GONE);
                        for (String error : errorList) {
                            Log.e("abx", "error:"+error);
                        }
                        showAlert("Something went wrong.\n please try again",false);
                    }
                });


    }
    private void showAlert(String message,boolean status){
        final Dialog dialog = new Dialog(VerifyNumberActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog);


        ImageView mImageView = (ImageView) dialog.findViewById(R.id.verify_im);
        TextView messageTv=(TextView) dialog.findViewById(R.id.messagetv);
        if (status) {
            mImageView.setImageResource(R.drawable.blue_tick);
        }else{
            mImageView.setImageResource(R.drawable.wrong);
        }

        messageTv.setText(message);
        dialog.show();
    }

}
