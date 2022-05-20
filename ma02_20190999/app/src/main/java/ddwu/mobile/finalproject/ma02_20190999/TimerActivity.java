package ddwu.mobile.finalproject.ma02_20190999;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import ddwu.mobile.finalproject.ma02_20190999.IntroActivity;
import ddwu.mobile.finalproject.ma02_20190999.MainActivity;
import ddwu.mobile.finalproject.ma02_20190999.R;

public class TimerActivity extends AppCompatActivity {
    private Chronometer chronometer;
    private boolean running;
    private long pauseOffset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);
        chronometer = findViewById(R.id.chronometer);
        chronometer.setFormat("시간: %s");
        Button startBtn = findViewById(R.id.start_btn);
        Button stopBtn = findViewById(R.id.stop_btn);
        Button resetBtn = findViewById(R.id.reset_btn); //시작
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!running) {
                    chronometer.setBase(SystemClock.elapsedRealtime() - pauseOffset);
                    chronometer.start();
                    running = true;
                }
            }
        }); //중지
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (running) {
                    chronometer.stop();
                    pauseOffset = SystemClock.elapsedRealtime() - chronometer.getBase();
                    running = false;
                }
            }
        }); //초기화
        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chronometer.setBase(SystemClock.elapsedRealtime());
                pauseOffset = 0;
                chronometer.stop();
                running = false;
            }
        });
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
                        .setTitle("타이머 종료")
                        .setMessage("타이머을 종료하시겠습니까?")
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
