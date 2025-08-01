package com.example.myapplicationvoice.voicecall;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.example.myapplicationvoice.R;
import com.example.myapplicationvoice.homescreen.UsersOnlineActivity;
import com.example.myapplicationvoice.servers.ActionType;
import com.example.myapplicationvoice.servers.UdpManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;


public class VoiceCallActivity extends AppCompatActivity implements UdpManager.UdpCallback {
    private static final int RECORD_AUDIO_PERMISSION_CODE = 202;

    private TextView callInfoTextView;
    private TextView ipAddressTextView;
    private TextView statusTextView;
    private FloatingActionButton endCallButton;
    private ImageButton speakerButton;
    private ImageButton muteButton;

    private AudioStream audioStream;
    private boolean isStreaming = false;
    private boolean isMuted = false;
    private boolean isSpeakerOn = false;

    private String ipAddress;
    private String userName;
    private UdpManager udpManager;

    private Handler timerHandler = new Handler();
    private long startTime = 0;
    private Runnable timerRunnable;
    private boolean isStart;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice);

        // Получаем данные из интента
        Intent intent = getIntent();
        userName = intent.getStringExtra("user_name");
        ipAddress = intent.getStringExtra("ip_address");
        isStart = intent.getBooleanExtra("isStart", false);

        initViews();
        setupCallInfo();

        // Инициализация UDP менеджера
        udpManager = new UdpManager(this, UdpManager.UDP_PORT_CALL);
        udpManager.startListening();

        if (isStart)
            checkAndRequestPermissions();
    }


    private void initViews() {
        callInfoTextView = findViewById(R.id.callInfoTextView);
        ipAddressTextView = findViewById(R.id.ipAddressTextView);
        statusTextView = findViewById(R.id.statusTextView);
        endCallButton = findViewById(R.id.endCallButton);
        speakerButton = findViewById(R.id.speakerButton);
        muteButton = findViewById(R.id.muteButton);

        endCallButton.setOnClickListener(v -> endCall());
        speakerButton.setOnClickListener(v -> toggleSpeaker());
        muteButton.setOnClickListener(v -> toggleMute());
    }


    private void setupCallInfo() {
        callInfoTextView.setText(userName);
        ipAddressTextView.setText(ipAddress);
        statusTextView.setText("Ожидание...");
    }


    private void initTimer() {

        timerRunnable = new Runnable() {
            @Override
            public void run() {
                long millis = System.currentTimeMillis() - startTime;
                int seconds = (int) (millis / 1000);
                int minutes = seconds / 60;
                seconds = seconds % 60;

                statusTextView.setText(String.format("%02d:%02d", minutes, seconds));
                timerHandler.postDelayed(this, 1000);
            }
        };
    }



    private void startTimer() {
        startTime = System.currentTimeMillis();
        timerHandler.postDelayed(timerRunnable, 0);
    }

    private void stopTimer() {
        timerHandler.removeCallbacks(timerRunnable);
    }



    private void checkAndRequestPermissions()
    {
        runOnUiThread(() ->
        {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        RECORD_AUDIO_PERMISSION_CODE);
            } else {
                startStreaming();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RECORD_AUDIO_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startStreaming();
            } else {
                Toast.makeText(this, "Разрешение на запись аудио отклонено", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void startStreaming()
    {
        runOnUiThread(() ->
        {
            if (isStreaming) return;

            try {
                audioStream = new AudioStream(this, ipAddress);
                audioStream.start();
                isStreaming = true;

                initTimer();
                startTimer();

            } catch (Exception e) {
                Toast.makeText(this, "Ошибка подключения", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }


    private void toggleSpeaker() {
        isSpeakerOn = !isSpeakerOn;
        speakerButton.setImageResource(isSpeakerOn ?
                R.drawable.ic_speaker_on :
                R.drawable.ic_speaker_off);
//        speakerButton.setBackgroundTintList(ContextCompat.getColorStateList(this,
//                (isSpeakerOn ? R.color.blue : R.color.white)));
        // Реализация переключения громкой связи
    }

    private void toggleMute() {
        isMuted = !isMuted;
        muteButton.setImageResource(isMuted ?
                R.drawable.ic_mic_off :
                R.drawable.ic_mic_on);
//        muteButton.setBackgroundTintList(ContextCompat.getColorStateList(this,
//                (isMuted ? R.color.red : R.color.white)));
        // Реализация отключения микрофона
    }


    private void endCall() {

        runOnUiThread(() ->
        {
            statusTextView.setText("Разговор завершен...");

            if (isStreaming)
                udpManager.sendResponseAsync(ActionType.CANCEL_CALL.getDescription(), ipAddress, UdpManager.UDP_PORT_CALL);
            else
                udpManager.sendResponseAsync(ActionType.CANCEL_CALL.getDescription(), ipAddress, UdpManager.UDP_PORT_INCOMING_CALL);

            stopStreaming();
            finish();


            Intent intent = new Intent(this, UsersOnlineActivity.class);
            //intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        });
    }


    private void stopStreaming() {
        if (!isStreaming) return;

        if (audioStream != null) {
            audioStream.stopStreaming();
            audioStream = null;
        }
        isStreaming = false;
    }

    @Override
    protected void onDestroy() {
        stopStreaming();
        stopTimer();

        stopStreaming();
        if (udpManager != null) {
            udpManager.stop();
            udpManager = null;
        }

        timerHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }


    // region UdpCallback реализация
    @Override
    public void onMessageReceived(String ip, String message) {
        addToLog("Получено от " + ip + ": " + message);

        if(message.equals(ActionType.CANCEL_CALL.getDescription()))
        {
            endCall();
        }
        else if(message.equals(ActionType.ACCEPT_CALL.getDescription()))
        {
            checkAndRequestPermissions();
        }
    }


    @Override
    public void onObjectReceived(String ip, String json) {

    }


    @Override
    public void onResponseSent(boolean success, String ip)
    {
        addToLog("Ответ " + (success ? "доставлен" : "не доставлен") + " к " + ip);
    }
    // endregion

    private void addToLog(String text)
    {
        runOnUiThread(() ->
        {
            Log.d("UDP_Voice", text);
        });
    }
}