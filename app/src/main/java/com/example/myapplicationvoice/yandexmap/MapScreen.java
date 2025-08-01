package com.example.myapplicationvoice.yandexmap;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PointF;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.myapplicationvoice.R;
import com.example.myapplicationvoice.homescreen.UsersOnlineActivity;
import com.example.myapplicationvoice.information.InfoActivity;
import com.example.myapplicationvoice.servers.ActionType;
import com.example.myapplicationvoice.servers.CallMessage;
import com.example.myapplicationvoice.servers.GeopositionMessage;
import com.example.myapplicationvoice.servers.UdpManager;
import com.example.myapplicationvoice.userinformation.NetworkUser;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;
import com.yandex.mapkit.Animation;
import com.yandex.mapkit.MapKit;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.ScreenPoint;
import com.yandex.mapkit.ScreenRect;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.geometry.Polyline;
import com.yandex.mapkit.layers.ObjectEvent;
import com.yandex.mapkit.location.Location;
import com.yandex.mapkit.location.LocationListener;
import com.yandex.mapkit.location.LocationManager;
import com.yandex.mapkit.location.LocationStatus;
import com.yandex.mapkit.location.Purpose;
import com.yandex.mapkit.location.SubscriptionSettings;
import com.yandex.mapkit.location.UseInBackground;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.CompositeIcon;
import com.yandex.mapkit.map.IconStyle;
import com.yandex.mapkit.map.Map;
import com.yandex.mapkit.map.MapObjectCollection;
import com.yandex.mapkit.map.MapObjectTapListener;
import com.yandex.mapkit.map.PlacemarkMapObject;
import com.yandex.mapkit.map.PolylineMapObject;
import com.yandex.mapkit.map.RotationType;
import com.yandex.mapkit.map.TextStyle;
import com.yandex.mapkit.mapview.MapView;
import com.yandex.mapkit.user_location.UserLocationLayer;
import com.yandex.mapkit.user_location.UserLocationObjectListener;
import com.yandex.mapkit.user_location.UserLocationTapListener;
import com.yandex.mapkit.user_location.UserLocationView;
import com.yandex.runtime.image.ImageProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.github.cdimascio.dotenv.Dotenv;

public class MapScreen extends AppCompatActivity implements UserLocationObjectListener, UdpManager.UdpCallback
{

    /**
     * Ключ доступа к API
     */
    private String MAPKIT_API_KEY;

    /** Отобажаемая карта */
    private MapView mapView;
    private UserLocationLayer userLocationLayer;

    /** Соседнее устройтсво */
    private PlacemarkMapObject placemark;
    /** Список соседних устройств */
    private ArrayList<PlacemarkMapObject> placemarksArrayList;
    /** Коллекция соседних утройств */
    MapObjectCollection placemarkCollection;

    /** Степень приближения карты */
    private float zoom = 25.0f;
    /** Степень изменения приближения карты */
    private final float deltaZoom = 0.5f;
    /** Минимально возможное отдаление камеры */
    private final float minZoom = 10.0f;

    /** Долгая анимация движения камеры */
    private Animation animationLong;
    /** Короткая анимация движения камеры */
    private Animation animationShort;

    /** Кнопка определения местоположения */
    private ImageButton imgbtnNavigator;
    /** Отображение текущей скорости */
    private TextView textViewSpeed;

    /** Время предыдущего положения абонента */
    private long prevTime;
    /** Время текущего положения абонента */
    private long currentTime;
    /** Разница во времени текущего и предыдущего положения абонента */
    private long deltaTime;
    /** Шаг по времени для отрисовки пути */
    public final long STEP_TIME = 2000;
    /** Скорость абонента */
    double speed;

    /** Предыдущее местоположение пользователя */
    Point pervPoint;
    /** Текущее местоположение пользователя */
    Point currPoint;

    /** Точки пройденного маршрута */
    ArrayList<Point> pointsPath;
    /** Пройденный путь */
    Polyline polyline;
    /** Отрисованный путь на карте */
    PolylineMapObject polylineOnMap;

