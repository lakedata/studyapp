package ddwu.mobile.finalproject.ma02_20190999.library;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import ddwu.mobile.finalproject.ma02_20190999.IntroActivity;
import ddwu.mobile.finalproject.ma02_20190999.MainActivity;
import ddwu.mobile.finalproject.ma02_20190999.R;

public class libraryKeywordSearch extends AppCompatActivity {

    EditText ettargetKeyword;
    String targetKeyword;
    ListView lvList; //메인 액티비티 리스트뷰
    String address; //api url
    ArrayList<libraryDto> resultList; //도서관정보arraylist
    MyCustomAdapter MyAdapter;
    Button btn_sc; //버튼 누르면 도서관상세정보페이지로
    List<Address> addList;
    Geocoder geocoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_librarykeyword);

        ettargetKeyword = (EditText) findViewById(R.id.et_sear);
        btn_sc = (Button) findViewById(R.id.btn_search);
        lvList = (ListView) findViewById(R.id.lvList);

        resultList = new ArrayList();
        MyAdapter = new MyCustomAdapter(this, R.layout.librarycustom_view, resultList);
        lvList.setAdapter(MyAdapter);
        geocoder = new Geocoder(this);

        //리스트를 클릭하면 클릭한 도서관 상세 정보 출력
        lvList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
                Intent intent = new Intent(libraryKeywordSearch.this, ListInfoLibrary.class);
                libraryDto selectList = resultList.get(pos);
                intent.putExtra("libId", resultList.get(pos).getLbrryNm());
                intent.putExtra("selectList", selectList);

                startActivity(intent);
            }
        });
        address = getResources().getString(R.string.library_api_url) + "&ctprvnNm=";
    }

    public void onClick(View v) { //btn_search누를시 도서관 시도명으로 검색 ex)서울특별시, 전라북도
        switch (v.getId()) {
            case R.id.btn_search:
                targetKeyword = ettargetKeyword.getText().toString(); // 시도명(키워드)
                if (targetKeyword.equals(""))
                    targetKeyword = ettargetKeyword.getHint().toString();
                new NetworkAsyncTask().execute(address);
                break;
        }
    }

    class NetworkAsyncTask extends AsyncTask<String, Integer, String> {

        public final static String TAG = "NetworkAsyncTask";
        public final static int TIME_OUT = 10000;

        ProgressDialog progressDlg;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDlg = ProgressDialog.show(libraryKeywordSearch.this, "Wait", "Downloading...");     // 진행상황 다이얼로그 출력
        }

        @Override
        protected String doInBackground(String... strings) {
            String address = strings[0];
            StringBuilder result = new StringBuilder();
            BufferedReader br = null;
            HttpURLConnection conn = null;

            try {
                String keyword = URLEncoder.encode(targetKeyword, "UTF-8"); //국문은 인코딩이 필요하여 추가.
                URL url = new URL(address + keyword);
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
            resultList = parser.parse(result);

            MyAdapter.setList(resultList);
            MyAdapter.notifyDataSetChanged();

            progressDlg.dismiss();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            Toast.makeText(libraryKeywordSearch.this, "Error!!!", Toast.LENGTH_SHORT).show();
            progressDlg.dismiss();
        }
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
