package com.example.myapplicationvoice;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.BlendMode;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity
{

    boolean isRunning;
    String address;
    ReceiverAudioData receiverAudioData;
    SenderAudioData senderAudioData;
    MyLogger logger;

    EditText edtxIpAddress;
    Button btnConnect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        isRunning = false;
        logger = new MyLogger();
        edtxIpAddress = (EditText) findViewById(R.id.ipEditText);
        btnConnect = (Button) findViewById(R.id.connectButton);

        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if (isRunning)
                {
                    logger.verbose("Debug","User disconnect: "+ edtxIpAddress.getText().toString());

                    btnConnect.setText(getResources().getText(R.string.connect_button));
                    btnConnect.setBackgroundTintList(getColorStateList(R.color.green));
                    edtxIpAddress.setEnabled(true);

                    receiverAudioData.setDisconnectedCall();
                    senderAudioData.setDisconnectedCall();

                    isRunning = false;
                }
                else
                {
                    address = edtxIpAddress.getText().toString();
                    logger.verbose("Debug","User connect: "+ address);

                    btnConnect.setText(getResources().getText(R.string.disconnect_button));
                    btnConnect.setBackgroundTintList(getColorStateList(R.color.red));
                    edtxIpAddress.setEnabled(false);


                    receiverAudioData = new ReceiverAudioData(logger);
                    senderAudioData = new SenderAudioData(address, logger, getApplicationContext());
                    receiverAudioData.start();
                    senderAudioData.start();

                    isRunning = true;
                }
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            case 0:
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                return true;
            case 1:
                Intent intentSecond = new Intent(this, SecondActivity.class);
                intentSecond.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intentSecond);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}