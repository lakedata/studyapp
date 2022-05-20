package ddwu.mobile.finalproject.ma02_20190999;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class PersonalActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    private TextView tv;
    private CheckBox cb1;
    private CheckBox cb2;
    private CheckBox cb3;
    private CheckBox cb4;

    Button save_btn;
    Button del_btn;
    Button upd_btn;
    EditText editText;
    TextView textView;
    String text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal);

        cb1 = (CheckBox) findViewById(R.id.checkBox1);
        cb2 = (CheckBox) findViewById(R.id.checkBox2);
        cb3 = (CheckBox) findViewById(R.id.checkBox3);
        cb4 = (CheckBox) findViewById(R.id.checkBox4);
        tv = (TextView) findViewById(R.id.textView2);
        cb1.setOnCheckedChangeListener(this);
        cb2.setOnCheckedChangeListener(this);
        cb3.setOnCheckedChangeListener(this);
        cb4.setOnCheckedChangeListener(this);

        //자소서입력
        save_btn = findViewById(R.id.button);
        upd_btn = findViewById(R.id.updbutton);
        del_btn = findViewById(R.id.delbutton);
        editText = findViewById(R.id.edit_text);
        textView = findViewById(R.id.text_view);

        //저장
        save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                text = editText.getText().toString();

                if(text != null)
                    textView.setText(text);

                editText.setText("");
            }
        });
        //수정클릭
        upd_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editText.setText(text);
                textView.setText(editText.getText());
            }

        });
        //삭제클릭
        del_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editText.setText("");
                textView.setText("");
            }
        });
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        // 체크박스를 클릭해서 상태가 바꾸었을 경우 호출되는 콜백 메서드

        String result = " "; // 문자열 초기화는 빈문자열로 하자
//        if(isChecked) tv.setText("체크했음");
//        else tv.setText("체크안했음");
        // 혹은 3항연산자
        //tx.setText(isChecked?"체크했음":"체크안했음");

        if (cb1.isChecked()) result += "★";
        if (cb2.isChecked()) result += "★";
        if (cb3.isChecked()) result += "★";
        if (cb4.isChecked()) result += "★";

        tv.setText("체크항목: " + result);
    }

    @Override
    protected void onPause() { // Activity가 보이지 않을때 값을 저장한다.
        super.onPause();
        saveState();
    }

    @Override
    protected void onStart() {  // Activity가 보이기 시작할때 값을 저장한다.
        super.onStart();
        restoreState();
        if(text != null)
            textView.setText(text);

    }

    protected void saveState(){ // 데이터를 저장한다.
        SharedPreferences pref = getSharedPreferences("pref", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("text", text);

        editor.commit();


    }
    protected void restoreState(){  // 데이터를 복구한다.
        SharedPreferences pref = getSharedPreferences("pref", Activity.MODE_PRIVATE);
        if((pref!=null) && (pref.contains("text"))){
            text = pref.getString("text", "");
        }

    }
    protected void clearPref(){  // sharedpreference에 쓰여진 데이터 지우기
        SharedPreferences pref = getSharedPreferences("pref", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        text = null;
        editor.commit();
    }

    //optionMenu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.developerIntro:
                Intent intentIntro = new Intent(this, IntroActivity.class);
                startActivity(intentIntro);
                break;
            case R.id.main:
                Intent intentMain = new Intent(this, MainActivity.class);
                startActivity(intentMain);
                break;
            case R.id.finish:
                new AlertDialog.Builder(this)
                        .setTitle("자소서 체크 종료")
                        .setMessage("자소서 체크를 종료하시겠습니까?")
                        .setPositiveButton("종료", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        .setNegativeButton("취소", null)
                        .show();
                break;
        }
        return true;
    }
}
