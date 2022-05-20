package ddwu.mobile.finalproject.ma02_20190999.todo;

import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.provider.BaseColumns;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import ddwu.mobile.finalproject.ma02_20190999.R;
import ddwu.mobile.finalproject.ma02_20190999.data.ItemContract;
import ddwu.mobile.finalproject.ma02_20190999.data.ItemDbHelper;


public class TodoListFragment extends Fragment implements DatePickerDialog.OnDateSetListener,
        LoaderManager.LoaderCallbacks<Cursor> {
    // 데이터베이스에 액세스
    ItemDbHelper dbHelper;
    SQLiteDatabase db;

    //item data loader 식별자
    private View mListItemView;
    private Activity mListItemActivity;
    private Context mListItemContext;

    private static final int ITEM_LOADER = 0;


    TextView mAchievementTextView;// 달성률
    TextView mDetailRateTextView;//달성률에 대한 세부 정보 표시
    ListView mTaskItemListView; // List View
    View mEmptyView; // Empty view
    TextView mDateTextView;// date text
    ImageButton mPreviousDateImgBtn, mNextDateImgBtn;// date image button

    public static final String DATE_FORMAT = "yyyy.MM.dd"; // Simple date format
    private Date mCurrentListDate; // List's date
    private String mCurrentListDateStr; // Date convert to String
    private boolean isOtherItemSelected = false;

    //ListView용 어댑터
    ItemCursorAdapter mCursorAdapter;

    private int mYear, mMonth, mDay;// Date year, month, day;

    // 저장 selected item data
    private int mItemID;
    private String mItemName;
    private String mItemDate;
    private int mItemUnit;
    private int mItemTotalUnit;
    private int mItemStatus;

    private int mPercent;
    private String mDetail;

    // 토스트 메시지를 표시하는 객체
    private SimpleToast toast;

    final static int REQUEST_NUMBER = 0;

    public TodoListFragment() {
        // 빈 공용 생성자가 필
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        mListItemView = inflater.inflate(R.layout.fragment_todolist, container, false);
        mListItemActivity = getActivity();
        mListItemContext = mListItemView.getContext();

        // 새 SimpleToast 개체 생성
        toast = new SimpleToast(mListItemContext);

        // list table db
        dbHelper = new ItemDbHelper(mListItemContext);
        db = dbHelper.getReadableDatabase();

        // 이전/다음 날짜 이미지 찾기 버튼
        mPreviousDateImgBtn = mListItemView.findViewById(R.id.listitem_previous_date_imgbtn);
        mNextDateImgBtn = mListItemView.findViewById(R.id.listitem_next_date_imgbtn);

        loadingPreviousOrNextDateList();

        // 항목 데이터로 채워질 ListView 찾기
        mTaskItemListView = mListItemView.findViewById(R.id.item_list_view);

        // 작업 목록 보기가 비어 있는 경우 이 보기 표시
        setEmptyView();

        // 작업 항목을 클릭하면 항목 정보를 확인
        getTaskItemInfoAndCheck();

        // 목록 태그를 가져와 목록 태그로 설정
        ((TodoViewPager) getActivity()).setListTag(getTag());

        //today's date
        mCurrentListDate = new Date();
        mCurrentListDateStr = getStringFromDate(mCurrentListDate);
        mDateTextView = mListItemView.findViewById(R.id.date_btn);
        mDateTextView.setText(mCurrentListDateStr + " (Today)");

       // 현재 날짜의 달성률을 설정
        setAchievementRate();

        // 날짜 텍스트 보기를 클릭
        showDialogForSelectDate();

        //사용자가 클릭하고 메시지를 보낼 때
        final Button addMessage
                = mListItemView.findViewById(R.id.add_message);
        clickAddMessage(addMessage);

        //목록에 항목을 추가하기 위해 사용자가 Add FloatingActionButton을 클릭
        final FloatingActionButton addFab
                = mListItemView.findViewById(R.id.add_fab_btn);
        clickAddFab(addFab);



//        addMessage.bringToFront();

//         displayListByDate();
        // Touch and hold the item to display the context menu (modify/delete).
        registerForContextMenu(mTaskItemListView);

        //Kick off the loader
        getLoaderManager().initLoader(ITEM_LOADER, null, this);

        return mListItemView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    //다른 작업이 완료되면 새로 고침을 수행
    @Override
    public void onResume() {
        super.onResume();
        // refresh achievement rate
        setAchievementRate();
    }

    //수정 또는 삭제를 위한 상황별 메뉴 생성
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

        mListItemActivity.getMenuInflater().inflate(R.menu.menu_editor, menu);

        super.onCreateContextMenu(menu, v, menuInfo);
    }


     //show menu list
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info =
                (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        // click item's index
        int id = (int) info.id;

        Intent intent = new Intent(mListItemContext, EditorActivity.class);
        Uri currentItemUri = ContentUris.withAppendedId(ItemContract.ItemEntry.CONTENT_URI, id);
        intent.setData(currentItemUri);

        switch (item.getItemId()) {
            case R.id.action_modify:
                if (!isThisTaskStarted(id)) {
                    startActivityForResult(intent, REQUEST_NUMBER);
                } else toast.showLongTimeToast(R.string.listitem_this_item_started);
                break;
            case R.id.action_delete:
                if (!isThisTaskStarted(id)) {
                    showDeleteConfirmationDialog(id);
                } else toast.showLongTimeToast(R.string.listitem_this_item_started);
                break;
            default:
                return false;
        }

        return true;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        //테이블 열 정의
        String[] projection = {
                ItemContract.ItemEntry._ID,
                ItemContract.ItemEntry.COLUMN_ITEM_NAME,
                ItemContract.ItemEntry.COLUMN_ITEM_DETAIL,
                ItemContract.ItemEntry.COLUMN_ITEM_STATUS,
                ItemContract.ItemEntry.COLUMN_ITEM_TOTAL_UNIT,
                ItemContract.ItemEntry.COLUMN_ITEM_UNIT};

        String[] date = {mCurrentListDateStr};

        return new CursorLoader(mListItemContext,
                ItemContract.ItemEntry.CONTENT_URI,  // 쿼리할 제공자 콘텐츠 URI
                projection,             // 결과 커서에 포함할 열
                "date = ?",             // 날짜 선택 인수
                date,                   // Date selection arguments
                null);                  // 기본 정렬 순서
    }

   //사용자에게 이 항목을 삭제할지 확인하라는 메시지를 표시
    private void showDeleteConfirmationDialog(final int index) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mListItemContext);
        builder.setMessage(getString(R.string.delete_confirm_msg));
        builder.setPositiveButton(getString(R.string.delete_btn), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // 사용자가 "삭제" 버튼을 클릭하였으니 항목을 삭제
                deleteItem(index);
                // Update List Date
                listUiUpdateFromDb();
            }
        });
        builder.setNegativeButton(getString(R.string.cancel_btn), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // 사용자가 "취소" 버튼을 클릭했으므로 대화 상자를 취소
                // 항목을 계속 편집
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Delete the item of the ID obtained by parameter.
     *
     * @param id item's id
     */
    private void deleteItem(int id) {
        Uri currentItemUri = ContentUris.withAppendedId(ItemContract.ItemEntry.CONTENT_URI, id);

        // 기존 항목인 경우에만 삭제
        if (currentItemUri != null) {
            // 지정된 콘텐츠 URI에서 항목을 삭제하려면 ContentResolver를 호출
            // mCurrentItem으로 인해 선택 및 선택 호에 대해 null을 전달
            // 컨텐츠 URI 원하는 항목을 식별
            int rowsDeleted
                    = mListItemActivity.getContentResolver().delete(currentItemUri, null, null);

            // 삭제 성공 여부에 따라 토스트 메시지를 표시
            if (rowsDeleted == 0) {
                // 행이 삭제되지 않은 경우 삭제 오류
                toast.showShortTimeToast(R.string.delete_item_fail);
                // TimerFragment에서 항목 이름 텍스트를 job_txt_view로 설정
            } else {
                //삭제가 완료되어 토스트를 표시
                toast.showShortTimeToast(R.string.delete_item_success);

             /*   String tabOfTimerFragment = ((MainActivity) getActivity()).getTimerTag();
                TimerFragment timerFragment = (TimerFragment) getActivity()
                        .getSupportFragmentManager()
                        .findFragmentByTag(tabOfTimerFragment);

                timerFragment.setDeleteItemDisable(id);*/
            }
        } else {
            // 오류가 발생했거나 선택한 항목이 이미 시작되었기 때문에 삭제X
            toast.showShortTimeToast(R.string.listitem_cannot_delete);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update {@link ITemCursorAdapter} with this new cursor containing updated item data
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // 데이터를 삭제해야 할 때 콜백이 호출
        mCursorAdapter.swapCursor(null);
    }

    /**
     * Date -> String
     *
     * @return Date type date
     */
    public String getStringFromDate(Date date) {
        return new SimpleDateFormat(DATE_FORMAT, Locale.KOREA).format(date);
    }

    /**
     * String -> Date
     *
     * @return String type date
     */
    public Date getDateFromString(String date) throws ParseException {
        return new SimpleDateFormat(DATE_FORMAT, Locale.KOREA).parse(date);
    }

    /**
     * @param view          DatePickerDialog
     * @param selectedYear  Year selected by the user
     * @param selectedMonth Month selected by the user
     * @param selectedDay   Date selected by the user
     */

    @Override
    public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
        Calendar calendar = Calendar.getInstance();

        //Selected Date 할당 in DatePickerDialog
        mYear = selectedYear;
        mMonth = selectedMonth;
        mDay = selectedDay;

        // Set Date in List
        calendar.set(mYear, mMonth, mDay);
        mCurrentListDate = calendar.getTime();
        mCurrentListDateStr = getStringFromDate(mCurrentListDate);
        if (mCurrentListDateStr.equals(getStringFromDate(new Date()))) {
            mDateTextView.setText(mCurrentListDateStr + " (Today)");
        } else {
            mDateTextView.setText(mCurrentListDateStr);
        }

        // Update List Date
        listUiUpdateFromDb();
    }

    //백분율 계산: 단위의 합계 / 총 단위의 합계
    void calculateAchievementRate() {
        int mSumOfTotalUnits = 0;
        int mSumOfUnits = 0;
        String[] date = {mCurrentListDateStr};
        Cursor cursor = db.rawQuery("SELECT totalUnit, unit FROM list WHERE date = ?", date);

        if (cursor.moveToFirst()) {
            do {
                mSumOfTotalUnits += cursor.getInt(cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_ITEM_TOTAL_UNIT));
                mSumOfUnits += cursor.getInt(cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_ITEM_UNIT));
            } while (cursor.moveToNext());
        }
        cursor.close();

        if (mSumOfTotalUnits != 0) {
            mPercent = Math.round(((float) mSumOfUnits / mSumOfTotalUnits) * 100);
        } else {
            mPercent = 0;
        }

        mDetail = "( " + mSumOfUnits + " / " + mSumOfTotalUnits + " )";
    }

    // 애니메이션 증가율 표시
    void setAchievementRate() {
        calculateAchievementRate();
        // Find the TextView which will show sum of units / sum of total units in list's date
        mAchievementTextView = mListItemView.findViewById(R.id.achievement_rate_txt_view);

        // Show increasing percent animation
        ValueAnimator animator = ValueAnimator.ofInt(0, mPercent);
        animator.setDuration(1000);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                String rate = animation.getAnimatedValue().toString() + " %";
                mAchievementTextView.setText(rate);
            }
        });
        animator.start();
        mDetailRateTextView = mListItemView.findViewById(R.id.rate_detail_txt_view);
        mDetailRateTextView.setText(mDetail);
    }

    /**
     * Check item's date
     *
     * @return when item's date == today then return true, but date != today then return false.
     */
    boolean checkDate() {
        if (mItemDate.equals(getStringFromDate(new Date()))) {
            return true;
        } else {
            toast.showShortTimeToast(R.string.not_today);
            return false;
        }
    }

    /**
     * Check item's status.
     * <p>
     * * status == To do then set item's info to Timer and change view List to Timer.
     * status == Do then just change view List to Timer.
     * status == Done then show toast message.
     */
    void checkStatus() {
        // Get MainActivity
        TodoViewPager mainActivity = (TodoViewPager) getActivity();

        // 선택한 항목의 상태 == 완료일 경우 작업을 중지
        if (mItemStatus == ItemContract.ItemEntry.STATUS_DONE) {
            toast.showShortTimeToast(R.string.already_done);
            return;
        } else if (mItemStatus == ItemContract.ItemEntry.STATUS_TODO) {
            // 다른 작업이 시작?
            if (isOtherItemSelected) {
                // re-initialize
                isOtherItemSelected = false;
                return;
            }

            // TimerFragment에서 항목 이름 텍스트를 job_txt_view로 설정
            String tabOfTimerFragment = mainActivity.getTimerTag();
            TimerFragment timerFragment = (TimerFragment) getActivity()
                    .getSupportFragmentManager()
                    .findFragmentByTag(tabOfTimerFragment);
//
//            // Set selected item info
//            //public void  setTimerFrag(int mId,int mStatus,int mUnit,int mTotalUnit,String mName)
            timerFragment.setTimerFragment(mItemID, mItemStatus, mItemUnit, mItemTotalUnit, mItemName);

        }

        // Change Fragment ListItemFragment -> TimerFragment
        (mainActivity).getViewPager().setCurrentItem(1);
    }

   //데이터베이스의 목록 보기에서 UI를 업데이트
    public void listUiUpdateFromDb() {
        getLoaderManager().restartLoader(0, null, this);
        setAchievementRate();
        updateStatisticsGraph();
    }

    /**
     * Get previous date from current date in list fragment view
     *
     * @param simpleCurrentDate The date selected by user in list view
     * @return previous date string via simple date format
     */
    String getPreviousDateFromCurrentDate(String simpleCurrentDate) throws ParseException {
        Date currentDate = getDateFromString(simpleCurrentDate);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);
        calendar.add(Calendar.DATE, -1);

        mCurrentListDate = calendar.getTime();

        return getStringFromDate(mCurrentListDate);
    }

    /**
     * Get next date from current date in list fragment view
     *
     * @param simpleCurrentDate The date selected by user in list view
     * @return next date string via simple date format
     */
    String getNextDateFromCurrentDate(String simpleCurrentDate) throws ParseException {
        Date currentDate = getDateFromString(simpleCurrentDate);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);
        calendar.add(Calendar.DATE, 1);

        mCurrentListDate = calendar.getTime();

        return getStringFromDate(mCurrentListDate);
    }

    //목록 보기에서 목록 날짜 업데이트
    void loadingPreviousOrNextDateList() {
        mPreviousDateImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    String previousDate = getPreviousDateFromCurrentDate(mCurrentListDateStr);
                    mCurrentListDateStr = previousDate;
                    if (mCurrentListDateStr.equals(getStringFromDate(new Date()))) {
                        mDateTextView.setText(mCurrentListDateStr + " (Today)");
                    } else {
                        mDateTextView.setText(mCurrentListDateStr);
                    }
                    listUiUpdateFromDb();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });

        mNextDateImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    String nextDate = getNextDateFromCurrentDate(mCurrentListDateStr);
                    mCurrentListDateStr = nextDate;
                    if (mCurrentListDateStr.equals(getStringFromDate(new Date()))) {
                        mDateTextView.setText(mCurrentListDateStr + " (Today)");
                    } else {
                        mDateTextView.setText(mCurrentListDateStr);
                    }
                    listUiUpdateFromDb();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // ListView에서 빈 보기
    void setEmptyView() {
        mEmptyView = mListItemView.findViewById(R.id.empty_relative_view);
        mTaskItemListView.setEmptyView(mEmptyView);
    }

    //// 항목을 클릭하면 DB의 목록 테이블에 액세스
    void getTaskItemInfoAndCheck() {
        mCursorAdapter = new ItemCursorAdapter(mListItemContext, null);
        mTaskItemListView.setAdapter(mCursorAdapter);

        // When click item, access to the list table in DB
        mTaskItemListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // 항목의 기본 ID 가져오기
                mItemID = (int) id;
                String[] idStr = {String.valueOf(mItemID)};
                Cursor cursor = db.rawQuery("SELECT name, unit, totalUnit, status, date FROM list WHERE "
                        + BaseColumns._ID + " = ?", idStr);

                // 타이머가 시작되었는지 점검
                if (isAnotherTaskStarted(mItemID)) {
                    timerIsAlreadyStarted();
                    return;
                }

                // Get current item's info
                if (cursor.moveToFirst()) {
                    mItemName = cursor.getString(
                            cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_ITEM_NAME));
                    mItemDate = cursor.getString(
                            cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_ITEM_DATE));
                    mItemUnit = cursor.getInt(
                            cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_ITEM_UNIT));
                    mItemTotalUnit = cursor.getInt(
                            cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_ITEM_TOTAL_UNIT));
                    mItemStatus = cursor.getInt(
                            cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_ITEM_STATUS));

                    // 선택한 항목의 날짜가 오늘인지 확인
                    if (checkDate()) {
                        // 선택한 항목의 상태 확인
                        checkStatus();
                    }
                }

                cursor.close();
            }
        });
    }

    private void timerIsAlreadyStarted() {
        toast.showShortTimeToast(R.string.already_start);
        isOtherItemSelected = true;
    }

    //Show data picker dialog,사용자가 현재 목록 날짜를 변경
    void showDialogForSelectDate() {
        mDateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Date date;
                if (mCurrentListDateStr != null) {
                    date = mCurrentListDate;
                } else {
                    date = new Date();
                }

                // Setting calender -> list's date
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                mYear = calendar.get(Calendar.YEAR);
                mMonth = calendar.get(Calendar.MONTH);
                mDay = calendar.get(Calendar.DAY_OF_MONTH);

//                DatePickerDialog datePickerDialog
//                        = DatePickerDialog.new Instance(TodoListActivity.this, mYear, mMonth, mDay);
//                datePickerDialog.show(TodoListActivity.getFragmentManager(), "DateFragment");
            }
        });
    }

    /**
     * When click add floating action button, then start EditorActivity.
     *
     * @param addFab floating action button for add task item.
     */
    void clickAddFab(FloatingActionButton addFab) {
        addFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mListItemContext, EditorActivity.class);
                intent.putExtra("date", mCurrentListDateStr);
                //startActivity(intent);
                startActivityForResult(intent, REQUEST_NUMBER);
            }
        });
    }

    //  메세지 전송 버튼 구현
    void clickAddMessage(Button addMessage) {
        addMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareTwitter();
            }
        });
    }

    public void shareTwitter() {
        String sharedText = null;
        try {
            sharedText = String.format("http://twitter.com/intent/tweet?text=%s",
                    URLEncoder.encode("오늘의 공부량은" + mDetail, "utf-8"));
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(sharedText));
            startActivity(intent);

        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
    }

    /**
     * This function check that another task item is already started.
     *
     * @param id selected item's id
     * @return if selected item's id is same with the task in execution
     * , or there is nothing in execution, return true. Otherwise return false.
     */
    boolean isAnotherTaskStarted(int id) {
        String[] date = {getStringFromDate(new Date())};
        Cursor cursor = db.rawQuery("SELECT status, " + BaseColumns._ID +
                " FROM list WHERE date = ?", date);

        if (cursor.moveToFirst()) {
            do {
                if (cursor.getInt(cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_ITEM_STATUS))
                        == ItemContract.ItemEntry.STATUS_DO) {
                    return cursor.getInt(cursor.getColumnIndex(ItemContract.ItemEntry._ID)) != id;
                }
            } while (cursor.moveToNext());
        }
        cursor.close();

        return false;
    }

    /**
     * When click an item long, then check the item is started task.
     *
     * @param id selected item's id
     * @return if the item's status == DO than return true, else return false.
     */
    boolean isThisTaskStarted(int id) {
        String[] strId = {String.valueOf(id)};
        Cursor cursor =
                db.rawQuery("SELECT status FROM list WHERE " + BaseColumns._ID + " =  ?", strId);

        if (cursor.moveToFirst()) {
            if (cursor.getInt(cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_ITEM_STATUS))
                    == ItemContract.ItemEntry.STATUS_DO) {
                cursor.close();
                return true;
            }
        }
        cursor.close();
        return false;
    }

    public void updateStatisticsGraph() {
        String tabOfStatisticsFragment = ((TodoViewPager) getActivity()).getStatisticsTag();
        if (tabOfStatisticsFragment != null) {
            /*StatisticsFragment statisticsFragment = (StatisticsFragment) getActivity()
                    .getSupportFragmentManager()
                    .findFragmentByTag(tabOfStatisticsFragment);

            statisticsFragment.updateChartGraph();*/
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        updateStatisticsGraph();
    }
}
