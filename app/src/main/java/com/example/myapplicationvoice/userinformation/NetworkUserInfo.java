package com.example.myapplicationvoice.userinformation;

import static android.content.Context.CONNECTIVITY_SERVICE;
import static android.content.Context.WIFI_SERVICE;

import static androidx.core.content.ContextCompat.getSystemService;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

public class NetworkUserInfo
{
    /**
     * Получает IP-адрес Wi-Fi подключения
     */
    public static String getWifiIpAddress(Context context)
    {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext()
                .getSystemService(WIFI_SERVICE);
        return Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());
    }


    /**
     * Получает локальный IP-адрес устройства (не только Wi-Fi)
     */
    public static String getLocalIpAddress()
    {
        try
        {
            // Получение всех сетей
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces)
            {
                // Получение адреса в каждой сети
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs)
                {
                    if (!addr.isLoopbackAddress())
                    {
                        String sAddr = addr.getHostAddress();
                        boolean isIPv4 = sAddr.indexOf(':') < 0;
                        if (isIPv4)
                        {
                            return sAddr;
                        }
                    }
                }
            }
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return null;
    }



    public static String getNetworkName(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                WifiManager wifiManager = (WifiManager) context.getApplicationContext()
                        .getSystemService(WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                return wifiInfo.getSSID().replace("\"", "");
            }
        }
        return "Не подключено";
    }

}
