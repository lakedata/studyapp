package ddwu.mobile.finalproject.ma02_20190999.library;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
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
import java.util.ArrayList;
import java.util.List;

import ddwu.mobile.finalproject.ma02_20190999.IntroActivity;
import ddwu.mobile.finalproject.ma02_20190999.MainActivity;
import ddwu.mobile.finalproject.ma02_20190999.R;

public class librarySearch extends AppCompatActivity {

    Intent intent;
    ListView lvList;
    String address; //api url
    ArrayList<libraryDto> resultList; //도서관정보 arraylist
    MyCustomAdapter MyAdapter;
    Geocoder geocoder;

    double mLat;
    double mLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_library_main);
        lvList = (ListView) findViewById(R.id.list_library);
        resultList = new ArrayList();
        MyAdapter = new MyCustomAdapter(this, R.layout.librarycustom_view, resultList);
        lvList.setAdapter(MyAdapter);
        geocoder = new Geocoder(this);
        //addList = null;


//        address = getResources().getString(R.string.library_api_url);
//        new NetworkAsyncTask().execute(address);
        address = getResources().getString(R.string.library_api_url);
        new NetworkAsyncTask().execute(address);

        //리스트를 클릭하면 클릭한 도서관 상세 정보 출력
        lvList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
                Intent intent = new Intent(librarySearch.this, ListInfoLibrary.class);
                libraryDto selectList = resultList.get(pos);
                intent.putExtra("libId", resultList.get(pos).getLbrryNm());
                intent.putExtra("selectList", selectList);

//                startActivity(intent);
//                Intent intent = new Intent(libraryKeywordSearch.this, ListInfoLibrary.class);
//                libraryDto selectList = resultList.get(pos);
//                intent.putExtra("skiId", resultList.get(pos).getContentid());
//                intent.putExtra("selectList", selectList);
//
//                startActivity(intent);
                intent.putExtra("resultList", resultList);
                startActivity(intent);
            }
        });
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_keyword:
                intent = new Intent(librarySearch.this, libraryKeywordSearch.class);
                startActivity(intent);
                break;
            case R.id.btn_map_sc:
                intent = new Intent(librarySearch.this, libraryMapSearch.class);
                Log.d("btnmapresultList", "resultList값" + resultList);
                intent.putExtra("resultList", resultList);
                startActivity(intent);
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

//            if (!isOnline()) {
//                Toast.makeText(CctvActivity.this, "네트워크가 설정되어 있지 않습니다.", Toast.LENGTH_SHORT).show();
//                return;
//            } // 네트워크 환경 조사


            progressDlg = ProgressDialog.show(librarySearch.this, "도서관 검색 중...", "잠시만 기다려주세요.");     // 진행상황 다이얼로그 출력
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
            resultList = parser.parse(result);//파싱 수행

            Log.d("onPostExeresultList", "resultList값" + resultList);

            for (int i = 0; i < resultList.size(); i++) {
                if (resultList.get(i).getMapX() == null || resultList.get(i).getMapY() == null) {
                    try {
                        List<Address> addList = geocoder.getFromLocationName(resultList.get(i).getRdnmadr(), 1);
                        mLat = addList.get(0).getLatitude();
                        mLng = addList.get(0).getLongitude();
                        Log.d("도서관이름", "도서관이름" + resultList.get(i).getLbrryNm());
                        Log.d("위도경도", "주소로 얻음" + mLat + "/" + mLng);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    resultList.get(i).setMapX(String.valueOf(mLat));
                    resultList.get(i).setMapY(String.valueOf(mLng));
                }
            }

            MyAdapter.setList(resultList);
            MyAdapter.notifyDataSetChanged();

            progressDlg.dismiss();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            Toast.makeText(librarySearch.this, "Error!!!", Toast.LENGTH_SHORT).show();
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