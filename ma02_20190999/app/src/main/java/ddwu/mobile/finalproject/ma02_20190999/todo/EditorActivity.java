package ddwu.mobile.finalproject.ma02_20190999.todo;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import ddwu.mobile.finalproject.ma02_20190999.R;
import ddwu.mobile.finalproject.ma02_20190999.data.ItemContract;


public class EditorActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener,
        LoaderManager.LoaderCallbacks<Cursor> {


    //항목 데이터  loader식별자
    private static final int EXISTING_ITEM_LOADER = 0;

    private static final int NAME_MAX_COUNT = 15;
    private static final int DETAIL_MAX_COUNT = 20;


    private Uri mCurrentItemUri;//기존 항목의 컨텐츠 URI(새 항목인 경우 null)
    private EditText mNameEditText; //항목 이름 입력
    private EditText mDetailEditText;//항목의 세부 정보를 입력
    private TextView mNameCountTextView;
    private TextView mDetailCountTextView;
    private EditText mDateEditText;//항목 날짜
    private TextView mTotalUnitTextView;//항목의 총 단위

    private ImageButton mUnitPlusImageButton;
    private ImageButton mUnitMinusImageButton;
    private int mUnitNumber;
    private int mTotalUnitNumber;
    private String mTotalUnitString;

    private String mDate;
    public static final String DATE_FORMAT = "yyyy.MM.dd";//date format
    private int mYear, mMonth, mDay;

    /**
     * Status of the item. The possible valid values are in the ItemContract.java file:
     * {@link ItemContract.ItemEntry#STATUS_TODO}, {@link ItemContract.ItemEntry#STATUS_DO}, {@link ItemContract.ItemEntry#STATUS_DONE}
     */
    private int mStatus = ItemContract.ItemEntry.STATUS_TODO;

    //항목이 편집되었는지(참) 여부를 추적
    private boolean mItemHasChanged = false;


    private SimpleToast toast; //showing toast.

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mItemHasChanged = true;//true로 변경
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        toast = new SimpleToast(this);

       // 새 항목을 만드는지 아니면 기존 항목을 편집
        Intent intent = getIntent();
        mCurrentItemUri = intent.getData();


        // creating a new item.
        TextView titleTextView = (TextView) findViewById(R.id.editor_title_txt_view);
        if (mCurrentItemUri == null) {
            titleTextView.setText(R.string.editor_title_add);
        } else {
            titleTextView.setText(R.string.editor_title_edit);

            //항목 데이터를 읽도록 초기화 및 편집기에 현재 값을 표시
            getSupportLoaderManager().initLoader(0, null, this);
        }

        mNameEditText = (EditText) findViewById(R.id.name_edit_txt);
        mDetailEditText = (EditText) findViewById(R.id.detail_edit_txt);
        mNameCountTextView = (TextView) findViewById(R.id.editor_name_count_txt);
        mDetailCountTextView = (TextView) findViewById(R.id.editor_detail_count_txt);

        countNameCharAndShow();
        nameCursorVisibility();
        countDetailCharAndShow();
        detailCursorVisibility();

        mTotalUnitTextView = (TextView) findViewById(R.id.get_total_unit_txt_view);
        mDate = intent.getStringExtra("date");
        mDateEditText = (EditText) findViewById(R.id.editor_date_edit_txt);
        mTotalUnitNumber = Integer.parseInt(mTotalUnitTextView.getText().toString().trim());

        mUnitMinusImageButton = (ImageButton) findViewById(R.id.unit_minus_btn);
        mUnitPlusImageButton = (ImageButton) findViewById(R.id.unit_plus_btn);

        mNameEditText.setOnTouchListener(mTouchListener);
        mDetailEditText.setOnTouchListener(mTouchListener);
        mUnitMinusImageButton.setOnTouchListener(mTouchListener);
        mUnitPlusImageButton.setOnTouchListener(mTouchListener);

        // 사용자가 선택한 날짜를 가져옴
        dateSelection();
        getTotalUnitNumber();

//        System.out.print("submit" + mDetailEditText.toString());
//        Log.d("submit", "세부"+ mDetailEditText.toString());
        submit();
    }


    @Override
    public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
        Calendar calendar = Calendar.getInstance();

        // DatePickerDialog에서 선택한 날짜 할당
        mYear = selectedYear;
        mMonth = selectedMonth;
        mDay = selectedDay;

        // Set Date in List
        calendar.set(mYear, mMonth, mDay);
        Date date = calendar.getTime();
        Date today = new Date();

        if (date.compareTo(today) >= 0) {
            mDate = getDate(date);
            mDateEditText.setText(mDate);
        } else {
            toast.showLongTimeToast(R.string.editor_invalid_date);
            mDate = getDate(today);
            mDateEditText.setText(mDate);
        }
    }

    void dateSelection() {
        mDateEditText.setFocusable(false);
        mDateEditText.setOnTouchListener(mTouchListener);
        mDateEditText.setText(mDate);
        mDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT, Locale.KOREA);
                try {
                    // String -> Date
                    Date date = format.parse(mDate);

                    // Setting calender -> list's date
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(date);

                    mYear = calendar.get(Calendar.YEAR);
                    mMonth = calendar.get(Calendar.MONTH);
                    mDay = calendar.get(Calendar.DAY_OF_MONTH);

                    DatePickerDialog datePickerDialog = new DatePickerDialog(EditorActivity.this, null, mYear, mMonth, mDay);
                    datePickerDialog.show();

//                    DatePickerDialog datePickerDialog
//                            = DatePickerDialog.newInstance(EditorActivity.this, mYear, mMonth, mDay);
//                    datePickerDialog.show(getFragmentManager(), "DateFragment");
                } catch (ParseException e) {
                    Log.e("EditorActivity", "ParseException: " + e);
                }
            }
        });
    }

   //편집기에서 사용자 입력을 가져와 항목을 데이터베이스에 저장
    private boolean saveItem() {
        // input fields 읽음
        // trim 사용

        System.out.print("saveItem" + mNameEditText.getText().toString());
        Log.d("saveItem", "세부" + mDetailEditText.getText().toString());

        String nameString = mNameEditText.getText().toString().trim();
        String detailString = mDetailEditText.getText().toString().trim();

        //이름이 비어 있으면 false를 ,아니면 항목을 저장
        Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
        if (TextUtils.isEmpty(nameString)) {
            mNameEditText.startAnimation(shake);
            toast.showShortTimeToast(R.string.input_name_toast);
            return false;
        } else {
            // 확인하여 새 item인지 기존 item인지 확인
            if (mCurrentItemUri == null) {
                //새 항목에 대한 ContentValues 개체 만들기
                ContentValues createValues = new ContentValues();
                createValues.put(ItemContract.ItemEntry.COLUMN_ITEM_NAME, nameString);
                createValues.put(ItemContract.ItemEntry.COLUMN_ITEM_DETAIL, detailString);
                createValues.put(ItemContract.ItemEntry.COLUMN_ITEM_TOTAL_UNIT, mTotalUnitNumber);
                createValues.put(ItemContract.ItemEntry.COLUMN_ITEM_STATUS, mStatus);
                createValues.put(ItemContract.ItemEntry.COLUMN_ITEM_DATE, mDate);

                // 새 항목이므로 공급자에 새 항목을 삽입
                // 새 항목의 URI를 반환
                Uri newUri = getContentResolver().insert(ItemContract.ItemEntry.CONTENT_URI, createValues);

                // 삽입 성공 여부에 따라 토스트 메시지를 표시
                if (newUri == null) {
                    // 새 콘텐츠 URI가 null이면 삽입 오류가 발생
                    toast.showShortTimeToast(R.string.create_item_fail);
                } else {
                    //삽입에 성공하여 토스트를 표시
                    toast.showShortTimeToast(R.string.create_item_success);
                }
            } else {
                // 기존 항목의 ContentValues 개체 만들기
                ContentValues editValues = new ContentValues();
                editValues.put(ItemContract.ItemEntry.COLUMN_ITEM_NAME, nameString);
                editValues.put(ItemContract.ItemEntry.COLUMN_ITEM_DETAIL, detailString);
                editValues.put(ItemContract.ItemEntry.COLUMN_ITEM_TOTAL_UNIT, mTotalUnitNumber);

                // Set status
                if (mTotalUnitNumber > mUnitNumber) {
                    mStatus = ItemContract.ItemEntry.STATUS_TODO;
                } else if (mTotalUnitNumber == mUnitNumber) {
                    mStatus = ItemContract.ItemEntry.STATUS_DONE;
                } else {
                    toast.showShortTimeToast(R.string.editor_status_invalid);
                    return false;
                }
                editValues.put(ItemContract.ItemEntry.COLUMN_ITEM_STATUS, mStatus);
                editValues.put(ItemContract.ItemEntry.COLUMN_ITEM_DATE, mDate);

                int rowsAffected = getContentResolver().update(mCurrentItemUri, editValues, null, null);

                // 업데이트 성공 여부에 따라 토스트 메시지를 표시
                if (rowsAffected == 0) {
                    // 영향을 받은 행이 없으면 업데이트에 오류가 발생
                    toast.showShortTimeToast(R.string.update_item_fail);
                } else {
                    //업데이트에 성공하여 토스트를 표시
                    toast.showShortTimeToast(R.string.update_item_success);
                }
            }
            return true;
        }
    }

    //항목을 저장 또는 취소
    private void submit() {
        Button okButton = (Button) findViewById(R.id.add_ok_btn);
        Button cancelButton = (Button) findViewById(R.id.add_cancel_btn);

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            okButton.setBackgroundColor(Color.parseColor("#FF5722"));
            cancelButton.setBackgroundColor(Color.parseColor("#616161"));
        }

        //click ok button
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // success to insert item
                if (saveItem()) finish();
            }
        });

        //click cancel button
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemHasChanged) {
                    // 저장되지 않은 변경 사항이 있는 경우 사용자에게 경고하도록 대화 상자를 설정
                    // 다음을 확인하는 사용자를 처리할 클릭 수신기
                    // 변경사항은 폐기
                    DialogInterface.OnClickListener discardButtonClickListener =
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    finish();
                                }
                            };

                    // 사용자에게 저장하지 않은 변경 사항이 있음을 알리는 대화 상자 표시
                    showUnsavedChangesDialog(discardButtonClickListener);
                } else finish();
            }
        });
    }

    //이 메소드는 뒤로 버튼을 누르면 호출
    @Override
    public void onBackPressed() {
        if (!mItemHasChanged) {
            super.onBackPressed();
            return;
        }

        // 그렇지 않으면 저장되지 않은 변경사항이 있는 경우 사용자에게 경고하도록 대화상자를 설정
        // 변경 내용을 삭제할지 확인하는 사용자를 처리할 클릭 수신기
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // 사용자가 "취소" 단추를 눌러 현재 활동을 닫음
                        finish();
                    }
                };

        // 저장되지 않은 변경 사항이 있다는 대화 상자 표시
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // 편집기에 모든 항목 특성이 표시되므로 다음 항목이 포함된 투영을 정의
        // 항목 테이블의 모든 열
        String[] projection = {
                ItemContract.ItemEntry._ID,
                ItemContract.ItemEntry.COLUMN_ITEM_NAME,
                ItemContract.ItemEntry.COLUMN_ITEM_DETAIL,
                ItemContract.ItemEntry.COLUMN_ITEM_DATE,
                ItemContract.ItemEntry.COLUMN_ITEM_TOTAL_UNIT,
                ItemContract.ItemEntry.COLUMN_ITEM_UNIT,
                ItemContract.ItemEntry.COLUMN_ITEM_STATUS
        };

        //로더는 백그라운드 스레드에서 ContentProvider의 쿼리 메서드를 실행
        return new CursorLoader(this,
                mCurrentItemUri,         // 현재 항목의 컨텐츠 URI를 쿼리
                projection,             // 결과 커서에 포함할 열
                null,                   // 선택 절 없음
                null,                  // 선택 인수 없음
                null);                 // 기본 정렬 순서
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if (cursor.moveToFirst()) {
            int nameColumnIndex = cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_ITEM_NAME);
            int detailColumnIndex = cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_ITEM_DETAIL);
            int unitColumnIndex = cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_ITEM_UNIT);
            int totalUnitColumnIndex = cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_ITEM_TOTAL_UNIT);
            int dateColumnIndex = cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_ITEM_DATE);

            String name = cursor.getString(nameColumnIndex);
            String detail = cursor.getString(detailColumnIndex);
            String date = cursor.getString(dateColumnIndex);
            mUnitNumber = cursor.getInt(unitColumnIndex);
            int totalUnit = cursor.getInt(totalUnitColumnIndex);
            //int status = cursor.getInt(statusColumnIndex);

            mTotalUnitString = Integer.toString(totalUnit);

            mNameEditText.setText(name);
            mDetailEditText.setText(detail);
            mTotalUnitNumber = totalUnit;
            mTotalUnitTextView.setText(mTotalUnitString);
            mDate = date;
            mDateEditText.setText(mDate);

            getTotalUnitNumber();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.setText("");
        mDetailEditText.setText("");
        mTotalUnitTextView.setText(getString(R.string.reset_total_unit));
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // AlertDialog를 만듭니다.메시지 작성 및 설정, 수신기 클릭
        // 대화상자의 양극 및 음극 버튼을 선택
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.unsaved_change_msg));
        builder.setPositiveButton(getString(R.string.discard_btn), discardButtonClickListener);
        builder.setNegativeButton(getString(R.string.keep_editing_btn), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the item.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // AlertDialog 만들기 및 표시
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * this function change unit number in unit text view according to plus/minus image button.
     * minimum number: 1 / maximum number: 20
     * ut
     */
    private void getTotalUnitNumber() {
        getUnitImageButtonSrc();
        // increase unit number
        mUnitPlusImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mTotalUnitNumber < 20) {
                    mUnitPlusImageButton.setImageResource(R.drawable.ic_unit_plus_true);
                    mTotalUnitNumber++;
                    mTotalUnitString = Integer.toString(mTotalUnitNumber);
                    mTotalUnitTextView.setText(mTotalUnitString);
                }
                getUnitImageButtonSrc();
            }
        });

        // decrease unit number
        mUnitMinusImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mTotalUnitNumber > 1 && mTotalUnitNumber > mUnitNumber) {
                    mTotalUnitNumber--;
                    mTotalUnitString = Integer.toString(mTotalUnitNumber);
                    mTotalUnitTextView.setText(mTotalUnitString);
                }
                getUnitImageButtonSrc();
            }
        });
    }


    //이 기능은 장치 번호 범위에 따라 이미지 플러스/마이너스 버튼 src를 변경
    private void getUnitImageButtonSrc() {
        if (mTotalUnitNumber <= 1 || mTotalUnitNumber == mUnitNumber) {
            mUnitMinusImageButton.setImageResource(R.drawable.ic_unit_minus_false);
            mUnitPlusImageButton.setImageResource(R.drawable.ic_unit_plus_true);
        } else if (mTotalUnitNumber < 20) {
            mUnitMinusImageButton.setImageResource(R.drawable.ic_unit_minus_true);
            mUnitPlusImageButton.setImageResource(R.drawable.ic_unit_plus_true);
        } else {
            mUnitMinusImageButton.setImageResource(R.drawable.ic_unit_minus_true);
            mUnitPlusImageButton.setImageResource(R.drawable.ic_unit_plus_false);
        }
    }

    /**
     * It is a function of today's date.
     *
     * @return Return the current month and day.
     */
    public String getDate(Date date) {
        return new SimpleDateFormat(DATE_FORMAT, Locale.KOREA).format(date);
    }

    //텍스트 뷰에 표시 및 편집 텍스트 뷰에서 이름 문자를 카운트하는 함수
    public void countNameCharAndShow() {
        mNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                int length = charSequence.length();
                mNameCountTextView.setText(length + " / " + NAME_MAX_COUNT);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    //텍스트 뷰 편집 및 텍스트 뷰에 표시되는 상세 문자 수
    public void countDetailCharAndShow() {
        mDetailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                int length = charSequence.length();
                mDetailCountTextView.setText(length + " / " + DETAIL_MAX_COUNT);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    //이름 커서 가시성을 변경
    public void nameCursorVisibility() {
        mNameEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_DONE) {
                    mNameEditText.setCursorVisible(false);
                }
                return false;
            }
        });

        mNameEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                mNameEditText.setCursorVisible(true);
                return false;
            }
        });
    }

    //상세 커서 가시성을 변경
    public void detailCursorVisibility() {
        mDetailEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_DONE) {
                    mDetailEditText.setCursorVisible(false);
                }
                return false;
            }
        });

        mDetailEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                mDetailEditText.setCursorVisible(true);
                return false;
            }
        });
    }
}
