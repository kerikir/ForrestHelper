package com.example.myapplicationvoice.yandexmap;

import java.util.regex.Pattern;


/** Класс для определения расстояния между абонентами и скорости движения абонента */
public class DeterminationDistance
{
    /** Количество наносекунд в миллисекунд */
    public static final int MILLISECONDS_TO_NANOSECONDS = 1_000_000;
    /** Количество миллисекунд в секунде */
    public static final int SECONDS_TO_MILLISECONDS = 1000;
    /** Количество секунд в минуте */
    public static final int MINUTES_TO_SECONDS = 60;
    /** Количество минут в часе */
    public static final int HOURS_TO_MINUTES = 60;

    /** Количество сантиметров в метре */
    public static final int METERS_TO_CENTIMETERS = 100;
    /** Количество метров в километре */
    public static final int KILOMETERS_TO_METERS = 1000;

    /** Количество дюймов в футы */
    public static final int FEET_TO_INCHES = 12;
    /** Количество футов в ярде */
    public static final int YARDS_TO_FEET = 3;
    /** Количество ярд в миле */
    public static final int MILES_TO_YARDS = 1760;

    /** Количество сантиметров в дюйме */
    public static double INCHES_TO_CENTIMETERS = 2.54;
    /** Количество сантиметров в футе */
    public static double FEET_TO_CENTIMETERS = 30.48;
    /** Количество сантиметров в ярде */
    public static double YARDS_TO_CENTIMETERS = 91.44;
    /** Количество киллометров в миле */
    public static double MILES_TO_KILOMETERS = 1.609344;

    /** Широта абонента в прошлый момент времени */
    private double prevLatitude;
    /** Долгота абонента в прошлый момент времени */
    private double prevLongitude;

    /** Радиус земли */
    public static final double EARTH_RADIUS = 6363.5644;


    public DeterminationDistance()
    {

    }


    /**
     * Инициализация начального положения абонента
     * @param latitude Широта абонента в точке старта движения
     * @param longitude Долгота абонента в точке старта движения
     */
    public DeterminationDistance(double latitude, double longitude)
    {
        setPrevLatitude(latitude);
        setPrevLongitude(longitude);
    }


    /**
     * Инициализация начального положения абонента
     * @param latitude Широта абонента в точке старта движения
     * @param longitude Долгота абонента в точке старта движения
     */
    public void initializeStartPoint(double latitude, double longitude)
    {
        setPrevLatitude(latitude);
        setPrevLongitude(longitude);
    }


    /**
     * Установка начальной широты абонента
     * @param prevLatitude Широта абонента в прошлый момент времени
     */
    public void setPrevLatitude(double prevLatitude)
    {
        if (prevLatitude <= 90.0 && prevLatitude >= -90.0)
        {
            this.prevLatitude = prevLatitude;
        }
    }


    /**
     * Установка начальной долготы абонента
     * @param prevLongitude Долгота абонента в прошлый момент времени
     */
    public void setPrevLongitude(double prevLongitude)
    {
        if (prevLongitude <= 180.0 && prevLongitude >= -180.0)
        {
            this.prevLongitude = prevLongitude;
        }
    }


    /**
     * Получение широты абонента в прошлый момент времени
     * @return Широта абонента в прошлый момент времени
     */
    public double getPrevLatitude()
    {
        return prevLatitude;
    }


    /**
     * Получение долготы абонента в прошлый момент времени
     * @return Долгота абонента в прошлый момент времени
     */
    public double getPrevLongitude()
    {
        return prevLongitude;
    }




    /**
     * Перевод минут в часы
     * @param minutes Количество минут
     * @return Количество часов
     */
    public static double convertMinutesToHours(double minutes)
    {
        if (minutes < 0.0)
            return 0.0;

        return minutes / HOURS_TO_MINUTES;
    }


    /**
     * Перевод часов в минуты
     * @param hours Количество часов
     * @return Количество минут
     */
    public static double convertHoursToMinutes(double hours)
    {
        if (hours < 0.0)
            return 0.0;

        return hours * HOURS_TO_MINUTES;
    }


    /**
     * Перевод секунд в минуты
     * @param seconds Количество секунд
     * @return Количество минут
     */
    public static double convertSecondsToMinutes(double seconds)
    {
        if (seconds < 0.0)
            return 0.0;

        return seconds / MINUTES_TO_SECONDS;
    }


    /**
     * Перевод минут в секунды
     * @param minutes Количество минут
     * @return Количество секунд
     */
    public static double convertMinutesToSeconds(double minutes)
    {
        if (minutes < 0.0)
            return 0.0;

        return minutes * MINUTES_TO_SECONDS;
    }


    /**
     * Перевод миллисекунд в секунды
     * @param milliseconds Количество миллисекунд
     * @return Количество секунд
     */
    public static double convertMillisecondsToSeconds(int milliseconds)
    {
        if (milliseconds < 0)
            return 0.0;

        return (double)milliseconds / SECONDS_TO_MILLISECONDS;
    }


    /**
     * Перевод секунд в миллисекунды
     * @param seconds Количество секунд
     * @return Количество миллисекунд
     */
    public static int convertSecondsToMilliseconds(double seconds)
    {
        if (seconds < 0.0)
            return 0;

        return (int)Math.round(seconds * SECONDS_TO_MILLISECONDS);
    }


