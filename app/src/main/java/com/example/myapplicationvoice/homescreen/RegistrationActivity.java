package com.example.myapplicationvoice.homescreen;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplicationvoice.R;
import com.example.myapplicationvoice.userinformation.User;
import com.google.android.material.textfield.TextInputEditText;

public class RegistrationActivity extends AppCompatActivity
{
    // Ключи для передачи данных
    public static final String EXTRA_FIRST_NAME = "extra_first_name";
    public static final String EXTRA_LAST_NAME = "extra_last_name";

    private TextInputEditText editTextFirstName, editTextLastName;
    private Button btnRegister;
    private TextView textViewError;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        // Инициализация UI элементов
        editTextFirstName = findViewById(R.id.editTextFirstName);
        editTextLastName = findViewById(R.id.editTextLastName);
        btnRegister = findViewById(R.id.btnRegister);
        textViewError = findViewById(R.id.textViewError);

        // Обработчик нажатия кнопки регистрации
        btnRegister.setOnClickListener(v -> registerUser());
    }


    private void registerUser()
    {
        // Скрываем предыдущие сообщения
        textViewError.setVisibility(View.GONE);

        try
        {
            // Получаем данные из полей ввода
            String firstName = editTextFirstName.getText().toString().trim();
            String lastName = editTextLastName.getText().toString().trim();

            // Создаем пользователя (валидация происходит в классе User)
            User newUser = new User(firstName, lastName);

            // Если дошли до этого места - регистрация успешна
            //showSuccessMessage(newUser);

            navigateToUsersOnline(newUser);
        }
        catch (IllegalArgumentException e)
        {
            // Показываем ошибку валидации
            showErrorMessage(e.getMessage());
        }
    }


    private void showSuccessMessage(User user)
    {
        String successMessage = "Пользователь " + user.getFirstName() + " " +
                user.getLastName() + " зарегистрирован.";

        Toast.makeText(this, successMessage, Toast.LENGTH_SHORT).show();
    }


    private void showErrorMessage(String message)
    {
        textViewError.setText(message);
        textViewError.setVisibility(View.VISIBLE);

        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        // Закрывает все Activity приложения
        finishAffinity();
    }


    private void navigateToUsersOnline(User user)
    {
        Intent intent = new Intent(this, UsersOnlineActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.putExtra(EXTRA_FIRST_NAME, user.getFirstName());
        intent.putExtra(EXTRA_LAST_NAME, user.getLastName());
        startActivity(intent);
    }
}