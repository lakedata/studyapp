package ddwu.mobile.finalproject.ma02_20190999.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ItemDbHelper extends SQLiteOpenHelper {
    public static final String LOG_TAG = ItemDbHelper.class.getSimpleName();

    //데이터베이스 파일 이름
    private static final String DATABASE_NAME = "iTimeU.db";

    //데이터베이스 버전. 데이터베이스 스키마를 변경할 경우 데이터베이스 버전을 늘려
    private static final int DATABASE_VERSION = 1;

    /**
     * Constructs a new instance of {@link ItemDbHelper}.
     *
     * @param context of the app
     */

    public ItemDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

   //데이터베이스가 처음 생성될 때 호출
    @Override
    public void onCreate(SQLiteDatabase db) {
        // SQL 문을 포함하는 문자열을 만들어 항목 테이블을 만든다
        String SQL_CREATE_LIST_TABLE = "CREATE TABLE " + ItemContract.ItemEntry.TABLE_NAME + "("
                + ItemContract.ItemEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ItemContract.ItemEntry.COLUMN_ITEM_NAME + " TEXT NOT NULL, "
                + ItemContract.ItemEntry.COLUMN_ITEM_DETAIL + " TEXT, "
                + ItemContract.ItemEntry.COLUMN_ITEM_DATE + " TEXT, "
                + ItemContract.ItemEntry.COLUMN_ITEM_TOTAL_UNIT + " INTEGER NOT NULL DEFAULT 1, "
                + ItemContract.ItemEntry.COLUMN_ITEM_UNIT + " INTEGER NOT NULL DEFAULT 0, "
                + ItemContract.ItemEntry.COLUMN_ITEM_STATUS + " INTEGER NOT NULL DEFAULT 0);";

        db.execSQL(SQL_CREATE_LIST_TABLE);
    }

   //데이터베이스를 업그레이드해야 할 때 호출
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        // 데이터베이스가 아직 버전 1이라 여기서 할 수 있는 게 없음
    }
}
