package ddwu.mobile.finalproject.ma02_20190999.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class ItemContract {


    //실수로 인스턴스화하는 것을 막기 위해 빈 생성자
    private ItemContract() {
    }

    /* The "Content authority" 전체 콘텐츠 공급자의 이름, similar to the
     도메인 이름과 해당 웹 사이트 간의 관계. 다음에 사용하기 편리한 문자열
     콘텐츠 권한이란 앱의 패키지 이름으로, 앱에서 고유함을 보장 장치 */
    public static final String CONTENT_AUTHORITY = "ddwu.mobile.finalproject.ma02_20190999";

    /*CONTENT_AUTHORITY를 사용하여 앱이 연결하는 데 사용할 모든 URI의 기반을 만듭니다.
     콘텐츠 공급자*/
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /*가능한 경로(가능한 URI에 대해 기본 콘텐츠 URI에 추가됨)
    예를 들어 content:/com.itto3.itimeu/itimeu/는 다음에 대한 유효한 경로
    항목 데이터를 본다. 컨텐츠://com.itto3.itimeu/staff/는 실패
    컨텐츠 제공자에게 "스태프"를 어떻게 해야 하는지에 대한 정보가 제공되지 않았기 때문*/
    public static final String PATH_ITIMEU = "itimeu";


    /*항목 데이터베이스 테이블에 대한 상수 값을 정의하는 내부 클래스입니다.
    표의 각 항목은 단일 항목*/
    public static final class ItemEntry implements BaseColumns {

        //공급자의 항목 데이터에 액세스하는 컨텐츠 URI
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_ITIMEU);

        //목 목록에 대한 {@link #CONTENT_URI}의 MIME 유형
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ITIMEU;

        //단일 항목에 대한 {@link #CONTENT_URI}의 MIME 유형
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ITIMEU;

       //항목에 대한 데이터베이스 테이블 이름
        public final static String TABLE_NAME = "list";

        /**
         * Unique ID number for the item (only for use in the database table).
         * <p>
         * Type: INTEGER
         */
        public final static String _ID = BaseColumns._ID;

        /**
         * Name of the item.
         * <p>
         * Type: TEXT
         */
        public final static String COLUMN_ITEM_NAME = "name";

        /**
         * Detail of the item.
         * <p>
         * Type: TEXT
         */
        public final static String COLUMN_ITEM_DETAIL = "detail";

        /**
         * Created date of the item.
         * <p>
         * Type: TEXT
         */
        public final static String COLUMN_ITEM_DATE = "date";

        /**
         * Total unit number of the item.
         * <p>
         * Type: INTEGER
         */
        public final static String COLUMN_ITEM_TOTAL_UNIT = "totalUnit";

        /**
         * Unit number of the item.
         * <p>
         * Type: INTEGER
         */
        public final static String COLUMN_ITEM_UNIT = "unit";

        /**
         * Status of the item.
         * <p>
         * Type: INTEGER
         */
        public final static String COLUMN_ITEM_STATUS = "status";

        //item의 상태에 대해 가능한 값
        public final static int STATUS_TODO = 0;
        public final static int STATUS_DO = 1;
        public final static int STATUS_DONE = 2;
    }
}
