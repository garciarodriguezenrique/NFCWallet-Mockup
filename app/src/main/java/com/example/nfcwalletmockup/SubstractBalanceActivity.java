package com.example.nfcwalletmockup;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import static com.example.nfcwalletmockup.NFCUtils.decrementBalance;
import static com.example.nfcwalletmockup.NFCUtils.readBalance;
import static com.example.nfcwalletmockup.NFCUtils.writeBalance;

public class SubstractBalanceActivity extends AppCompatActivity {

    private TextView resultTextView;
    private EditText subsAmount;
    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v("test","Initialize activity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subs);

        subsAmount = (EditText) findViewById(R.id.editTextAmountSubs);
        resultTextView = (TextView) findViewById(R.id.textViewResultSubs);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (nfcAdapter == null){
            Toast.makeText(this,"This device does not support NFC",
                    Toast.LENGTH_SHORT).show();
            finish();
        }

        pendingIntent = PendingIntent.getActivity(this,0, new Intent(this,
                this.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),0);
    }

    //Enable FD on resuming to gain priority over other apps
    @Override
    protected void onResume() {
        super.onResume();
        assert nfcAdapter != null;
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
    }

    //Stop listening for NFC tags when the user browses away from the app
    @Override
    protected void onPause() {
        super.onPause();
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(this);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        Log.v("test","New intent received");
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            Tag tag = (Tag) intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            assert tag != null;
            try{
                MifareClassic mifareTag = MifareClassic.get(tag);
                Log.v("test","Got a Mifare Tag");
                subsBalanceDefaultMifareClassicTagValueBlock(mifareTag);
            } catch (Exception e) {
                Log.v("test","Unsupported tag");
            }
        }
    }

    private void subsBalanceDefaultMifareClassicTagValueBlock(MifareClassic mifareTag){
        String amount = subsAmount.getText().toString();
        StringBuilder sb = new StringBuilder();

        if (!amount.matches("")){
            int amountInt = Integer.parseInt(amount);


            String balance = readBalance(mifareTag);

            if (balance != null) {

                sb.append("Balance previous to the operation was: " + balance);
                sb.append("\n");
                sb.append("Amount to be substracted is: " + amount);
                sb.append("\n");

                int balanceInt = Integer.parseInt(balance);
                int totalAmount = balanceInt - amountInt;
                sb.append("Expected final amount after the operation is: " + totalAmount);
                sb.append("\n");


                Log.v("test", "Total final amount is " + totalAmount + ".Substracting Balance...");
                decrementBalance(mifareTag, String.valueOf(amount));
                String resultBalance = readBalance(mifareTag);
                if (Integer.parseInt(resultBalance) == totalAmount) {
                    sb.append("Operation successful. Current balance is: " + resultBalance);
                } else {
                    sb.append("Something went wrong. Current balance is: " + resultBalance);
                }
                resultTextView.setText(sb.toString());
                Log.v("test", "Balance added, current value is: " + resultBalance);
            } else {
                Toast.makeText(this,"Something went wrong while reading the current balance.",
                        Toast.LENGTH_SHORT).show();
            }

        } else {
            Toast.makeText(this,"Specify an amount to recharge",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void subsBalanceDeafultMifareClassicTag(MifareClassic mifareTag){
        String amount = subsAmount.getText().toString();
        StringBuilder sb = new StringBuilder();

        if (!amount.matches("")){
            int amountInt = Integer.parseInt(amount);
            if (amountInt<999){

                String balance = readBalance(mifareTag);

                sb.append("Balance previous to the operation was: "+balance);
                sb.append("\n");
                sb.append("Amount to be recharged is: "+amount);
                sb.append("\n");

                int balanceInt = Integer.parseInt(balance);
                int totalAmount = balanceInt - amountInt;
                sb.append("Expected final amount after the operation is: "+totalAmount);
                sb.append("\n");

                if (totalAmount>balanceInt){
                    Log.v("test","Cannot substract "+amountInt+" to current balance of "+balanceInt+".");
                    Toast.makeText(this,"Cannot substract "+amountInt+" to current balance of "+balanceInt+".",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Log.v("test","Total final amount is "+totalAmount+".Substracting Balance...");
                    writeBalance(mifareTag, String.valueOf(totalAmount));
                    String resultBalance = readBalance(mifareTag);
                    if (Integer.parseInt(resultBalance)==totalAmount){
                        sb.append("Operation successful. Current balance is: "+resultBalance);
                    } else {
                        sb.append("Something went wrong. Current balance is: "+resultBalance);
                    }
                    resultTextView.setText(sb.toString());
                    Log.v("test","Balance added, current value is: "+resultBalance);
                }

            } else {
                Toast.makeText(this,"Specified amount must be inferior to 999",
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this,"Specify an amount to recharge",
                    Toast.LENGTH_SHORT).show();
        }
    }
}
