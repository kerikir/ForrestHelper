package com.example.myapplicationvoice.messenger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class MessageReceiver implements Runnable {
    private MessageListener listener;

    public MessageReceiver(MessageListener listener) {
        this.listener = listener;
    }

    @Override
    public void run() {
        try {
            // Создаем серверный сокет на порту 8080
            ServerSocket serverSocket = new ServerSocket(8080);

            while (!Thread.currentThread().isInterrupted()) {
                // Ожидаем подключения клиента
                Socket clientSocket = serverSocket.accept();

                // Получаем входной поток для чтения данных
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream()));

                // Читаем сообщение
                String message = reader.readLine();

                // Передаем сообщение через интерфейс
                if (message != null && listener != null) {
                    listener.onMessageReceived(message);
                }

                // Закрываем соединение с клиентом
                clientSocket.close();
            }

            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public interface MessageListener {
        void onMessageReceived(String message);
    }
}
