package com.example.myapplicationvoice.messenger;

import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplicationvoice.R;
import com.example.myapplicationvoice.encryption.AlgorithmMMB;
import com.example.myapplicationvoice.homescreen.UsersOnlineActivity;
import com.example.myapplicationvoice.voicecall.VoiceCallActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class MessengerActivity extends AppCompatActivity
{
    private EditText messageEditText;
    private ImageButton sendButton;
    private ImageButton backButton;
    private RecyclerView messagesRecyclerView;
    private MessageAdapter messageAdapter;
    private List<Message> messages = new ArrayList<>();

    private String ipAddress;
    private String firstName;
    private String lastName;
    private String myFirstName;
    private String myLastName;

    private final int PORT = 8080;
    private ServerSocket serverSocket;
    private Thread serverThread;

    private AlgorithmMMB algorithmMMB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messenger);

        // Получаем данные из Intent
        ipAddress = getIntent().getStringExtra("ip");
        firstName = getIntent().getStringExtra("firstName");
        lastName = getIntent().getStringExtra("lastName");
        myFirstName = getIntent().getStringExtra("myFirstName");
        myLastName = getIntent().getStringExtra("myLastName");

        // Инициализация UI
        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);
        backButton = findViewById(R.id.backButton);
        messagesRecyclerView = findViewById(R.id.messagesRecyclerView);
        TextView contactNameTextView = findViewById(R.id.contactNameTextView);

        // Устанавливаем имя абонента
        contactNameTextView.setText(firstName + " " + lastName);

        algorithmMMB = new AlgorithmMMB();

        // Настройка RecyclerView
        messageAdapter = new MessageAdapter(messages);
        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        messagesRecyclerView.setAdapter(messageAdapter);

        // Обработчик кнопки отправки
        sendButton.setOnClickListener(v -> sendMessage());

        // Обработчик кнопки "Назад"
        backButton.setOnClickListener(v ->
        {
            Intent intent = new Intent(this, UsersOnlineActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);

            finish(); // Закрываем текущую активность
        });

        // Запускаем сервер для приема сообщений
        startMessageReceiver();
    }

    private void sendMessage() {
        String messageText = messageEditText.getText().toString().trim();

        if (!messageText.isEmpty()) {
            // Создаем и добавляем сообщение в список
            Message message = new Message(messageText, true, myFirstName + " " + myLastName);
            messageAdapter.addMessage(message);
            messageEditText.setText("");

            // Прокручиваем список вниз
            messagesRecyclerView.scrollToPosition(messages.size() - 1);

            // Отправляем сообщение по сети в отдельном потоке
            new Thread(() -> {
                try {
                    Socket socket = new Socket(ipAddress, PORT);
                    PrintWriter out = new PrintWriter(new OutputStreamWriter(
                            socket.getOutputStream(), StandardCharsets.UTF_16BE), true);

                    String encriptionMsg = encryptedMessage(messageText);

                    out.println(encriptionMsg);
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    runOnUiThread(() -> {
                        // Показываем ошибку, если не удалось отправить сообщение
                        Message errorMessage = new Message("Не удалось отправить сообщение", true, "Система");
                        messageAdapter.addMessage(errorMessage);
                        messagesRecyclerView.scrollToPosition(messages.size() - 1);
                    });
                }
            }).start();
        }
    }

    private void startMessageReceiver() {
        serverThread = new Thread(() -> {
            try {
                serverSocket = new ServerSocket(PORT);
                while (!Thread.currentThread().isInterrupted()) {
                    Socket clientSocket = serverSocket.accept();

                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_16BE));
                    String receivedMessage = in.readLine();

                    String decryptionMessage = decryptedMessage(receivedMessage);

                    runOnUiThread(() -> {
                        // Добавляем полученное сообщение в список
                        Message message = new Message(
                                decryptionMessage.substring(decryptionMessage.indexOf(":") + 2),
                                false,
                                decryptionMessage.substring(0, decryptionMessage.indexOf("("))
                        );
                        messageAdapter.addMessage(message);
                        messagesRecyclerView.scrollToPosition(messages.size() - 1);
                    });

                    clientSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                if (!Thread.currentThread().isInterrupted()) {
                    runOnUiThread(() -> {
                        // Уведомление об ошибке подключения
                        Message errorMessage = new Message("Ошибка приёма сообщений", false, "Система");
                        messageAdapter.addMessage(errorMessage);
                    });
                }
            } finally {
                if (serverSocket != null && !serverSocket.isClosed()) {
                    try {
                        serverSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        serverThread.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Остановка серверного потока и закрытие сокета
        if (serverThread != null && serverThread.isAlive()) {
            serverThread.interrupt();
            try {
                if (serverSocket != null && !serverSocket.isClosed()) {
                    serverSocket.close(); // Закрытие сокета для выхода из accept()
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    private String encryptedMessage(String message)
    {
        byte[] messageByte = message.getBytes(StandardCharsets.UTF_16BE);
        byte[] encryptionMessageByte;

        if((messageByte.length / 16) < 10)
            encryptionMessageByte = algorithmMMB.encryptionMessage(messageByte);
        else
            encryptionMessageByte = algorithmMMB.encryptionMessageParallelWithStreamModificate(
                    messageByte , 2);

        int lackByte = algorithmMMB.getLackOfByte();

        String encryptionMessage = Base64.encodeToString(encryptionMessageByte, Base64.NO_WRAP);

        return myFirstName + " " + myLastName + "(" + lackByte + ")" + ": " + encryptionMessage;
    }


    private String decryptedMessage(String encryptedMessage)
    {
        String lackByteStr = encryptedMessage.substring(encryptedMessage.indexOf("(") + 1,
                encryptedMessage.indexOf(")"));
        int lackByte = Integer.parseInt(lackByteStr);
        String ecryptionMsg = encryptedMessage.substring(
                encryptedMessage.indexOf(":") + 2);

        byte[] encryptionMsgByte = Base64.decode(ecryptionMsg, Base64.NO_WRAP);
        byte[] decryptionMsgByte;
        if((encryptionMsgByte.length / 16) < 10)
            decryptionMsgByte = algorithmMMB.decryptionMessage(encryptionMsgByte, lackByte);
        else
            decryptionMsgByte = algorithmMMB.decryptionMessageParallelWithStreamModificate(
                    encryptionMsgByte, lackByte, 2);

        String decryptionMsg = new String(decryptionMsgByte, StandardCharsets.UTF_16BE);

        return encryptedMessage.substring(0, encryptedMessage.indexOf(":") + 2) + decryptionMsg;
    }
}