    /** Калькулятор расчета скорости абонента */
    DeterminationDistance determinationDistance;

    SubscriptionSettings subscriptionSettings =
            new SubscriptionSettings(UseInBackground.ALLOW, Purpose.PEDESTRIAN_NAVIGATION);

    LocationManager locationManager;

    String firstName;
    String lastName;
    String ipAddress;
    GeopositionMessage geopositionMessage;

    private UdpManager udpManager;
    private Gson gson;

    //NetworkUser[] users;
    List<GeopositionMessage> users;
    boolean isLocationDetermined = false;
    private String broadcastAddress = "255.255.255.255";
    private boolean isFirstMsg = true;

    private final int[] ColorsResourcesId = {
            R.drawable.person_avatar_black_icon,
            R.drawable.person_avatar_yellow_icon,
            R.drawable.person_avatar_green_icon,
            R.drawable.person_avatar_red_icon,
            R.drawable.person_avatar_blue_icon
    };


    /** Слушатель обработки нажатия на соседнее устройство */
    private final MapObjectTapListener placemarkTapListener = (mapObject, point) ->
    {
        Toast.makeText(this, mapObject.getUserData().toString(), Toast.LENGTH_SHORT).show();
        return true;
    };


    /** Слушатель обработки нажатия на свое местоположение */
    private final UserLocationTapListener userLocationTapListener = (point) ->
    {
        Toast.makeText(this,
                DeterminationDistance.getGeoLatitudeOnRussianFromGPS(currPoint.getLatitude()) + "\n" +
                        DeterminationDistance.getGeoLongitudeOnRussianFromGPS(currPoint.getLongitude()),
                Toast.LENGTH_SHORT).show();
    };


    /** Обработчик окончания анимации движения камеры */
    private final Map.CameraCallback cameraCallback = new Map.CameraCallback()
    {
        @Override
        public void onMoveFinished(boolean b)
        {

        }
    };