    /**
     * Перевод наносекунд в миллисекунды
     * @param nanoseconds Количество наносекунд
     * @return Количество миллисекунд
     */
    public static double convertNanosecondsToMilliseconds(long nanoseconds)
    {
        if (nanoseconds < 0)
            return 0.0;

        return (double)nanoseconds / MILLISECONDS_TO_NANOSECONDS;
    }


    /**
     * Перевод миллисекунд в наносекунды
     * @param milliseconds Количество миллисекунд
     * @return Количество наносекунд
     */
    public static long convertMillisecondsToNanoseconds(int milliseconds)
    {
        if (milliseconds < 0)
            return 0;

        return milliseconds * (long)MILLISECONDS_TO_NANOSECONDS;
    }


    /**
     * Перевод часов в секунды
     * @param hours Количество часов
     * @return Количество секунд
     */
    public static double convertHoursToSeconds(double hours)
    {
        if (hours < 0.0)
            return 0.0;

        return convertMinutesToSeconds(convertHoursToMinutes(hours));
    }


    /**
     * Перевод часов в миллисекунды
     * @param hours Количество часов
     * @return Количество миллисекунд
     */
    public static int convertHoursToMilliseconds(double hours)
    {
        if (hours < 0.0)
            return 0;

        return convertSecondsToMilliseconds(convertMinutesToSeconds(convertHoursToMinutes(hours)));
    }


    /**
     * Перевод часов в наносекунды
     * @param hours Количество часов
     * @return Количество наносекунд
     */
    public static long convertHoursToNanoseconds(double hours)
    {
        if (hours < 0.0)
            return 0;

        return convertMillisecondsToNanoseconds(convertSecondsToMilliseconds(
                convertMinutesToSeconds(convertHoursToMinutes(hours))));
    }


    /**
     * Перевод минут в миллисекунды
     * @param minutes Количество минут
     * @return Количество миллисекунд
     */
    public static int convertMinutesToMilliseconds(double minutes)
    {
        if (minutes < 0.0)
            return 0;

        return convertSecondsToMilliseconds(convertMinutesToSeconds(minutes));
    }


    /**
     * Перевод минут в наносекунды
     * @param minutes Количество минут
     * @return Количество наносекунд
     */
    public static long convertMinutesToNanoseconds(double minutes)
    {
        if (minutes < 0.0)
            return 0;

        return convertMillisecondsToNanoseconds(convertSecondsToMilliseconds(
                convertMinutesToSeconds(minutes)));
    }


    /**
     * Перевод секунд в часы
     * @param seconds Количество секунд
     * @return Количество часов
     */
    public static double convertSecondsToHours(double seconds)
    {
        if (seconds < 0.0)
            return 0.0;

        return convertSecondsToMinutes(seconds) / HOURS_TO_MINUTES;
    }


    /**
     * Перевод секунд в наносекунды
     * @param seconds Количество секунд
     * @return Количество наносекунд
     */
    public static long convertSecondsToNanoseconds(double seconds)
    {
        if (seconds < 0.0)
            return 0;

        return convertMillisecondsToNanoseconds(convertSecondsToMilliseconds(seconds));
    }


    /**
     * Перевод миллисекунд в минуты
     * @param milliseconds Количество миллисекунд
     * @return Количество минут
     */
    public static double convertMillisecondsToMinutes(int milliseconds)
    {
        if (milliseconds < 0)
            return 0.0;

        return convertMillisecondsToSeconds(milliseconds) / MINUTES_TO_SECONDS;
    }


    /**
     * Перевод миллисекунд в часы
     * @param milliseconds Количество миллисекунд
     * @return Количество часов
     */
    public static double convertMillisecondsToHours(int milliseconds)
    {
        if (milliseconds < 0)
            return 0.0;

        return convertMillisecondsToMinutes(milliseconds) / HOURS_TO_MINUTES;
    }


    /**
     * Перевод наносекунд в секунды
     * @param nanoseconds Количество наносекунд
     * @return Количество секунд
     */
    public static double convertNanosecondsToSeconds(long nanoseconds)
    {
        if (nanoseconds < 0)
            return 0.0;

        return convertNanosecondsToMilliseconds(nanoseconds) / SECONDS_TO_MILLISECONDS;
    }


    /**
     * Перевод наносекунд в минуты
     * @param nanoseconds Количество наносекунд
     * @return Количество минут
     */
    public static double convertNanosecondsToMinutes(long nanoseconds)
    {
        if (nanoseconds < 0)
            return 0.0;

        return convertNanosecondsToSeconds(nanoseconds) / MINUTES_TO_SECONDS;
    }


    /**
     * Перевод наносекунд в часы
     * @param nanoseconds Количество наносекунд
     * @return Количество часов
     */
    public static double convertNanosecondsToHours(long nanoseconds)
    {
        if (nanoseconds < 0)
            return 0.0;

        return convertNanosecondsToMinutes(nanoseconds) / HOURS_TO_MINUTES;
    }




