package ddwu.mobile.finalproject.ma02_20190999.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import ddwu.mobile.finalproject.ma02_20190999.todo.TodoListFragment;
import ddwu.mobile.finalproject.ma02_20190999.todo.TodoViewPager;

public class TimerDbUtil {
    private static ItemDbHelper dbHelper;

    public static void update(Context context, int value, int mId, boolean isUnit) {
        if (dbHelper == null)
            dbHelper = new ItemDbHelper(context);
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        String query = null;
        if (isUnit)
            query = "UPDATE " + ItemContract.ItemEntry.TABLE_NAME + " SET unit = '" + value + "' WHERE _ID = '" + mId + "';";
        else
            query = "UPDATE " + ItemContract.ItemEntry.TABLE_NAME + " SET status = '" + value + "' WHERE _ID = '" + mId + "';";
        database.execSQL(query);
        database.close();
        updateListFragment(context);
    }

    public static void update(Context context, int unit, int status, int mId) {
        if (dbHelper == null)
            dbHelper = new ItemDbHelper(context);
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        String query = "UPDATE " + ItemContract.ItemEntry.TABLE_NAME + " SET unit = '" + unit + "', status = '" + status + "' WHERE _ID = '" + mId + "';";
        database.execSQL(query);
        database.close();
        updateListFragment(context);
    }

    public static void updateListFragment(Context context) {
        // Item 목록 항목 단위 수 업데이트
        TodoViewPager todoViewPager = (TodoViewPager) context;
        String listTag = todoViewPager.getListTag();
        TodoListFragment listItemFragment = (TodoListFragment) todoViewPager.getSupportFragmentManager().findFragmentByTag(listTag);
        listItemFragment.listUiUpdateFromDb();
    }
}