    private final LocationListener locationListener = new LocationListener()
    {
        @Override
        public void onLocationUpdated(@NonNull Location location)
        {
            if(pervPoint.getLatitude() == 0.0 || pervPoint.getLongitude() == 0.0)
            {
                pervPoint = new Point(location.getPosition().getLatitude(),
                        location.getPosition().getLongitude());

                determinationDistance.initializeStartPoint(
                        pervPoint.getLatitude(), pervPoint.getLongitude());
            }

            currentTime = System.currentTimeMillis();
            deltaTime = currentTime - prevTime;

            if (deltaTime >= STEP_TIME)
            {
                prevTime = currentTime;

                currPoint = new Point(location.getPosition().getLatitude(),
                        location.getPosition().getLongitude());

                geopositionMessage.latitude = location.getPosition().getLatitude();
                geopositionMessage.longitude = location.getPosition().getLongitude();
                sendBroadcastMyPosition();

                speed = determinationDistance.calculateSpeed(
                        currPoint.getLatitude(),
                        currPoint.getLongitude(),
                        deltaTime);

                speed = DeterminationDistance.roundingDistance(
                        DeterminationDistance.convertMsToKmh(speed), 1);

                textViewSpeed.setText(String.format("%.1f км/ч", speed));
                drawPath(pervPoint, currPoint, speed);

                pervPoint = currPoint;
            }
        }

        @Override
        public void onLocationStatusUpdated(@NonNull LocationStatus locationStatus)
        {
            switch (locationStatus)
            {
                case AVAILABLE:
                    isLocationDetermined = true;
                    break;
                case NOT_AVAILABLE:
                    isLocationDetermined = false;
                    break;
            }

        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Dotenv dotenv = Dotenv.configure()
                .filename(".env")
                .ignoreIfMalformed()
                .ignoreIfMissing()
                .load();

        MAPKIT_API_KEY = dotenv.get("MAPKIT_API_KEY");


        MapKitFactory.setApiKey(MAPKIT_API_KEY);
        MapKitFactory.initialize(this);
        setContentView(R.layout.activity_map);
        super.onCreate(savedInstanceState);
        mapView = findViewById(R.id.mapview);
        mapView.getMap().setRotateGesturesEnabled(false);
        mapView.getMap().move(new CameraPosition
                (new Point(56.860707, 40.508221), zoom, 0, 0));


        // Получаем данные из интента
        Intent intent = getIntent();
        firstName = intent.getStringExtra("first_name");
        lastName = intent.getStringExtra("last_name");
        ipAddress = intent.getStringExtra("ip_address");
        users = new ArrayList<GeopositionMessage>();

        geopositionMessage = new GeopositionMessage(ipAddress, firstName, lastName,
                56.860707, 40.508221);
        //usersGeo = initializeGeoUsers(users);


        // В onCreate()
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(item -> {
            // Обработка нажатий в боковом меню
            int id = item.getItemId();

            if (id == R.id.nav_contacts)
            {
                Intent intentOnline = new Intent(this, UsersOnlineActivity.class);
                intentOnline.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intentOnline);
            }
            else if (id == R.id.nav_map)
            {

            }
            else if (id == R.id.nav_info)
            {
                Intent intentInfo = new Intent(this, InfoActivity.class);
                intentInfo.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                intentInfo.putExtra("first_name", firstName);
                intentInfo.putExtra("last_name", lastName);
                intentInfo.putExtra("network_name", ipAddress);
                startActivity(intentInfo);
            }

            drawer.closeDrawer(GravityCompat.START);
            return true;
        });



        placemarksArrayList = new ArrayList<PlacemarkMapObject>();

        animationLong = new Animation(Animation.Type.SMOOTH, 1.0f);
        animationShort = new Animation(Animation.Type.LINEAR, 0.2f);

        imgbtnNavigator = findViewById(R.id.imageButton_navigation);
        calculateFocusRectangle();

        textViewSpeed = findViewById(R.id.textView_speed);
        textViewSpeed.setText("0.0 км/ч");

        pervPoint = new Point();
        currPoint = new Point();

        MapKit mapKit = MapKitFactory.getInstance();
        userLocationLayer = mapKit.createUserLocationLayer(mapView.getMapWindow());
        userLocationLayer.setVisible(true);
        userLocationLayer.setHeadingEnabled(true);
        userLocationLayer.setObjectListener(this);
        userLocationLayer.setTapListener(userLocationTapListener);
        locationManager = mapKit.createLocationManager();

        // Инициализация UDP менеджера
        udpManager = new UdpManager(this, UdpManager.UDP_PORT_MAP);
        udpManager.startListening();
        gson = new Gson();
    }


    @Override
    protected void onStart()
    {
        super.onStart();
        MapKitFactory.getInstance().onStart();
        mapView.onStart();

        if (locationManager != null && locationListener != null && subscriptionSettings != null)
            locationManager.subscribeForLocationUpdates(subscriptionSettings, locationListener);
    }


    @Override
    protected void onStop()
    {
        mapView.onStop();
        MapKitFactory.getInstance().onStop();

        locationManager.unsubscribe(locationListener);
        super.onStop();
    }


