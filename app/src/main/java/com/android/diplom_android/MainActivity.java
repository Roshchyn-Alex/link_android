package com.android.diplom_android;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private DataBase dataBase;
    private ListView listView;
    private ArrayAdapter<String> arrayAdapter;
    private EditText fullLinkText, shortLinkText;
    private Button addButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        fullLinkText = findViewById(R.id.full_link);
        shortLinkText = findViewById(R.id.short_link);
        addButton = findViewById(R.id.add_button);
        dataBase = new DataBase(this);
        listView = findViewById(R.id.task_list);

//        обработчик нажатия на кнопку
        addButton.setOnClickListener(v -> {
            String fullLink = fullLinkText.getText().toString();
            String shortLink = shortLinkText.getText().toString();
            if (!fullLink.contains(".")) {
                showError("Неверная ссылка");
                Toast.makeText(
                        getApplicationContext(),
                        "Возможно вы пропустили точку",
                        Toast.LENGTH_LONG).show();
                return;
            }
            if (shortLink.isEmpty()) {
                showError("Сокращенная ссылка неверная");
                return;
            }
//            вставляем в БД
            if (dataBase.insertLink(fullLink, shortLink)) {
                addButton.setText("Готово");
                addButton.setBackgroundResource(R.drawable.button);
                addButton.setTextColor(ContextCompat.getColor(this, R.color.colorTextSecond));
//                очищаем поля
                fullLinkText.setText("");
                shortLinkText.setText("");
                loadAllShortLinks();
            } else {
                showError("Ссылка уже есть");
            }
        });

//        обработчик нажатия на ссылку
        listView.setOnItemClickListener((parent, view, position, id) -> {
//        получаем полную ссылку по позиции
            String fullLink = dataBase.getFullLinkByPosit(position);
            if (!fullLink.isEmpty()) {
                AlertDialog dialog = new AlertDialog.Builder(this)
//                    указываем настройки для всплывающ окна
                        .setTitle("Переход по ссылке")
                        .setMessage("Вы действительно желаете перейти по ссылке?")
                        .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
//                открываем ссылку в браузере
                                Intent browser = new Intent(Intent.ACTION_VIEW, Uri.parse(fullLink));
                                startActivity(browser);
                            }
                        })
                        .setNegativeButton("Нет", null)
                        .create();
                dialog.show();
            } else {
                addButton.setBackgroundResource(R.drawable.btn_error);
                addButton.setTextColor(ContextCompat.getColor(this, R.color.white));
                addButton.setText("Ошибка");
            }
        });
    }

//    метод для отображения ошибок
    private void showError (String btnText) {
        addButton.setText(btnText);
        addButton.setBackgroundResource(R.drawable.btn_error);
        addButton.setTextColor(ContextCompat.getColor(this, R.color.white));
    }

//      подгружаем все сокращенные ссылки и выводим в ListView
    private void loadAllShortLinks() {
        ArrayList<String> allShortLinks = dataBase.getAllShortLinks();
//        при старте программы
        if(arrayAdapter == null) {
            arrayAdapter = new ArrayAdapter<String>(this, R.layout.link_list_row, R.id.show_short_link, allShortLinks);
            listView.setAdapter(arrayAdapter);
        } else {
//      для обновления
            arrayAdapter.clear();
            arrayAdapter.addAll(allShortLinks);
            arrayAdapter.notifyDataSetChanged();
        }
    }
}