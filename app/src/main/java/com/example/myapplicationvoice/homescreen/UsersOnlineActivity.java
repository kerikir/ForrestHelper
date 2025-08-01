package com.example.myapplicationvoice.homescreen;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplicationvoice.MainActivity;
import com.example.myapplicationvoice.R;
import com.example.myapplicationvoice.SecondActivity;
import com.example.myapplicationvoice.information.InfoActivity;
import com.example.myapplicationvoice.messenger.MessengerActivity;
import com.example.myapplicationvoice.servers.ActionType;
import com.example.myapplicationvoice.servers.CallMessage;
import com.example.myapplicationvoice.servers.UdpManager;
import com.example.myapplicationvoice.userinformation.NetworkUser;
import com.example.myapplicationvoice.userinformation.NetworkUserInfo;
import com.example.myapplicationvoice.voicecall.IncomingCallActivity;
import com.example.myapplicationvoice.voicecall.VoiceCallActivity;
import com.example.myapplicationvoice.yandexmap.MapScreen;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UsersOnlineActivity extends AppCompatActivity implements
        UserAdapter.OnUserActionListener, NavigationView.OnNavigationItemSelectedListener,
        UdpManager.UdpCallback
{
    private UdpManager udpManager;
    private Gson gson;


    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private static final int REQUEST_CALL_PERMISSION = 101;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;

    public NetworkUser myDataNetworkUser;
    List<NetworkUser> users;
    private ImageButton btnSearchUsers;


    private String broadcastAddress = "255.255.255.255";
    private boolean isFirstMsg = true;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_online);

        // Настройка Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Инициализация DrawerLayout и NavigationView
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Настройка кнопки "гамбургер"
        toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        recyclerView = findViewById(R.id.users_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Получаем переданные данные
        String firstName = getIntent().getStringExtra(RegistrationActivity.EXTRA_FIRST_NAME);
        String lastName = getIntent().getStringExtra(RegistrationActivity.EXTRA_LAST_NAME);
        myDataNetworkUser = new NetworkUser(firstName, lastName, this);

        adapter = new UserAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);

        btnSearchUsers = findViewById(R.id.search_button);
        btnSearchUsers.setOnClickListener(this::btnClick_SearchUser);

        users = new ArrayList<NetworkUser>();
        gson = new Gson();

        // Инициализация UDP менеджера
        udpManager = new UdpManager(this, UdpManager.UDP_PORT_USERS_ONLINE);
        udpManager.startListening();

        //sendBroadcastSearch();
    }


    private void sendBroadcastSearch()
    {
        String msg = ActionType.SEARCH.getDescription();

        // Отправляем на broadcast адрес
        udpManager.sendResponseAsync(msg, getWifiBroadcastAddress(), UdpManager.UDP_PORT_USERS_ONLINE);
        udpManager.sendResponseAsync(msg, "192.168.1.255", UdpManager.UDP_PORT_USERS_ONLINE);
        //udpManager.sendResponseAsync(msg, "192.168.2.255", UdpManager.UDP_PORT_USERS_ONLINE);
        //udpManager.sendResponseAsync(msg, "192.168.37.255", UdpManager.UDP_PORT_USERS_ONLINE);
        //udpManager.sendResponseAsync(msg, "192.168.255.255", UdpManager.UDP_PORT_USERS_ONLINE);
        //udpManager.sendResponseAsync(msg, "255.255.255.255", UdpManager.UDP_PORT_USERS_ONLINE);
        udpManager.sendResponseAsync(msg, broadcastAddress, UdpManager.UDP_PORT_USERS_ONLINE);

        addToLog("Отправлен запрос: " + msg);
    }


    // region UdpCallback реализация
    @Override
    public void onMessageReceived(String ip, String message) {
        addToLog("Получено от " + ip + ": " + message);

        checkIpRecv(ip);

        if(message.equals(ActionType.SEARCH.getDescription()))
        {
            udpManager.sendObjectAsync(myDataNetworkUser, ip, UdpManager.UDP_PORT_USERS_ONLINE);
        }
        else if(message.equals(ActionType.MESSAGE.getDescription()))
        {
            printNewMessage(ip);
        }
    }


    @Override
    public void onObjectReceived(String ip, String json) {
        try {
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
            if (obj.has("actionType"))
            {
                CallMessage callMessage = gson.fromJson(json, CallMessage.class);
                addToLog(String.format(
                        "Получен объект от %s:\n%s",
                        ip,
                        callMessage.toString()
                ));

                if(!callMessage.ipAddress.equals(ip))
                    callMessage.ipAddress = ip;

                incomingCall(callMessage);
            }
            else
            {
                NetworkUser dataSubscriber = gson.fromJson(json, NetworkUser.class);
                addToLog(String.format(
                        "Получен объект от %s:\n%s",
                        ip,
                        dataSubscriber.toString()
                ));

                if(!dataSubscriber.getIpAddress().equals(ip))
                    dataSubscriber.setIpAddress(ip);

                if(dataSubscriber.getFirstName().equals(myDataNetworkUser.getFirstName()) &&
                        dataSubscriber.getLastName().equals(myDataNetworkUser.getLastName())&&
                        !dataSubscriber.getIpAddress().equals(myDataNetworkUser.getIpAddress()))
                {
                    myDataNetworkUser.setIpAddress(dataSubscriber.getIpAddress());
                }

                addUserNetwork(dataSubscriber);
            }

        } catch (Exception e)
        {
            addToLog("Ошибка парсинга JSON от " + ip);
        }
    }


    @Override
    public void onResponseSent(boolean success, String ip)
    {
        addToLog("Ответ " + (success ? "доставлен" : "не доставлен") + " к " + ip);
    }
    // endregion

    private void addToLog(String text) {

        runOnUiThread(() -> Log.d("UDP_Activity", text));

    }


    private void printNewMessage(String ip)
    {
        runOnUiThread(() ->
        {
            Toast.makeText(this, "Сообщение от " + ip, Toast.LENGTH_SHORT).show();
        });
    }


    private void checkIpRecv(String ip)
    {
        runOnUiThread(() ->
        {
            if(isFirstMsg)
            {
                isFirstMsg = false;
                int index = ip.lastIndexOf('.');
                broadcastAddress = ip.substring(0, index) + ".255";
            }
        });
    }


    private void incomingCall(CallMessage callMessage) {

        runOnUiThread(() -> {
            Intent callIntent = new Intent(this, IncomingCallActivity.class);
            callIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            callIntent.putExtra("caller_name", callMessage.firstName + " " + callMessage.lastName);
            callIntent.putExtra("caller_ip", callMessage.ipAddress);
            startActivity(callIntent);
        });

    }

    private void addUserNetwork(NetworkUser user)
    {
        runOnUiThread(() ->
        {
            if (user.getIpAddress() != null && !user.getIpAddress().trim().isEmpty() &&
                    user.getFirstName() != null && !user.getFirstName().trim().isEmpty() &&
                    user.getLastName() != null && !user.getLastName().trim().isEmpty() &&
                    !user.getIpAddress().equals(myDataNetworkUser.getIpAddress()))
            {
                if (!users.contains(user))
                {
                    updateUsers(user);
                    udpManager.sendObjectAsync(myDataNetworkUser, user.getIpAddress(),
                            UdpManager.UDP_PORT_USERS_ONLINE);
                }
            }
        });
    }


    public void btnClick_SearchUser(View view)
    {
        sendBroadcastSearch();
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

        }
        else if (id == R.id.nav_map)
        {
            //NetworkUser[] networkUsers = new NetworkUser[users.size()];
            //networkUsers = users.toArray(networkUsers);
            Intent intent = new Intent(this, MapScreen.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            intent.putExtra("first_name", myDataNetworkUser.getFirstName());
            intent.putExtra("last_name", myDataNetworkUser.getLastName());
            intent.putExtra("ip_address", myDataNetworkUser.getIpAddress());
            //intent.putExtra("users", networkUsers);
            startActivity(intent);
        }
        else if (id == R.id.nav_info)
        {
            Intent intent = new Intent(this, InfoActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            intent.putExtra("first_name", myDataNetworkUser.getFirstName());
            intent.putExtra("last_name", myDataNetworkUser.getLastName());
            intent.putExtra("network_name", myDataNetworkUser.getIpAddress());
            startActivity(intent);
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


    private void updateUsers(NetworkUser user)
    {
        users.add(user);
        adapter.updateUsers(users);
    }


    @Override
    public void onMessageClick(NetworkUser user)
    {
        udpManager.sendResponseAsync(ActionType.MESSAGE.getDescription(), user.getIpAddress(),
                UdpManager.UDP_PORT_USERS_ONLINE);

        // Обработка нажатия на кнопку сообщения
        Intent intent = new Intent(this, MessengerActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.putExtra("ip", user.getIpAddress());
        intent.putExtra("firstName", user.getFirstName());
        intent.putExtra("lastName", user.getLastName());
        intent.putExtra("myFirstName", myDataNetworkUser.getFirstName());
        intent.putExtra("myLastName", myDataNetworkUser.getLastName());
        startActivity(intent);
    }


    @Override
    public void onCallClick(NetworkUser user)
    {
        // Обработка нажатия на кнопку звонка
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.RECORD_AUDIO},
                    REQUEST_CALL_PERMISSION);
        }
        else
        {
            CallMessage callMessage = new CallMessage(ActionType.CALL, myDataNetworkUser.getIpAddress(),
                    myDataNetworkUser.getFirstName(), myDataNetworkUser.getLastName());
            udpManager.sendObjectAsync(callMessage, user.getIpAddress(), UdpManager.UDP_PORT_USERS_ONLINE);
            startVoiceCall(user);
        }
    }


    private void startVoiceCall(NetworkUser user)
    {
        // Реализация звонка
        //Toast.makeText(this, "Вызов пользователя " + user.getFirstName(), Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, VoiceCallActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.putExtra("user_name", user.getFirstName() + " " + user.getLastName());
        intent.putExtra("ip_address", user.getIpAddress());
        intent.putExtra("isStart", false);
        startActivity(intent);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CALL_PERMISSION)
        {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                // Повторная попытка звонка после получения разрешения
            }
        }
    }


    @Override
    protected void onDestroy() {
        // 1. Отменить все асинхронные задачи
        if (udpManager != null) {
            udpManager.stop();
            udpManager = null; // Важно для предотвращения утечек
        }

        super.onDestroy(); // Всегда в конце!
    }


    public String getWifiBroadcastAddress() {
        try {
            WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
            if (wifi == null || !wifi.isWifiEnabled()) {
                return "255.255.255.255";
            }

            DhcpInfo dhcp = wifi.getDhcpInfo();
            if (dhcp == null) {
                return "255.255.255.255";
            }

            int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
            return String.format("%d.%d.%d.%d",
                    (broadcast) & 0xFF,
                    (broadcast >> 8) & 0xFF,
                    (broadcast >> 16) & 0xFF,
                    (broadcast >> 24) & 0xFF);
        } catch (Exception e) {
            return "255.255.255.255";
        }
    }
}