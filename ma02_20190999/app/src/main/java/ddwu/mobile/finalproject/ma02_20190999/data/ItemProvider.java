package ddwu.mobile.finalproject.ma02_20190999.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

/**
 * Created by hyemin on 17. 8. 1.
 */
public class ItemProvider extends ContentProvider {

   //로그 메시지 태그
    public static final String LOG_TAG = ItemProvider.class.getSimpleName();

   //항목 테이블의 컨텐츠 URI에 대한 RI 매처 코드
    private static final int ITEMS = 100;

    //항목 테이블의 단일 항목에 대한 컨텐츠 URI에 대한 URI 매처 코드
    private static final int ITEM_ID = 101;

    /*콘텐츠 URI를 해당 코드와 일치시킬 UriMatcher 개체
    생성자에 전달된 입력은 루트 URI에 대해 반환할 코드를 나타냄
     이 경우 NO_MATCH를 입력으로 사용하는 것이 일반적.*/
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // 정적 이니셜라이저. 이 클래스에서 어떤 항목이 처음 호출될 때 실행
    static {
        // 공급자가 제공하는 모든 콘텐츠 URI 패턴에 대해 addURI() 호출이 여기에 들어감
        // 알아채야 한다. UriMatcher에 추가된 모든 경로에는 반환할 코드가 있음
        // "content://com.itto3.itimeu" 형식의 컨텐츠 URI는
        // 정수 코드 {@link #ITEMS}입니다. 이 URI는 여러 행에 대한 액세스를 제공하는 데 사용됩니다.
        // 목록에 있는 테이블.
        sUriMatcher.addURI(ItemContract.CONTENT_AUTHORITY, ItemContract.PATH_ITIMEU, ITEMS);

        // "content://com.itto3.itimeu"/itimeu/# 형식의 컨텐츠 URI는 다음 항목에 매핑
        // 정수 코드 {@link #ITEM_ID}. 이 URI는 하나의 단일 행에 대한 액세스를 제공하는 데 사용
        // 이 경우 "#" 와일드카드는 "#"이 정수로 대체될 수 있는 경우에 사용됨
        // 예를 들어 "content:/com.itto3.itimeu/itimeu/3"는 일치
        // "content://com.itto3.itimeu/itimeu"(끝에 숫자가 없음)가 일치하지 않음
        sUriMatcher.addURI(ItemContract.CONTENT_AUTHORITY,
                ItemContract.PATH_ITIMEU + "/#", ITEM_ID);
    }

