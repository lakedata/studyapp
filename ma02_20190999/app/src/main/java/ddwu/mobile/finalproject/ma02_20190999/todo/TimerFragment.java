package ddwu.mobile.finalproject.ma02_20190999.todo;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import ddwu.mobile.finalproject.ma02_20190999.R;
import ddwu.mobile.finalproject.ma02_20190999.data.ItemContract;
import ddwu.mobile.finalproject.ma02_20190999.data.ItemDbHelper;
import ddwu.mobile.finalproject.ma02_20190999.data.SharedPreferenceUtil;
import ddwu.mobile.finalproject.ma02_20190999.data.TimerDbUtil;


public class TimerFragment extends Fragment {
    /*Setting UI*/
    public static final String WORKTIME = "worktime";
    public static final String BREAKTIME = "breaktime";
    public static final String LONGBREAKTIME = "longbreaktime";
    public static final String SESSION = "session";
    public static final String CONTINUOUS_OPTION = "continuous";

    private TextView leftTime;
    private TextView mItemNameText;

    private ProgressBar progressBar;
    private Button stateButton;
    //타이머 서비스 구성 요소
    private TimerService mTimerService;

    boolean mServiceBound = false;
    private TimerHandler timerHandler;
    private int progressBarValue = 0;
    public int runTime; // minute

    private Intent intent;
    private Thread mReadThread;
    private int timerCounter;

    // Item info come from ListView
    private int mId, mStatus, mUnit, mTotalUnit;
    private String mName;

    public TimerFragment() {
    } //빈 생성자

    BroadcastReceiver mReceiver;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View timerView = inflater.inflate(R.layout.fragment_timer, container, false);

        // Timer 태그를 가져와 TimerTag로 설정
        String timerTag = getTag();
        ((TodoViewPager) getActivity()).setTimerTag(timerTag);

        mItemNameText = timerView.findViewById(R.id.job_name_txt);
        //progressBar button init
        progressBar = (ProgressBar) timerView.findViewById(R.id.progressBar);
        stateButton = (Button) timerView.findViewById(R.id.state_bttn_view);
        stateButton.setOnClickListener(stateChecker);
        stateButton.setEnabled(false);
        //Time Text Initialize
        leftTime = (TextView) timerView.findViewById(R.id.time_txt_view);
        //progressBar button init
        progressBar = (ProgressBar) timerView.findViewById(R.id.progressBar);
        progressBar.bringToFront();

