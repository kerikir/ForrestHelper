package com.example.myapplicationvoice.messenger;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class MessageSender implements Runnable {
    private final String ip;
    private final String firstName;
    private final String lastName;
    private final String message;

    public MessageSender(String ip, String firstName, String lastName, String message) {
        this.ip = ip;
        this.firstName = firstName;
        this.lastName = lastName;
        this.message = message;
    }

    @Override
    public void run() {
        try {
            // Устанавливаем соединение с сервером (порт 8080)
            Socket socket = new Socket(ip, 8080);

            // Получаем выходной поток для отправки данных
            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);

            // Формируем и отправляем сообщение
            String fullMessage = String.format("%s %s: %s", firstName, lastName, message);
            writer.println(fullMessage);

            // Закрываем соединение
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}