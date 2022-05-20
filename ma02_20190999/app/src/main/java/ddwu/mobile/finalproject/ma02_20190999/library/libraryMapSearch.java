package ddwu.mobile.finalproject.ma02_20190999.library;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ddwu.mobile.finalproject.ma02_20190999.IntroActivity;
import ddwu.mobile.finalproject.ma02_20190999.MainActivity;
import ddwu.mobile.finalproject.ma02_20190999.R;

public class libraryMapSearch extends AppCompatActivity {
    private final static int ZOOM_LEVEL = 10;                   // 지도 확대 배율
    private final static int PERMISSION_REQ_CODE = 100;         // permission 요청 코드
    private GoogleMap mGoogleMap;           // 구글맵 객체 저장 멤버 변수
    Intent intent;
    Geocoder geocoder;
    ArrayList<libraryDto> resultList = null; //결과 받아옴
    List<Address> addList = null;
    private LocationManager locmanager; //위치정보 매니저
    private Location myLocation; //나의 위치 받으옴
    private Marker centerMarker;
    private MarkerOptions options;
    private ArrayList<Marker> markerList;
    private MarkerOptions poiMarkerOptions;
    String searchString;
    EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_librarymap);

        editText = (EditText) findViewById(R.id.et_search);
        geocoder = new Geocoder(this);
        locmanager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        markerList = new ArrayList<Marker>();


        //인텐트로 전달된 ArrayList 가져오기
        intent = getIntent();
        resultList = (ArrayList<libraryDto>) intent.getSerializableExtra("resultList");

        //구글맵 준비
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(mapReadyCallback);

        if (ActivityCompat.checkSelfPermission(libraryMapSearch.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(libraryMapSearch.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(libraryMapSearch.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQ_CODE);
            return;
        }
        myLocation = locmanager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);

    }


    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_s:
                searchString = editText.getText().toString();

                for (int i = 0; i < resultList.size(); i++) { //바꿈
                    if ((resultList.get(i).getLbrryNm()).contains(searchString))//검색어와 타이틀이 맞는 부분이 있으면
                    {
                        //위치 이동
                        markerList.get(i).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));//색변경
                        Toast.makeText(libraryMapSearch.this, resultList.get(i).getLbrryNm() + "의 위치입니다.", Toast.LENGTH_SHORT).show();

                        break;
                    }
                }
                break;
            case R.id.my_position: //누르면 내 위치로 이동
                Toast.makeText(libraryMapSearch.this, "현재 위치로 이동합니다. 오늘도 화이팅୧(˵°~°˵)୨]", Toast.LENGTH_SHORT).show();

                locationUpdate();

//                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                    ActivityCompat.requestPermissions(this, new String[] {android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION }, PERMISSION_REQ_CODE);
//                    return;
//                }
//                locmanager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 3000, 5, locationListener);
                break;
        }
    }

    private void locationUpdate() {
        if (checkPermission()) {
            locmanager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 5, locationListener);
        }
    }

    /*필요 permission요청*/
    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQ_CODE);
                return false;
            }
        }
        return true;
    }

    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            LatLng myLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, 10));//사용자 위치 이동
            centerMarker.setPosition(myLatLng);
//            try {
//                addList = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            options = new MarkerOptions();
//            options.position(myLatLng);
//            options.title("사용자 위치");
//            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));//주석할 수 도
//            //options.snippet(addList.get(0).getAddressLine(0));
//
//            //지도에 마커 추가 후 추가한 마커 정보 등록
//            centerMarker = mGoogleMap.addMarker(options);
//            centerMarker.showInfoWindow();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    };

    /*필요 permission요청*/
//    private boolean checkPermission() {
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
//                    != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQ_CODE);
//                return false;
//            }
//        }
//        return true;
//    }

    OnMapReadyCallback mapReadyCallback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {
//            로딩한 구글맵을 보관

            mGoogleMap = googleMap;//화면 상 구굴 객체 얻음

            LatLng lastLatLng;
            if (myLocation != null) {
                lastLatLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
            } else {
                lastLatLng = new LatLng(37.606320, 127.041808);
            }

            //이동
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastLatLng, ZOOM_LEVEL));//초기에 사용자 위치

            MarkerOptions options = new MarkerOptions();
            options.position(lastLatLng);
            options.title("현재 위치");
            options.snippet("넌 언제나 잘하고 있어 :)");
            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));

            centerMarker = mGoogleMap.addMarker(options);
            centerMarker.showInfoWindow();


            poiMarkerOptions = new MarkerOptions();//도서관 리스트 마커 표시
            LatLng poiPosition;
            for (int i = 0; i < resultList.size(); i++) { //바꿈
                poiPosition = new LatLng(Double.parseDouble(resultList.get(i).getMapX()), Double.parseDouble(resultList.get(i).getMapY()));
                poiMarkerOptions.position(poiPosition);
                poiMarkerOptions.title(resultList.get(i).getLbrryNm());
                poiMarkerOptions.snippet(resultList.get(i).getRdnmadr());
                poiMarkerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                markerList.add(mGoogleMap.addMarker(poiMarkerOptions));
            }

            for (int i = 0; i < markerList.size(); i++)
                markerList.get(i).showInfoWindow();

            mGoogleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(Marker marker) {
                    String markerId = marker.getId();
                    String[] values = markerId.split("m"); //마커 아이디는 m으로 시작해
                    Intent intent = new Intent(libraryMapSearch.this, ListInfoLibrary.class);
                    libraryDto selectList = resultList.get(Integer.parseInt(values[1])-1);
                    intent.putExtra("libId", resultList.get(Integer.parseInt(values[1])-1).getLbrryNm());
                    intent.putExtra("selectList", selectList);
                    Log.d("맵도서관아이디" , resultList.get(Integer.parseInt(values[1])-1).getLbrryNm());
                    startActivity(intent);
                }
            });
        }
    };

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