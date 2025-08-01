package com.example.myapplicationvoice.userinformation;

import java.util.Objects;

/**
 * Класс, представляющий пользователя системы.
 * Содержит информацию об имени и фамилии пользователя.
 * Обеспечивает валидацию данных при установке значений.
 */
public class User
{
    /**
     * Имя пользователя. Не может быть null или пустым.
     */
    private String firstName;
    /**
     * Фамилия пользователя. Не может быть null или пустой.
     */
    private String lastName;


    /**
     * Конструктор по умолчанию.
     * Необходим для работы некоторых библиотек (например, Firebase, Gson).
     */
    public User()
    {
        // Пустой конструктор
    }


    /**
     * Конструктор с параметрами.
     *
     * @param firstName Имя пользователя. Не может быть null или пустым.
     * @param lastName  Фамилия пользователя. Не может быть null или пустой.
     * @throws IllegalArgumentException если имя или фамилия null или пустые.
     */
    public User(String firstName, String lastName)
    {
        setFirstName(firstName);
        setLastName(lastName);
    }


    /**
     * Возвращает имя пользователя.
     *
     * @return Имя пользователя.
     */
    public String getFirstName()
    {
        return firstName;
    }


    /**
     * Устанавливает имя пользователя.
     *
     * @param firstName Имя пользователя. Не может быть null или пустым.
     * @throws IllegalArgumentException если имя null или пустое.
     */
    public void setFirstName(String firstName)
    {
        if (firstName == null || firstName.trim().isEmpty())
        {
            throw new IllegalArgumentException("Имя не может быть пустым");
        }

        this.firstName = firstName.trim();
    }


    /**
     * Возвращает фамилию пользователя.
     *
     * @return Фамилия пользователя.
     */
    public String getLastName()
    {
        return lastName;
    }


    /**
     * Устанавливает фамилию пользователя.
     *
     * @param lastName Фамилия пользователя. Не может быть null или пустой.
     * @throws IllegalArgumentException если фамилия null или пустая.
     */
    public void setLastName(String lastName)
    {
        if (lastName == null || lastName.trim().isEmpty())
        {
            throw new IllegalArgumentException("Фамилия не может быть пустой");
        }

        this.lastName = lastName.trim();
    }


    /**
     * Сравнивает текущего пользователя с другим объектом.
     *
     * @param o Объект для сравнения.
     * @return true если объекты равны, false в противном случае.
     */
    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        User user = (User) o;
        boolean flag = Objects.equals(firstName, user.firstName) && Objects.equals(lastName, user.lastName);
        return flag;
    }


    /**
     * Возвращает хеш-код пользователя.
     *
     * @return Хеш-код, вычисленный на основе имени и фамилии.
     */
    @Override
    public int hashCode()
    {
        return Objects.hash(firstName, lastName);
    }


    /**
     * Возвращает строковое представление пользователя.
     *
     * @return Строка в формате "User{firstName='...', lastName='...'}".
     */
    @Override
    public String toString()
    {
        return "User{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                '}';
    }
}