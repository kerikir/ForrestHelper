package com.example.myapplicationvoice.servers;

import android.util.Log;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * Класс для управления UDP-коммуникацией в фоновых потоках.
 * Обеспечивает асинхронный прием и отправку сообщений с callback-уведомлениями.
 */
public class UdpManager {
    private static final String TAG = "UdpManager";

    /** Порт по умолчанию для UDP-коммуникации */
    public static final int UDP_PORT_USERS_ONLINE = 6318;
    public static final int UDP_PORT_INCOMING_CALL = 6320;
    public static final int UDP_PORT_CALL = 6322;
    public static final int UDP_PORT_MESSENGER = 6324;
    public static final int UDP_PORT_MAP = 6326;

    public final int PORT;

    private DatagramSocket socket;
    private boolean isRunning;
    private ExecutorService executor;
    private UdpCallback callback;


    private final Gson gson = new Gson();



    /**
     * Интерфейс callback для обработки сетевых событий.
     * Все методы вызываются в фоновых потоках.
     */
    public interface UdpCallback
    {
        /**
         * Вызывается при получении JSON-объекта
         * @param ip адрес отправителя
         * @param json полученный JSON
         */
        void onObjectReceived(String ip, String json);

        /**
         * Вызывается при получении нового UDP-сообщения.
         * @param ip IP-адрес отправителя
         * @param message полученное сообщение
         */
        void onMessageReceived(String ip, String message);

        /**
         * Вызывается после попытки отправки ответа.
         * @param success true если отправка прошла успешно
         * @param ip IP-адрес получателя
         */
        void onResponseSent(boolean success, String ip);

    }

    /**
     * Создает новый экземпляр UdpManager.
     * @param callback интерфейс для получения уведомлений о событиях
     */
    public UdpManager(UdpCallback callback, int port) {
        this.callback = callback;
        this.executor = Executors.newFixedThreadPool(2);

        this.PORT = port;
    }

    /**
     * Запускает фоновый поток для прослушивания входящих сообщений.
     * Сообщения будут поступать через {@link UdpCallback#onMessageReceived}.
     */
    public void startListening() {
        isRunning = true;
        executor.execute(this::listenLoop);
    }

    /** Внутренний метод для цикла прослушивания */
    private void listenLoop() {
        try {
            socket = new DatagramSocket(PORT);
            socket.setBroadcast(true);
            Log.i(TAG, "UDP listening started on port " + PORT);

            byte[] buffer = new byte[1024];

            while (isRunning) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String senderIp = packet.getAddress().getHostAddress();
                String message = new String(packet.getData(), 0, packet.getLength());
                Log.d(TAG, "Received from " + senderIp + ": " + message);

                if (isJsonObject(message))
                    notifyObjectReceived(senderIp, message);
                else
                    notifyMessageReceived(senderIp, message);
            }
        } catch (IOException e) {
            if (isRunning) {
                Log.e(TAG, "Listen error: " + e.getMessage());
            }
        } finally {
            closeSocket();
        }
    }

    /** Уведомляет callback о полученном сообщении */
    private void notifyMessageReceived(String ip, String message) {
        if (callback != null) {
            try {
                callback.onMessageReceived(ip, message);
            } catch (Exception e) {
                Log.e(TAG, "Callback error: " + e.getMessage());
            }
        }
    }


    /** Уведомляет callback о полученном сообщении */
    private void notifyObjectReceived(String ip, String message) {
        if (callback != null) {
            try {
                callback.onObjectReceived(ip, message);
            } catch (Exception e) {
                Log.e(TAG, "Callback error: " + e.getMessage());
            }
        }
    }


    /**
     * Асинхронно отправляет UDP-сообщение.
     * Результат будет доступен через {@link UdpCallback#onResponseSent}.
     *
     * @param message текст сообщения для отправки
     * @param ip IP-адрес получателя
     * @param port порт получателя
     */
    public void sendResponseAsync(String message, String ip, int port) {
        executor.execute(() -> sendResponse(message, ip, port));
    }

    /** Внутренний метод для отправки сообщения */
    private void sendResponse(String message, String ip, int port) {
        try {
            if (socket == null || socket.isClosed()) {
                socket = new DatagramSocket();
                socket.setBroadcast(true);
            }

            byte[] sendData = message.getBytes();
            InetAddress address = InetAddress.getByName(ip);
            DatagramPacket packet = new DatagramPacket(sendData, sendData.length, address, port);

            socket.send(packet);
            Log.d(TAG, "Sent to " + ip + ": " + message);

            notifyResponseSent(true, ip);
        } catch (IOException e) {
            Log.e(TAG, "Send error to " + ip + ": " + e.getMessage());
            notifyResponseSent(false, ip);
        }
    }

    /** Уведомляет callback о результате отправки */
    private void notifyResponseSent(boolean success, String ip) {
        if (callback != null) {
            try {
                callback.onResponseSent(success, ip);
            } catch (Exception e) {
                Log.e(TAG, "Callback error: " + e.getMessage());
            }
        }
    }

    /**
     * Останавливает все сетевые операции и освобождает ресурсы.
     * Должен вызываться при завершении работы с объектом.
     */
    public void stop() {
        isRunning = false;
        closeSocket();
        executor.shutdown();
    }

    /** Закрывает сокет и освобождает ресурсы */
    private void closeSocket() {
        if (socket != null)
        {
            if (!socket.isClosed())
            {
                socket.disconnect();
                socket.close();

            }
            socket = null;
        }

        executor.shutdown();

        callback = null;
    }


    /**
     * Отправляет объект в JSON-формате
     * @param responseObject объект для отправки
     * @param ip адрес получателя
     * @param port порт получателя
     */
    public <T> void sendObjectAsync(T responseObject, String ip, int port) {
        executor.execute(() -> {
            String json = gson.toJson(responseObject);
            sendResponse(json, ip, port);
        });
    }


    /**
     * Проверяет, является ли строка валидным JSON объектом
     */
    private boolean isJsonObject(String str) {
        try {
            JsonElement element = JsonParser.parseString(str);
            return element.isJsonObject();
        } catch (JsonSyntaxException e) {
            return false;
        }
    }
}