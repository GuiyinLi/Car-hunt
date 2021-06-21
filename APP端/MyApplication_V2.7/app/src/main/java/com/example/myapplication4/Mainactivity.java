package com.example.myapplication4;


import android.app.AlertDialog;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.IndoorRouteResult;
import com.baidu.mapapi.search.route.MassTransitRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteResult;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.IOException;

import overlayutil.WalkingRouteOverlay;

import static com.example.myapplication4.R.drawable.abc_btn_check_to_on_mtrl_015;
import static com.example.myapplication4.R.drawable.abc_cab_background_internal_bg;
import static com.example.myapplication4.R.drawable.abc_cab_background_top_material;
import static com.example.myapplication4.R.drawable.abc_cab_background_top_mtrl_alpha;
import static com.example.myapplication4.R.drawable.abc_ic_ab_back_mtrl_am_alpha;
import static com.example.myapplication4.R.drawable.abc_ic_clear_mtrl_alpha;
import static com.example.myapplication4.R.drawable.abc_ic_search_api_mtrl_alpha;
import static com.example.myapplication4.R.drawable.location;

public class Mainactivity extends ActionBarActivity {

    private MapView mMapView;     // 定义百度地图组件
    private BaiduMap mBaiduMap;   // 定义百度地图对象
    Handler handler = new Handler();//timer control
    private boolean isFirstLoc2 = true;  //button2 定义第一次启动
    private boolean isFirstLoc1 = true;  //button1 定义第一次启动
    private MyLocationConfiguration.LocationMode locationMode;  //定义当前定位模式
    private RoutePlanSearch mSearch;
    private EditText et;
    private TextView text;
    HttpClient client;
    boolean running = false;   //计时标志位  !!!!!
    private int seconds = 0;    //按钮含义
    public Button button2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());   //初始化地图SDK
        setContentView(R.layout.activity_main);
        mMapView = (MapView) findViewById(R.id.bmapView);  //获取地图组件
        mMapView.removeViewAt(1);//delete baidu map logo
        mBaiduMap = mMapView.getMap();  //获取百度地图对象
        mSearch = RoutePlanSearch.newInstance();
        client=new DefaultHttpClient();
        //获取系统的LocationManager对象
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        final Runnable runnable=new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                if(running){
                    String url="http://39.97.235.93:80?execute=00";
                    readNet(url);
                }
                else{
                    handler.removeCallbacks(this);
                }
                handler.postDelayed(this, 10000);
            }
        };

        final Button button2 = (Button)findViewById(R.id.button) ;
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if(running){
                    handler.removeCallbacks(runnable);
                    ClickReset();
                    button2.setText("一键寻车");
                }
                else{
                    ClickStart();
                    handler.post(runnable);
                    button2.setText("结束寻车");
                }
            }
        });

        //获取当前位置
        Button button =(Button)findViewById(R.id.tton3);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //获取系统的LocationManager对象
                if(running){
                    new  AlertDialog.Builder(Mainactivity.this)
                            .setTitle("警告")
                            .setIcon(R.drawable.warning)
                            .setMessage("请先结束一键寻车!")
                            .setPositiveButton("确认", null)
                            .setCancelable(false)
                            .show();
                }
                else{//一键寻车状态下不允许点击获取现在的位置
                isFirstLoc1=true;
                LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                //设置每一秒获取一次location信息
                locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,      //GPS定位提供者
                        1000,       //更新数据时间为1秒
                        1,      //位置间隔为1米
                        //位置监听器
                        new LocationListener() {  //GPS定位信息发生改变时触发，用于更新位置信息
                            @Override
                            public void onLocationChanged(Location location) {
                                //GPS信息发生改变时，更新位置
                                locationUpdates(location);
                            }
                            @Override
                            //位置状态发生改变时触发
                            public void onStatusChanged(String provider, int status, Bundle extras) {
                            }
                            @Override
                            //定位提供者启动时触发
                            public void onProviderEnabled(String provider) {
                            }
                            @Override
                            //定位提供者关闭时触发
                            public void onProviderDisabled(String provider) {
                            }
                        });

                //从GPS获取最新的定位信息
                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                locationUpdates(location);    //将最新的定位信息传递给创建的locationUpdates()方法中
                }
            }
        });
    }


    public void locationUpdates(Location location) {  //获取指定的查询信息
        //如果location不为空时
        if (location != null) {

            LatLng ll = new LatLng(location.getLatitude(), location.getLongitude()); //获取用户当前经纬度
            //Log.i("Location", "纬度：" + location.getLatitude() + " | 经度：" + location.getLongitude());
            if (isFirstLoc1) {  //如果是第一次定位,就定位到以自己为中心
                MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);  //更新坐标位置
                mBaiduMap.animateMapStatus(u);                            //设置地图位置
                isFirstLoc1 = false;                                      //取消第一次定位
                mBaiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(new MapStatus.Builder().zoom(19).build()));
            }
            // 构造定位数据
            MyLocationData locData = new MyLocationData.Builder().accuracy(location.getAccuracy())
                    .direction(0) // 此处设置开发者获取到的方向信息，顺时针0-360
                    .latitude(location.getLatitude())//设置纬度坐标
                    .longitude(location.getLongitude())//设置经度坐标
                    .build();
            // 设置定位数据
            mBaiduMap.setMyLocationData(locData);

            //设置自定义定位图标
            BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromResource(abc_cab_background_top_material);
            locationMode=MyLocationConfiguration.LocationMode.NORMAL;//设置定位模式-following mode
            //位置构造方式，将定位模式，定义图标添加其中
            MyLocationConfiguration config = new MyLocationConfiguration(locationMode, true, bitmapDescriptor);
            mBaiduMap.setMyLocationConfiguration(config); //地图显示定位图标


        } else { //否则输出空信息
            Log.i("Location", "没有获取到GPS信息");
        }
    }

    @Override
    protected void onStart() {  //地图定位与Activity生命周期绑定
        super.onStart();
        mBaiduMap.setMyLocationEnabled(true);//开启定位图层
    }


    @Override
    protected void onStop() {  //停止地图定位
        super.onStop();
        mBaiduMap.setMyLocationEnabled(false);//停止定位图层
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onDestroy() {  //销毁地图
        mMapView.onDestroy();
        mMapView = null;
        super.onDestroy();
    }


    public void readNet(String url){

        new AsyncTask<String,Void, Void>(){
            @Override
            protected Void doInBackground(String... arg0) {
                //  StringBuffer urlString = new StringBuffer(arg0[0]);
                // urlString.append(arg0[1]);
                String urlString=arg0[0];
                //   System.out.println(urlString);

                HttpGet get =new HttpGet(urlString);

                try {
                    HttpResponse  response= client.execute(get);
                    String value = EntityUtils.toString(response.getEntity());
                    //   @SuppressWarnings("rawtypes")
                    //   Map mapsMap = new HashMap();
                    Map mapsMap = (Map) JSON.parse(value);
                    float   longitude = Float.valueOf((String)(mapsMap.get("longitude")));
                    float   latitude = Float.valueOf((String)(mapsMap.get("latitude"))) ;
                    float   direction =  Float.valueOf((String)(mapsMap.get("direction"))) ;
                    locationUpdate(longitude, latitude, direction);

                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute(url);
    }

    //寻车
    public void locationUpdate(float longitude,float latitude,float direction) {  //获取指定的查询信息

        LatLng ll = new LatLng(latitude,longitude); //获取用户当前经纬度
        Log.i("Location", "纬度：" + latitude + " | 经度：" + longitude);
        if (isFirstLoc2) {  //如果是第一次定位,就定位到以自己为中心
            MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);  //更新坐标位置
            mBaiduMap.animateMapStatus(u);                            //设置地图位置
            isFirstLoc2 = false;                                      //取消第一次定位
            mBaiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(new MapStatus.Builder().zoom(19).build()));
        }
        // 构造定位数据
        MyLocationData locData = new MyLocationData.Builder().accuracy(1)
                // 此处设置开发者获取到的方向信息，顺时针0-360
                .direction(direction)
                .latitude(latitude)//设置纬度坐标
                .longitude(longitude)//设置经度坐标
                .build();
        // 设置定位数据
        mBaiduMap.setMyLocationData(locData);
        //设置自定义定位图标
        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromResource(abc_cab_background_top_material);
        locationMode=MyLocationConfiguration.LocationMode.NORMAL;//设置定位模式-LUOPAN
        //位置构造方式，将定位模式，定义图标添加其中
        MyLocationConfiguration config = new MyLocationConfiguration(locationMode, true, bitmapDescriptor);
        mBaiduMap.setMyLocationConfiguration(config); //地图显示定位图标
    }

    // 计时重置
    public void ClickReset() {
        running = false;
        //seconds = 0;
    }
    // 计时开始/继续
    public void ClickStart() {
        isFirstLoc2=true;
        running = true;
    }
}