    @Override
    public void onObjectAdded(@NonNull UserLocationView userLocationView)
    {
        float focusPointX = mapView.getWidth() * 0.5f;

        try
        {
            focusPointX = mapView.getMapWindow().getFocusRect().getBottomRight().getX() / 2.0f;
            pervPoint = new Point(userLocationLayer.cameraPosition().getTarget().getLatitude(),
                    userLocationLayer.cameraPosition().getTarget().getLatitude());
        }
        catch (NullPointerException exception)
        {

        }
        finally
        {
            userLocationLayer.setAnchor(
                    new PointF(focusPointX, (float)(mapView.getHeight() * 0.5)),
                    new PointF(focusPointX, (float)(mapView.getHeight() * 0.83)));

            userLocationView.getArrow().setIcon(ImageProvider.fromResource(
                    this, R.drawable.user_arrow));

            CompositeIcon pinIcon = userLocationView.getPin().useCompositeIcon();

            pinIcon.setIcon(
                    "icon",
                    ImageProvider.fromResource(this, R.drawable.maps_arrow),
                    new IconStyle().setAnchor(new PointF(0.5f, 0.5f))
                            .setRotationType(RotationType.ROTATE)
                            .setZIndex(0f)
                            .setScale(1f)
            );

            pinIcon.setIcon(
                    "pin",
                    ImageProvider.fromResource(this, R.drawable.search_result),
                    new IconStyle().setAnchor(new PointF(0.5f, 0.5f))
                            .setRotationType(RotationType.ROTATE)
                            .setZIndex(1f)
                            .setScale(0.5f)
            );

            userLocationView.getAccuracyCircle().setFillColor(Color.BLUE & 0x99ffffff);



            determinationDistance = new DeterminationDistance(
                    pervPoint.getLatitude(), pervPoint.getLongitude());

            prevTime = System.currentTimeMillis();
        }
    }


    @Override
    public void onObjectRemoved(@NonNull UserLocationView userLocationView)
    {

    }


    @Override
    public void onObjectUpdated(@NonNull UserLocationView userLocationView, @NonNull ObjectEvent objectEvent)
    {

    }




    /**
     * Установка положения абонента на карте
     * @param point Местоположение абонента
     * @param resourceId Иконка абонента на карте
     * @param titlePlacemark Надпись около абонента на карте
     * @param infoUser Информация об абоненте при нажатии на него
     * @return Объект абонента на карте
     */
    @NonNull
    private PlacemarkMapObject setPlacemarkOnMap(Point point, int resourceId,
                                                 String titlePlacemark, String infoUser)
    {
        // Добавление метки на карту
        PlacemarkMapObject placemark = mapView.getMap().getMapObjects().
                addPlacemark(point);

        // Установка изображения метки
        ImageProvider imageProvider = ImageProvider.fromResource(
                this, resourceId);
        IconStyle iconStyle = new IconStyle();
        iconStyle.setAnchor(new PointF(0.5f, 1.0f));
        placemark.setIcon(imageProvider, iconStyle);

        // Установка подписи к абоненту на карте
        TextStyle textStyle = new TextStyle();
        textStyle.setPlacement(TextStyle.Placement.BOTTOM);
        placemark.setText(titlePlacemark, textStyle);
        placemark.setUserData(infoUser);

        // Устанавливаем слушателя нажатия на метку
        placemark.addTapListener(placemarkTapListener);

        return placemark;
    }


    /**
     * Добавление метки в коллекцию
     * @param placemarkCollection Коллекция меток-абонентов на карте
     * @param point Местоположение абонента
     * @param resourceId Иконка абонента на карте
     * @param titlePlacemark Надпись около абонента на карте
     * @param infoUser Информация об абоненте при нажатии на него
     */
    private void addPlacemarkInCollection(MapObjectCollection placemarkCollection, Point point,
                                          int resourceId, String titlePlacemark, String infoUser)
    {
        // Добавление метки на карту
        PlacemarkMapObject placemarkMapObject = placemarkCollection.addPlacemark(point);

        // Установка изображения метки
        ImageProvider imageProvider = ImageProvider.fromResource(
                this, resourceId);
        IconStyle iconStyle = new IconStyle();
        iconStyle.setAnchor(new PointF(0.5f, 1.0f));
        placemarkMapObject.setIcon(imageProvider, iconStyle);

        // Установка подписи к абоненту на карте
        TextStyle textStyle = new TextStyle();
        textStyle.setPlacement(TextStyle.Placement.BOTTOM);
        placemarkMapObject.setText(titlePlacemark, textStyle);
        placemarkMapObject.setUserData(infoUser);

        // Установка обработчика при нажатии на абонента
        placemarkMapObject.addTapListener(placemarkTapListener);
    }


