package com.example.myapplicationvoice;

import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

public class RecieverMessages extends Thread
{
    private volatile boolean isDisconnected;
    private String ipAddress;
    private int port;
    private int sampleRate;
    private MyLogger logger;

    private final int BUFF_COUNT = 20;
    private AudioRecord audioRecord;

    Context context;
    ArrayList<String> messages;
    DataAdapter dataAdapter;
    RecyclerView recyclerView;
    Socket socket;

    private static BufferedReader in;


    public RecieverMessages(Socket socket, ArrayList<String> messages, MyLogger logger, DataAdapter dataAdapter, RecyclerView recyclerView)
    {
        isDisconnected = false;

        this.socket = socket;
        this.messages = messages;

        this.logger = logger;
        this.dataAdapter = dataAdapter;
        this.recyclerView = recyclerView;
    }


    public void setDisconnectedCall()
    {
        isDisconnected = true;
    }


    public void run()
    {
        try
        {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String msg;


            while(!isDisconnected)
            {
                msg = in.readLine();
                messages.add(msg);

                logger.verbose("Debug","Recv message: "+ msg);

                // Сообщаем адаптеру об изменениях
                dataAdapter.notifyDataSetChanged();
                // Смещаем ресайкл вниз
                recyclerView.smoothScrollToPosition(messages.size());
            }

            in.close();
        }
        catch (IOException e)
        {
            logger.error("Error", e.getMessage());
        }
    }
}
