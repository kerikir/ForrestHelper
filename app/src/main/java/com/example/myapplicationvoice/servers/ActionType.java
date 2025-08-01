package com.example.myapplicationvoice.servers;

public enum ActionType
{
    SEARCH("поиск"),
    CALL("звонок"),
    ACCEPT_CALL("принять звонок"),
    CANCEL_CALL("отклонить звонок"),
    MESSAGE("сообщение"),
    POSITION_USER("геолокация");

    private final String description;

    // Конструктор enum
    ActionType(String description) {
        this.description = description;
    }

    // Геттер для получения описания
    public String getDescription() {
        return description;
    }
}
