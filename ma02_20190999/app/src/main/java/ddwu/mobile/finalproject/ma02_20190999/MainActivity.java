package ddwu.mobile.finalproject.ma02_20190999;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import ddwu.mobile.finalproject.ma02_20190999.library.librarySearch;
import ddwu.mobile.finalproject.ma02_20190999.todo.TodoViewPager;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.studyCheck:
                Intent intentCheck = new Intent(this, TodoViewPager.class);
                startActivity(intentCheck);
                break;
            case R.id.studyPlace:
                Intent intentPlace = new Intent(this, librarySearch.class);
                startActivity(intentPlace);
                break;
            case R.id.studyPersonal:
                Intent intentPersonal = new Intent(this, PersonalActivity.class);
                startActivity(intentPersonal);
                break;
            case R.id.studyCalender:
                Intent intentCalender = new Intent(this, CalenderActivity.class);
                startActivity(intentCalender);
                break;
            case R.id.studyTimer:
                Intent intentTimer = new Intent(this, TimerActivity.class);
                startActivity(intentTimer);
                break;
        }
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
                        .setTitle("앱 종료")
                        .setMessage("앱을 종료하시겠습니까?")
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