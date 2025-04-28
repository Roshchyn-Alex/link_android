package com.android.diplom_android;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;
import java.util.ArrayList;

public class DataBase extends SQLiteOpenHelper {

    private static final String db_name = "link_manager";
    private static final int db_version = 1;
    private static final String db_table = "link";
    private static final String db_column_full = "full_link";
    private static final String db_column_short = "short_link";

    public DataBase(@Nullable Context context) {
        super(context, db_name, null, db_version);
        //    очищаем БД при открытии
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM " + db_table);
        db.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
//        создаем табличку
        String query = String.format("CREATE TABLE %s (ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "%s TEXT NOT NULL, %s TEXT NOT NULL UNIQUE);", db_table, db_column_full, db_column_short);
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//        если меняем что-то в таблице (поле), то удалеяем старую и созд новую
        String query = String.format("DELETE TABLE IF EXISTS %s", db_table);
        db.execSQL(query);
        onCreate(db);
    }

//    метод для добавления новой записи
    public boolean insertLink(String fullLink, String shortLink) {
//        получаем ту БД, кот создали
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
//        указываем значения, которые добавим
        contentValues.put(db_column_full, fullLink);
        contentValues.put(db_column_short, shortLink);
//        insertWithOnConflict - более безопасный, CONFLICT_IGNORE - не вставит строку
        long result = db.insertWithOnConflict(db_table, null, contentValues, SQLiteDatabase.CONFLICT_IGNORE);
        db.close();
        return result != -1;
//        true если вставка успешна, false - дубликат
    }
//    метод получение всех сокращенных ссылок
    public ArrayList<String> getAllShortLinks() {
//        создаем список
        ArrayList<String> allShortLinks = new ArrayList<>();
//        подключаемся к БД
        SQLiteDatabase db = this.getReadableDatabase();
//        получаем записи по определенным параметрам и помещаем в cursor
//        добавляем сортировку по ID,  чтобы в ListView отображались в порядке добавления, а не по алфавиту
        Cursor cursor = db.query(db_table, new String[] {db_column_short}, null, null,
                        null, null, "ID ASC");
//        преобразуем и помещаем в ArrayList
        while (cursor.moveToNext()) {
//            получаем у каждой записи индекс (это не ID)
            int index = cursor.getColumnIndex(db_column_short);
            allShortLinks.add(cursor.getString(index));
        }
        cursor.close();
        db.close();
        return allShortLinks;
    }
//    получение полных ссылок по позиции
    public String getFullLinkByPosit(int position) {
        String fullLink = null;
        SQLiteDatabase db = this.getReadableDatabase();

//      ID ASC - сортировка по  ID,  position + ",1" - берем одну запись, начиная с position
        Cursor cursor = db.query(db_table, new String[] {db_column_full}, null, null,
                        null,null, "ID ASC", position + ",1");
        if (cursor.moveToFirst()) {
            int index = cursor.getColumnIndex(db_column_full);
            fullLink = cursor.getString(index);
//            проверка на http, если нет, то добавляем
            if(fullLink != null && !fullLink.startsWith("http://") &&  !fullLink.startsWith("https://")) {
                fullLink = "http://" + fullLink;
            }
        }
        cursor.close();
        db.close();
        return fullLink;
    }
}