    /**
     * Перевод километров в метры
     * @param kilometers Количество километров
     * @return Количество метров
     */
    public static double convertKilometersToMeters(double kilometers)
    {
        if (kilometers < 0.0)
            return 0.0;

        return kilometers * KILOMETERS_TO_METERS;
    }


    /**
     * Перевод метров в километры
     * @param meters Количество метров
     * @return Количество километров
     */
    public static double convertMetersToKilometers(double meters)
    {
        if (meters < 0.0)
            return 0.0;

        return meters / KILOMETERS_TO_METERS;
    }


    /**
     * Перевод метров в сантиметры
     * @param meters Количество метров
     * @return Количество сантиметров
     */
    public static int convertMetersToCentimeters(double meters)
    {
        if (meters < 0.0)
            return 0;

        return (int)Math.ceil(meters * METERS_TO_CENTIMETERS);
    }


    /**
     * Перевод сантиметров в метры
     * @param centimeters Количество сантиметров
     * @return Количество метров
     */
    public static double convertCentimetersToMeters(int centimeters)
    {
        if (centimeters < 0)
            return 0.0;

        return (double)centimeters / METERS_TO_CENTIMETERS;
    }


    /**
     * Перевод километров в сантиметры
     * @param kilometers Количество километров
     * @return Количество сантиметров
     */
    public static int convertKilometersToCentimeters(double kilometers)
    {
        if (kilometers < 0.0)
            return 0;

        return convertMetersToCentimeters(convertKilometersToMeters(kilometers));
    }


    /**
     * Перевод сантиметров в километры
     * @param centimeters Количество сантиметров
     * @return Количество километров
     */
    public static double convertCentimetersToKilometers(int centimeters)
    {
        if (centimeters < 0)
            return 0.0;

        return convertMetersToKilometers(convertCentimetersToMeters(centimeters));
    }


    /**
     * Перевод сантиметров в дюймы
     * @param centimeters Количество сантиметров
     * @return Количество дюймов
     */
    public static int convertCentimetersToInches(int centimeters)
    {
        if (centimeters < 0)
            return 0;

        return (int)Math.ceil(centimeters / INCHES_TO_CENTIMETERS);
    }


    /**
     * Перевод дюйм в сантиметры
     * @param inches Количество дюйм
     * @return Количество сантиметров
     */
    public static int convertInchesToCentimeters(int inches)
    {
        if (inches < 0)
            return 0;

        return (int)Math.ceil(inches * INCHES_TO_CENTIMETERS);
    }


    /**
     * Перевод сантиметров в футы
     * @param centimeters Количество сантиметров
     * @return Количество футов
     */
    public static int convertCentimetersToFeet(int centimeters)
    {
        if (centimeters < 0)
            return 0;

        return (int)Math.ceil(centimeters / FEET_TO_CENTIMETERS);
    }


    /**
     * Перевод футов в сантиметры
     * @param feet Количество футов
     * @return Количество сантиметров
     */
    public static int convertFeetToCentimeters(int feet)
    {
        if (feet < 0)
            return 0;

        return (int)Math.ceil(feet * FEET_TO_CENTIMETERS);
    }


    /**
     * Перевод сантиметров в ярды
     * @param centimeters Количество сантиметров
     * @return Количество ярдов
     */
    public static double convertCentimetersToYards(int centimeters)
    {
        if (centimeters < 0)
            return 0.0;

        return centimeters / YARDS_TO_CENTIMETERS;
    }


    /**
     * Перевод ярдов в сантиметры
     * @param yards Количество ярдов
     * @return Количество сантиметров
     */
    public static int convertYardsToCentimeters (double yards)
    {
        if (yards < 0.0)
            return 0;

        return (int)Math.ceil(yards * YARDS_TO_CENTIMETERS);
    }


    /**
     * Перевод километров в мили
     * @param kilometers Количество километров
     * @return Количество миль
     */
    public static double convertKilometersToMiles(double kilometers)
    {
        if (kilometers < 0.0)
            return 0.0;

        return kilometers / MILES_TO_KILOMETERS;
    }


    /**
     * Перевод миль в километры
     * @param miles Количество миль
     * @return Количество километров
     */
    public static double convertMilesToKilometers(double miles)
    {
        if (miles < 0.0)
            return 0.0;

        return miles * MILES_TO_KILOMETERS;
    }


    /**
     * Перевод дюймов в футы
     * @param inches Количество дюймов
     * @return Количество футов
     */
    public static int convertInchesToFeet(int inches)
    {
        if (inches < 0)
            return 0;

        return (int)Math.round((double)inches / FEET_TO_INCHES);
    }


    /**
     * Перевод футов в дюймы
     * @param feet Количество футов
     * @return Количество дюймов
     */
    public static int convertFeetToInches(int feet)
    {
        if (feet < 0)
            return 0;

        return (int)Math.ceil(feet * FEET_TO_INCHES);
    }


    /**
     * Перевод фут в ярды
     * @param feet Количество фут
     * @return Количество ярдов
     */
    public static double convertFeetToYards(int feet)
    {
        if (feet < 0)
            return 0.0;

        return (double)feet / YARDS_TO_FEET;
    }


    /**
     * Перевод ярдов в футы
     * @param yards Количество ярдов
     * @return Количество фут
     */
    public static int convertYardsToFeet(double yards)
    {
        if (yards < 0.0)
            return 0;

        return (int)Math.ceil(yards * YARDS_TO_FEET);
    }


