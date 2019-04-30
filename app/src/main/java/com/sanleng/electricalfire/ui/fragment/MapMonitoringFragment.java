package com.sanleng.electricalfire.ui.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ZoomControls;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.walknavi.WalkNavigateHelper;
import com.baidu.mapapi.walknavi.adapter.IWEngineInitListener;
import com.baidu.mapapi.walknavi.adapter.IWRoutePlanListener;
import com.baidu.mapapi.walknavi.model.WalkRoutePlanError;
import com.baidu.mapapi.walknavi.params.WalkNaviLaunchParam;
import com.baidu.navisdk.adapter.BNRoutePlanNode;
import com.baidu.navisdk.adapter.BNRoutePlanNode.CoordinateType;
import com.baidu.navisdk.adapter.BaiduNaviManagerFactory;
import com.baidu.navisdk.adapter.IBNRoutePlanManager;
import com.baidu.navisdk.adapter.IBNTTSManager;
import com.baidu.navisdk.adapter.IBaiduNaviManager;
import com.baidu.navisdk.adapter.impl.BaiduNaviManager;
import com.baidu.platform.comapi.walknavi.widget.ArCameraView;
import com.bigkoo.svprogresshud.SVProgressHUD;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;
import com.sanleng.electricalfire.R;
import com.sanleng.electricalfire.baidumap.DemoGuideActivity;
import com.sanleng.electricalfire.baidumap.NormalUtils;
import com.sanleng.electricalfire.baidumap.WNaviGuideActivity;
import com.sanleng.electricalfire.dialog.E_StationDialog;
import com.sanleng.electricalfire.ui.activity.BaseActivity;
import com.sanleng.electricalfire.ui.activity.FirsafetyAtivity;
import com.sanleng.electricalfire.ui.activity.HostMonitoringActivity;
import com.sanleng.electricalfire.ui.activity.RealDataActivity;
import com.sanleng.electricalfire.ui.activity.WaterSystemActivity;
import com.sanleng.electricalfire.ui.bean.StationBean;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 地图监控
 *
 * @author qiaoshi
 */
