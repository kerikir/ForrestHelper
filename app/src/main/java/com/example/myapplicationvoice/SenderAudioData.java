package com.example.myapplicationvoice;


import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;

import androidx.core.app.ActivityCompat;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class SenderAudioData extends Thread
{
    private volatile boolean isDisconnected;
    private String ipAddress;
    private int port;
    private int sampleRate;
    private MyLogger logger;

    private final int BUFF_COUNT = 20;
    private AudioRecord audioRecord;

    Context context;


    public SenderAudioData(String ipAddress, MyLogger logger, Context context)
    {
        this.ipAddress = ipAddress;
        isDisconnected = false;
        port = 6541;
        sampleRate = 8000;

        this.logger = logger;
        this.context = context;
    }


    public void setDisconnectedCall()
    {
        isDisconnected = true;
    }


    public void run()
    {
        try
        {
            int buffSize = AudioRecord.getMinBufferSize(sampleRate,
                    AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT);

            //int buffSize = 800;
            InetAddress ipAddr = InetAddress.getByName(ipAddress);

            DatagramSocket datagramSocket = new DatagramSocket();

            if(buffSize == AudioRecord.ERROR)
            {
                logger.error("Error","getMinBufferSize() returned ERROR");
                return;
            }

            if(buffSize == AudioRecord.ERROR_BAD_VALUE)
            {
                logger.error("Error","getMinBufferSize() returned ERROR_BAD_VALUE");
                return;
            }


            byte[][] buffers = new byte[BUFF_COUNT][buffSize >> 1];
            byte[] buffer = new byte[buffSize >> 1];

            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
            {
                logger.error("Error","Not permission mic");
                return;
            }
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT,
                    buffSize * 10);

            if(audioRecord.getState() != AudioRecord.STATE_INITIALIZED)
            {
                System.err.println("getState() != STATE_INITIALIZED");
                return;
            }

            try
            {
                audioRecord.startRecording();
            }
            catch(IllegalStateException e)
            {
                logger.error("Error", e.getMessage());
                return;
            }

            logger.verbose("Debug", "Start recording...");
            int count = 0;
            DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length, ipAddr, port);

            while(!isDisconnected)
            {
                int samplesRead = audioRecord.read(buffers[count], 0, buffers[count].length);

                if(samplesRead == AudioRecord.ERROR_INVALID_OPERATION)
                {
                    logger.error("Error","read() returned ERROR_INVALID_OPERATION");
                    return;
                }

                if(samplesRead == AudioRecord.ERROR_BAD_VALUE)
                {
                    logger.error("Error","read() returned ERROR_BAD_VALUE");
                    return;
                }

                for (int i = 0; i < buffers[count].length; i++)
                {
                    buffer[i] = buffers[count][i];
                }

                datagramSocket.send(datagramPacket);
                logger.verbose("Debug","Send "+ datagramPacket.getData().length + " bytes.");

                count = (count + 1) % BUFF_COUNT;
            }

            try
            {
                audioRecord.stop();
                logger.verbose("Debug", "Stop recording...");
            }
            catch(IllegalStateException e)
            {
                logger.error("Error", e.getMessage());
                return;
            }
            finally
            {
                // освобождаем ресурсы
                audioRecord.release();
                datagramSocket.close();
                audioRecord = null;
            }
        }
        catch (IllegalStateException exception)
        {
            logger.error("Error", exception.getMessage());
        }
        catch (Exception e)
        {
            logger.error("Error", e.getMessage() );
        }
    }
}