    /**
     * Получение приближения карты
     * @return Степень приближение карты
     */
    private float getZoom()
    {
        return zoom;
    }


    /**
     * Установка приближения карты
     * @param zoom Степень приближения
     */
    private void setZoom(float zoom)
    {
        this.zoom = zoom;
    }


    /**
     * Обработчка нажатия на кнопку увеличения карты
     * @param view Нажатая кнопка
     */
    public void buttonPlusZoom_click(View view)
    {
        float currentZoom = mapView.getMap().getCameraPosition().getZoom() + deltaZoom;
        Animation animation = animationShort;

        if(currentZoom < minZoom)
        {
            currentZoom = minZoom;
            animation = animationLong;
        }

        mapView.getMap().move(new CameraPosition(
                        mapView.getMap().getCameraPosition().getTarget(),
                        currentZoom,
                        mapView.getMap().getCameraPosition().getAzimuth(),
                        mapView.getMap().getCameraPosition().getTilt()),
                animation,
                cameraCallback);
    }


    /**
     * Обработчка нажатия на кнопку уменьшения карты
     * @param view Нажатая кнопка
     */
    public void buttonMinusZoom_click(View view)
    {
        float currentZoom = mapView.getMap().getCameraPosition().getZoom() - deltaZoom;
        Animation animation = animationShort;

        if(currentZoom < minZoom)
        {
            currentZoom = minZoom;
            animation = animationLong;
        }

        mapView.getMap().move(new CameraPosition(
                        mapView.getMap().getCameraPosition().getTarget(),
                        currentZoom,
                        mapView.getMap().getCameraPosition().getAzimuth(),
                        mapView.getMap().getCameraPosition().getTilt()),
                animation,
                cameraCallback);
    }


    /**
     * Обработчка нажатия на кнопку поворота карты
     * @param view Нажатая кнопка
     */
    public void buttonRotate_click(View view)
    {
        mapView.getMap().move(new CameraPosition(
                        mapView.getMap().getCameraPosition().getTarget(),
                        mapView.getMap().getCameraPosition().getZoom(),
                        mapView.getMap().getCameraPosition().getAzimuth() + 90.0f,
                        mapView.getMap().getCameraPosition().getTilt()),
                animationLong,
                cameraCallback);
    }


    /**
     * Обработчка нажатия на кнопку поиска своего устройства
     * @param view Нажатая кнопка
     */
    public void buttonNavigation_click(View view)
    {
        if (userLocationLayer != null &&  userLocationLayer.cameraPosition() != null)
        {
            try
            {
                mapView.getMap().move(new CameraPosition(
                                userLocationLayer.cameraPosition().getTarget(),
                                mapView.getMap().getCameraPosition().getZoom(),
                                mapView.getMap().getCameraPosition().getAzimuth(),
                                mapView.getMap().getCameraPosition().getTilt()),
                        animationLong,
                        cameraCallback);
            }
            catch (NullPointerException e)
            {

            }
        }
    }



    /**
     * Симуляция движения абонента
     * @param placemark Метка абонента на карте
     */
    private void simulateMovePerson(PlacemarkMapObject placemark)
    {
        // Один градус широты = 111,66 км
        // Одна минута широты = 1,86 км
        // Одна секунда широты = 31,02 м

        // Один градус долготы = 19,39 км
        // Одна минута долготы = 0,32 км
        // Одна секунда долготы = 5,39 м

        Random random = new Random();
        // генерируем от -0,00050 до 0,00050
        double deltaLatitude = Math.round(((random.nextDouble() - 0.5) / 1000.0)
                * Math.pow(10, 6)) / Math.pow(10, 6);
        // генерируем от -0,002500 до 0,002500
        double deltaLongitude = Math.round(((random.nextDouble() - 0.5) / 200.0)
                * Math.pow(10, 6)) / Math.pow(10, 6);

        Point coords = placemark.getGeometry();
        placemark.setGeometry(new Point(
                coords.getLatitude() + deltaLatitude,
                coords.getLongitude() + deltaLongitude));
    }