public class MapMonitoringFragment extends BaseFragment implements OnClickListener {
    private LocationClient mLocationClient = null; // 定位对象
    private BDLocationListener myListener = new MyLocationListener(); // 定位监听
    private double S_mylatitude;// 纬度
    private double S_mylongitude;// 经度
    private double E_mylatitude;// 纬度
    private double E_mylongitude;// 经度
    private LocationManager locationManager;
    private MapView mMapView;  // 地图应用
    private BaiduMap mBaiduMap;
    private LatLng latLng;
    private List<OverlayOptions> listoption;
    private boolean isFirstLoc = true; // 是否首次定位
    BitmapDescriptor bdA = BitmapDescriptorFactory.fromResource(R.drawable.bd_fire);//火灾标识
    BitmapDescriptor bdB = BitmapDescriptorFactory.fromResource(R.drawable.bd_lectrical);//电气标识
    BitmapDescriptor bdC = BitmapDescriptorFactory.fromResource(R.drawable.bd_watere);//水系统标识
    private static final double EARTH_RADIUS = 6378137.0;
    WalkNaviLaunchParam walkParam;
    /*导航起终点Marker，可拖动改变起终点的坐标*/
    private Marker mStartMarker;
    private Marker mEndMarker;
    BitmapDescriptor bdStart = BitmapDescriptorFactory
            .fromResource(R.drawable.icon_start);
    BitmapDescriptor bdEnd = BitmapDescriptorFactory
            .fromResource(R.drawable.icon_end);
    private static boolean isPermissionRequested = false;
    private LatLng startPt, endPt;
    //驾车导航
    private static final String[] authBaseArr = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    private static final int authBaseRequestCode = 1;
    private boolean hasInitSuccess = false;
    private static final String APP_FOLDER_NAME = "智慧应急";
    public static final String ROUTE_PLAN_NODE = "routePlanNode";
    private String mSDCardPath = null;
    private BNRoutePlanNode mStartNode = null;
    private E_StationDialog e_stationDialog;
    private View view;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.mapmonitoringfragment, null);
        RequestPermission();
        requestPermissions();
        SpeechUtility.createUtility(getActivity(), SpeechConstant.APPID + "=5ca5c1aa");
        initview();
        return view;
    }

    private void initMap() {
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
            dialog.setTitle("GPS未打开");
            dialog.setMessage("请打开GPS或WIFI，提高定位精度");
            dialog.setCancelable(false);
            dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // TODO Auto-generated method stub
                    Intent setting_Intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivityForResult(setting_Intent, 0);
                }
            });
            dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // TODO Auto-generated method stub
                }
            });
            dialog.show();
        }
        //获取地图控件引用
        mBaiduMap = mMapView.getMap();
        // 普通地图BMAP_PERSPECTIVE_MAP。。MAP_TYPE_NORMAL
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
        View child = mMapView.getChildAt(1);
        if (child != null && (child instanceof ImageView || child instanceof ZoomControls)) {
            child.setVisibility(View.INVISIBLE);
        }
        // 开启交通图
        mBaiduMap.setTrafficEnabled(true);
        // 开启热力图
        mBaiduMap.setBaiduHeatMapEnabled(false);
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        mLocationClient = new LocationClient(getActivity().getApplicationContext());
        // 声明LocationClient类 //配置定位SDK参数
        initLocation();
        mLocationClient.registerLocationListener(myListener);// 注册监听函数
        // 开启定位
        mLocationClient.start();
        // 图片点击事件，回到定位点
        mLocationClient.requestLocation();
        mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker arg0) {
                StationBean bean = (StationBean) arg0.getExtraInfo().get("marker");
                E_mylatitude = bean.getE_mylatitude();
                E_mylongitude = bean.getE_mylongitude();
                String names = bean.getName();
                String address = bean.getAddress();
                int type = bean.getType();
                LatLng llA = new LatLng(E_mylatitude, E_mylongitude);
                showInfoWindow(llA, names, address, type);
                // 获得marker中的数据
//                StationBean bean = (StationBean) arg0.getExtraInfo().get("marker");
//                E_mylatitude = bean.getE_mylatitude();
//                E_mylongitude = bean.getE_mylongitude();
//                String names = bean.getName();
//                String addresss = bean.getAddress();
//
//                // 获得marker中的数据
//                e_stationDialog = new E_StationDialog(MapMonitoringActivity.this, names, addresss, clickListener);
//                e_stationDialog.show();
                return true;

            }

        });
        //拖拽定位
        mBaiduMap.setOnMapStatusChangeListener(new BaiduMap.OnMapStatusChangeListener() {
            @Override
            public void onMapStatusChangeStart(MapStatus mapStatus) {

            }

            @Override
            public void onMapStatusChangeStart(MapStatus mapStatus, int i) {

            }

            @Override
            public void onMapStatusChange(MapStatus mapStatus) {

            }

            @Override
            public void onMapStatusChangeFinish(MapStatus mapStatus) {
                //改变结束之后，获取地图可视范围的中心点坐标
                S_mylatitude = mapStatus.target.latitude;
                S_mylongitude = mapStatus.target.longitude;

            }
        });
    }

    //配置定位SDK参数
    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        //可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");
        //可选，默认gcj02，设置返回的定位结果坐标系
        int span = 8000;
        option.setScanSpan(span);
        //可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);
        //可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);
        //可选，默认false,设置是否使用gps
        option.setLocationNotify(true);
        //可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);
        //可选，默认false，设置是否需要位置语义化结果，可以在BDLocation
        // .getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);
        //可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);
        option.setOpenGps(true);// 打开gps
        // 可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程， 设置是否在stop的时候杀死这个进程，默认不杀死
        option.SetIgnoreCacheException(false);
        //可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤GPS仿真结果，默认需要
        mLocationClient.setLocOption(option);
    }

    //实现BDLocationListener接口,BDLocationListener为结果监听接口，异步获取定位结果
    public class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            latLng = new LatLng(location.getLatitude(), location.getLongitude());
            S_mylatitude = location.getLatitude();
            S_mylongitude = location.getLongitude();
            if (S_mylatitude == 0.0 && S_mylongitude == 0.0) {
                new SVProgressHUD(getActivity()).showErrorWithStatus("当前网络不通畅，请重新获取");
            }
