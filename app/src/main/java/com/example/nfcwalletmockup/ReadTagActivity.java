package com.example.nfcwalletmockup;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

import static com.example.nfcwalletmockup.NFCUtils.readBalance;
import static com.example.nfcwalletmockup.NFCUtils.toDec;
import static com.example.nfcwalletmockup.NFCUtils.toHex;
import static com.example.nfcwalletmockup.NFCUtils.toReversedDec;
import static com.example.nfcwalletmockup.NFCUtils.toReversedHex;
import static com.example.nfcwalletmockup.NFCUtils.detectChineseTag;

public class ReadTagActivity extends AppCompatActivity {

    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private TextView mainTextView;
    private TextView hdumpTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read);

        mainTextView = (TextView) findViewById(R.id.read_textView);
        hdumpTextView = (TextView) findViewById(R.id.hexdump_textView);
        hdumpTextView.setMovementMethod(new ScrollingMovementMethod());
        mainTextView.setMovementMethod(new ScrollingMovementMethod());
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
        mainTextView.setText("");
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            Tag tag = (Tag) intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            assert tag != null;
            byte[] payload = detectTagData(tag).getBytes();
        }
    }



    private String readDeafultMifareClassicTag(MifareClassic mifareTag){
        StringBuilder sb = new StringBuilder();

        if(detectChineseTag(mifareTag)){
            Toast.makeText(this,"Warning! This is a cloned tag!",
                    Toast.LENGTH_SHORT).show();
        }

        try {
            mifareTag.connect();

            sb.append("Tag content: ").append("\n");

            byte[] data;

            //Try to authenticate with default keys
            boolean authDefault = false;

            int sectorCount = mifareTag.getSectorCount();
            int blockCount = 0;
            int currentBlockIndex = 0;

            for(int i = 0; i < sectorCount; i++){
                authDefault = mifareTag.authenticateSectorWithKeyA(i, MifareClassic.KEY_DEFAULT);
                if (authDefault){
                    Log.v("test","Authentication successfull on sector "+i);
                    sb.append("Sector "+i+": ").append("\n\n");
                    blockCount = mifareTag.getBlockCountInSector(i);
                    currentBlockIndex = mifareTag.sectorToBlock(i);

                    for(int j=0; j < blockCount; j++){
                        data = mifareTag.readBlock(currentBlockIndex);
                        Log.v("test", toReversedHex(data));
                        sb.append(toReversedHex(data)).append("\n");
                        if (j == 3){ sb.append("\n");}
                        currentBlockIndex++;
                    }

                } else {
                    Log.v("test","Authentication failed on sector "+i+"! Likely due to non-default keys being used for this sector");
                }
            }

            mifareTag.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return sb.toString();
    }

    private String detectTagData(Tag tag){
        StringBuilder sb = new StringBuilder();
        StringBuilder hdump_sb = new StringBuilder();
        String tagContent;
        byte[] id = tag.getId();

        sb.append("ID (hex): ").append(toHex(id)).append('\n');
        sb.append("ID (reversed hex): ").append(toReversedHex(id)).append('\n');
        sb.append("ID (dec): ").append(toDec(id)).append('\n');
        sb.append("ID (reversed dec): ").append(toReversedDec(id)).append('\n');

        String prefix = "android.nfc.tech.";
        sb.append("Technologies: ");
        for (String tech : tag.getTechList()) {
            sb.append(tech.substring(prefix.length()));
            sb.append(", ");
        }

        sb.delete(sb.length() - 2, sb.length());

        for (String tech : tag.getTechList()) {
            if (tech.equals(MifareClassic.class.getName())) {
                sb.append('\n');
                String type = "Unknown";

                try {
                    MifareClassic mifareTag = MifareClassic.get(tag);

                    switch (mifareTag.getType()) {
                        case MifareClassic.TYPE_CLASSIC:
                            type = "Classic";
                            break;
                        case MifareClassic.TYPE_PLUS:
                            type = "Plus";
                            break;
                        case MifareClassic.TYPE_PRO:
                            type = "Pro";
                            break;
                    }
                    sb.append("Mifare Classic type: ");
                    sb.append(type);
                    sb.append('\n');

                    sb.append("Mifare size: ");
                    sb.append(mifareTag.getSize() + " bytes");
                    sb.append('\n');

                    sb.append("Mifare sectors: ");
                    sb.append(mifareTag.getSectorCount());
                    sb.append('\n');

                    sb.append("Mifare blocks: ");
                    sb.append(mifareTag.getBlockCount());
                    sb.append('\n');

                    sb.append("Wallet balance:");
                    sb.append(readBalance(mifareTag));
                    sb.append("\n");

                    //Read Tag Content (currently assumes target tag is a Mifare Classic with chip delivery configuration)
                    tagContent = readDeafultMifareClassicTag(mifareTag);
                    hdump_sb.append(tagContent);
                } catch (Exception e) {
                    sb.append("Mifare classic error: " + e.getMessage());
                }
            }
        }
        Log.v("test",sb.toString());
        mainTextView.setText(sb.toString());
        hdumpTextView.setText(hdump_sb);
        return sb.toString();
    }

}
