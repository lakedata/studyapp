package ddwu.mobile.finalproject.ma02_20190999.library;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import ddwu.mobile.finalproject.ma02_20190999.R;

public class ListInfoLibrary extends AppCompatActivity {
//    String address;
    String libAddress;
    ArrayList<libraryDto> libResultList;
    String mapx; //메인에서 받아온 스키장의 경도
    String mapy; //메인에서 받아온 스키장의 위도
    String libId;
    libraryDto select;
    TextView libName;
    TextView op_top;
    TextView op_name_tv;
    TextView op_ctp_tv;
    TextView op_close_tv;
    TextView op_road_tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.library_info);

        libName = (TextView)findViewById(R.id.skiName);
        op_top = (TextView)findViewById(R.id.op_top);
        op_name_tv = (TextView)findViewById(R.id.op_name_tv);//도서관명
        op_ctp_tv = (TextView)findViewById(R.id.op_ctp_tv);//시도명
        op_close_tv = (TextView)findViewById(R.id.op_close_tv);//휴관일
        op_road_tv = (TextView)findViewById(R.id.op_road_tv);//소새지도로명주소

        Intent intent = getIntent();
        libId = intent.getStringExtra("libId");
        select = (libraryDto) intent.getSerializableExtra("selectList"); //용평이면 용평의 title,
        libName.setText(select.getLbrryNm());


        libAddress = getResources().getString(R.string.library_api_url) + "&lbrryNm=";
        libAddress += libId;
        new skiNetworkAsyncTask().execute(libAddress);

    }

    class NetworkAsyncTask extends AsyncTask<String, Integer, String> {
        public final static String TAG = "NetworkAsyncTask";
        public final static int TIME_OUT = 10000;
        ProgressDialog progressDlg;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDlg = ProgressDialog.show(ListInfoLibrary.this, "도서관 찾는 중...", "Downloading...");     // 진행상황 다이얼로그 출력
        }
        @Override
        protected String doInBackground(String... strings) {
            String address = strings[0];
            StringBuilder result = new StringBuilder();
            BufferedReader br = null;
            HttpURLConnection conn = null;

            try {
                URL url = new URL(address);
                conn = (HttpURLConnection) url.openConnection();

                if (conn != null) {
                    conn.setConnectTimeout(TIME_OUT);
                    conn.setUseCaches(false);
                    if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                        for (String line = br.readLine(); line != null; line = br.readLine()) {
                            result.append(line + '\n');
                        }
                    }
                }

            } catch (MalformedURLException ex) {
                ex.printStackTrace();
                cancel(false);
            } catch (Exception ex) {
                ex.printStackTrace();
                cancel(false);
            } finally {
                try {
                    if (br != null) br.close();
                    if (conn != null) conn.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return result.toString();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            Toast.makeText(ListInfoLibrary.this, "Error!!!", Toast.LENGTH_SHORT).show();
            progressDlg.dismiss();
        }
    }

    class skiNetworkAsyncTask extends AsyncTask<String, Integer, String> {
        public final static String TAG = "NetworkAsyncTask";
        public final static int TIME_OUT = 10000;
        ProgressDialog progressDlg;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDlg = ProgressDialog.show(ListInfoLibrary.this, "Wait", "Downloading...");     // 진행상황 다이얼로그 출력
        }
        @Override
        protected String doInBackground(String... strings) {
            String address = strings[0];
            StringBuilder result = new StringBuilder();
            BufferedReader br = null;
            HttpURLConnection conn = null;

            try {
                URL url = new URL(address);
                conn = (HttpURLConnection) url.openConnection();

                if (conn != null) {
                    conn.setConnectTimeout(TIME_OUT);
                    conn.setUseCaches(false);
                    if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                        for (String line = br.readLine(); line != null; line = br.readLine()) {
                            result.append(line + '\n');
                        }
                    }
                }

            } catch (MalformedURLException ex) {
                ex.printStackTrace();
                cancel(false);
            } catch (Exception ex) {
                ex.printStackTrace();
                cancel(false);
            } finally {
                try {
                    if (br != null) br.close();
                    if (conn != null) conn.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return result.toString();
        }
        @Override
        protected void onPostExecute(String result) {
            libraryXmlparser parser = new libraryXmlparser();
            libResultList = parser.parse(result);
            op_top.setText(libResultList.get(0).getLbrryNm() + "에서 공부해보세요!\n");
            op_name_tv.setText(libResultList.get(0).getLbrryNm());
            op_ctp_tv.setText(select.getCtprvnNm());
            op_close_tv.setText(libResultList.get(0).getCloseDay());
            op_road_tv.setText(libResultList.get(0).getRdnmadr());
            progressDlg.dismiss();
        }


        @Override
        protected void onCancelled() {
            super.onCancelled();
            Toast.makeText(ListInfoLibrary.this, "Error!!!", Toast.LENGTH_SHORT).show();
            progressDlg.dismiss();
        }
    }
}