//            // 构造定位数据
//            MyLocationData locData = new MyLocationData.Builder().accuracy(location.getRadius())
//                    // 此处设置开发者获取到的方向信息，顺时针0-360
//                    .direction(0).latitude(location.getLatitude()).longitude(location.getLongitude()).build();
//            // 设置定位数据
//            mBaiduMap.setMyLocationData(locData);
            // 当不需要定位图层时关闭定位图层
            mBaiduMap.setMyLocationEnabled(true);
            if (isFirstLoc) {
                isFirstLoc = false;
                LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(ll).zoom(18.0f);
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
                if (location.getLocType() == BDLocation.TypeGpsLocation) {
                    // GPS定位结果
//                    Toast.makeText(getActivity(), location.getAddrStr(), Toast.LENGTH_SHORT).show();
                } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {
                    // 网络定位结果
//                    Toast.makeText(getActivity(), location.getAddrStr(), Toast.LENGTH_SHORT).show();
                } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {
                    // 离线定位结果
//                    Toast.makeText(getActivity(), location.getAddrStr(), Toast.LENGTH_SHORT).show();
                } else if (location.getLocType() == BDLocation.TypeServerError) {
                    Toast.makeText(getActivity(), "服务器错误，请检查", Toast.LENGTH_SHORT).show();
                } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
                    Toast.makeText(getActivity(), "网络错误，请检查", Toast.LENGTH_SHORT).show();
                } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
                    Toast.makeText(getActivity(), "手机模式错误，请检查是否飞行", Toast.LENGTH_SHORT).show();
                }
            }
        }

    }

    private void fire() {
        StationBean beana = new StationBean();
        beana.setName("火灾报警");
        beana.setAddress("南京市-江宁区-秣周东路");
        beana.setE_mylatitude(31.87308);
        beana.setE_mylongitude(118.83488);
        beana.setType(1);
        // 构建MarkerOption，用于在地图上添加Marker
        LatLng llA = new LatLng(31.87308, 118.83488);
        MarkerOptions option = new MarkerOptions().position(llA).icon(bdA);
        Marker marker = (Marker) mBaiduMap.addOverlay(option);
        // 将信息保存
        Bundle bundle = new Bundle();
        bundle.putSerializable("marker", beana);
        marker.setExtraInfo(bundle);
        mBaiduMap.addOverlays(listoption);
    }

    private void lectrical() {
        StationBean beanb = new StationBean();
        beanb.setName("电气安全");
        beanb.setAddress("南京市-江宁区-秣周东路");
        beanb.setE_mylatitude(31.87208);
        beanb.setE_mylongitude(118.83388);
        beanb.setType(2);
        // 构建MarkerOption，用于在地图上添加Marker
        LatLng llA = new LatLng(31.87208, 118.83388);
        MarkerOptions option = new MarkerOptions().position(llA).icon(bdB);
        Marker marker = (Marker) mBaiduMap.addOverlay(option);
        // 将信息保存
        Bundle bundle = new Bundle();
        bundle.putSerializable("marker", beanb);
        marker.setExtraInfo(bundle);
        mBaiduMap.addOverlays(listoption);
    }

    private void watere() {
        StationBean beanc = new StationBean();
        beanc.setName("水系统");
        beanc.setAddress("南京市-江宁区-秣周东路");
        beanc.setE_mylatitude(31.87408);
        beanc.setE_mylongitude(118.83588);
        beanc.setType(3);
        // 构建MarkerOption，用于在地图上添加Marker
        LatLng llA = new LatLng(31.87408, 118.83588);
        MarkerOptions option = new MarkerOptions().position(llA).icon(bdC);
        Marker marker = (Marker) mBaiduMap.addOverlay(option);
        // 将信息保存
        Bundle bundle = new Bundle();
        bundle.putSerializable("marker", beanc);
        marker.setExtraInfo(bundle);
        mBaiduMap.addOverlays(listoption);
    }

    @Override
    public void onResume() {
        super.onResume();
        initMap();
        if (initDirs()) {
            initNavi();
        }
        fire();
        lectrical();
        watere();
        // 在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        WalkNavigateHelper.getInstance().resume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        // 在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        WalkNavigateHelper.getInstance().pause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        // 当不需要定位图层时关闭定位图层
        WalkNavigateHelper.getInstance().quit();
        mBaiduMap.setMyLocationEnabled(false);
        mLocationClient.stop();
        // 在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
        super.onDestroy();
    }

    // 初始化数据
    private void initview() {
        mMapView = view.findViewById(R.id.bmapView);
    }


    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