    /**
     * Удаление абонента с карты
     * @param index Индекс абонента в списке
     */
    private void deletePersonOnMap(int index)
    {
        if(index < 0 || index >= placemarksArrayList.size())
            return;

        PlacemarkMapObject placemark = placemarksArrayList.get(index);
        placemark.setVisible(false);
        placemark.removeTapListener(placemarkTapListener);
        placemarksArrayList.remove(index);
    }


    /**
     * Удаление абонента с карты
     * @param placemark Метка абонента на карте
     */
    private void deletePersonOnMap(PlacemarkMapObject placemark)
    {
        if(placemark == null || placemarksArrayList.isEmpty()
                || !placemarksArrayList.contains(placemark))
            return;

        placemark.setVisible(false);
        placemark.removeTapListener(placemarkTapListener);
        placemarksArrayList.remove(placemark);
    }


    /**
     * Установка фокусировки на части экрана без UI
     */
    private void calculateFocusRectangle()
    {
        int rightPadding = imgbtnNavigator.getLayoutParams().width + imgbtnNavigator.getPaddingRight();
        mapView.getMapWindow().setFocusRect(new ScreenRect(
                new ScreenPoint(0.0f, 0.0f),
                new ScreenPoint(mapView.getMapWindow().width() - rightPadding,
                        mapView.getMapWindow().height())));
    }



    private void drawPath(Point prevPoint, Point currentPoint, double speed)
    {
        pointsPath = new ArrayList<Point>(2);
        pointsPath.add(prevPoint);
        pointsPath.add(currentPoint);

        polyline = new Polyline(pointsPath);

        polylineOnMap = mapView.getMap().getMapObjects().addPolyline(polyline);
        if (speed <= 6.0)
            polylineOnMap.setStrokeColor(ContextCompat.getColor(this, R.color.blue_path));
        else if (speed <= 25.0)
            polylineOnMap.setStrokeColor(ContextCompat.getColor(this, R.color.green_path));
        else
            polylineOnMap.setStrokeColor(ContextCompat.getColor(this, R.color.red_path));
    }


    private void drawPath(Point prevPoint, Point currentPoint, boolean isMine)
    {
        ArrayList<Point> pointsPath = new ArrayList<Point>(2);
        pointsPath.add(prevPoint);
        pointsPath.add(currentPoint);

        polyline = new Polyline(pointsPath);

        polylineOnMap = mapView.getMap().getMapObjects().addPolyline(polyline);
        polylineOnMap.setStrokeColor(ContextCompat.getColor(this, R.color.gray_path));
    }


    @Override
    public void onObjectReceived(String ip, String json)
    {
        GeopositionMessage geopositionUser = gson.fromJson(json, GeopositionMessage.class);
        addToLog(String.format(
                "Получен объект от %s:\n%s",
                ip,
                geopositionUser.toString()
        ));

        checkIpRecv(ip);

        if(!geopositionUser.ipAddress.equals(ip))
            geopositionUser.ipAddress = ip;

        if(geopositionUser.firstName.equals(geopositionMessage.firstName) &&
                geopositionUser.lastName.equals(geopositionMessage.lastName)&&
                !geopositionUser.ipAddress.equals(geopositionMessage.ipAddress))
        {
            geopositionMessage.ipAddress = geopositionUser.ipAddress;
        }

        addUserPosition(geopositionUser);
    }


    @Override
    public void onMessageReceived(String ip, String message)
    {
        addToLog("Получено от " + ip + ": " + message);

        checkIpRecv(ip);

        if(message.equals(ActionType.POSITION_USER.getDescription()))
        {
            if (isLocationDetermined)
                udpManager.sendObjectAsync(geopositionMessage, ip, UdpManager.UDP_PORT_MAP);
        }
    }


    @Override
    public void onResponseSent(boolean success, String ip)
    {
        addToLog("Ответ " + (success ? "доставлен" : "не доставлен") + " к " + ip);
    }


