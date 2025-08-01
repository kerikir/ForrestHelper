package com.example.myapplicationvoice.userinformation;

import android.content.Context;
import androidx.annotation.NonNull;
import java.util.Objects;

/**
 * Класс, представляющий пользователя сети.
 * Расширяет базовый класс User, добавляя информацию об IP-адресе устройства.
 */
public class NetworkUser extends User {
    private String ipAddress;


    /**
     * Конструктор по умолчанию.
     */
    public NetworkUser()
    {
        super();
    }


    /**
     * Конструктор с параметрами.
     *
     * @param firstName Имя пользователя
     * @param lastName Фамилия пользователя
     * @param context Контекст приложения для получения IP-адреса
     */
    public NetworkUser(@NonNull String firstName, @NonNull String lastName, @NonNull Context context)
    {
        super(firstName, lastName);
        this.ipAddress = NetworkUserInfo.getWifiIpAddress(context);
    }


    /**
     * Возвращает IP-адрес пользователя.
     *
     * @return IP-адрес в формате строки
     */
    public String getIpAddress()
    {
        return ipAddress;
    }


    /**
     * Устанавливает IP-адрес пользователя.
     *
     * @param ipAddress IP-адрес для установки
     */
    public void setIpAddress(String ipAddress)
    {
        this.ipAddress = ipAddress;
    }


    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        if (!super.equals(o))
            return false;

        NetworkUser that = (NetworkUser) o;
        return Objects.equals(ipAddress, that.ipAddress);
    }


    @Override
    public int hashCode()
    {
        return Objects.hash(super.hashCode(), ipAddress);
    }


    @Override
    public String toString()
    {
        return "NetworkUser{" +
                "firstName='" + getFirstName() + '\'' +
                ", lastName='" + getLastName() + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                '}';
    }
}