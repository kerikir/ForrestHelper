package com.example.myapplicationvoice.voicecall;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplicationvoice.R;
import com.example.myapplicationvoice.homescreen.UsersOnlineActivity;
import com.example.myapplicationvoice.servers.ActionType;
import com.example.myapplicationvoice.servers.UdpManager;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class IncomingCallActivity extends AppCompatActivity implements UdpManager.UdpCallback {
    private TextView callerInfoTextView;
    private TextView callerIpTextView;
    private ImageButton acceptButton;
    private ImageButton rejectButton;

    private String callerName;
    private String callerIp;


    private UdpManager udpManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_call);

        // Получаем данные из интента
        Intent intent = getIntent();
        callerName = intent.getStringExtra("caller_name");
        callerIp = intent.getStringExtra("caller_ip");

        initViews();
        setupCallInfo();

        // Инициализация UDP менеджера
        udpManager = new UdpManager(this, UdpManager.UDP_PORT_INCOMING_CALL);
        udpManager.startListening();
    }

    private void initViews() {
        callerInfoTextView = findViewById(R.id.callerInfoTextView);
        callerIpTextView = findViewById(R.id.callerIpTextView);
        acceptButton = findViewById(R.id.acceptButton);
        rejectButton = findViewById(R.id.rejectButton);

        acceptButton.setOnClickListener(v -> acceptCall());
        rejectButton.setOnClickListener(v -> rejectCall());
    }

    private void setupCallInfo() {
        callerInfoTextView.setText(callerName);
        callerIpTextView.setText(callerIp);
    }

    private void acceptCall() {
        udpManager.sendResponseAsync(ActionType.ACCEPT_CALL.getDescription(), callerIp, UdpManager.UDP_PORT_CALL);

        // Переходим к активности звонка
        Intent intent = new Intent(this, VoiceCallActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.putExtra("user_name", callerName);
        intent.putExtra("ip_address", callerIp);
        intent.putExtra("isStart", true);
        startActivity(intent);

        finish();
    }

    private void rejectCall() {
        // Закрываем активность и отклоняем вызов
        udpManager.sendResponseAsync(ActionType.CANCEL_CALL.getDescription(), callerIp, UdpManager.UDP_PORT_CALL);

        Intent intent = new Intent(this, UsersOnlineActivity.class);
        //intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);

        finish();
    }

    @Override
    public void onObjectReceived(String ip, String json) {

    }

    @Override
    public void onMessageReceived(String ip, String message) {
        if(message.equals(ActionType.CANCEL_CALL.getDescription()))
        {
            endCalling();
        }
    }

    @Override
    public void onResponseSent(boolean success, String ip) {

    }


    public void endCalling()
    {
        runOnUiThread(() ->{
            Intent intent = new Intent(this, UsersOnlineActivity.class);
            //intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);

            finish();
        });
    }
}