    private ItemDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new ItemDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // 읽을 수 있는 데이터베이스 가져오기
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // 이 커서는 쿼리 결과를 저
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                // Items 코드의 경우 주어진 항목으로 직접 항목 테이블을 쿼리
                // 투영, 선택, 선택 인수 및 정렬 순서. 커서
                // 항목 테이블의 여러 행을 포함
                cursor = database.query(ItemContract.ItemEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case ITEM_ID:
                selection = ItemContract.ItemEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                // 이렇게 하면 _id가 3인 항목 테이블에 대해 쿼리를 수행하여 a를 반환
                // 테이블의 해당 행을 포함하는 커서
                cursor = database.query(ItemContract.ItemEntry.TABLE_NAME, projection,
                        selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        // 커서에 알림 URI를 설정
        // 따라서 커서가 생성된 콘텐츠 URI를 알 수 있음
        // 이 URI의 데이터가 변경되면 커서를 업데이트
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                return insertItem(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /*새 콘텐츠 URI 반환 데이터베이스의 특정 행에 대해 지정된 내용 값을 가진 항목을 데이터베이스에 삽입 */
    private Uri insertItem(Uri uri, ContentValues values) {
        // 이름이 null이 아닌지 확인
        String name = values.getAsString(ItemContract.ItemEntry.COLUMN_ITEM_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Item requires a name");
        }


        // 쓰기 가능한 데이터베이스 가져오기
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // 지정된 값을 사용하여 새 항목 삽입
        long id = database.insert(ItemContract.ItemEntry.TABLE_NAME, null, values);
        // ID가 -1이면 삽입에 실패한 것입니다. 오류를 기록하고 null을 반환
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // 모든 수신기에 항목 콘텐츠 URI에 대한 데이터가 변경되었음을 알림
        getContext().getContentResolver().notifyChange(uri, null);

        // 끝에 ID(새로 삽입된 행)가 추가된 새 URI 반환
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                return updateItem(uri, contentValues, selection, selectionArgs);
            case ITEM_ID:
                // 항목_의 경우ID 코드, URI에서 ID를 추출
                // 어떤 행을 업데이트할지 알 수 있습니다. 선택 항목은 "_id=?"이며 선택 항목은 다음과 같습니다.
                // 인수는 실제 ID를 포함하는 문자열 배열
                selection = ItemContract.ItemEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateItem(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /*데이터베이스의 항목을 지정된 내용 값으로 업데이트합니다. 행에 변경 내용 적용
     선택 및 선택 인수(0개 또는 1개 이상의 항목일 수 있음)에 명시
     성공적으로 업데이트된 행 수를 반환*/
    private int updateItem(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // {@link ItemEntry#COLUMN_Item_인 경우NAME} 키가 있다.
        // 이름 값이 null이 아닌지 확인
        if (values.containsKey(ItemContract.ItemEntry.COLUMN_ITEM_NAME)) {
            String name = values.getAsString(ItemContract.ItemEntry.COLUMN_ITEM_NAME);
            if (name == null) {
                throw new IllegalArgumentException("item requires a name");
            }
        }

        //{@link ItemEntry#COLUMN_ITEM_DETAIL}
        if (values.containsKey(ItemContract.ItemEntry.COLUMN_ITEM_DETAIL)) {
            String detail = values.getAsString(ItemContract.ItemEntry.COLUMN_ITEM_DETAIL);
        }

        // {@link ItemEntry#COLUMN_ITEM_TOTAL_UNIT} 키가 있는 경우 중량 값이 유효한지 점검
        if (values.containsKey(ItemContract.ItemEntry.COLUMN_ITEM_TOTAL_UNIT)) {
            // 무게가 0kg 이상인지 확인
            Integer totalUnit = values.getAsInteger(ItemContract.ItemEntry.COLUMN_ITEM_TOTAL_UNIT);
            Integer unit = values.getAsInteger(ItemContract.ItemEntry.COLUMN_ITEM_UNIT);
        }

        // 업데이트할 값이 없는 경우, 데이터베이스를 업데이트x
        if (values.size() == 0) {
            return 0;
        }

        // 그렇지 않으면 데이터를 업데이트할 쓰기 가능한 데이터베이스를 가져옴
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // 데이터베이스에서 업데이트를 수행하고 영향을 받는 행 수를 가져옴
        int rowsUpdated = database.update(ItemContract.ItemEntry.TABLE_NAME,
                values, selection, selectionArgs);

        // 하나 이상의 행이 업데이트된 경우 모든 수신기에 데이터가 지정된 URI가 변경
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // 업데이트된 행 수를 반환
        return rowsUpdated;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // 쓰기 가능한 데이터베이스 가져옴
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // 삭제된 행 수 추적
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                // 선택 및 선택 호와 일치하는 모든 행 삭제
                rowsDeleted = database.delete(ItemContract.ItemEntry.TABLE_NAME,
                        selection, selectionArgs);
                break;
            case ITEM_ID:
                // URI에서 ID로 지정된 단일 행을 삭제
                selection = ItemContract.ItemEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(ItemContract.ItemEntry.TABLE_NAME, selection,
                        selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // 하나 이상의 행이 삭제된 경우 모든 수신기에 데이터 지정된 URI가 변경
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // 삭제된 행 수를 반환
        return rowsDeleted;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                return ItemContract.ItemEntry.CONTENT_LIST_TYPE;
            case ITEM_ID:
                return ItemContract.ItemEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}