        //동적 리시버 구현
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                onUnitFinish();
            }
        };

        //초기화 timer count
        timerCounter = 1;

        // 초기화 shared prefernce
        SharedPreferenceUtil.save(getContext(), "COUNT", timerCounter);

        return timerView;
    }

    public void onUnitFinish() {
        stopUpdateLeftTime();
        // UPDATE mCountTimner range 1..8
        // if Long Break Time has just finished, change to 1
        timerCounter++;
        int sessionNumber = SharedPreferenceUtil.get(getContext(), SESSION, 4) * 2;
        if (timerCounter == sessionNumber + 1)
            timerCounter = 1;

        SharedPreferenceUtil.save(getContext(), "COUNT", timerCounter);

        stateButton.setText("start");

        if (!isWorkTime())
            mUnit++;
        setTimerTimeName();

        storeUnitStatus();

        //단위 값이 업데이트된 후 ServiceFinished로 false로 설정
        TimerService.mTimerServiceFinished = false;
        onResume();
    }

    public void changeScreenToList() {
        TodoViewPager todoViewPager = (TodoViewPager) getActivity();
        (todoViewPager).getViewPager().setCurrentItem(0);
    }

    public void storeUnitStatus() {
        //mUnit 및 mStatus 저장
        if (isTaskComplete()) {
            TimerDbUtil.update(getContext(), mUnit, ItemContract.ItemEntry.STATUS_DONE, mId);
            if (isWorkTime()) {
                stateButton.setEnabled(false);
                changeScreenToList();
            }
        } else {
            TimerDbUtil.update(getContext(), mUnit, ItemContract.ItemEntry.STATUS_TODO, mId);
        }
    }

    public boolean isTaskComplete() {
        return mUnit == mTotalUnit;
    }

    public void setTimerTimeName() {
        timerCounter = SharedPreferenceUtil.get(getContext(), "COUNT", 1);
        if (isLongBreakTime()) {// assign time by work,short & long break
            runTime = SharedPreferenceUtil.get(getContext(), LONGBREAKTIME, 20);
            mItemNameText.setText("Long Break Time");
        } else if (isWorkTime()) {
            runTime = SharedPreferenceUtil.get(getContext(), WORKTIME, 25);
            mItemNameText.setText(mName);
        } else {
            runTime = SharedPreferenceUtil.get(getContext(), BREAKTIME, 5);
            mItemNameText.setText("Break Time");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        intent = new Intent(getActivity(), TimerService.class);

        if (TimerService.mTimerServiceFinished) {
            onUnitFinish();
        }
        getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        //getActivity().bindService(new Intent(getActivity(), TimerService.class), mConnection , Context.BIND_AUTO_CREATE);
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            mTimerService = ((TimerService.MyBinder) service).getService();
            mServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mTimerService = null;
            //mTimerService.stopCountNotification();
            progressBar.setProgress(0);
            timerHandler.removeMessages(0);
            mItemNameText.setText("");
            stateButton.setEnabled(false);
            mServiceBound = false;
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(mReceiver, new IntentFilter(mTimerService.strReceiver));
        if (SharedPreferenceUtil.get(getContext(), CONTINUOUS_OPTION, false) &&
                !isTaskComplete() && stateButton.getText().toString().equals("start")) {
            mTimerService.stopCountNotification();
            getActivity().stopService(intent); //stop service
            stopUpdateLeftTime();
            progressBar.setProgress(0);
            timerHandler.removeMessages(0);
            progressBarValue = 0; //must be set 0
            stateButton.setText(R.string.start);
            //mStatus를 작업관리(0)로 설정
            TimerDbUtil.update(getContext(), ItemContract.ItemEntry.STATUS_TODO, mId, false);
            setTimerTimeName();
            stateButton.performClick();
        }
    }

    public void setStatusToDo() {
        //mStatus를 작업관리(0)로 설정
        if (stateButton.getText().toString().equals("stop")) {
            TimerDbUtil.update(getContext(), ItemContract.ItemEntry.STATUS_TODO, mId, false);
        }
    }

    public boolean isLongBreakTime() {
        int session = SharedPreferenceUtil.get(getContext(), SESSION, 4) * 2;
        return timerCounter % session == 0;
    }

    public boolean isWorkTime() {
        return timerCounter % 2 == 1;
    }

    Button.OnClickListener stateChecker = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (stateButton.getText().toString().equals("start")) { // checked
                //목록 항목을 클릭하면 mUnit 초기화
                if (mServiceBound) {
                    /* set mStatus DB to DO(1)*/
                    TimerDbUtil.update(getContext(), ItemContract.ItemEntry.STATUS_DO, mId, false);
                    setTimerTimeName();
                    progressBar.setMax(runTime * 60 + 3); // setMax by sec
                    timerHandler = new TimerHandler();
                    mTimerService.setRunTimeTaskName(runTime, mItemNameText.getText().toString());
                    updateLeftTime();
                    stateButton.setText(R.string.stop);
                    timerHandler.sendEmptyMessage(0);
                }
            } else {
                mTimerService.stopCountNotification();
                getActivity().stopService(intent); //stop service
                stopUpdateLeftTime();
                progressBar.setProgress(0);
                timerHandler.removeMessages(0);
                progressBarValue = 0; //must be set 0
                stateButton.setText(R.string.start);
                //mStatus를 작업관리(0)
                TimerDbUtil.update(getContext(), ItemContract.ItemEntry.STATUS_TODO, mId, false);
            }
        }
    };


    public void updateLeftTime() {
        mReadThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (mTimerService.getRun()) {
                    //check out if it is still available
                    if (getActivity() == null)
                        return;

                    try {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                leftTime.setText(mTimerService.getTime());
                            }
                        });
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace(); //back to list
                    }
                }
            }
        });
        mReadThread.start();
    }

    public void stopUpdateLeftTime() {
        mReadThread.interrupt();
        leftTime.setText("");
    }

    public class TimerHandler extends Handler {
        TimerHandler() {
            super();
        }

        @Override
        public void handleMessage(android.os.Message msg) {
            if (mTimerService.getRun()) {
                progressBarValue++;
                progressBar.bringToFront();
                progressBar.setProgress(progressBarValue);
                timerHandler.sendEmptyMessageDelayed(0, 1000); //increase by sec
            } else { // Timer must be finished
                progressBar.setProgress(0);
                progressBarValue = 0;
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        setStatusToDo();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mServiceBound) {
            mTimerService.stopService(intent);
            getActivity().unbindService(mConnection);
            mServiceBound = false;
        }
    }

    public void setTimerFragment(int mId, int mStatus, int mUnit, int mTotalUnit, String mName) {
        this.mId = mId;
        this.mStatus = mStatus;
        this.mUnit = mUnit;
        this.mTotalUnit = mTotalUnit;
        this.mName = mName;
        this.stateButton.setEnabled(true);
        if (timerCounter % 2 == 1) {
            //breakTimer가 아직 실행되지 않았을 때 설정을 계속
            mItemNameText.setText(mName);
        }
    }

    public void setDeleteItemDisable(int dId) {
        //항목이 삭제되면
        if (dId == mId) {
           //버튼 비활성화
            stateButton.setEnabled(false);
            mItemNameText.setText("Deleted");
        }
    }
}