    private void addToLog(String text) {

        runOnUiThread(() -> Log.d("UDP_Activity", text));

    }


    private List<GeopositionMessage> initializeGeoUsers(NetworkUser[] array)
    {
        if (array == null || array.length == 0)
            return new ArrayList<GeopositionMessage>();

        List<GeopositionMessage> users = new ArrayList<GeopositionMessage>(array.length);
        for (int i = 0; i < array.length; i++)
        {
            users.add(new GeopositionMessage(array[i].getIpAddress(), array[i].getFirstName(),
                    array[i].getLastName(), 56.860707 + i * 0.00001,
                    40.508221 + i * 0.0002));
        }

        return users;
    }


    private void sendBroadcastSearch()
    {
        String msg = ActionType.SEARCH.getDescription();

        // Отправляем на broadcast адрес
        udpManager.sendResponseAsync(msg, getWifiBroadcastAddress(), UdpManager.UDP_PORT_MAP);
        udpManager.sendResponseAsync(msg, "192.168.1.255", UdpManager.UDP_PORT_MAP);
        udpManager.sendResponseAsync(msg, broadcastAddress, UdpManager.UDP_PORT_MAP);

        for (int i = 0; i < users.size(); i++)
        {
            udpManager.sendResponseAsync(msg, users.get(i).ipAddress, UdpManager.UDP_PORT_MAP);
        }

        addToLog("Отправлен запрос: " + msg);
    }


    private void sendBroadcastMyPosition()
    {
        if (isLocationDetermined)
        {
            // Отправляем на broadcast адрес
            udpManager.sendObjectAsync(geopositionMessage, getWifiBroadcastAddress(), UdpManager.UDP_PORT_MAP);
            udpManager.sendObjectAsync(geopositionMessage, "192.168.1.255", UdpManager.UDP_PORT_MAP);
            udpManager.sendObjectAsync(geopositionMessage, broadcastAddress, UdpManager.UDP_PORT_MAP);

            for (int i = 0; i < users.size(); i++)
            {
                udpManager.sendObjectAsync(geopositionMessage, users.get(i).ipAddress, UdpManager.UDP_PORT_MAP);
            }

            addToLog("Отправлен запрос: " + geopositionMessage);
        }
    }


    private void addUserPosition(GeopositionMessage user)
    {
        runOnUiThread(() ->
        {
            if (user.ipAddress != null && !user.ipAddress.trim().isEmpty() &&
                    user.firstName != null && !user.firstName.trim().isEmpty() &&
                    user.lastName != null && !user.lastName.trim().isEmpty() &&
                    !user.ipAddress.equals(geopositionMessage.ipAddress))
            {
                if (!users.contains(user))
                {
                    addNewUsers(user);
                    if (isLocationDetermined)
                    {
                        udpManager.sendObjectAsync(geopositionMessage, user.ipAddress,
                                UdpManager.UDP_PORT_MAP);
                    }
                }
                else
                {
                    updatePositionUser(user);
                }
            }
        });
    }


    private void addNewUsers(GeopositionMessage user)
    {
        users.add(user);
        placemarksArrayList.add(setPlacemarkOnMap(new Point(user.latitude, user.longitude),
                ColorsResourcesId[users.size() % ColorsResourcesId.length],
                user.firstName + " " + user.lastName,
                user.ipAddress + "\n" +
                        DeterminationDistance.getGeoLatitudeOnRussianFromGPS(user.latitude) + "\n" +
                        DeterminationDistance.getGeoLongitudeOnRussianFromGPS(user.longitude)));
    }


    private void updatePositionUser(GeopositionMessage user)
    {
        int index = users.indexOf(user);
        users.set(index, user);
        PlacemarkMapObject placemark = placemarksArrayList.get(index);
        Point firstPoint = placemark.getGeometry();
        placemark.setGeometry(new Point(user.latitude, user.longitude));
        Point secondPoint = placemark.getGeometry();
        drawPath(firstPoint, secondPoint, false);
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
}