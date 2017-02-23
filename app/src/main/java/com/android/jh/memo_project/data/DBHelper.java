package com.android.jh.memo_project.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.android.jh.memo_project.domain.Memo;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;


public class DBHelper extends OrmLiteSqliteOpenHelper {

    public static final String DATABASE_NAME = "memo.db";
    public static final int DB_VERSION = 1;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DB_VERSION);
    }
    /**
     * 생성자에서 호출되는 super(context 에서 database.db 파일이 생성되어 있지 않으면 호출된다.
     * @param database
     * @param connectionSource
     */
    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            // Memo.class 파일에 정의된 테이블을 생성한다.
            TableUtils.createTable(connectionSource, Memo.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 생성자에서 호출된 super(context... 에서 database.db 파일이 존재하지만 DB_VERSION이 증가되면 호출된다.
     * @param database
     * @param connectionSource
     * @param oldVersion
     * @param newVersion
     */
    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        try {
            //Bbs.class에 정의된 테이블 삭제
            TableUtils.dropTable(connectionSource, Memo.class, false);
            //데이터를 보존해야 될 필요성이 있으면 중간 처리를 해줘야만 합니다.
            // TODO : 임시테이블을 생성한 후 데이터를 먼저 저장하고 onCreate 이후에 데이터를 입력해준다.
            //onCreate 호출해서 테이블을 생성해준다
            onCreate(database, connectionSource);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private Dao<Memo, Integer> MemoDao = null;

    public Dao<Memo, Integer> getMemoDao() throws SQLException {
        if(MemoDao == null) {
            MemoDao = getDao(Memo.class);
        }
        return MemoDao;
    }

    public void releaseBbsDao() {
        if (MemoDao != null) {
            MemoDao = null;
        }
    }
}