    /**
     * Перевод ярд в мили
     * @param yards Количество ярд
     * @return Количество миль
     */
    public static double convertYardsToMiles(double yards)
    {
        if (yards < 0.0)
            return 0.0;

        return yards / MILES_TO_YARDS;
    }


    /**
     * Перевод миль в ярды
     * @param miles Количество миль
     * @return Количество ярд
     */
    public static double convertMilesToYards(double miles)
    {
        if (miles < 0.0)
            return 0.0;

        return miles * MILES_TO_YARDS;
    }


    /**
     * Перевод километров в ярды
     * @param kilometers Количество километров
     * @return Количество ярд
     */
    public static double convertKilometersToYards(double kilometers)
    {
        if (kilometers < 0.0)
            return 0.0;

        return convertMilesToYards(convertKilometersToMiles(kilometers));
    }


    /**
     * Перевод километров в футы
     * @param kilometers Количество километров
     * @return Количество фут
     */
    public static int convertKilometersToFeet(double kilometers)
    {
        if (kilometers < 0.0)
            return 0;

        return convertYardsToFeet(convertMilesToYards(convertKilometersToMiles(kilometers)));
    }


    /**
     * Перевод километров в дюймы
     * @param kilometers Количество километров
     * @return Количество дюйм
     */
    public static int convertKilometersToInches(double kilometers)
    {
        if (kilometers < 0.0)
            return 0;

        return convertFeetToInches(convertYardsToFeet(convertMilesToYards(
                convertKilometersToMiles(kilometers))));
    }


    /**
     * Перевод метров в мили
     * @param meters Количество метров
     * @return Количество миль
     */
    public static double convertMetersToMiles(double meters)
    {
        if (meters < 0.0)
            return 0.0;

        return convertKilometersToMiles(convertMetersToKilometers(meters));
    }


    /**
     * Перевод метров в ярды
     * @param meters Количество метров
     * @return Количество ярд
     */
    public static double convertMetersToYards(double meters)
    {
        if (meters < 0.0)
            return 0.0;

        return convertMilesToYards(convertMetersToMiles(meters));
    }


    /**
     * Перевод метров в футы
     * @param meters Количество метров
     * @return Количество фут
     */
    public static int convertMetersToFeet(double meters)
    {
        if (meters < 0.0)
            return 0;

        return convertYardsToFeet(convertMetersToYards(meters));
    }


    /**
     * Перевод метров в дюймы
     * @param meters Количество метров
     * @return Количество дюйм
     */
    public static int convertMetersToInches(double meters)
    {
        if (meters < 0.0)
            return 0;

        return convertFeetToInches(convertMetersToFeet(meters));
    }


    /**
     * Перевод сантиметров в мили
     * @param centimeters Количество сантиметров
     * @return Количество миль
     */
    public static double convertCentimetersToMiles(int centimeters)
    {
        if (centimeters < 0)
            return 0.0;

        return convertKilometersToMiles(convertCentimetersToKilometers(centimeters));
    }


    /**
     * Перевод миль в метры
     * @param miles Количество миль
     * @return Количество метров
     */
    public static double convertMilesToMeters(double miles)
    {
        if (miles < 0.0)
            return 0.0;

        return convertKilometersToMeters(convertMilesToKilometers(miles));
    }


    /**
     * Перевод миль в сантиметры
     * @param miles Количество миль
     * @return Количество сантиметров
     */
    public static int convertMilesToCentimeters(double miles)
    {
        if (miles < 0.0)
            return 0;

        return convertMetersToCentimeters(convertMilesToMeters(miles));
    }


    /**
     * Перевод миль в футы
     * @param miles Количество миль
     * @return Количество фут
     */
    public static int convertMilesToFeet(double miles)
    {
        if (miles < 0.0)
            return 0;

        return convertYardsToFeet(convertMilesToYards(miles));
    }


    /**
     * Перевод миль в дюймы
     * @param miles Количество миль
     * @return Количество дюймов
     */
    public static int convertMilesToInches(double miles)
    {
        if (miles < 0.0)
            return 0;

        return convertFeetToInches(convertMilesToFeet(miles));
    }


    /**
     * Перевод ярд в километры
     * @param yards Количество ярд
     * @return Количество километров
     */
    public static double convertYardsToKilometers(double yards)
    {
        if (yards < 0.0)
            return 0.0;

        return convertMilesToKilometers(convertYardsToMiles(yards));
    }


    /**
     * Перевод ярд в метры
     * @param yards Количество ярд
     * @return Количество метров
     */
    public static double convertYardsToMeters(double yards)
    {
        if (yards < 0.0)
            return 0.0;

        return convertKilometersToMeters(convertYardsToKilometers(yards));
    }


    /**
     * Перевод ярд в дюймы
     * @param yards Количество ярд
     * @return Количество дюйм
     */
    public static int convertYardsToInches(double yards)
    {
        if (yards < 0.0)
            return 0;

        return convertFeetToInches(convertYardsToFeet(yards));
    }


