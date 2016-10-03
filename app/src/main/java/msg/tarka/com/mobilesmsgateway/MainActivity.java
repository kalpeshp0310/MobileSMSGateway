package msg.tarka.com.mobilesmsgateway;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;
    private static final String TAG = MainActivity.class.getSimpleName();
    public static final String SMS_SENT_ACTION = "msg.tarka.com.mobilesmsgateway.SMS_SENT_ACTION";
    public static final String SMS_DELIVERED_ACTION = "msg.tarka.com.mobilesmsgateway.SMS_DELIVERED_ACTION";

    EditText mobileEditText;
    TextInputLayout mobileTextInputLayout;

    EditText messageEditText;
    TextInputLayout messageTextInputLayout;

    EditText countEditText;
    TextInputLayout countTextInputLayout;

    TextView statusTextview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int hasSMSPermission = ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.SEND_SMS);
        if (hasSMSPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.SEND_SMS}, REQUEST_CODE_ASK_PERMISSIONS);
        }

        mobileTextInputLayout = (TextInputLayout) findViewById(R.id.mobileTextInputLayout);
        messageTextInputLayout = (TextInputLayout) findViewById(R.id.messageTextInputLayout);
        countTextInputLayout = (TextInputLayout) findViewById(R.id.countTextInputLayout);

        mobileEditText = (EditText) findViewById(R.id.mobileEditText);
        messageEditText = (EditText) findViewById(R.id.messageEditText);
        countEditText = (EditText) findViewById(R.id.countEditText);

        statusTextview = (TextView) findViewById(R.id.statusTextview);

        //SMS Sent Receiver
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String message = null;
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        message = "Message sent successfully";
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        message = "Error";
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        message = "Error: No service";
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        message = "Error: Null PDU";
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        message = "Error: Radio off";
                        break;
                }

                if (message != null) {
                    Log.i(TAG, message);
                    final String result = message;
                    runOnUiThread(new Runnable() {
                        public void run() {
                            statusTextview.setText("Status: " + result);
                        }
                    });
                }

            }
        }, new IntentFilter(SMS_SENT_ACTION));


        // SMS Delivery Receiver
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (getResultCode() == Activity.RESULT_OK) {
                    Log.i(TAG, "SMS Delivered");
                }
            }
        }, new IntentFilter(SMS_DELIVERED_ACTION));


        findViewById(R.id.sendButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = mobileEditText.getText().toString().trim();
                String message = messageEditText.getText().toString().trim();
                String count = countEditText.getText().toString().trim();

                if (TextUtils.isEmpty(phoneNumber)) {
                    mobileTextInputLayout.setError("Required field");
                    return;
                } else if (phoneNumber.length() < 10) {
                    mobileTextInputLayout.setError("Enter valid mobile number");
                    return;
                } else
                    mobileTextInputLayout.setError(null);

                if (TextUtils.isEmpty(message)) {
                    messageTextInputLayout.setError("Required field");
                    return;
                } else
                    messageTextInputLayout.setError(null);

                if (TextUtils.isEmpty(count)) {
                    countTextInputLayout.setError("Required field");
                    return;
                } else
                    countTextInputLayout.setError(null);

                reset();
                // send SMS
                sendSms(phoneNumber, message, Integer.parseInt(count));
            }
        });

    }

    private void sendSms(String phoneNumber, String message, int counter) {
        SmsManager sms = SmsManager.getDefault();
        for (int i = 0; i <= counter; i++) {
            sms.sendTextMessage(phoneNumber, null, message, PendingIntent.getBroadcast(
                    this, 0, new Intent(SMS_SENT_ACTION), 0), PendingIntent.getBroadcast(this, 0, new Intent(SMS_DELIVERED_ACTION), 0));
        }

        Toast.makeText(this, "Message added to Queue", Toast.LENGTH_LONG).show();
    }

    private void reset() {
        mobileEditText.setText(null);
        messageEditText.setText(null);
        countEditText.setText(null);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode != REQUEST_CODE_ASK_PERMISSIONS) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }
        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
            // we don't have permission,
            finish();
        }
    }

}
