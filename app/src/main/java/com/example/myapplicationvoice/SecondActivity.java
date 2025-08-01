package com.example.myapplicationvoice;

import android.content.res.ColorStateList;
import android.graphics.BlendMode;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class SecondActivity extends AppCompatActivity
{
    MyLogger logger;
    EditText editTextAddress;
    EditText editTextMessage;
    Button btnAddressConnect;
    Button btnSendMessage;
    RecyclerView recyclerView;

    ServerSocket serverSocket;
    Socket socket;

    RecieverMessages recieverMessages;
    private static BufferedWriter out;

    boolean isRunning;
    String address;

    final int PORT = 6053;


    // Максимальный размер сообщения
    private static final int MaxLenthMessage = 150;

    // Список сообщений
    ArrayList<String> messages = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        isRunning = false;
        logger = new MyLogger();

        editTextAddress = (EditText) findViewById(R.id.edtxIpAddressMessage);
        editTextMessage = (EditText) findViewById(R.id.edtxMessage);
        btnAddressConnect = (Button) findViewById(R.id.btnConnectSendMsg);
        btnSendMessage = (Button) findViewById(R.id.btnSendMessage);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_messages);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        DataAdapter dataAdapter = new DataAdapter(this, messages);
        recyclerView.setAdapter(dataAdapter);


        btnAddressConnect.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(isRunning)
                {
                    isRunning = false;

                    btnAddressConnect.setText(getResources().getText(R.string.connectMsg_button));
                    btnAddressConnect.setBackgroundTintList(getColorStateList(R.color.green));
                    editTextAddress.setEnabled(true);

                    logger.verbose("Debug","User disconnect: "+ editTextAddress.getText().toString());

                    recieverMessages.setDisconnectedCall();

                    try
                    {
                        socket.close();
                        serverSocket.close();
                    }
                    catch (IOException e)
                    {
                        logger.error("Error", e.getMessage());
                    }
                }
                else
                {
                    isRunning = true;

                    btnAddressConnect.setText(getResources().getText(R.string.disconnectMsg_button));
                    btnAddressConnect.setBackgroundTintList(getColorStateList(R.color.red));
                    editTextAddress.setEnabled(false);

                    address = editTextAddress.getText().toString();

                    // Проверка является ли устройство сервером
                    if(address.equals("") || address.equals(" "))
                    {
                        try
                        {
                            serverSocket = new ServerSocket(PORT);
                            socket = serverSocket.accept();

                            logger.verbose("Debug","User connect: "+ socket.getInetAddress().toString());

                            recieverMessages = new RecieverMessages(socket, messages, logger, dataAdapter, recyclerView);
                            recieverMessages.start();

                            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                        }
                        catch (IOException e)
                        {
                            logger.error("Error", e.getMessage());
                        }
                        catch (Exception ex)
                        {
                            logger.error("Error", ex.getMessage());
                        }
                    }
                    else
                    {
                        try
                        {
                            socket = new Socket(address, PORT);

                            logger.verbose("Debug","User connect: "+ socket.getInetAddress().toString());

                            recieverMessages = new RecieverMessages(socket, messages, logger, dataAdapter, recyclerView);
                            recieverMessages.start();

                            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                        }
                        catch (IOException e)
                        {
                            logger.error("Error", e.getMessage());
                        }
                        catch (Exception ex)
                        {
                            logger.error("Error", ex.getMessage());
                        }
                    }
                }
            }
        });


        btnSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                // Чтение сообщение из формы
                String msg = editTextMessage.getText().toString();

                // Проверка на размер сообщения
                if(msg.equals(""))
                {
                    logger.verbose("Debug","Empty message");
                    Toast.makeText(getApplicationContext(), "Введите сообщение!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(msg.length() > MaxLenthMessage)
                {
                    logger.verbose("Debug","Long message");
                    Toast.makeText(getApplicationContext(), "Слишком длинное сообщение!", Toast.LENGTH_SHORT).show();
                    return;
                }

                logger.verbose("Debug","Send message: "+ msg);
                messages.add(msg);

                // Сброс сообщения
                editTextMessage.setText("");

                // Сообщаем адаптеру об изменениях
                dataAdapter.notifyDataSetChanged();
                // Смещаем ресайкл вниз
                recyclerView.smoothScrollToPosition(messages.size());

                try
                {
                    out.write(msg + "\n");
                    out.flush();
                }
                catch (IOException e)
                {
                    logger.error("Error", e.getMessage());
                }
                catch (Exception e)
                {
                    logger.error("Error", e.getMessage());
                }
                finally
                {

                }
            }
        });


        // Слушать изменения списка сообщений
        // Берем сообщение и помещаем в список
    }
}
