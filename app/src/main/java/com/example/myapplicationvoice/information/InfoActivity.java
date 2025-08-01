package com.example.myapplicationvoice.information;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.myapplicationvoice.R;
import com.example.myapplicationvoice.homescreen.UserAdapter;
import com.example.myapplicationvoice.homescreen.UsersOnlineActivity;
import com.example.myapplicationvoice.userinformation.NetworkUserInfo;
import com.example.myapplicationvoice.yandexmap.MapScreen;
import com.google.android.material.navigation.NavigationView;

public class InfoActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final int PERMISSION_REQUEST_CODE = 632;
    private TextView ipAddressView;
    private TextView firstNameView;
    private TextView lastNameView;

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;

    private String firstName;
    private String lastName;
    private String networkName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        // Настройка Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Инициализация DrawerLayout
        drawerLayout = findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Инициализация views
        ipAddressView = findViewById(R.id.ip_address);
        firstNameView = findViewById(R.id.first_name);
        lastNameView = findViewById(R.id.last_name);

        // Получение данных пользователя из Intent
        Intent intent = getIntent();
        firstName = intent.getStringExtra("first_name");
        lastName = intent.getStringExtra("last_name");
        networkName = intent.getStringExtra("network_name");
        firstNameView.setText(firstName);
        lastNameView.setText(lastName);
        ipAddressView.setText(networkName);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Обработка нажатия на кнопку "гамбургер"
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Обработка нажатий в боковом меню
        int id = item.getItemId();

        if (id == R.id.nav_contacts)
        {
            Intent intent = new Intent(this, UsersOnlineActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        }
        else if (id == R.id.nav_map)
        {
            Intent intent = new Intent(this, MapScreen.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            intent.putExtra("first_name", firstName);
            intent.putExtra("last_name", lastName);
            intent.putExtra("ip_address", networkName);
            startActivity(intent);
        }
        else if (id == R.id.nav_info)
        {

        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    private String getCurrentNetworkName() {
        try {
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

            // Проверка поддержки Wi-Fi и подключения
            if (wifiManager == null) {
                return "Wi-Fi не поддерживается";
            }

            if (!wifiManager.isWifiEnabled()) {
                return "Wi-Fi выключен";
            }

            // Для Android 10+ (API 29+) проверяем разрешение
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = cm.getActiveNetworkInfo();

                if (networkInfo == null || !networkInfo.isConnected()) {
                    return "Нет подключения";
                }

                if (networkInfo.getType() != ConnectivityManager.TYPE_WIFI) {
                    return "Не Wi-Fi подключение";
                }
            }

            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            String ssid = wifiInfo.getSSID();

            if (ssid == null || ssid.equals("<unknown ssid>") || ssid.equals("0x")) {
                return "Не удалось определить сеть";
            }

            return ssid.replace("\"", ""); // Удаляем кавычки

        } catch (Exception e) {
            return "Ошибка: " + e.getMessage();
        }
    }

    private boolean checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQUEST_CODE
            );
        }
    }
}