package com.example.nfcwalletmockup;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button readButton;
    Button addBalance;
    Button subsBalance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        readButton = (Button) findViewById(R.id.button_readTag);
        readButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v){
                Intent i = new Intent(getApplicationContext(), ReadTagActivity.class);
                startActivity(i);
            }
        });

        addBalance = (Button) findViewById(R.id.button_addBalance);
        addBalance.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent i = new Intent(getApplicationContext(), AddBalanceActivity.class);
                startActivity(i);
            }
        });

        subsBalance = (Button) findViewById(R.id.button_subsBalance);
        subsBalance.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent i = new Intent(getApplicationContext(), SubstractBalanceActivity.class);
                startActivity(i);
            }
        });
    }
}