    /**
     * Перевод фут в километры
     * @param feet Количество фут
     * @return Количество километров
     */
    public static double convertFeetToKilometers(int feet)
    {
        if (feet < 0)
            return 0.0;

        return convertYardsToKilometers(convertFeetToYards(feet));
    }


    /**
     * Перевод фут в метры
     * @param feet Количество фут
     * @return Количество метров
     */
    public static double convertFeetToMeters(int feet)
    {
        if (feet < 0)
            return 0.0;

        return convertYardsToMeters(convertFeetToYards(feet));
    }


    /**
     * Перевод фут в мили
     * @param feet Количество фут
     * @return Количество миль
     */
    public static double convertFeetToMiles(int feet)
    {
        if (feet < 0)
            return 0.0;

        return convertYardsToMiles(convertFeetToYards(feet));
    }


    /**
     * Перевод дюйм в километры
     * @param inches Количество дюйм
     * @return Количество километров
     */
    public static double convertInchesToKilometers(int inches)
    {
        if (inches < 0)
            return 0.0;

        return convertCentimetersToKilometers(convertInchesToCentimeters(inches));
    }


    /**
     * Перевод дюйм в метры
     * @param inches Количество дюйм
     * @return Количество метров
     */
    public static double convertInchesToMeters(int inches)
    {
        if (inches < 0)
            return 0.0;

        return convertCentimetersToMeters(convertInchesToCentimeters(inches));
    }


    /**
     * Перевод дюйм в мили
     * @param inches Количество дюйм
     * @return Количество миль
     */
    public static double convertInchesToMiles(int inches)
    {
        if (inches < 0)
            return 0.0;

        return convertFeetToMiles(convertInchesToFeet(inches));
    }


    /**
     * Перевод дюйм в ярды
     * @param inches Количество дюйм
     * @return Количество ярд
     */
    public static double convertInchesToYards(int inches)
    {
        if (inches < 0)
            return 0.0;

        return convertFeetToYards(convertInchesToFeet(inches));
    }




    /**
     * Округление числа до ближайшего с указанием точности
     * @param value Округляемое число
     * @param accuracy Точность округления
     * @return Округленное число
     */
    public static double roundingDistance(double value, int accuracy)
    {
        if (accuracy < 0)
            return 0.0;

        double scale = Math.pow(10.0, accuracy);
        double result = Math.round(value * scale) / scale;
        return result;
    }