//            case R.id.r_back:
//                getActivity().finish();
//                break;
            default:
                break;
        }
    }


    // 返回单位是米
    private double gps_m(double lat_a, double lng_a, double lat_b, double lng_b) {
        double radLat1 = (lat_a * Math.PI / 180.0);
        double radLat2 = (lat_b * Math.PI / 180.0);
        double a = radLat1 - radLat2;
        double b = (lng_a - lng_b) * Math.PI / 180.0;
        double s = 2 * Math.asin(Math.sqrt(
                Math.pow(Math.sin(a / 2), 2) + Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS;
        s = Math.round(s * 10000) / 10000;
        return s;
    }

    /**
     * 初始化导航起终点Marker
     */
    public void initOverlay() {
        LatLng llA = new LatLng(S_mylatitude, S_mylongitude);
        LatLng llB = new LatLng(E_mylatitude, E_mylongitude);
        MarkerOptions ooA = new MarkerOptions().position(llA).icon(bdStart)
                .zIndex(9).draggable(true);
        mStartMarker = (Marker) (mBaiduMap.addOverlay(ooA));
        mStartMarker.setDraggable(true);
        MarkerOptions ooB = new MarkerOptions().position(llB).icon(bdEnd)
                .zIndex(5);
        mEndMarker = (Marker) (mBaiduMap.addOverlay(ooB));
        mEndMarker.setDraggable(true);

        mBaiduMap.setOnMarkerDragListener(new BaiduMap.OnMarkerDragListener() {
            public void onMarkerDrag(Marker marker) {
            }

            public void onMarkerDragEnd(Marker marker) {
                if (marker == mStartMarker) {
                    startPt = marker.getPosition();
                } else if (marker == mEndMarker) {
                    endPt = marker.getPosition();
                }
                walkParam.stPt(startPt).endPt(endPt);
            }

            public void onMarkerDragStart(Marker marker) {
            }
        });
    }

    /**
     * 开始步行导航
     */
    private void startWalkNavi() {
        try {
            WalkNavigateHelper.getInstance().initNaviEngine(getActivity(), new IWEngineInitListener() {
                @Override
                public void engineInitSuccess() {
                    routePlanWithWalkParam();

                }

                @Override
                public void engineInitFail() {
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 发起步行导航算路
     */
    private void routePlanWithWalkParam() {
        WalkNavigateHelper.getInstance().routePlanWithParams(walkParam, new IWRoutePlanListener() {
            @Override
            public void onRoutePlanStart() {
            }

            @Override
            public void onRoutePlanSuccess() {
                Intent intent = new Intent();
                intent.setClass(getActivity(), WNaviGuideActivity.class);
                startActivity(intent);
            }

            @Override
            public void onRoutePlanFail(WalkRoutePlanError error) {
            }
        });
    }


    /**
     * 显示火灾报警弹出窗
     */
    private void showInfoWindow(LatLng ll, String name, String addresses, final int type) {
        //创建InfoWindow展示的view
        View contentView = LayoutInflater.from(getActivity()).inflate(R.layout.infowindow_items, null);
        TextView tvCount = contentView.findViewById(R.id.tv_count);
        TextView address = contentView.findViewById(R.id.address);
        final ImageView index_a = contentView.findViewById(R.id.index_a);
        RelativeLayout viewdetails= contentView.findViewById(R.id.viewdetails);
        tvCount.setText("名称："+name);
        address.setText("地址："+addresses);
        if (type == 1) {
            index_a.setBackground(MapMonitoringFragment.this.getResources().getDrawable(R.drawable.bd_fire));
        }
        if (type == 2) {
            index_a.setBackground(MapMonitoringFragment.this.getResources().getDrawable(R.drawable.bd_lectrical));
        }
        if (type == 3) {
            index_a.setBackground(MapMonitoringFragment.this.getResources().getDrawable(R.drawable.bd_watere));
        }

        viewdetails.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (type == 1) {
                    Intent HostMonitoring=new Intent(getActivity(),HostMonitoringActivity.class);
                    startActivity(HostMonitoring);
                }
                if (type == 2) {
                    Intent intent_RealTimeData = new Intent(getActivity(), RealDataActivity.class);
                    startActivity(intent_RealTimeData);
                }
                if (type == 3) {
                    Intent WaterSystemintent=new Intent(getActivity(),WaterSystemActivity.class);
                    startActivity(WaterSystemintent);
                }
            }
        });
        //创建InfoWindow , 传入 view， 地理坐标， y 轴偏移量
        InfoWindow infoWindow = new InfoWindow(contentView, ll, -80);
        //显示InfoWindow
        mBaiduMap.showInfoWindow(infoWindow);

    }

    private boolean hasBasePhoneAuth() {
        PackageManager pm = getActivity().getPackageManager();
        for (String auth : authBaseArr) {
            if (pm.checkPermission(auth, getActivity().getPackageName()) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    //============驾车导航===================
    private boolean initDirs() {
        mSDCardPath = getSdcardDir();
        if (mSDCardPath == null) {
            return false;
        }
        File f = new File(mSDCardPath, APP_FOLDER_NAME);
        if (!f.exists()) {
            try {
                f.mkdir();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    private void initNavi() {
        // 申请权限
        if (Build.VERSION.SDK_INT >= 23) {
            if (!hasBasePhoneAuth()) {
                this.requestPermissions(authBaseArr, authBaseRequestCode);
                return;
            }
        }
        BaiduNaviManagerFactory.getBaiduNaviManager().init(getActivity(),
                mSDCardPath, APP_FOLDER_NAME, new IBaiduNaviManager.INaviInitListener() {
                    @Override
                    public void onAuthResult(int status, String msg) {
                        String result;
                        if (0 == status) {
                            result = "key校验成功!";
                        } else {
                            result = "key校验失败, " + msg;
                        }
                    }

                    @Override
                    public void initStart() {
                    }

                    @Override
                    public void initSuccess() {
                        hasInitSuccess = true;
                        // 初始化tts
                        initTTS();
                    }

                    @Override
                    public void initFailed() {
                    }
                });

    }

    private String getSdcardDir() {
        if (Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            return Environment.getExternalStorageDirectory().toString();
        }
        return null;
    }

    private void initTTS() {
        // 使用内置TTS
        BaiduNaviManagerFactory.getTTSManager().initTTS(getActivity().getApplicationContext(),
                getSdcardDir(), APP_FOLDER_NAME, NormalUtils.getTTSAppID());
        // 注册同步内置tts状态回调
        BaiduNaviManagerFactory.getTTSManager().setOnTTSStateChangedListener(
                new IBNTTSManager.IOnTTSPlayStateChangedListener() {
                    @Override
                    public void onPlayStart() {
                        Log.e("BNSDKDemo", "ttsCallback.onPlayStart");
                    }

                    @Override
                    public void onPlayEnd(String speechId) {
                        Log.e("BNSDKDemo", "ttsCallback.onPlayEnd");
                    }

                    @Override
                    public void onPlayError(int code, String message) {
                        Log.e("BNSDKDemo", "ttsCallback.onPlayError");
                    }
                }
        );

        // 注册内置tts 异步状态消息
        BaiduNaviManagerFactory.getTTSManager().setOnTTSStateChangedHandler(
                new Handler(Looper.getMainLooper()) {
                    @Override
                    public void handleMessage(Message msg) {
                        Log.e("BNSDKDemo", "ttsHandler.msg.what=" + msg.what);
                        int type = msg.what;
                        switch (type) {
                            case BaiduNaviManager.TTSPlayMsgType.PLAY_START_MSG: {
                                break;
                            }
                            case BaiduNaviManager.TTSPlayMsgType.PLAY_END_MSG: {
                                break;
                            }
                            default:
                                break;
                        }
                    }
                }
        );
    }

    private void routeplanToNavi(final int coType) {
        if (!hasInitSuccess) {
            Toast.makeText(getActivity(), "还未初始化!", Toast.LENGTH_SHORT).show();
        }

        BNRoutePlanNode sNode = new BNRoutePlanNode(S_mylongitude, S_mylatitude, "", "", coType);
        BNRoutePlanNode eNode = new BNRoutePlanNode(E_mylongitude, E_mylatitude, "", "", coType);
        switch (coType) {
            case CoordinateType.BD09LL: {
                sNode = new BNRoutePlanNode(S_mylongitude, S_mylatitude, "", "", coType);
                eNode = new BNRoutePlanNode(E_mylongitude, E_mylatitude, "", "", coType);
                break;
            }
            default:
                break;
        }

        mStartNode = sNode;
        List<BNRoutePlanNode> list = new ArrayList<>();
        list.add(sNode);
        list.add(eNode);

        BaiduNaviManagerFactory.getRoutePlanManager().routeplanToNavi(
                list,
                IBNRoutePlanManager.RoutePlanPreference.ROUTE_PLAN_PREFERENCE_DEFAULT,
                null,
                new Handler(Looper.getMainLooper()) {
                    @Override
                    public void handleMessage(Message msg) {
                        switch (msg.what) {
                            case IBNRoutePlanManager.MSG_NAVI_ROUTE_PLAN_START:
                                break;
                            case IBNRoutePlanManager.MSG_NAVI_ROUTE_PLAN_SUCCESS:
                                break;
                            case IBNRoutePlanManager.MSG_NAVI_ROUTE_PLAN_FAILED:
                                break;
                            case IBNRoutePlanManager.MSG_NAVI_ROUTE_PLAN_TO_NAVI:
                                Intent intent = new Intent(getActivity(),
                                        DemoGuideActivity.class);
                                Bundle bundle = new Bundle();
                                bundle.putSerializable(ROUTE_PLAN_NODE, mStartNode);
                                intent.putExtras(bundle);
                                startActivity(intent);
                                break;
                            default:
                                // nothing
                                break;
                        }
                    }
                });
    }
    //=================================================================================

    /**
     * Android6.0之后需要动态申请权限
     */
    private void RequestPermission() {
        if (Build.VERSION.SDK_INT >= 23 && !isPermissionRequested) {
            isPermissionRequested = true;
            ArrayList<String> permissions = new ArrayList<>();
            if (getActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }
            if (permissions.size() == 0) {
                return;
            } else {
                requestPermissions(permissions.toArray(new String[permissions.size()]), 0);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == ArCameraView.WALK_AR_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(getActivity(), "没有相机权限,请打开后重试", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == authBaseRequestCode) {
            for (int ret : grantResults) {
                if (ret == 0) {
                    continue;
                } else {
                    Toast.makeText(getActivity(), "缺少导航基本的权限!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            initNavi();
        }
    }

    private void requestPermissions() {
        try {
            if (Build.VERSION.SDK_INT >= 23) {
                int permission = ActivityCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (permission != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]
                            {Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                    Manifest.permission.LOCATION_HARDWARE, Manifest.permission.READ_PHONE_STATE,
                                    Manifest.permission.WRITE_SETTINGS, Manifest.permission.READ_EXTERNAL_STORAGE,
                                    Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_CONTACTS}, 0x0010);
                }

                if (permission != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION}, 0x0010);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private OnClickListener clickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            switch (v.getId()) {
                //步行导航
                case R.id.walknavigation:
                    //初始化导航数据
                    initOverlay();
                    startPt = new LatLng(S_mylatitude, S_mylongitude);
                    endPt = new LatLng(E_mylatitude, E_mylongitude);
                    /*构造导航起终点参数对象*/
                    walkParam = new WalkNaviLaunchParam().stPt(startPt).endPt(endPt);
                    walkParam.extraNaviMode(0);
                    startWalkNavi();
                    mBaiduMap.clear();
                    break;
                //驾车导航
                case R.id.drivenavigation:
                    routeplanToNavi(CoordinateType.BD09LL);
                    break;
                // 取消
                case R.id.canles:
                    e_stationDialog.dismiss();
                    break;
                default:
                    break;
            }
        }
    };
}
