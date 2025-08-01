package com.example.myapplicationvoice.homescreen;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplicationvoice.R;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.normal.TedPermission;

import java.util.List;

/**
 * Заставка приложения
 */
public class WelcomeActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        Thread thread = new Thread(){
            @Override
            public void run()
            {
                super.run();
                try
                {
                    sleep(1200);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                finally
                {
                    PermissionListener permissionlistener = new PermissionListener()
                    {
                        /**
                         * Разрешение получено
                         */
                        @Override
                        public void onPermissionGranted()
                        {
                            Intent intent = new Intent(WelcomeActivity.this, RegistrationActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            startActivity(intent);
                        }

                        /**
                         * Отказано в разрешении
                         * @param deniedPermissions Список запрашиваемых разрешений
                         */
                        @Override
                        public void onPermissionDenied(List<String> deniedPermissions)
                        {
                            Toast.makeText(WelcomeActivity.this,
                                    "Доступ запрещен\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
                        }
                    };

                    TedPermission.create()
                            .setPermissionListener(permissionlistener)
                            .setDeniedMessage("Для использования приложения необходимо предоставить доступ к местоположению, " +
                                    "доступу к микрофону и доступ к сети")
                            .setPermissions(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.RECORD_AUDIO,
                                    Manifest.permission.INTERNET, Manifest.permission.ACCESS_COARSE_LOCATION,
                                    Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.ACCESS_NETWORK_STATE,
                                    Manifest.permission.CHANGE_WIFI_MULTICAST_STATE)
                            .check();
                }
            }
        };
        thread.start();
    }
}