    /**
     * Расчет расстояния, пройденное абонентом
     * @param latitude Широта пользователя в текущий момент времени
     * @param longitude Долгота пользователя в текущий момент времени
     * @return Пройденное расстояние пользователем в км
     */
    public double calculateDistance(double latitude, double longitude)
    {
        if (latitude > 90.0 || latitude < -90.0 || longitude > 180.0 || longitude < -180.0)
            return 0.0;

        double deltaLatitude = Math.toRadians(latitude - getPrevLatitude());
        double deltaLongitude = Math.toRadians(longitude - getPrevLongitude());

        // Формула гаверсинуса
        double a = Math.sin(deltaLatitude / 2.0) * Math.sin(deltaLatitude / 2.0) +
                Math.cos(Math.toRadians(getPrevLatitude()) * Math.cos(Math.toRadians(latitude))) *
                        Math.sin(deltaLongitude / 2.0) * Math.sin(deltaLongitude / 2.0);

        double c = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a));

        return EARTH_RADIUS * c;
    }


    /**
     * Расчет скорости движения пользователя
     * @param latitude Широта пользователя в текущий момент времени
     * @param longitude Долгота пользователя в текущий момент времени
     * @param time Прошедшее время в мс между измерениями
     * @return Скорость пользователя в м/с
     */
    public double calculateSpeed(double latitude, double longitude, long time)
    {
        if (latitude > 90.0 || latitude < -90.0 || longitude > 180.0 || longitude < -180.0)
            return 0.0;

        double seconds = convertMillisecondsToSeconds((int)time);
        double distance = convertKilometersToMeters(calculateDistance(latitude, longitude));

        timeStep(latitude, longitude);

        double speed = distance / seconds;
        return speed;
    }


    /**
     * Шаг по времени - смена начальной точки абонента
     * @param latitude Широта пользователя в текущий момент времени
     * @param longitude Долгота пользователя в текущий момент времени
     */
    private void timeStep(double latitude, double longitude)
    {
        setPrevLatitude(latitude);
        setPrevLongitude(longitude);
    }


    /**
     * Перевод скорости из м/с в км/ч
     * @param ms Скорость абонента в м/с
     * @return Скорость абонента в км/ч
     */
    public static double convertMsToKmh(double ms)
    {
        if(ms < 0.0)
            return 0.0;

        return convertMetersToKilometers(ms) * MINUTES_TO_SECONDS * HOURS_TO_MINUTES;
    }


    /**
     * Перевод скорости из км/ч в м/с
     * @param kmh Скорость абонента в км/ч
     * @return Скорость абонента в м/с
     */
    public static double convertKmhToMs(double kmh)
    {
        if(kmh < 0.0)
            return 0.0;

        return convertKilometersToMeters(kmh) / (MINUTES_TO_SECONDS * HOURS_TO_MINUTES);
    }




    /**
     * Перевод географических координат северной широты в GPS
     * @param northLatitude Градусы северной широты
     * @return Широта GPS
     */
    public static double convertNorthLatitudeToGlobalLatitude(double northLatitude)
    {
        if (northLatitude < 0.0 || northLatitude > 90.0)
        {
            return Double.NaN;
        }

        return northLatitude;
    }


    /**
     * Перевод географических координат южной широты в GPS
     * @param southLatitude Градусы южной широты
     * @return Широта GPS
     */
    public static double convertSouthLatitudeToGlobalLatitude(double southLatitude)
    {
        if (southLatitude < 0.0 || southLatitude > 90.0)
        {
            return Double.NaN;
        }

        double koef = -1.0;
        if (southLatitude == 0.0)
            koef = 1.0;

        return koef * southLatitude;
    }


    /**
     * Перевод GPS широты в географические координаты северной широты
     * @param latitude Широта GPS
     * @return Градусы северной широты
     */
    public static double convertGlobalLatitudeToNorthLatitude(double latitude)
    {
        if (latitude < 0.0 || latitude > 90.0)
        {
            return Double.NaN;
        }

        return latitude;
    }


    /**
     * Перевод GPS широты в географические координаты южной широты
     * @param latitude Широта GPS
     * @return Градусы южной широты
     */
    public static double convertGlobalLatitudeToSouthLatitude(double latitude)
    {
        if (latitude < -90.0 || latitude > 0.0)
        {
            return Double.NaN;
        }

        double koef = -1.0;
        if (latitude == 0.0)
            koef = 1.0;

        return koef * latitude;
    }


    /**
     * Перевод географических координат восточной долготы в GPS
     * @param eastLongitude Градусы восточной долготы
     * @return Долгота GPS
     */
    public static double convertEastLongitudeToGlobalLongitude(double eastLongitude)
    {
        if (eastLongitude < 0.0 || eastLongitude > 180.0)
        {
            return Double.NaN;
        }

        return eastLongitude;
    }


    /**
     * Перевод географических координат западной долготы в GPS
     * @param westLongitude Градусы западной долготы
     * @return Долгота GPS
     */
    public static double convertWestLongitudeToGlobalLongitude(double westLongitude)
    {
        if (westLongitude < 0.0 || westLongitude > 180.0)
        {
            return Double.NaN;
        }

        double koef = -1.0;
        if (westLongitude == 0.0)
            koef = 1.0;

        return koef * westLongitude;
    }


    /**
     * Перевод долготы GPS в географических координат западной долготы
     * @param longitude Долгота GPS
     * @return Градусы западной долготы
     */
    public static double convertGlobalLongitudeToWestLongitude(double longitude)
    {
        if (longitude < -180.0 || longitude > 0.0)
        {
            return Double.NaN;
        }

        double koef = -1.0;
        if (longitude == 0.0)
            koef = 1.0;

        return koef * longitude;
    }


    /**
     * Перевод долготы GPS в географических координат восточной долготы
     * @param longitude Долгота GPS
     * @return Градусы восточной долготы
     */
    public static double convertGlobalLongitudeToEastLongitude(double longitude)
    {
        if (longitude < 0.0 || longitude > 180.0)
        {
            return Double.NaN;
        }

        return longitude;
    }


    /**
     * Перевод временного представления точки на карте в GPS
     * @param degree Угол
     * @param minutes Количество минут
     * @param seconds Количество секунд
     * @return Дробное значение угла без минут и секунд
     */
    private static double convertGeoDegreeToGeoTimeRepresentation(int degree, int minutes, double seconds)
    {
        double result = degree + ((double) minutes / HOURS_TO_MINUTES) +
                (seconds / (HOURS_TO_MINUTES * MINUTES_TO_SECONDS));

        return result;
    }


    /**
     * Перевод временного представления южной широты (в минутах и секундах) в градусное представление GPS
     * @param degree Угол
     * @param minutes Количество минут
     * @param seconds Количество секунд
     * @return GPS южной широты
     */
    public static double convertSouthGeoLatitudeInTemporaryRepresentationToGeoLatitudeInDegree
    (int degree, int minutes, double seconds)
    {
        if (degree > 90 || degree < 0 || minutes >= 60 || minutes < 0 || seconds < 0.0 || seconds >= 60.0)
            return Double.NaN;

        double result = convertGeoDegreeToGeoTimeRepresentation(degree, minutes, seconds);

        return convertSouthLatitudeToGlobalLatitude(result);
    }


    /**
     * Перевод временного представления северной широты (в минутах и секундах) в градусное представление GPS
     * @param degree Угол
     * @param minutes Количество минут
     * @param seconds Количество секунд
     * @return GPS северной широты
     */
    public static double convertNorthGeoLatitudeInTemporaryRepresentationToGeoLatitudeInDegree
    (int degree, int minutes, double seconds)
    {
        if (degree > 90 || degree < 0 || minutes >= 60 || minutes < 0 || seconds < 0.0 || seconds >= 60.0)
            return Double.NaN;

        double result = convertGeoDegreeToGeoTimeRepresentation(degree, minutes, seconds);

        return convertNorthLatitudeToGlobalLatitude(result);
    }


    /**
     * Перевод временного представления западной долготы (в минутах и секундах) в градусное представление GPS
     * @param degree Угол
     * @param minutes Количество минут
     * @param seconds Количество секунд
     * @return GPS западной долготы
     */
    public static double convertWestGeoLongitudeInTemporaryRepresentationToGeoLongitudeInDegree
    (int degree, int minutes, double seconds)
    {
        if (degree > 180 || degree < 0 || minutes >= 60 || minutes < 0 || seconds < 0.0 || seconds >= 60.0)
            return Double.NaN;

        double result = convertGeoDegreeToGeoTimeRepresentation(degree, minutes, seconds);

        return convertWestLongitudeToGlobalLongitude(result);
    }


    /**
     * Перевод временного представления восточной долготы (в минутах и секундах) в градусное представление GPS
     * @param degree Угол
     * @param minutes Количество минут
     * @param seconds Количество секунд
     * @return GPS восточной долготы
     */
    public static double convertEastGeoLongitudeInTemporaryRepresentationToGeoLongitudeInDegree
    (int degree, int minutes, double seconds)
    {
        if (degree > 180 || degree < 0 || minutes >= 60 || minutes < 0 || seconds < 0.0 || seconds >= 60.0)
            return Double.NaN;

        double result = convertGeoDegreeToGeoTimeRepresentation(degree, minutes, seconds);

        return convertEastLongitudeToGlobalLongitude(result);
    }


    /**
     * Получение угла временного представления координаты пользователя из данных GPS
     * @param gps Данные GPS
     * @return Угол временного представления местоположения
     */
    private static int getGeoDegreeFromGPS(double gps)
    {
        int degree = (int) gps;
        return degree;
    }


    /**
     * Получение минут временного представления координаты пользователя из данных GPS
     * @param gps Данные GPS
     * @return Минута временного представления местоположения
     */
    private static int getGeoMinutesFromGPS(double gps)
    {
        double minutesTemp = gps - getGeoDegreeFromGPS(gps);
        minutesTemp *= 60.0;
        int minutes = (int) minutesTemp;
        return minutes;
    }


    /**
     * Получение секунд временного представления координаты пользователя из данных GPS
     * @param gps Данные GPS
     * @return Секунда временного представления местоположения
     */
    private static double getGeoSecondsFromGPS(double gps)
    {
        double minutes = gps - getGeoDegreeFromGPS(gps);
        minutes *= 60.0;
        double seconds = minutes - getGeoMinutesFromGPS(gps);
        seconds *= 60.0;
        return seconds;
    }


    /**
     * Получение временного представления координаты пользователя из данных GPS
     * @param gps Данные GPS
     * @return Временное представление местоположения
     */
    private static String getGeoPositionFromGPS(double gps)
    {
        int degree = getGeoDegreeFromGPS(gps);
        int minutes = getGeoMinutesFromGPS(gps);
        double seconds = getGeoSecondsFromGPS(gps);

        String format = String.format("%d° %d' %.2f\"", degree, minutes, seconds);
        format = format.replace(',', '.');
        return format;
    }


    /**
     * Получение временного представления широты из данных GPS.
     * Если входные данные не корректны, возвратится пустая строка.
     * @param gps Данные GPS
     * @return Временное представление широты
     */
    public static String getGeoLatitudeFromGPS(double gps)
    {
        if (gps < -90.0 || gps > 90.0 || Double.isNaN(gps))
            return "";

        boolean isNegative;
        if (gps >= 0.0)
        {
            isNegative = false;
        }
        else
        {
            isNegative = true;
            gps *= -1.0;
        }

        String geoData = getGeoPositionFromGPS(gps);

        if (!isNegative)
            geoData += " N";
        else
            geoData += " S";

        return geoData;
    }


    /**
     * Получение временного представления долготы из данных GPS.
     * Если входные данные не корректны, возвратится пустая строка.
     * @param gps Данные GPS
     * @return Временное представление долготы
     */
    public static String getGeoLongitudeFromGPS(double gps)
    {
        if (gps < -180.0 || gps > 180.0 || Double.isNaN(gps))
            return "";

        boolean isNegative;
        if (gps >= 0.0)
        {
            isNegative = false;
        }
        else
        {
            isNegative = true;
            gps *= -1.0;
        }

        String geoData = getGeoPositionFromGPS(gps);

        if (!isNegative)
            geoData += " E";
        else
            geoData += " W";

        return geoData;
    }


    /**
     * Получение временного представления широты из данных GPS.
     * Если входные данные не корректны, возвратится пустая строка.
     * @param gps Данные GPS
     * @return Временное представление широты на русском языке
     */
    public static String getGeoLatitudeOnRussianFromGPS(double gps)
    {
        String geoData = getGeoLatitudeFromGPS(gps);
        geoData = geoData.replace("N", "с.ш.");
        geoData = geoData.replace("S", "ю.ш.");
        return geoData;
    }


    /**
     * Получение временного представления долготы из данных GPS.
     * Если входные данные не корректны, возвратится пустая строка.
     * @param gps Данные GPS
     * @return Временное представление долготы на русском языке
     */
    public static String getGeoLongitudeOnRussianFromGPS(double gps)
    {
        String geoData = getGeoLongitudeFromGPS(gps);
        geoData = geoData.replace("W", "з.д.");
        geoData = geoData.replace("E", "в.д.");
        return geoData;
    }


    /**
     * Проверка на корректность введенных географических координат широты во временном представлении
     * @param geoData Временное представление географических координат широты
     * @return Корректны ли данные
     */
    public static boolean isValidGeoLatitudeData(String geoData)
    {
        boolean isValid = false;
        String regex = "^\\d{1,2}°\\s{0,3}\\d{1,2}'\\s{0,3}\\d{1,2}(\\.\\d{1,8}|)\"\\s{0,3}";

        if (Pattern.matches(regex + "S$", geoData))
            isValid = true;

        if (Pattern.matches(regex + "N$", geoData))
            isValid = true;

        if (Pattern.matches(regex + "с.ш.$", geoData))
            isValid = true;

        if (Pattern.matches(regex + "ю.ш.$", geoData))
            isValid = true;

        return isValid;
    }


    /**
     * Проверка на корректность введенных географических координат долготы во временном представлении
     * @param geoData Временное представление географических координат долготы
     * @return Корректны ли данные
     */
    public static boolean isValidGeoLongitudeData(String geoData)
    {
        boolean isValid = false;
        String regex = "^\\d{1,3}°\\s{0,3}\\d{1,2}'\\s{0,3}\\d{1,2}(\\.\\d{1,8}|)\"\\s{0,3}";

        if (Pattern.matches(regex + "W$", geoData))
            isValid = true;

        if (Pattern.matches(regex + "E$", geoData))
            isValid = true;

        if (Pattern.matches(regex + "в.д.$", geoData))
            isValid = true;

        if (Pattern.matches(regex + "з.д.$", geoData))
            isValid = true;

        return isValid;
    }


    /**
     * Получение угла из гео данных в временном представлении
     * @param geoData Временное представление географических координат
     * @return Величина угла
     */
    private static int getDegreeFromGeoData(String geoData)
    {
        int indexEnd = geoData.indexOf('°');
        String degreeString = geoData.substring(0, indexEnd);
        degreeString = degreeString.trim();

        int degree = Integer.parseInt(degreeString);
        return degree;
    }


    /**
     * Получение минут из гео данных в временном представлении
     * @param geoData Временное представление географических координат
     * @return Количество минут
     */
    private static int getMinutesFromGeoData(String geoData)
    {
        int indexStart = geoData.indexOf('°') + 1;
        int indexEnd = geoData.indexOf('\'');
        String minutesString = geoData.substring(indexStart, indexEnd);
        minutesString = minutesString.trim();

        int minutes = Integer.parseInt(minutesString);
        return minutes;
    }


    /**
     * Получение секунд из гео данных в временном представлении
     * @param geoData Временное представление географических координат
     * @return Количество секунд
     */
    private static double getSecondsFromGeoData(String geoData)
    {
        int indexStart = geoData.indexOf('\'') + 1;
        int indexEnd = geoData.indexOf('"');
        String secondsString = geoData.substring(indexStart, indexEnd);
        secondsString = secondsString.trim();

        double seconds = Double.parseDouble(secondsString);
        return seconds;
    }


    /**
     * Получение широты GPS из гео данных временного представления.
     * Если данные введены не корректны, вернется NaN.
     * @param geoData Временное представление географических координат широты
     * @return Широта GPS
     */
    public static double getGPSFromGeoLatitude(String geoData)
    {
        geoData = geoData.trim();
        boolean isValid = isValidGeoLatitudeData(geoData);

        if (!isValid)
            return Double.NaN;

        int degree = getDegreeFromGeoData(geoData);
        int minutes = getMinutesFromGeoData(geoData);
        double seconds = getSecondsFromGeoData(geoData);

        if (geoData.endsWith("N") || geoData.endsWith("с.ш."))
            return convertNorthGeoLatitudeInTemporaryRepresentationToGeoLatitudeInDegree(
                    degree, minutes, seconds);
        if (geoData.endsWith("S") || geoData.endsWith("ю.ш."))
            return convertSouthGeoLatitudeInTemporaryRepresentationToGeoLatitudeInDegree(
                    degree, minutes, seconds);
        else return Double.NaN;
    }


    /**
     * Получение долготы GPS из гео данных временного представления.
     * Если данные введены не корректны, вернется NaN.
     * @param geoData Временное представление географических координат долготы
     * @return Долгота GPS
     */
    public static double getGPSFromGeoLongitude(String geoData)
    {
        geoData = geoData.trim();
        boolean isValid = isValidGeoLongitudeData(geoData);

        if (!isValid)
            return Double.NaN;

        int degree = getDegreeFromGeoData(geoData);
        int minutes = getMinutesFromGeoData(geoData);
        double seconds = getSecondsFromGeoData(geoData);

        if (geoData.endsWith("W") || geoData.endsWith("з.д."))
            return convertWestGeoLongitudeInTemporaryRepresentationToGeoLongitudeInDegree(
                    degree, minutes, seconds);
        if (geoData.endsWith("E") || geoData.endsWith("в.д."))
            return convertEastGeoLongitudeInTemporaryRepresentationToGeoLongitudeInDegree(
                    degree, minutes, seconds);
        else return Double.NaN;
    }
}