package ddwu.mobile.finalproject.ma02_20190999.todo;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import ddwu.mobile.finalproject.ma02_20190999.IntroActivity;
import ddwu.mobile.finalproject.ma02_20190999.MainActivity;
import ddwu.mobile.finalproject.ma02_20190999.R;
import ddwu.mobile.finalproject.ma02_20190999.data.SharedPreferenceUtil;


public class TodoViewPager extends AppCompatActivity {
    // ViewPager tabs사이 이동
    ViewPager viewPager;

    //Fragment 태크
    String mTimerTag;
    String mListTag;
    String mStatisticsTag;

    //Getter/Setter
    public void setTimerTag(String timerTag) {
        mTimerTag = timerTag;
    }

    public String getTimerTag() {
        return mTimerTag;
    }

    public void setListTag(String listTag) {
        mListTag = listTag;
    }

    public String getListTag() {
        return mListTag;
    }

    public String getStatisticsTag() {
        return mStatisticsTag;
    }

    public void setStatisticsTag(String statisticsTag) {
        mStatisticsTag = statisticsTag;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_todo);
        // Add tabs
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.list_selector).setText(R.string.tab_list));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.timer_selector).setText(R.string.tab_timer));
//        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.statistics_selector).
//                setText(R.string.tab_statistics));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.setting_selector).
                setText(R.string.tab_setting));
//        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.about_selector).setText(R.string.tab_about));

        // 선택한 탭으로 탐색하기 위한 ViewPager
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setBackgroundColor(Color.WHITE);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // Hide keyboard
                ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(viewPager.getWindowToken(), 0);
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        // Setting pager adapter
        SimpleFragmentPagerAdapter adapter
                = new SimpleFragmentPagerAdapter(getSupportFragmentManager(),
                tabLayout.getTabCount());
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(4);

        showIntroSlideWhenFirst();
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
                        .setTitle("공부체크 종료")
                        .setMessage("공부체크를 종료하시겠습니까?")
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

    /*@return  Return existing viewpager*/
    public ViewPager getViewPager() {
        if (null == viewPager) {
            viewPager = (ViewPager) findViewById(R.id.viewpager);
        }
        return viewPager;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        TimerFragment timerFragment = (TimerFragment) getSupportFragmentManager().findFragmentByTag(mTimerTag);
        timerFragment.setStatusToDo();
    }

    void showIntroSlideWhenFirst() {
        // thread선언 환경설정을 검사
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                //SharedPreferences 초기화
                SharedPreferences getPrefs = PreferenceManager
                        .getDefaultSharedPreferences(getBaseContext());

                //  Create a new boolean 및 환경설정을 만들고 true로 설정
                boolean isFirstStart = getPrefs.getBoolean("firstStart", true);

                // 활동 시작 전
                if (isFirstStart) {
                    //  Launch app intro
                    final Intent mainActivity = new Intent(TodoViewPager.this, MainActivity.class);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            startActivity(mainActivity);
                        }
                    });

                    // new preferences editor
                    SharedPreferences.Editor e = getPrefs.edit();

                    // false 설정
                    e.putBoolean("firstStart", false);

                    // 변경 사항 적용
                    e.apply();
                }
            }
        });

        // Start the thread
        thread.start();
    }
}