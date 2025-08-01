package com.example.myapplicationvoice;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ReceiverAudioData
{
    private volatile boolean isDisconnected;
    private int port;
    private int sampleRate;
    private MyLogger logger;
    private BlockingQueue<byte[]> audioDataQueue;


    public ReceiverAudioData( MyLogger logger)
    {

        isDisconnected = false;
        port = 6541;
        sampleRate = 8000;

        this.logger = logger;

        this.audioDataQueue = new LinkedBlockingQueue<>();
    }


    public void setDisconnectedCall()
    {
        isDisconnected = true;
    }


    public void start()
    {
        // Запуск потока для получения данных
        Thread receiveThread = new Thread(this::receiveAudioData);
        receiveThread.start();

        // Запуск потока для воспроизведения данных
        Thread playThread = new Thread(this::playAudioData);
        playThread.start();
    }


    private void receiveAudioData() {
        try
        {
            DatagramSocket datagramSocket = new DatagramSocket(port);
            int bufferSize = AudioTrack.getMinBufferSize(sampleRate,
                    AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT);

            //int bufferSize = 800;
            byte[] audioData = new byte[bufferSize];
            DatagramPacket datagramPacket = new DatagramPacket(audioData, bufferSize);

            logger.verbose("Debug", "Start listening...");

            while (!isDisconnected)
            {
                datagramSocket.receive(datagramPacket);
                logger.verbose("Debug", "Receive " + bufferSize + " bytes.");

                // Добавляем данные в очередь
                audioDataQueue.put(datagramPacket.getData().clone());
            }

            logger.verbose("Debug", "Stop listening...");
            datagramSocket.close();
        }
        catch (Exception e)
        {
            logger.error("Error", e.getMessage());
        }
    }



    private void playAudioData()
    {
        try
        {
            int bufferSize = AudioTrack.getMinBufferSize(sampleRate,
                    AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT);

            AudioTrack aTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                    sampleRate, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize, AudioTrack.MODE_STREAM);
            aTrack.play();

            logger.verbose("Debug", "Start playing audio...");

            while (!isDisconnected)
            {
                // Извлекаем данные из очереди
                byte[] data = audioDataQueue.take();
                aTrack.write(data, 0, data.length);
            }

            logger.verbose("Debug", "Stop playing audio...");
            aTrack.stop();
            aTrack.release();
        }
        catch (Exception e)
        {
            logger.error("Error", e.getMessage());
        }
    }
}
