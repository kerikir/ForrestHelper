package com.example.myapplicationvoice.voicecall;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.util.Base64;
import android.util.Log;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.pm.PackageManager;

import com.example.myapplicationvoice.encryption.AlgorithmMMB;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public class AudioStream {
    private static final String TAG = "MyAudioStream";
    private static final int SAMPLE_RATE = 8000;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(
            SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);
    private static final int PORT = 16825;

    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final String ipAddress;
    private final Context context;

    private DatagramSocket socket;
    private InetAddress address;
    private Thread recordingThread;
    private Thread playbackThread;

    AlgorithmMMB algorithmMMB;

    private AudioRecord audioRecord;
    private AudioTrack audioTrack;

    public AudioStream(Context context, String ipAddress) throws IOException {
        this.context = context;
        this.ipAddress = ipAddress;

        algorithmMMB = new AlgorithmMMB();

        initializeNetwork();
    }

    private void initializeNetwork() throws SocketException, UnknownHostException {
        socket = new DatagramSocket(PORT);
        try {
            address = InetAddress.getByName(ipAddress);
            Log.i(TAG, "Network initialized for IP: " + ipAddress);
        } catch (IOException e) {
            Log.e(TAG, "Failed to resolve IP address: " + ipAddress, e);
            throw e;
        }
    }

    public void start() {
        if (!hasRecordPermission()) {
            Log.w(TAG, "No record permission, can't start audio stream");
            return;
        }

        if (isRunning.get()) {
            Log.w(TAG, "Audio stream is already running");
            return;
        }

        isRunning.set(true);

        // Поток для записи аудио
        recordingThread = new Thread(() -> {
            audioRecord = null;
            try {
                Log.d(TAG, "Starting recording thread");
                audioRecord = createAudioRecord();
                byte[] bufferRecord = new byte[BUFFER_SIZE];

                while (isRunning.get()) {
                    int bytesRead = audioRecord.read(bufferRecord, 0, bufferRecord.length);
                    if (bytesRead > 0) {
                        sendAudioPacket(bufferRecord, bytesRead);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Recording thread error", e);
            } finally {
                if (audioRecord != null) {
                    audioRecord.release();
                }
                Log.d(TAG, "Recording thread stopped");
            }
        });

        // Поток для воспроизведения аудио
        playbackThread = new Thread(() -> {
            audioTrack = null;
            try {
                Log.d(TAG, "Starting playback thread");
                audioTrack = createAudioTrack();
                byte[] buffer = new byte[BUFFER_SIZE];

                while (isRunning.get()) {
                    receiveAndPlayAudio(audioTrack, buffer);
                }
            } catch (Exception e) {
                Log.e(TAG, "Playback thread error", e);
            } finally {
                if (audioTrack != null) {
                    audioTrack.release();
                }
                Log.d(TAG, "Playback thread stopped");
            }
        });

        recordingThread.start();
        playbackThread.start();
        Log.i(TAG, "Audio stream started");
    }

    private AudioRecord createAudioRecord() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Recording permission not granted");
            return null;
        }

        AudioRecord recorder = new AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                BUFFER_SIZE * 10);

        if (recorder.getState() != AudioRecord.STATE_INITIALIZED) {
            Log.e(TAG, "AudioRecord initialization failed");
            return null;
        }

        recorder.startRecording();
        return recorder;
    }

    private AudioTrack createAudioTrack() {
        AudioTrack player = new AudioTrack(
                AudioManager.STREAM_VOICE_CALL,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AUDIO_FORMAT,
                BUFFER_SIZE * 10,
                AudioTrack.MODE_STREAM);

        if (player.getState() != AudioTrack.STATE_INITIALIZED) {
            Log.e(TAG, "AudioTrack initialization failed");
            return null;
        }

        player.play();
        return player;
    }

    private void sendAudioPacket(byte[] buffer, int length) {
        try {
            byte[] voiceData = encryptedVoiceData(buffer, length);
            DatagramPacket packet = new DatagramPacket(voiceData, voiceData.length, address, PORT);
            socket.send(packet);
            Log.v(TAG, "Sent audio packet, size: " + length);
        } catch (IOException e) {
            Log.e(TAG, "Error sending audio packet", e);
        }
    }

    private void receiveAndPlayAudio(AudioTrack player, byte[] buffer) {
        try {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);
            byte[] decryptionVoice = decryptedVoiceData(packet.getData(), packet.getLength());
            player.write(decryptionVoice, 0, decryptionVoice.length);
            Log.v(TAG, "Received and played audio packet, size: " + packet.getLength());
        } catch (IOException e) {
            if (isRunning.get()) {
                Log.e(TAG, "Error receiving audio packet", e);
            }
        }
    }


    private byte[] encryptedVoiceData(byte[] buffer, int length)
    {
        byte[] voiceByte = Arrays.copyOf(buffer, length);
        byte[] encryptionByte = algorithmMMB.encryptionMessageParallelWithStreamModificate(
                voiceByte, 2);
        int lackByte = algorithmMMB.getLackOfByte();

        byte[] encryptionVoiceData = new byte[encryptionByte.length + 1];
        encryptionVoiceData[0] = (byte)lackByte;
        for (int i = 0; i < encryptionByte.length; i++)
        {
            encryptionVoiceData[i + 1] = encryptionByte[i];
        }
        //byte[] array = Base64.encode(encryptionVoiceData, Base64.DEFAULT);

        return encryptionVoiceData;
    }


    private byte[] decryptedVoiceData(byte[] buffer, int length)
    {
        //byte[] array = Base64.decode(buffer, Base64.DEFAULT);

        byte[] encryptionVoice = Arrays.copyOfRange(buffer, 1, length);
        byte[] decryptionVoice = algorithmMMB.decryptionMessageParallelWithStreamModificate(
                encryptionVoice, buffer[0], 2);

        return decryptionVoice;
    }

    public void stopStreaming() {
        if (!isRunning.get())
        {
            return;
        }
        isRunning.set(false);

        try {

            if (recordingThread != null) {
                recordingThread.interrupt();
            }
            if (playbackThread != null) {
                playbackThread.interrupt();
            }

            if (socket != null && !socket.isClosed()) {
                socket.close();
            }

            if (audioRecord != null) {
                audioRecord.stop();
                audioRecord.release();
                audioRecord = null;
            }
            if (audioTrack != null) {
                audioTrack.stop();
                audioTrack.release();
                audioTrack = null;
            }

            Log.i(TAG, "Audio stream stopped");
        } catch (Exception e) {
            Log.e(TAG, "Error stopping audio stream", e);
        }
    }

    private boolean hasRecordPermission() {
        return ContextCompat.checkSelfPermission(context,
                Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    protected void finalize() throws Throwable {
        stopStreaming();
        super.finalize();
    }
}