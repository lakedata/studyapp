package ddwu.mobile.finalproject.ma02_20190999;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class IntroActivity extends AppCompatActivity {
    Button btn1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        btn1 = (Button) findViewById(R.id.btnHello);

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = "안녕하세요, 두미 개발자 이지영입니다. 두미와 함께 처음과 끝을 함께해봐요:)";
                Toast.makeText(IntroActivity.this, text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onMyExitBtnClick(View v) {
        finish();
    }
}
