package kr.saintdev.pst.models.libs.manager;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import kr.saintdev.pst.models.consts.version.Versions;


/**
 * Created by 5252b on 2018-05-04.
 */

public class DBHelper extends SQLiteOpenHelper {
    private SQLiteDatabase readDB = null;
    private SQLiteDatabase writeDB = null;

    public DBHelper(Context context) {
        super(context, "project_screen_trans", null, Versions.getVersionCode());
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQLQuerys.SETTING_QUERY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public void open() {
        // read 와 write 할 수 있는 db 객체를 가져옵니다.
        this.readDB = getReadableDatabase();
        this.writeDB = getWritableDatabase();
    }

    public Cursor sendReadableQuery(String query) {
        return this.readDB.rawQuery(query, null);
    }

    public void sendWriteableQuery(String query) {
        this.writeDB.execSQL(query);
    }

    public SQLiteDatabase getReadDB() {
        return readDB;
    }

    public SQLiteDatabase getWriteDB() {
        return writeDB;
    }

    public interface SQLQuerys {
        String SETTING_QUERY = "CREATE TABLE `repository` (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "repo_key TEXT NOT NULL," +
                "repo_value TEXT NOT NULL" +
                ")";
    }
}
