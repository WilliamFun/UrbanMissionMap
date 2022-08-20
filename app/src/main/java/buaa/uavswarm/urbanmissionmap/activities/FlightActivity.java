package buaa.uavswarm.urbanmissionmap.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.duke.udp.UDPReceiveHelper;
import com.duke.udp.UDPSendHelper;
import com.duke.udp.util.UDPListener;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.Polyline;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import buaa.uavswarm.urbanmissionmap.R;
import buaa.uavswarm.urbanmissionmap.util.MarkerWithLabel;
import egolabsapps.basicodemine.offlinemap.Interfaces.GeoPointListener;
import egolabsapps.basicodemine.offlinemap.Interfaces.MapListener;
import egolabsapps.basicodemine.offlinemap.Utils.MapUtils;
import egolabsapps.basicodemine.offlinemap.Views.OfflineMapView;

import static buaa.uavswarm.urbanmissionmap.util.ByteTransFormUtil.bytesToHex;
import static buaa.uavswarm.urbanmissionmap.util.ByteTransFormUtil.hexToByteArray;
import static egolabsapps.basicodemine.offlinemap.Utils.DistanceUtils.getDistanceBetweenTwoGeoPoint;
import static java.lang.Math.pow;

public class FlightActivity extends AppCompatActivity implements MapListener, GeoPointListener, View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    public static final String TAG = "UDP_TAG";

    //源码显示相关
    private TextView receive_string;//接受源码显示
    private TextView send_string;//发送源码显示

    //数据回显
    private TextView plane_address;//飞机地址
    private TextView plane_type;//飞机类型
    private TextView formation_type;//编队类型
    private TextView froward_waypoint;//目标航点
    private TextView plane_status;//飞机状态
    private TextView plane_speed;//飞机速度
    private TextView plane_longitude;//飞机位置经度
    private TextView plane_latitude;//飞机位置纬度
    private TextView plane_height;//飞机高度
    private TextView leader_height;//长机高度
    private TextView aim_distance;//距离攻击点的距离

    //地图交互相关
    private ArrayList<Overlay> Waypoint_overlays = new ArrayList<Overlay>();//航线图层列表
    private Overlay Attack_overlay;//攻击点图层
    private ArrayList<Overlay> Nofly_overlays = new ArrayList<Overlay>();//禁飞区图层列表
    private boolean start_waypoint=false;//判断是否需要创建图层
    private boolean start_attack=false;
    private boolean start_nofly=false;
    int a=0;//航点计数
    int b=1;//攻击点计数
    int polygonnum=0;//禁飞区个数
    int waypointsnum=0;//记录创建航线个数
    private ArrayList<GeoPoint> GeoPoints_waypoints = new ArrayList<GeoPoint>();//正在操作的航线
    private ArrayList<Double> Height_waypoints = new ArrayList<Double>();
    private ArrayList<Integer> waypointsnum_record=new ArrayList<Integer>();
    private ArrayList<GeoPoint> Geopoints_attack = new ArrayList<GeoPoint>();
    private ArrayList<Double> Height_attack = new ArrayList<>();
    private Map<Integer,Integer> Attackallocation = new HashMap<>();
    private ArrayList<Polygon> Polygon_record = new ArrayList<Polygon>();

    //内部逻辑变量
    private int swarmname;//下拉框中选择的编队
    private int planeid;//下拉框中选择的飞机
    private int selectformationno;//选择的编队号
    private int selectplaneid;//选择的飞机地址
    private String[] fnameArray;//编队名称数组
    private int[] planeidArray;//已注册的无人机地址数组

    //隐藏显示布局
    private FrameLayout ll_formationnew;//图形编队栏布局
    private LinearLayout ll_waypoint;//开启编辑航点弹出的布局
    private LinearLayout ll_attack;//开启设置攻击点弹出的布局
    private LinearLayout ll_control;//右侧控制指令布局
    private LinearLayout ll_receivesourcecode;//接收源码显示布局
    private LinearLayout ll_sendsourcecode;//发送源码显示布局

    //udp接收发送数据
    protected Context mContext;//this
    public byte[] recievepacket;//接收到的数据帧
    private int planeaddress;
    private int planetype;
    private int formationtype;
    private double Latitude=37.514100;
    private double Longitude=122.091652;
    private double mheight;
    private double pitch;
    private double roll;
    private float yaw;
    private double speed;
    private int arrivewaypoint;
    private double lheight;
    private int sendPort = 20005;
    private int receivePort = 20005;
    private String ip = "224.1.1.1";
    private UDPReceiveHelper udpHelper;
    private UDPSendHelper udpSendHelper;

    //功能按键
    private ToggleButton btn_waypoint;
    private ToggleButton btn_attack;
    private ToggleButton swarm_switch;
    private ToggleButton telemetry_switch;
    private ToggleButton btn_nofly;

    //地图
    private OfflineMapView offlineMapView;
    private MapView mMapview;
    private MapUtils mapUtils;

    //移动飞机相关
    private Map<Integer, MarkerWithLabel> Planemarkers = new HashMap<Integer,MarkerWithLabel>();
    private Map<Integer,GeoPoint> GeoPoints_plane = new HashMap<Integer, GeoPoint>();
    private Map<Integer,Float> Yaw_plane = new HashMap<Integer, Float>();
    private Map<Integer, Boolean> isAdd = new HashMap<Integer, Boolean>();
    private ArrayList<Marker> Tracklist = new ArrayList<>();

    //rtsp播放器
    private VideoView rtspvideo;

    private int timerecord=0;

    Handler newhandler=new Handler();
    Runnable newrunnable=new Runnable(){
        @Override
        public void run() {
            // 定时器触发，需要执行的逻辑。
            for(int i=0;i<planeidArray.length;i++){
                if(Yaw_plane.get(planeidArray[i])!=null&&GeoPoints_plane.get(planeidArray[i])!=null){
                    Planemarkers.get(planeidArray[i]).setRotation(90-Yaw_plane.get(planeidArray[i]));
                    Planemarkers.get(planeidArray[i]).setPosition(GeoPoints_plane.get(planeidArray[i]));

                    if(Planemarkers.get(planeidArray[i]).getTitle()!=null){
                        Planemarkers.get(planeidArray[i]).mLabel=Planemarkers.get(planeidArray[i]).getTitle();
                    }

                    Marker marker = new Marker(mMapview);
                    marker.setPosition(GeoPoints_plane.get(planeidArray[i]));
                    marker.setIcon(ContextCompat.getDrawable(mContext,R.drawable.purpletrack));
                    Tracklist.add(marker);
                    mMapview.getOverlays().add(marker);

                    if(timerecord>100){//一段时间后开始删除航迹
                        mMapview.getOverlays().remove(Tracklist.get(0));
                        Tracklist.remove(0);
                    }
                }
            }
            timerecord++;
            mMapview.invalidate();
            newhandler.postDelayed(this, 300);
        }

    };

//    Timer mTimer = new Timer();//新定时器
//    TimerTask mTask = new TimerTask() {//定时刷新飞机位置
//    @Override
//     public void run() {
//    // 定时器触发，需要执行的逻辑。
//        for(int i=0;i<planeidArray.length;i++){
//            if(Yaw_plane.get(planeidArray[i])!=null&&GeoPoints_plane.get(planeidArray[i])!=null){
//                Planemarkers.get(planeidArray[i]).setRotation(90-Yaw_plane.get(planeidArray[i]));
//                Planemarkers.get(planeidArray[i]).setPosition(GeoPoints_plane.get(planeidArray[i]));
//
//                if(Planemarkers.get(planeidArray[i]).getTitle()!=null){
//                    Planemarkers.get(planeidArray[i]).mLabel=Planemarkers.get(planeidArray[i]).getTitle();
//                }
//
//                Marker marker = new Marker(mMapview);
//                marker.setPosition(GeoPoints_plane.get(planeidArray[i]));
//                marker.setIcon(ContextCompat.getDrawable(mContext,R.drawable.purpletrack));
//                Tracklist.add(marker);
//                mMapview.getOverlays().add(marker);
//
//                if(timerecord>100){//一段时间后开始删除航迹
//                    mMapview.getOverlays().remove(Tracklist.get(0));
//                    Tracklist.remove(0);
//                }
//            }
//        }
//        timerecord++;
//        mMapview.invalidate();
//        }
//    };

    Handler handler=new Handler();
    Runnable runnable=new Runnable(){//刷新数据回显
        @SuppressLint({"DefaultLocale", "SetTextI18n"})
        @Override
        public void run() {
            if(planeaddress==selectplaneid){
                if(planetype==0){
                    plane_type.setText("长机");
                }else if(planetype==1){
                    plane_type.setText("僚机");
                }else{
                    plane_type.setText("备份长机");
                }
                if(formationtype==0){
                    formation_type.setText("三角形");
                }else{
                    formation_type.setText("矩形");
                }
                plane_address.setText(Integer.toString(planeaddress));
                froward_waypoint.setText(Integer.toString(arrivewaypoint));
                if(mheight>5){
                    plane_status.setText("飞行中");
                }else{
                    plane_status.setText("未起飞");
                }
                plane_speed.setText(String.format("%.1f",speed) +"m/s");
                plane_longitude.setText(String.format("%.6f",Longitude));
                plane_latitude.setText(String.format("%.6f",Latitude));
                plane_height.setText(String.format("%.1f",mheight) +"m");
                leader_height.setText(String.format("%.1f",lheight) +"m");
                if(Attackallocation.size()!=0){
                    for(int i =1;i<=Attackallocation.size();i++){
                        if(Attackallocation.get(i)!=null&&Attackallocation.get(i)==selectformationno){
                            float distance = getDistanceBetweenTwoGeoPoint(GeoPoints_plane.get(selectplaneid),Geopoints_attack.get(i-1));
                            aim_distance.setText(String.format("%.1f",distance*1000) +"m");
                        }
                    }
                }
            }
            handler.postDelayed(this, 300);
        }

    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flight);
        initview();
        mContext=this;
        udpHelper = new UDPReceiveHelper(this,receivePort, ip);
        udpHelper.setUDPListener(udpListener);
        udpHelper.start();
    }

    private void initview() {
        //测试用
//        waypointsnum_record.add(4);
//        waypointsnum=1;
//        GeoPoints_waypoints.add(new GeoPoint(37.500300333317064,122.0901132844295));
//        Height_waypoints.add(100.00);
//        GeoPoints_waypoints.add(new GeoPoint(37.50408036855627,122.08350609020434));
//        Height_waypoints.add(100.00);
//        GeoPoints_waypoints.add(new GeoPoint(37.53841247056922,122.09216257975635));
//        Height_waypoints.add(100.00);
//        GeoPoints_waypoints.add(new GeoPoint(37.54335213778889,122.08408962711881));
//        Height_waypoints.add(100.00);
        //测试用

        rtspvideo = findViewById(R.id.video_player);
        ll_receivesourcecode =findViewById(R.id.ll_receivesourcecode);
        ll_sendsourcecode = findViewById(R.id.ll_sendsourcecode);
        send_string = findViewById(R.id.send_string);
        receive_string = findViewById(R.id.receive_string);
        leader_height = findViewById(R.id.leader_height);
        plane_height = findViewById(R.id.plane_height);
        plane_latitude = findViewById(R.id.plane_latitude);
        plane_longitude = findViewById(R.id.plane_longitude);
        plane_speed = findViewById(R.id.plane_speed);
        plane_status = findViewById(R.id.plane_status);
        aim_distance = findViewById(R.id.aim_distance);
        froward_waypoint = findViewById(R.id.froward_waypoint);
        formation_type = findViewById(R.id.formation_type);
        plane_address = findViewById(R.id.plane_address);
        plane_type = findViewById(R.id.plane_type);
        ll_formationnew=findViewById(R.id.ll_formationnew);
        ll_waypoint=findViewById(R.id.ll_waypoint);
        ll_attack=findViewById(R.id.ll_attack);
        ll_control=findViewById(R.id.ll_control);
        offlineMapView  = findViewById(R.id.Flight_map);
        offlineMapView.init(this, this);
        telemetry_switch = findViewById(R.id.telemetry_switch);
        telemetry_switch.setOnCheckedChangeListener(this);
        btn_waypoint = findViewById(R.id.btn_waypoint);
        btn_waypoint.setOnCheckedChangeListener(this);
        btn_attack = findViewById(R.id.btn_attack);
        btn_attack.setOnCheckedChangeListener(this);
        swarm_switch = findViewById(R.id.swarm_switch);
        swarm_switch.setOnCheckedChangeListener(this);
        btn_nofly=findViewById(R.id.btn_nofly);
        btn_nofly.setOnCheckedChangeListener(this);

        //接收源码显示的隐藏
        findViewById(R.id.receive_sourcecode).setOnClickListener(v -> {
            if(ll_receivesourcecode.getVisibility()==View.VISIBLE){
                ll_receivesourcecode.setVisibility(View.GONE);
            }else if(ll_receivesourcecode.getVisibility()==View.GONE){
                ll_receivesourcecode.setVisibility(View.VISIBLE);
            }
        });
        findViewById(R.id.send_sourcecode).setOnClickListener(v -> {
            if(ll_sendsourcecode.getVisibility()==View.VISIBLE){
                ll_sendsourcecode.setVisibility(View.GONE);
            }else if(ll_sendsourcecode.getVisibility()==View.GONE){
                ll_sendsourcecode.setVisibility(View.VISIBLE);
            }
        });
        //右侧控制指令按钮的隐藏
        findViewById(R.id.hide_control).setOnClickListener(v -> {
            findViewById(R.id.hide_control).setVisibility(View.GONE);
            ll_control.setVisibility(View.GONE);
            findViewById(R.id.show_control).setVisibility(View.VISIBLE);
        });
        findViewById(R.id.show_control).setOnClickListener(v -> {
            findViewById(R.id.show_control).setVisibility(View.GONE);
            ll_control.setVisibility(View.VISIBLE);
            findViewById(R.id.hide_control).setVisibility(View.VISIBLE);
        });
        //RTSP视频播放器的隐藏
        findViewById(R.id.btn_videogone).setOnClickListener(v -> {
            findViewById(R.id.btn_videogone).setVisibility(View.GONE);
            findViewById(R.id.video_player).setVisibility(View.GONE);
            findViewById(R.id.btn_videoshow).setVisibility(View.VISIBLE);
        });
        findViewById(R.id.btn_videoshow).setOnClickListener(v -> {
            findViewById(R.id.btn_videoshow).setVisibility(View.GONE);
            findViewById(R.id.video_player).setVisibility(View.VISIBLE);
            findViewById(R.id.btn_videogone).setVisibility(View.VISIBLE);
            SharedPreferences plane = getSharedPreferences("Plane_"+selectplaneid,MODE_PRIVATE);
            RtspStream(plane.getString("Video_address",""));
        });

        loadconfiguration();
        initTypeSpinner();// 初始化下拉

        //各种按键设置监听函数
        findViewById(R.id.btn_takeoff).setOnClickListener(this);
        findViewById(R.id.logo_icon).setOnClickListener(this);
        findViewById(R.id.btn_formationgone).setOnClickListener(this);
        findViewById(R.id.waypoint_upload).setOnClickListener(this);
        findViewById(R.id.speed_down).setOnClickListener(this);
        findViewById(R.id.speed_add).setOnClickListener(this);
        findViewById(R.id.height_yes).setOnClickListener(this);
        findViewById(R.id.height_no).setOnClickListener(this);
        findViewById(R.id.btn_addteam).setOnClickListener(this);
        findViewById(R.id.btn_deleteteam).setOnClickListener(this);
        findViewById(R.id.waypoint_route).setOnClickListener(this);
        findViewById(R.id.waypoint_delete).setOnClickListener(this);
        findViewById(R.id.attack_upload).setOnClickListener(this);
        findViewById(R.id.btn_startattack).setOnClickListener(this);

        //初始化编队图标
        SharedPreferences FormationShared = getSharedPreferences("FormationRecord", MODE_PRIVATE);
        Map<String, String> mapParam = (Map<String, String>) FormationShared.getAll();
        for (Map.Entry<String, String> item_map : mapParam.entrySet()) {
            final String key = item_map.getKey(); // 获取该配对的键信息
            SharedPreferences team = getSharedPreferences("Formation_"+key,MODE_PRIVATE);
            if(team.getString("Formation_type", "").equals("三角形")){
                switch (key){
                    case "1":findViewById(R.id.team10).setVisibility(View.VISIBLE);break;
                    case "2":findViewById(R.id.team20).setVisibility(View.VISIBLE);break;
                    case "3":findViewById(R.id.team30).setVisibility(View.VISIBLE);break;
                    case "4":findViewById(R.id.team40).setVisibility(View.VISIBLE);break;
                    case "5":findViewById(R.id.team50).setVisibility(View.VISIBLE);findViewById(R.id.btn_addteam).setVisibility(View.GONE);break;
                    default:break;
                }
            }else{
                switch (key){
                    case "1":findViewById(R.id.team11).setVisibility(View.VISIBLE);break;
                    case "2":findViewById(R.id.team21).setVisibility(View.VISIBLE);break;
                    case "3":findViewById(R.id.team31).setVisibility(View.VISIBLE);break;
                    case "4":findViewById(R.id.team41).setVisibility(View.VISIBLE);break;
                    case "5":findViewById(R.id.team51).setVisibility(View.VISIBLE);findViewById(R.id.btn_addteam).setVisibility(View.GONE);break;
                    default:break;
                }
            }
        }

        //隐藏虚拟按键
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);

    }

    private void loadconfiguration() {
        //编队名称数组
        SharedPreferences FormationShared = getSharedPreferences("FormationRecord", MODE_PRIVATE);
        Map<String, String> mapParam = (Map<String, String>) FormationShared.getAll();
        fnameArray=new String[mapParam.size()+1];
        int i=0;
        for (Map.Entry<String, String> item_map : mapParam.entrySet()) {
            final String value = item_map.getValue(); // 获取该配对的键信息
            fnameArray[i] = value;
            i++;
        }
        fnameArray[i]="不在编队";
        if(mapParam.size()==0){
            fnameArray = new String[]{"无编队"};
        }


    }

    @Override
    protected void onStart() {
        //调用配置
        super.onStart();
    }

    @Override
    public void onGeoPointRecieved(GeoPoint geoPoint) {
        Toast.makeText(this, geoPoint.toDoubleString(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void mapLoadSuccess(MapView mapView, MapUtils mapUtils) {
        this.mapUtils = mapUtils;
        mMapview = mapView;
        mapView.setMinZoomLevel((double) 11);
        mapView.setMaxZoomLevel((double) 18);
        offlineMapView.setInitialPositionAndZoom(new GeoPoint(37.514100, 122.091652), 15);

        //已注册的无人机编号数组
        SharedPreferences PlaneShared = getSharedPreferences("PlaneRecord",MODE_PRIVATE);
        Map<String, String> mapPlane = (Map<String, String>) PlaneShared.getAll();
        planeidArray=new int[mapPlane.size()];
        int j=0;
        for (Map.Entry<String, String> item_map : mapPlane.entrySet()) {
            final String key = item_map.getKey(); // 获取该配对的键信息
            planeidArray[j] = Integer.parseInt(key);
            j++;
        }

        for(int i = 0; i<planeidArray.length; i++){//初始化已注册的无人机marker
            MarkerWithLabel marker = new MarkerWithLabel(mapView,Integer.toString(planeidArray[i]));
            initmarker(marker,planeidArray[i]);
            marker.setDraggable(true);
            marker.setOnMarkerDragListener(new MarkerWithLabel.OnMarkerDragListener() {
                @Override
                public void onMarkerDrag(Marker marker) {
                    //拖动过程中
                    Log.i("test","图标拖动中");
                }

                @Override
                public void onMarkerDragEnd(Marker marker) {
                    Log.i("test","图标拖拽事件结束");
                    GeoPoint pos = marker.getPosition();
                    Log.i("test",pos.toString());
                    final Projection pj = mMapview.getProjection();
                    Point mPosition = new Point();
                    mPosition = (Point) pj.toPixels(pos, mPosition);
                    Log.i("test", mPosition.toString());
                    //此处增加判断是否进入到固定区域
                    if(mPosition.x>120&&mPosition.x<186&&mPosition.y>150&&mPosition.y<210&&(findViewById(R.id.team10).getVisibility()==View.VISIBLE||findViewById(R.id.team11).getVisibility()==View.VISIBLE)){//判断拖入编队1
                        //加入编队、保存到配置文件、改变与下拉框有关的数组、发送编队设置指令、根据编队改变飞机颜色
                        add2team(marker.getId(),"1");
                        initTypeSpinner();// 初始化下拉
                        sendformationpacket(marker.getId(),"1");
                        marker.setIcon(getResources().getDrawable(R.mipmap.plane_cyan));
                    }else if(mPosition.x>206&&mPosition.x<272&&mPosition.y>150&&mPosition.y<210&&(findViewById(R.id.team20).getVisibility()==View.VISIBLE||findViewById(R.id.team21).getVisibility()==View.VISIBLE)){
                        //加入编队、保存到配置文件、改变与下拉框有关的数组、发送编队设置指令、根据编队改变飞机颜色
                        add2team(marker.getId(),"2");
                        initTypeSpinner();// 初始化下拉
                        sendformationpacket(marker.getId(),"2");
                        marker.setIcon(getResources().getDrawable(R.mipmap.plane_yellow));
                    }else if(mPosition.x>292&&mPosition.x<358&&mPosition.y>150&&mPosition.y<210&&(findViewById(R.id.team30).getVisibility()==View.VISIBLE||findViewById(R.id.team31).getVisibility()==View.VISIBLE)){
                        //加入编队、保存到配置文件、改变与下拉框有关的数组、发送编队设置指令、根据编队改变飞机颜色
                        add2team(marker.getId(),"3");
                        initTypeSpinner();// 初始化下拉
                        sendformationpacket(marker.getId(),"3");
                        marker.setIcon(getResources().getDrawable(R.mipmap.plane_green));
                    }else if(mPosition.x>378&&mPosition.x<444&&mPosition.y>150&&mPosition.y<210&&(findViewById(R.id.team40).getVisibility()==View.VISIBLE||findViewById(R.id.team41).getVisibility()==View.VISIBLE)){
                        //加入编队、保存到配置文件、改变与下拉框有关的数组、发送编队设置指令、根据编队改变飞机颜色
                        add2team(marker.getId(),"4");
                        initTypeSpinner();// 初始化下拉
                        sendformationpacket(marker.getId(),"4");
                        marker.setIcon(getResources().getDrawable(R.mipmap.plane_blue));
                    }else if(mPosition.x>464&&mPosition.x<530&&mPosition.y>150&&mPosition.y<210&&(findViewById(R.id.team50).getVisibility()==View.VISIBLE||findViewById(R.id.team51).getVisibility()==View.VISIBLE)){
                        //加入编队、保存到配置文件、改变与下拉框有关的数组、发送编队设置指令、根据编队改变飞机颜色
                        add2team(marker.getId(),"5");
                        initTypeSpinner();// 初始化下拉
                        sendformationpacket(marker.getId(),"5");
                        marker.setIcon(getResources().getDrawable(R.mipmap.plane_red));
                    }else if(mPosition.x>1584&&mPosition.x<1634&&mPosition.y>110&&mPosition.y<160&&(findViewById(R.id.attack_1).getVisibility()==View.VISIBLE)){
                        Attackallocation.put(1,Integer.parseInt(marker.getId()));
                        marker.setTitle(marker.getId()+"-->"+"1");
                    }else if(mPosition.x>1654&&mPosition.x<1704&&mPosition.y>110&&mPosition.y<160&&(findViewById(R.id.attack_2).getVisibility()==View.VISIBLE)){
                        Attackallocation.put(2,Integer.parseInt(marker.getId()));
                        marker.setTitle(marker.getId()+"-->"+"2");
                    }else if(mPosition.x>1724&&mPosition.x<1774&&mPosition.y>110&&mPosition.y<160&&(findViewById(R.id.attack_3).getVisibility()==View.VISIBLE)){
                        Attackallocation.put(3,Integer.parseInt(marker.getId()));
                        marker.setTitle(marker.getId()+"-->"+"3");
                    }else if(mPosition.x>1794&&mPosition.x<1844&&mPosition.y>110&&mPosition.y<160&&(findViewById(R.id.attack_4).getVisibility()==View.VISIBLE)){
                        Attackallocation.put(4,Integer.parseInt(marker.getId()));
                        marker.setTitle(marker.getId()+"-->"+"4");
                    }
                    //拖动结束后，Marker需要自动返回到原来的位置，继续根据UDP数据移动。
                    marker.setPosition(GeoPoints_plane.get(Integer.parseInt(marker.getId())));
                }

                @Override
                public void onMarkerDragStart(Marker marker) {
                    Yaw_plane.remove(Integer.parseInt(marker.getId()));
                    GeoPoints_plane.remove(Integer.parseInt(marker.getId()));
                    Log.i("test","图标拖拽事件开始");
                }
            });
            isAdd.put(planeidArray[i],false);
            Planemarkers.put(planeidArray[i],marker);
        }
        //mTimer.schedule(mTask,0,300);
        newhandler.postDelayed(newrunnable,300);
        handler.postDelayed(runnable, 300);
    }

    @Override
    public void mapLoadFailed(String ex) {

    }

    private void initTypeSpinner() {
        ArrayAdapter<String> nameAdapter = new ArrayAdapter<String>(this,
                R.layout.spinner_item, fnameArray);
        nameAdapter.setDropDownViewResource(R.layout.item_dropdown);
        Spinner sp_lid = findViewById(R.id.sp_team);
        sp_lid.setAdapter(nameAdapter);
        sp_lid.setSelection(0);
        sp_lid.setOnItemSelectedListener(new MySelectedListener());
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.logo_icon){//返回主页
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }else if(v.getId()==R.id.btn_formationgone){//图形编队栏的隐藏
            if(ll_formationnew.getVisibility()==View.VISIBLE){
                ll_formationnew.setVisibility(View.GONE);
            }else{
                ll_formationnew.setVisibility(View.VISIBLE);
            }
        }else if(v.getId()==R.id.btn_takeoff){//发出起飞指令
            byte[] bytes=new byte[25];
            bytes[0]=(byte) 0xee;
            bytes[1]=(byte) 0x16;
            bytes[2]=(byte) 0xa5;
            bytes[3]=(byte) 0x15;
            bytes[4]=(byte) 0x63;
            bytes[5]=(byte)selectformationno;
            bytes[24]=CheckSum(bytes,25);//校验和
            ArrayList<String> multibytes=new ArrayList<String>();
            multibytes.add(bytesToHex(bytes));
            Sendpacket(multibytes);
        }else if(v.getId()==R.id.waypoint_upload){
            if(GeoPoints_waypoints.size()>0){
                ArrayList<String> multibytes=new ArrayList<String>();
                for(int i=0;i<waypointsnum_record.get(waypointsnum-1);i++){
                    byte[] bytes=new byte[25];
                    bytes[0]=(byte) 0xee;
                    bytes[1]=(byte) 0x16;
                    bytes[2]=(byte) 0xa5;
                    bytes[3]=(byte) 0x15;
                    bytes[4]=(byte) 0x9c;
                    bytes[5]=(byte) selectformationno;
                    bytes[6]=(byte) waypointsnum;
                    bytes[7]= (byte) i;
                    byte[] log=toLH((int)(GeoPoints_waypoints.get(i).getLongitude()*11930464.7056));
                    bytes[8]=log[0];
                    bytes[9]=log[1];
                    bytes[10]=log[2];
                    bytes[11]=log[3];
                    byte[] lag=toLH((int)(GeoPoints_waypoints.get(i).getLatitude()*11930464.7056));
                    bytes[12]=lag[0];
                    bytes[13]=lag[1];
                    bytes[14]=lag[2];
                    bytes[15]=lag[3];
                    byte[] height=toLH2((int) (Height_waypoints.get(i)*3.2767));
                    bytes[16]=height[0];
                    bytes[17]=height[1];
                    bytes[24]=CheckSum(bytes,25);//校验和
                    multibytes.add(bytesToHex(bytes));
                }
                Sendpacket(multibytes);
                GeoPoints_waypoints.clear();
            }
        }else if(v.getId()==R.id.speed_down){
            byte[] bytes=new byte[25];
            bytes[0]=(byte) 0xee;
            bytes[1]=(byte) 0x16;
            bytes[2]=(byte) 0xa5;
            bytes[3]=(byte) 0x15;
            bytes[4]=(byte) 0xa7;
            bytes[5]=(byte) selectformationno;
            bytes[24]=CheckSum(bytes,25);//校验和
            ArrayList<String> multibytes=new ArrayList<String>();
            multibytes.add(bytesToHex(bytes));
            Sendpacket(multibytes);
        }else if(v.getId()==R.id.speed_add){
            byte[] bytes=new byte[25];
            bytes[0]=(byte) 0xee;
            bytes[1]=(byte) 0x16;
            bytes[2]=(byte) 0xa5;
            bytes[3]=(byte) 0x15;
            bytes[4]=(byte) 0x3a;
            bytes[5]=(byte) selectformationno;
            bytes[24]=CheckSum(bytes,25);//校验和
            ArrayList<String> multibytes=new ArrayList<String>();
            multibytes.add(bytesToHex(bytes));
            Sendpacket(multibytes);
        }else if(v.getId()==R.id.height_yes){
            byte[] bytes=new byte[25];
            bytes[0]=(byte) 0xee;
            bytes[1]=(byte) 0x16;
            bytes[2]=(byte) 0xa5;
            bytes[3]=(byte) 0x15;
            bytes[4]=(byte) 0xc3;
            bytes[5]=(byte) selectformationno;
            bytes[24]=CheckSum(bytes,25);//校验和
            ArrayList<String> multibytes=new ArrayList<String>();
            multibytes.add(bytesToHex(bytes));
            Sendpacket(multibytes);
        }else if(v.getId()==R.id.height_no){
            byte[] bytes=new byte[25];
            bytes[0]=(byte) 0xee;
            bytes[1]=(byte) 0x16;
            bytes[2]=(byte) 0xa5;
            bytes[3]=(byte) 0x15;
            bytes[4]=(byte) 0xc9;
            bytes[5]=(byte) selectformationno;
            bytes[24]=CheckSum(bytes,25);//校验和
            ArrayList<String> multibytes=new ArrayList<String>();
            multibytes.add(bytesToHex(bytes));
            Sendpacket(multibytes);
        }else if(v.getId()==R.id.btn_addteam){
            SharedPreferences FormationShared = getSharedPreferences("FormationRecord", MODE_PRIVATE);
            Map<String, String> mapParam = (Map<String, String>) FormationShared.getAll();
            switch (mapParam.size()){
                case 0:findViewById(R.id.team10).setVisibility(View.VISIBLE);break;
                case 1:findViewById(R.id.team20).setVisibility(View.VISIBLE);break;
                case 2:findViewById(R.id.team30).setVisibility(View.VISIBLE);break;
                case 3:findViewById(R.id.team40).setVisibility(View.VISIBLE);break;
                case 4:findViewById(R.id.team50).setVisibility(View.VISIBLE);findViewById(R.id.btn_addteam).setVisibility(View.GONE);break;
                default:break;
            }
            if(mapParam.size()!=5){
                //新建一个编队文件
                SharedPreferences formation = getSharedPreferences("Formation_" + (mapParam.size()+1), MODE_PRIVATE);
                SharedPreferences.Editor editor = formation.edit(); // 获得编辑器的对象
                editor.putString("Formation_No", Integer.toString((mapParam.size()+1)));
                editor.putString("Formation_Name", "编队"+(mapParam.size()+1));
                editor.putString("Formation_type", "三角形");
                editor.putString("Formation_size", "50");
                SharedPreferences system = getSharedPreferences("Systemparameter",MODE_PRIVATE);//从系统参数中调取新建编队的参数
                editor.putString("distance_x", Integer.toString(system.getInt("x_separation",0)));
                editor.putString("distance_y", Integer.toString(system.getInt("y_separation",0)));
                editor.putString("distance_z", Integer.toString(system.getInt("z_separation",0)));
                editor.putString("launch_delay", Integer.toString(system.getInt("launch_Interval",0)));
                editor.putString("escape_delay", Integer.toString(system.getInt("escape_Interval",0)));
                editor.putString("Leader_no", "暂不选择");
                editor.putString("Leader2_no", "暂不选择");
                editor.apply();
                //在FormationRecord中
                SharedPreferences.Editor editor2 = FormationShared.edit();
                editor2.putString(Integer.toString(mapParam.size()+1),"编队"+(mapParam.size()+1));
                editor2.apply();
                //改变fnameararay，并重新初始化下拉框
                SharedPreferences FormationShared2 = getSharedPreferences("FormationRecord", MODE_PRIVATE);
                Map<String, String> mapParam2 = (Map<String, String>) FormationShared2.getAll();
                fnameArray=new String[mapParam2.size()+1];
                int i=0;
                for (Map.Entry<String, String> item_map : mapParam2.entrySet()) {
                    final String value = item_map.getValue(); // 获取该配对的键信息
                    fnameArray[i] = value;
                    i++;
                }
                fnameArray[i]="不在编队";
                if(mapParam.size()==0){
                    fnameArray = new String[]{"无编队"};
                }
                initTypeSpinner();// 初始化下拉
            }
        }else if(v.getId()==R.id.btn_deleteteam){
            SharedPreferences FormationShared = getSharedPreferences("FormationRecord", MODE_PRIVATE);
            Map<String, String> mapParam = (Map<String, String>) FormationShared.getAll();
            switch (mapParam.size()){
                case 0:break;
                case 1:
                    //删掉该编队的图标
                    if(findViewById(R.id.team10).getVisibility()==View.VISIBLE){
                        findViewById(R.id.team10).setVisibility(View.GONE);
                    }else{
                        findViewById(R.id.team11).setVisibility(View.GONE);
                    }
                    break;
                case 2:
                    //删掉该编队的图标
                    if(findViewById(R.id.team20).getVisibility()==View.VISIBLE){
                        findViewById(R.id.team20).setVisibility(View.GONE);
                    }else{
                        findViewById(R.id.team21).setVisibility(View.GONE);
                    }
                    break;
                case 3:
                    //删掉该编队的图标
                    if(findViewById(R.id.team30).getVisibility()==View.VISIBLE){
                        findViewById(R.id.team30).setVisibility(View.GONE);
                    }else{
                        findViewById(R.id.team31).setVisibility(View.GONE);
                    }
                    break;
                case 4:
                    //删掉该编队的图标
                    if(findViewById(R.id.team40).getVisibility()==View.VISIBLE){
                        findViewById(R.id.team40).setVisibility(View.GONE);
                    }else{
                        findViewById(R.id.team41).setVisibility(View.GONE);
                    }
                    break;
                default:
                    //删掉该编队的图标
                    if(findViewById(R.id.team50).getVisibility()==View.VISIBLE){
                        findViewById(R.id.team50).setVisibility(View.GONE);
                    }else{
                        findViewById(R.id.team51).setVisibility(View.GONE);
                    }
                    findViewById(R.id.btn_addteam).setVisibility(View.VISIBLE);
                    break;
            }
            //删掉编队
            deleteteam(mapParam.size());
            //改变fnameararay，并重新初始化下拉框
            SharedPreferences FormationShared2 = getSharedPreferences("FormationRecord", MODE_PRIVATE);
            Map<String, String> mapParam2 = (Map<String, String>) FormationShared2.getAll();
            fnameArray=new String[mapParam2.size()+1];
            int i=0;
            for (Map.Entry<String, String> item_map : mapParam2.entrySet()) {
                final String value = item_map.getValue(); // 获取该配对的键信息
                fnameArray[i] = value;
                i++;
            }
            fnameArray[i]="不在编队";
            if(mapParam.size()==0){
                fnameArray = new String[]{"无编队"};
            }
            initTypeSpinner();// 初始化下拉
        }else if (v.getId()==R.id.waypoint_route){
            GeoPoint start = GeoPoints_waypoints.get(0);
            GeoPoint end = GeoPoints_waypoints.get(GeoPoints_waypoints.size()-1);
            Point_ startPt = new Point_();//路径起点
            Point_ endPt = new Point_();//路径终点
            startPt.lat = start.getLatitude();
            startPt.lng = start.getLongitude();
            endPt.lat = end.getLatitude();
            endPt.lng = end.getLongitude();
            Vector<Obstacle> Obtls = new Vector<Obstacle>();//禁飞区集合
            //Fill in no fly zone
            for(Polygon polygon:Polygon_record)//装填禁飞区集合
            {
                Obstacle obtl_temp = new Obstacle();//禁飞区
                obtl_temp.pts = new Vector<Point_>();//禁飞区包含的点
                for(GeoPoint geopoint:polygon.getPoints())
                {
                    obtl_temp.pts.add(new Point_(geopoint.getLongitude(),geopoint.getLatitude()));
                }
                Obtls.add(obtl_temp);
            }
            Params para = new Params();//算法参数设置
            para.horizontal = 50;//设定水平精确度
            Route res = RoutePlan.getRoute(startPt, endPt, Obtls, para);//调用算法
//            for(Overlay overlay:mMapview.getOverlays())
//            {
//                if(overlay instanceof Polygon)
//                    continue;
//                mMapview.getOverlays().remove(overlay);
//            }
            for(Overlay overlay:mMapview.getOverlays()){
                if(overlay instanceof Polygon)
                    continue;
                if(overlay instanceof Polyline){
                    mMapview.getOverlays().remove(overlay);
                }
                if(overlay instanceof MarkerWithLabel){
                    mMapview.getOverlays().remove(overlay);
                }
                //mMapview.getOverlays().remove(overlay);
            }
            for (int value : planeidArray) {
                isAdd.put(value, false);
            }
            GeoPoints_waypoints.clear();
            Height_waypoints.clear();
            int i=0;
            for(Point_ point:res.pts){
                MarkerWithLabel marker = new MarkerWithLabel(mMapview,selectformationno+"-"+ i);
                marker.setId(Integer.toString(i));
                marker.setPosition(new GeoPoint(point.lat,point.lng));
                if(i==0){
                    marker.setIcon(ContextCompat.getDrawable(mContext,R.drawable.waypoint_takeoff));
                }else if(i==res.pts.size()-1){
                    marker.setIcon(ContextCompat.getDrawable(mContext,R.drawable.waypoint_down));
                }else{
                    marker.setIcon(ContextCompat.getDrawable(mContext,R.drawable.waypoint_normal));
                }
                mMapview.getOverlays().add(marker);
                if(i>0){
                    List<GeoPoint> geoPoints = new ArrayList<>();
                /*if(i == 0)
                {
                    i++;
                    continue;
                }

                 */
                    geoPoints.add(GeoPoints_waypoints.get(i-1));
                    geoPoints.add(new GeoPoint(point.lat,point.lng));
                    Polyline line = new Polyline();   //see note below!
                    line.setId(Integer.toString(i));
                    line.setPoints(geoPoints);
                    line.setColor(Color.BLUE);
                    line.setWidth(5);
                    mMapview.getOverlayManager().add(line);
                }
                GeoPoints_waypoints.add(new GeoPoint(point.lat,point.lng));
                Height_waypoints.add(200.00);
                i++;
            }
            waypointsnum_record.add(waypointsnum-1,i);
            waypointsnum_record.remove(waypointsnum);

        }else if(v.getId()==R.id.waypoint_delete){
            GeoPoints_waypoints.clear();
            Height_waypoints.clear();
            waypointsnum_record.clear();
            Geopoints_attack.clear();
            Height_attack.clear();
            Attackallocation.clear();
            Polygon_record.clear();
            polygonnum = 0;
            waypointsnum=-1;
            a=0;
            b=1;
            start_waypoint=false;
            start_attack=false;
            start_nofly=false;
            Waypoint_overlays.clear();
            Nofly_overlays.clear();
            System.out.println(mMapview.getOverlays().size());
            while(mMapview.getOverlays().size() > 0)
            {
                Overlay overlay=mMapview.getOverlays().get(0);
                mMapview.getOverlays().remove(overlay);
            }
            for (int value : planeidArray) {
                isAdd.put(value, false);
            }
            for (int value : planeidArray) {
                Planemarkers.get(value).mLabel = Planemarkers.get(value).getId();
                Planemarkers.get(value).setTitle(null);
            }
            if(findViewById(R.id.attack_4).getVisibility()==View.VISIBLE){
                findViewById(R.id.attack_4).setVisibility(View.GONE);
                findViewById(R.id.attack_3).setVisibility(View.GONE);
                findViewById(R.id.attack_2).setVisibility(View.GONE);
                findViewById(R.id.attack_1).setVisibility(View.GONE);
            }else if(findViewById(R.id.attack_3).getVisibility()==View.VISIBLE){
                findViewById(R.id.attack_3).setVisibility(View.GONE);
                findViewById(R.id.attack_2).setVisibility(View.GONE);
                findViewById(R.id.attack_1).setVisibility(View.GONE);
            }else if(findViewById(R.id.attack_2).getVisibility()==View.VISIBLE){
                findViewById(R.id.attack_2).setVisibility(View.GONE);
                findViewById(R.id.attack_1).setVisibility(View.GONE);
            }else if(findViewById(R.id.attack_1).getVisibility()==View.VISIBLE){
                findViewById(R.id.attack_1).setVisibility(View.GONE);
            }
//            for(int i=0;i<mMapview.getOverlays().size();i++){
//                Overlay overlay=mMapview.getOverlays().get(i);
//                mMapview.getOverlays().remove(overlay);
//            }
        }else if(v.getId()==R.id.attack_upload){
            ArrayList<String> multibytes=new ArrayList<String>();
            for (Integer key : Attackallocation.keySet()){
                String planeno = Integer.toString(Attackallocation.get(key));
                SharedPreferences team = getSharedPreferences("TeamRecord_"+notoname(Integer.toString(selectformationno)),MODE_PRIVATE);
                byte[] bytes=new byte[25];
                bytes[0]=(byte) 0xee;
                bytes[1]=(byte) 0x16;
                bytes[2]=(byte) 0xa5;
                bytes[3]=(byte) 0x15;
                bytes[4]=(byte) 0x33;
                bytes[5]=(byte) selectformationno;
                bytes[6]=(byte) Integer.parseInt(team.getString(planeno,""));
                bytes[7]= (byte) 0x00;
                byte[] log=toLH((int)(Geopoints_attack.get(key-1).getLongitude()*11930464.7056));
                bytes[8]=log[0];
                bytes[9]=log[1];
                bytes[10]=log[2];
                bytes[11]=log[3];
                byte[] lag=toLH((int)(Geopoints_attack.get(key-1).getLatitude()*11930464.7056));
                bytes[12]=lag[0];
                bytes[13]=lag[1];
                bytes[14]=lag[2];
                bytes[15]=lag[3];
                byte[] height=toLH2((int) (Height_attack.get(key-1)*3.2767));//改成高度list
                bytes[16]=height[0];
                bytes[17]=height[1];
                bytes[24]=CheckSum(bytes,25);//校验和
                multibytes.add(bytesToHex(bytes));
            }
            Sendpacket(multibytes);
        }else if(v.getId()==R.id.btn_startattack){
            byte[] bytes=new byte[25];
            bytes[0]=(byte) 0xee;
            bytes[1]=(byte) 0x16;
            bytes[2]=(byte) 0xa5;
            bytes[3]=(byte) 0x15;
            bytes[4]=(byte) 0x69;
            bytes[5]=(byte)selectformationno;
            bytes[6]=(byte) 0x02;
            bytes[24]=CheckSum(bytes,25);//校验和
            ArrayList<String> multibytes=new ArrayList<String>();
            multibytes.add(bytesToHex(bytes));
            Sendpacket(multibytes);
        }

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(buttonView.getId()==R.id.btn_waypoint){
            if(isChecked){
                ll_waypoint.setVisibility(View.VISIBLE);
                if(!start_waypoint){
                    start_waypoint=true;
                    Overlay touchOverlay = null;
                    touchOverlay = new Overlay(this){
                        ItemizedIconOverlay<OverlayItem> anotherItemizedIconOverlay = null;
                        ArrayList<OverlayItem> overlayArray = new ArrayList<OverlayItem>();
                        ArrayList<GeoPoint> GeoPoints = new ArrayList<GeoPoint>();
                        ArrayList<MarkerWithLabel> markerArray = new ArrayList<MarkerWithLabel>();
                        ArrayList<Polyline>lineArray = new ArrayList<>();
                        @Override
                        public void draw(Canvas arg0, MapView arg1, boolean arg2) {

                        }
                        @Override
                        public boolean onSingleTapConfirmed(final MotionEvent e, final MapView mapView) {
                            Drawable marker = getApplicationContext().getResources().getDrawable(R.drawable.tran);
                            Projection proj = mapView.getProjection();
                            GeoPoint loc = (GeoPoint) proj.fromPixels((int)e.getX(), (int)e.getY());
                            String longitude = Double.toString(((double)loc.getLongitudeE6())/1000000);
                            String latitude = Double.toString(((double)loc.getLatitudeE6())/1000000);
                            System.out.println("- Latitude = " + latitude + ", Longitude = " + longitude );
                            OverlayItem mapItem = new OverlayItem(Integer.toString(a), Integer.toString(a), new GeoPoint((((double)loc.getLatitudeE6())/1000000), (((double)loc.getLongitudeE6())/1000000)));
                            mapItem.setMarker(marker);
                            GeoPoints.add(new GeoPoint((((double)loc.getLatitudeE6())/1000000), (((double)loc.getLongitudeE6())/1000000)));
                            GeoPoints_waypoints.add(new GeoPoint((((double)loc.getLatitudeE6())/1000000), (((double)loc.getLongitudeE6())/1000000)));
                            Height_waypoints.add(200.00);
                            overlayArray.add(mapItem);

                            MarkerWithLabel marker1 = new MarkerWithLabel(mapView,selectformationno+"-"+ a);
                            //marker1.setTitle(Integer.toString(a));
                            marker1.setId(Integer.toString(a));
                            marker1.setPosition(new GeoPoint((((double)loc.getLatitudeE6())/1000000), (((double)loc.getLongitudeE6())/1000000)));
                            if(a==0){
                                marker1.setIcon(ContextCompat.getDrawable(mContext,R.drawable.waypoint_takeoff));
                                marker1.setTitle("start");
                            }else {
                                marker1.setIcon(ContextCompat.getDrawable(mContext,R.drawable.waypoint_normal));
                            }
                            markerArray.add(marker1);
                            mapView.getOverlays().add(marker1);

                            if(anotherItemizedIconOverlay==null){
                                anotherItemizedIconOverlay = new ItemizedIconOverlay<OverlayItem>(getApplicationContext(), overlayArray,null);
                                mapView.getOverlays().add(anotherItemizedIconOverlay);
                                mapView.invalidate();
                            }else{
                                mapView.getOverlays().remove(anotherItemizedIconOverlay);
                                mapView.invalidate();
                                anotherItemizedIconOverlay = new ItemizedIconOverlay<OverlayItem>(getApplicationContext(), overlayArray,null);
                                mapView.getOverlays().add(anotherItemizedIconOverlay);
                            }
                            if(a >0){
                                List<GeoPoint> geoPoints = new ArrayList<>();
                                geoPoints.add(GeoPoints.get(a -1));
                                geoPoints.add(new GeoPoint((((double)loc.getLatitudeE6())/1000000), (((double)loc.getLongitudeE6())/1000000)));
                                Polyline line = new Polyline();   //see note below!
                                line.setId(Integer.toString(a));
                                line.setPoints(geoPoints);
                                line.setColor(Color.BLUE);
                                line.setWidth(5);
                                lineArray.add(line);
                                mapView.getOverlayManager().add(line);
                            }
                            //      dlgThread();
                            a++;
                            return true;
                        }

                        @Override
                        public boolean onLongPress(MotionEvent event, MapView mapView) {
                            Projection proj = mapView.getProjection();
                            GeoPoint loc = (GeoPoint) proj.fromPixels((int)event.getX(), (int)event.getY());
                            for(MarkerWithLabel marker : markerArray) {
                                if(Math.abs(marker.getPosition().getLatitude() - ((double)loc.getLatitudeE6())/1000000) < 0.05 && Math.abs(marker.getPosition().getLongitude() - ((double)loc.getLongitudeE6())/1000000) < 0.05) {
                                    if(marker.getId().equals("0")){
                                        for (MarkerWithLabel marker1 : markerArray){
                                            mapView.getOverlays().remove(marker1);
                                        }
                                        for (Polyline line:lineArray){
                                            mapView.getOverlays().remove(line);
                                        }
                                        markerArray.clear();
                                        lineArray.clear();
                                        GeoPoints.clear();
                                        GeoPoints_waypoints.clear();
                                        Height_waypoints.clear();
                                        a=0;
                                        break;
                                    }else{
                                        mapView.getOverlays().remove(marker);
                                        int delid=Integer.parseInt(marker.getId());
                                        mapView.getOverlays().remove(lineArray.get(delid));
                                        mapView.getOverlays().remove(lineArray.get(delid-1));
                                        lineArray.remove(delid);
                                        lineArray.remove(delid-1);
                                        GeoPoints_waypoints.remove(Integer.parseInt(marker.getId()));
                                        Height_waypoints.remove(Integer.parseInt(marker.getId()));
                                        GeoPoints.remove(Integer.parseInt(marker.getId()));
                                        markerArray.remove(Integer.parseInt(marker.getId()));
                                        List<GeoPoint> geoPoints = new ArrayList<>();
                                        geoPoints.add(GeoPoints.get(Integer.parseInt(marker.getId())-1));
                                        geoPoints.add(GeoPoints.get(Integer.parseInt(marker.getId())));
                                        Polyline line = new Polyline();   //see note below!
                                        line.setId(marker.getId());
                                        line.setPoints(geoPoints);
                                        line.setColor(Color.BLUE);
                                        line.setWidth(5);
                                        lineArray.add(Integer.parseInt(marker.getId())-1,line);
                                        mapView.getOverlayManager().add(line);
                                        for(Polyline line1:lineArray){
                                            if(Integer.parseInt(line1.getId())>delid){
                                                line1.setId(Integer.toString(Integer.parseInt(line1.getId())-1));
                                            }
                                        }
                                        for(MarkerWithLabel marker1 : markerArray){
                                            if(Integer.parseInt(marker1.getId())>delid){
                                                marker1.setId(Integer.toString(Integer.parseInt(marker1.getId())-1));
                                                marker1.mLabel=selectformationno+"-"+Integer.parseInt(marker1.getId());
                                            }
                                        }//do some stuff
                                        break;
                                    }
                                }
                            }
                            if(a!=0){
                                a--;
                            }
                            return true;
                        }

                        @Override
                         public boolean onDoubleTapEvent(final MotionEvent e, final MapView mapView){
                            Drawable marker = getApplicationContext().getResources().getDrawable(R.drawable.tran);
                            Projection proj = mapView.getProjection();
                            GeoPoint loc = (GeoPoint) proj.fromPixels((int)e.getX(), (int)e.getY());
                            String longitude = Double.toString(((double)loc.getLongitudeE6())/1000000);
                            String latitude = Double.toString(((double)loc.getLatitudeE6())/1000000);
                            System.out.println("- Latitude = " + latitude + ", Longitude = " + longitude );
                            OverlayItem mapItem = new OverlayItem(Integer.toString(a), Integer.toString(a), new GeoPoint((((double)loc.getLatitudeE6())/1000000), (((double)loc.getLongitudeE6())/1000000)));
                            mapItem.setMarker(marker);
                            GeoPoints.add(new GeoPoint((((double)loc.getLatitudeE6())/1000000), (((double)loc.getLongitudeE6())/1000000)));
                            GeoPoints_waypoints.add(new GeoPoint((((double)loc.getLatitudeE6())/1000000), (((double)loc.getLongitudeE6())/1000000)));
                            Height_waypoints.add(200.00);
                            overlayArray.add(mapItem);

                            MarkerWithLabel marker1 = new MarkerWithLabel(mapView,selectformationno+"-"+ a);
                            marker1.setTitle("end");
                            marker1.setId(Integer.toString(a));
                            marker1.setPosition(new GeoPoint((((double)loc.getLatitudeE6())/1000000), (((double)loc.getLongitudeE6())/1000000)));
                            marker1.setIcon(ContextCompat.getDrawable(mContext,R.drawable.waypoint_down));
                            markerArray.add(marker1);
                            mapView.getOverlays().add(marker1);

                            if(anotherItemizedIconOverlay==null){
                                anotherItemizedIconOverlay = new ItemizedIconOverlay<OverlayItem>(getApplicationContext(), overlayArray,null);
                                mapView.getOverlays().add(anotherItemizedIconOverlay);
                                mapView.invalidate();
                            }else{
                                mapView.getOverlays().remove(anotherItemizedIconOverlay);
                                mapView.invalidate();
                                anotherItemizedIconOverlay = new ItemizedIconOverlay<OverlayItem>(getApplicationContext(), overlayArray,null);
                                mapView.getOverlays().add(anotherItemizedIconOverlay);
                            }
                            if(a >0){
                                List<GeoPoint> geoPoints = new ArrayList<>();
                                geoPoints.add(GeoPoints.get(a -1));
                                geoPoints.add(new GeoPoint((((double)loc.getLatitudeE6())/1000000), (((double)loc.getLongitudeE6())/1000000)));
                                Polyline line = new Polyline();   //see note below!
                                line.setId(Integer.toString(a));
                                line.setPoints(geoPoints);
                                line.setColor(Color.BLUE);
                                line.setWidth(5);
                                lineArray.add(line);
                                mapView.getOverlayManager().add(line);
                            }
                            a++;
                            mMapview.getOverlays().remove((Waypoint_overlays.get(waypointsnum)));
                            waypointsnum_record.add(a);
                            a=0;
                            start_waypoint=false;
                            return true;
                        }
                    };
                     Waypoint_overlays.add(touchOverlay);
                     mMapview.getOverlays().add(touchOverlay);
                     mMapview.invalidate();
                }else {
                    mMapview.getOverlays().add(Waypoint_overlays.get(waypointsnum));
                }
            }else{
                if(Waypoint_overlays.size()>0){
                    mMapview.getOverlays().remove((Waypoint_overlays.get(waypointsnum)));
                }
                ll_waypoint.setVisibility(View.GONE);
                if(!start_waypoint){
                    waypointsnum++;
                }
            }
        }else if(buttonView.getId()==R.id.btn_attack){
            if(isChecked){
                ll_attack.setVisibility(View.VISIBLE);
                if(!start_attack){
                    start_attack = true;
                    Overlay touchOverlay = new Overlay(this){
                        ArrayList<GeoPoint> GeoPoints = new ArrayList<GeoPoint>();
                        ItemizedIconOverlay<OverlayItem> anotherItemizedIconOverlay = null;
                        ArrayList<OverlayItem> overlayArray = new ArrayList<OverlayItem>();
                        @Override
                        public void draw(Canvas arg0, MapView arg1, boolean arg2) {

                        }
                        @Override
                        public boolean onSingleTapConfirmed(final MotionEvent e, final MapView mapView) {
                            Drawable marker = getApplicationContext().getResources().getDrawable(R.drawable.tran);
                            Projection proj = mapView.getProjection();
                            GeoPoint loc = (GeoPoint) proj.fromPixels((int)e.getX(), (int)e.getY());
                            String longitude = Double.toString(((double)loc.getLongitudeE6())/1000000);
                            String latitude = Double.toString(((double)loc.getLatitudeE6())/1000000);
                            System.out.println("- Latitude = " + latitude + ", Longitude = " + longitude );
                            OverlayItem mapItem = new OverlayItem(Integer.toString(b), Integer.toString(b), new GeoPoint((((double)loc.getLatitudeE6())/1000000), (((double)loc.getLongitudeE6())/1000000)));
                            mapItem.setMarker(marker);
                            GeoPoints.add(new GeoPoint((((double)loc.getLatitudeE6())/1000000), (((double)loc.getLongitudeE6())/1000000)));
                            Geopoints_attack.add(new GeoPoint((((double)loc.getLatitudeE6())/1000000), (((double)loc.getLongitudeE6())/1000000)));
                            Height_attack.add(50.00);
                            overlayArray.add(mapItem);

                            Marker marker1 = new MarkerWithLabel(mapView,Integer.toString(b));
                            marker1.setTitle(Integer.toString(b));
                            marker1.setPosition(new GeoPoint((((double)loc.getLatitudeE6())/1000000), (((double)loc.getLongitudeE6())/1000000)));
                            marker1.setIcon(ContextCompat.getDrawable(mContext,R.drawable.attack_point));
                            mapView.getOverlays().add(marker1);

                            if(anotherItemizedIconOverlay==null){
                                anotherItemizedIconOverlay = new ItemizedIconOverlay<OverlayItem>(getApplicationContext(), overlayArray,null);
                                mapView.getOverlays().add(anotherItemizedIconOverlay);
                                mapView.invalidate();
                            }else{
                                mapView.getOverlays().remove(anotherItemizedIconOverlay);
                                mapView.invalidate();
                                anotherItemizedIconOverlay = new ItemizedIconOverlay<OverlayItem>(getApplicationContext(), overlayArray,null);
                                mapView.getOverlays().add(anotherItemizedIconOverlay);
                            }
                            switch (b){
                                case 1:findViewById(R.id.attack_1).setVisibility(View.VISIBLE);break;
                                case 2:findViewById(R.id.attack_2).setVisibility(View.VISIBLE);break;
                                case 3:findViewById(R.id.attack_3).setVisibility(View.VISIBLE);break;
                                case 4:findViewById(R.id.attack_4).setVisibility(View.VISIBLE);break;
                                default:break;
                            }
                            if(b<4){
                                b++;
                            }else{
                                mMapview.getOverlays().remove(Attack_overlay);
                            }
                            //      dlgThread();
                            return true;
                        }
                    };
                    Attack_overlay = touchOverlay;
                    mMapview.getOverlays().add(touchOverlay);
                    mMapview.invalidate();
                }else{
                    mMapview.getOverlays().add(Attack_overlay);
                }
            }else{
                mMapview.getOverlays().remove(Attack_overlay);
                ll_attack.setVisibility(View.GONE);
            }
        }else if(buttonView.getId()==R.id.swarm_switch){
            if(isChecked){//集结编队
                byte[] bytes=new byte[25];
                bytes[0]=(byte) 0xee;
                bytes[1]=(byte) 0x16;
                bytes[2]=(byte) 0xa5;
                bytes[3]=(byte) 0x15;
                bytes[4]=(byte) 0x93;
                bytes[5]=(byte)selectformationno;
                bytes[24]=CheckSum(bytes,25);//校验和
                ArrayList<String> multibytes=new ArrayList<String>();
                multibytes.add(bytesToHex(bytes));
                Sendpacket(multibytes);
            }else {//解散编队
                byte[] bytes=new byte[25];
                bytes[0]=(byte) 0xee;
                bytes[1]=(byte) 0x16;
                bytes[2]=(byte) 0xa5;
                bytes[3]=(byte) 0x15;
                bytes[4]=(byte) 0x96;
                bytes[5]=(byte)selectformationno;
                bytes[24]=CheckSum(bytes,25);//校验和
                ArrayList<String> multibytes=new ArrayList<String>();
                multibytes.add(bytesToHex(bytes));
                Sendpacket(multibytes);
            }
        }else if(buttonView.getId()==R.id.telemetry_switch){
            if(isChecked){//单机遥测开
                SharedPreferences plane;
                plane = getSharedPreferences("Plane_"+selectplaneid, MODE_PRIVATE);
                byte[] bytes=new byte[25];
                bytes[0]=(byte) 0xee;
                bytes[1]=(byte) 0x16;
                bytes[2]=(byte) 0xa5;
                bytes[3]=(byte) 0x15;
                bytes[4]=(byte) 0xa9;
                bytes[5]=(byte)selectformationno;
                bytes[6]=(byte)plane.getInt("Location_inteam", 0);
                bytes[24]=CheckSum(bytes,25);//校验和
                ArrayList<String> multibytes=new ArrayList<String>();
                multibytes.add(bytesToHex(bytes));
                Sendpacket(multibytes);
            }else {//单机遥测关
                SharedPreferences plane;
                plane = getSharedPreferences("Plane_"+selectplaneid, MODE_PRIVATE);
                byte[] bytes=new byte[25];
                bytes[0]=(byte) 0xee;
                bytes[1]=(byte) 0x16;
                bytes[2]=(byte) 0xa5;
                bytes[3]=(byte) 0x15;
                bytes[4]=(byte) 0xac;
                bytes[5]=(byte)selectformationno;
                bytes[6]=(byte)plane.getInt("Location_inteam", 0);
                bytes[24]=CheckSum(bytes,25);//校验和
                ArrayList<String> multibytes=new ArrayList<String>();
                multibytes.add(bytesToHex(bytes));
                Sendpacket(multibytes);
            }
        }else if(buttonView.getId()==R.id.btn_nofly){
            if(isChecked){
                if(!start_nofly){
                    start_nofly = true;
                    // Initialize no fly zone polygon
                    Polygon polygon = new Polygon();
                    polygon.setFillColor(0x4DFF0000);
                    polygon.setStrokeColor(Color.RED);
                    polygon.setStrokeWidth(5);

                    Overlay touchOverlay = new Overlay(this) {
                        ItemizedIconOverlay<OverlayItem> anotherItemizedIconOverlay = null;
                        ArrayList<OverlayItem> overlayArray = new ArrayList<OverlayItem>();
                        ArrayList<GeoPoint> GeoPoints = new ArrayList<GeoPoint>();
                        @Override
                        public void draw(Canvas arg0, MapView arg1, boolean arg2) {

                        }
                        @Override
                        public boolean onSingleTapConfirmed(final MotionEvent e, final MapView mapView) {
                            Drawable marker = getApplicationContext().getResources().getDrawable(R.drawable.tran);
                            Projection proj = mapView.getProjection();
                            GeoPoint loc = (GeoPoint) proj.fromPixels((int) e.getX(), (int) e.getY());
                            String longitude = Double.toString(((double) loc.getLongitudeE6()) / 1000000);
                            String latitude = Double.toString(((double) loc.getLatitudeE6()) / 1000000);
                            System.out.println("- Latitude = " + latitude + ", Longitude = " + longitude);
                            OverlayItem mapItem = new OverlayItem(Integer.toString(a), Integer.toString(a), new GeoPoint((((double) loc.getLatitudeE6()) / 1000000), (((double) loc.getLongitudeE6()) / 1000000)));
                            mapItem.setMarker(marker);
                            GeoPoints.add(new GeoPoint((((double) loc.getLatitudeE6()) / 1000000), (((double) loc.getLongitudeE6()) / 1000000)));
                            overlayArray.add(mapItem);

                            if (anotherItemizedIconOverlay == null) {
                                anotherItemizedIconOverlay = new ItemizedIconOverlay<OverlayItem>(getApplicationContext(), overlayArray, null);
                                mapView.getOverlays().add(anotherItemizedIconOverlay);
                                mapView.invalidate();
                            } else {
                                mapView.getOverlays().remove(anotherItemizedIconOverlay);
                                mapView.invalidate();
                                anotherItemizedIconOverlay = new ItemizedIconOverlay<OverlayItem>(getApplicationContext(), overlayArray, null);
                                mapView.getOverlays().add(anotherItemizedIconOverlay);
                            }

                            GeoPoint geopoint = new GeoPoint((((double) loc.getLatitudeE6()) / 1000000), (((double) loc.getLongitudeE6()) / 1000000));
                            mapView.getOverlayManager().remove(polygon);
                            polygon.addPoint(geopoint);
                            mapView.getOverlayManager().add(polygon);
                            return true;
                        }
                        @Override
                        public boolean onLongPress(final MotionEvent e, final MapView mapView) {
                            Drawable marker = getApplicationContext().getResources().getDrawable(R.drawable.tran);
                            Projection proj = mapView.getProjection();
                            GeoPoint loc = (GeoPoint) proj.fromPixels((int) e.getX(), (int) e.getY());
                            String longitude = Double.toString(((double) loc.getLongitudeE6()) / 1000000);
                            String latitude = Double.toString(((double) loc.getLatitudeE6()) / 1000000);
                            System.out.println("- Latitude = " + latitude + ", Longitude = " + longitude);
                            OverlayItem mapItem = new OverlayItem(Integer.toString(a), Integer.toString(a), new GeoPoint((((double) loc.getLatitudeE6()) / 1000000), (((double) loc.getLongitudeE6()) / 1000000)));
                            mapItem.setMarker(marker);
                            GeoPoints.add(new GeoPoint((((double) loc.getLatitudeE6()) / 1000000), (((double) loc.getLongitudeE6()) / 1000000)));
                            overlayArray.add(mapItem);

                            if (anotherItemizedIconOverlay == null) {
                                anotherItemizedIconOverlay = new ItemizedIconOverlay<OverlayItem>(getApplicationContext(), overlayArray, null);
                                mapView.getOverlays().add(anotherItemizedIconOverlay);
                                mapView.invalidate();
                            } else {
                                mapView.getOverlays().remove(anotherItemizedIconOverlay);
                                mapView.invalidate();
                                anotherItemizedIconOverlay = new ItemizedIconOverlay<OverlayItem>(getApplicationContext(), overlayArray, null);
                                mapView.getOverlays().add(anotherItemizedIconOverlay);
                            }
                            GeoPoint geopoint = new GeoPoint((((double) loc.getLatitudeE6()) / 1000000), (((double) loc.getLongitudeE6()) / 1000000));
                            mapView.getOverlayManager().remove(polygon);
                            polygon.addPoint(geopoint);
                            mapView.getOverlayManager().add(polygon);
                            //      dlgThread();
                            Polygon_record.add(polygon);
                            mMapview.getOverlays().remove(Nofly_overlays.get(polygonnum));
                            start_nofly=false;
                            return true;
                        }
                    };
                    Nofly_overlays.add(touchOverlay);
                    mMapview.getOverlays().add(touchOverlay);
                    mMapview.invalidate();
                }else{
                    mMapview.getOverlays().add(Nofly_overlays.get(polygonnum));
                }
            }else{
                mMapview.getOverlays().remove(Nofly_overlays.get(polygonnum));
                if(!start_nofly){
                    polygonnum++;
                }
            }
        }

    }

    class MySelectedListener implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            swarmname=arg2;
            String sn=fnameArray[swarmname];//选中的编队名称
            if(!sn.equals("不在编队") && !sn.equals("无编队")){
                selectformationno = Integer.parseInt(nametono(sn));//选中的编队号
            }
            SharedPreferences TeamShared = getSharedPreferences("TeamRecord_"+sn, MODE_PRIVATE);
            Map<String, String> planemap = (Map<String, String>) TeamShared.getAll();
            String[] planeidArray=new String[planemap.size()+1];
            int j=0;
            for (Map.Entry<String, String> item_map : planemap.entrySet()) {
                final String key = item_map.getKey(); // 获取该配对的键信息
                planeidArray[j]=key;
                j++;
            }
            planeidArray[j]="不选择";
            //获取该编队中各个飞机的队内位置
            if(sn.equals("无编队")){
                SharedPreferences PlaneShared = getSharedPreferences("PlaneRecord", MODE_PRIVATE);
                Map<String, String> pmap = (Map<String, String>) PlaneShared.getAll();
                if(pmap.size()==0){
                    planeidArray= new String[]{"无飞机"};
                }else{
                    planeidArray=new String[pmap.size()+1];
                    int k=0;
                    for (Map.Entry<String, String> item_map : pmap.entrySet()) {
                        final String key = item_map.getKey(); // 获取该配对的键信息
                        planeidArray[k]=key;
                        k++;
                    }
                    planeidArray[k]="不选择";
                    //获取全部飞机的飞机地址
                }
            }
            ArrayAdapter<String> idAdapter = new ArrayAdapter<String>(mContext,
                    R.layout.spinner_item, planeidArray);
            idAdapter.setDropDownViewResource(R.layout.item_dropdown);
            Spinner sp_lid2 = findViewById(R.id.sp_plane);
            sp_lid2.setAdapter(idAdapter);
            sp_lid2.setSelection(0);
            sp_lid2.setOnItemSelectedListener(new MySelectedListener2());
        }

        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }

    class MySelectedListener2 implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            planeid=arg2;
            String sn=fnameArray[swarmname];//选中的编队名称
            SharedPreferences TeamShared = getSharedPreferences("TeamRecord_"+sn, MODE_PRIVATE);
            Map<String, String> planemap = (Map<String, String>) TeamShared.getAll();
            String[] planeidArray=new String[planemap.size()+1];
            int j=0;
            for (Map.Entry<String, String> item_map : planemap.entrySet()) {
                final String key = item_map.getKey(); // 获取该配对的键信息
                planeidArray[j]=key;
                j++;
            }
            planeidArray[j]="不选择";
            //获取该编队中各个飞机的队内位置
            if(sn.equals("无编队")){
                SharedPreferences PlaneShared = getSharedPreferences("PlaneRecord", MODE_PRIVATE);
                Map<String, String> pmap = (Map<String, String>) PlaneShared.getAll();
                if(pmap.size()==0){
                    planeidArray= new String[]{"无飞机"};
                }else{
                    planeidArray=new String[pmap.size()];
                    int k=0;
                    for (Map.Entry<String, String> item_map : pmap.entrySet()) {
                        final String key = item_map.getKey(); // 获取该配对的键信息
                        planeidArray[k]=key;
                        k++;
                    }//获取全部飞机的飞机地址
                }
            }
            if(!planeidArray[planeid].equals("无飞机")&&!planeidArray[planeid].equals("不选择")){
                selectplaneid = Integer.parseInt(planeidArray[planeid]);
            }
            SharedPreferences plane = getSharedPreferences("Plane_"+selectplaneid,MODE_PRIVATE);
            RtspStream(plane.getString("Video_address",""));
        }

        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }

    private UDPListener udpListener = new UDPListener() {
        @Override
        public void onError(String error) {
            Toast.makeText(mContext, "错误：" + error, Toast.LENGTH_SHORT).show();
            Log.v(TAG, error);
        }

        @Override
        public void onReceive(String content) {//UI线程
            recievepacket = hexToByteArray(content);
            receive_string.setText(content);//接收源码显示
            Packetanalysis(recievepacket);//数据解析
            //Log.v(TAG, content);
        }

        @Override
        public void onSend(String content) {
            //Toast.makeText(mContext, "发送成功：" + content, Toast.LENGTH_SHORT).show();
            send_string.setText(content);
            Log.v(TAG, content);
        }
    };

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    private void Packetanalysis(byte[] recievepacket) {
        if(recievepacket[0]==(byte)0xee&&recievepacket[1]==(byte)0x16&&recievepacket[2]==(byte)0x5a){
            planeaddress = recievepacket[4];
            planetype = recievepacket[11];
            formationtype = recievepacket[9];
            Longitude = ((recievepacket[19]& 0xff)*pow(16,6)+(recievepacket[18]& 0xff)*pow(16,4)+(recievepacket[17]& 0xff)*pow(16,2)+(recievepacket[16]& 0xff))/11930464.7056;
            Latitude = ((recievepacket[23]& 0xff)*pow(16,6)+(recievepacket[22]& 0xff)*pow(16,4)+(recievepacket[21]& 0xff)*pow(16,2)+(recievepacket[20]& 0xff))/11930464.7056;
            mheight = ((recievepacket[25]& 0xff)*pow(16,2)+(recievepacket[24]& 0xff))/3.2767;
            pitch = ((recievepacket[27]& 0xff)*pow(16,2)+(recievepacket[26]& 0xff))/182.0389;
            roll = ((recievepacket[29]& 0xff)*pow(16,2)+(recievepacket[28]& 0xff))/182.0389;
            yaw = (float) ((float)((recievepacket[31]& 0xff)*pow(16,2)+(recievepacket[30]& 0xff))/182.0389);
            speed = ((recievepacket[33]& 0xff)*pow(16,2)+(recievepacket[32]& 0xff))/3.2767;
            arrivewaypoint = recievepacket[37];
            lheight = ((recievepacket[42]& 0xff)*pow(16,2)+(recievepacket[41]& 0xff))/3.2767;
            //回传得到的数据展示
           if(!isAdd.get(planeaddress)){
               mMapview.getOverlays().add(Planemarkers.get(planeaddress));
               isAdd.put(planeaddress,true);
           }
            GeoPoints_plane.put(planeaddress,new GeoPoint(Latitude,Longitude));
            Yaw_plane.put(planeaddress,yaw);
        }


    }

    private String nametono(String name){
        String no = "";
        SharedPreferences formation = getSharedPreferences("FormationRecord", MODE_PRIVATE);
        Map<String, Object> mapParam = (Map<String, Object>) formation.getAll();
        for (Map.Entry<String, Object> item_map : mapParam.entrySet()) {
            final String key = item_map.getKey(); // 获取该配对的键信息
            if(name.equals(item_map.getValue())){
                no=key;
            }
        }
        if(name.equals("不在编队")){
            no="0";
        }
        return no;
    }

    public byte CheckSum (byte[] bytes,int length){
        int i;
        byte sum=0;
        for(i=2;i<length-1;i++){
            sum+=bytes[i];
        }
        return sum;
    }

    public void Sendpacket(ArrayList<String> multibytes){
        udpSendHelper=new UDPSendHelper(mContext,sendPort,ip);
        udpSendHelper.setUDPListener(udpListener);
        udpSendHelper.multibytes=multibytes;
        udpSendHelper.start();
    }

    private void initmarker(Marker marker,int planeaddress){
        marker.setAnchor((float)0.5, (float)0.5);
        marker.setPosition(new GeoPoint(40.85759398, 109.21969));
        marker.setId(Integer.toString(planeaddress));
        SharedPreferences PlaneShared = getSharedPreferences("PlaneRecord", MODE_PRIVATE);
        Map<String, String> pmap = (Map<String, String>) PlaneShared.getAll();
        String formationname=null;
        for (Map.Entry<String, String> item_map : pmap.entrySet()) {
            if(Integer.toString(planeaddress).equals(item_map.getKey())){
                formationname=item_map.getValue();
            }
        }
        switch (nametono(formationname)) {
            case "1":
                marker.setIcon(getResources().getDrawable(R.mipmap.plane_cyan));
                break;
            case "2":
                marker.setIcon(getResources().getDrawable(R.mipmap.plane_yellow));
                break;
            case "3":
                marker.setIcon(getResources().getDrawable(R.mipmap.plane_green));
                break;
            case "4":
                marker.setIcon(getResources().getDrawable(R.mipmap.plane_blue));
                break;
            case "5":
                marker.setIcon(getResources().getDrawable(R.mipmap.plane_red));
                break;
            default:
                marker.setIcon(getResources().getDrawable(R.mipmap.plane_black));
                break;
        }
    }

    public static byte[] toLH(int n) {
        byte[] b = new byte[4];
        b[0] = (byte) (n & 0xff);
        b[1] = (byte) (n >> 8 & 0xff);
        b[2] = (byte) (n >> 16 & 0xff);
        b[3] = (byte) (n >> 24 & 0xff);
        return b;
    }

    public static byte[] toLH2(int n) {
        byte[] b = new byte[2];
        b[0] = (byte) (n & 0xff);
        b[1] = (byte) (n >> 8 & 0xff);
        return b;
    }

    private void RtspStream(String rtspUrl) {
        rtspvideo.setVideoURI(Uri.parse(rtspUrl));
        rtspvideo.requestFocus();
        rtspvideo.start();
    }
    private void deleteteam(int size){
        File file= new File("/data/data/"+getPackageName().toString()+"/shared_prefs","Formation_"+size+".xml");
        if(file.exists())
        {
            file.delete();
        }
        SharedPreferences team = getSharedPreferences("TeamRecord_" + size, MODE_PRIVATE);
        Map<String, String> mapParam1 = (Map<String, String>) team.getAll();
        for (Map.Entry<String, String> item_map : mapParam1.entrySet()) {
            final String key = item_map.getKey();
            SharedPreferences plane = getSharedPreferences("Plane_" + key, MODE_PRIVATE);
            SharedPreferences.Editor editor=plane.edit();
            editor.putString("At_Formation","不在编队");
            editor.apply();
            SharedPreferences planerecord = getSharedPreferences("PlaneRecord",MODE_PRIVATE);
            SharedPreferences.Editor editor1 = planerecord.edit();
            editor1.putString(key,"不在编队");
            editor1.apply();
            SharedPreferences teamrecord = getSharedPreferences("TeamRecord_" + "不在编队", MODE_PRIVATE);
            SharedPreferences.Editor editor3=teamrecord.edit();
            editor3.putString(key,"0");
            editor3.apply();
        }
        File file2= new File("/data/data/"+getPackageName().toString()+"/shared_prefs","TeamRecord_"+notoname(Integer.toString(size))+".xml");
        if(file2.exists())
        {
            file2.delete();
        }
        SharedPreferences FormationShared = getSharedPreferences("FormationRecord", MODE_PRIVATE);
        SharedPreferences.Editor editor2 = FormationShared.edit();
        editor2.remove(Integer.toString(size));
        editor2.apply();
    }

    private void add2team(String planeno,String teamno){
        SharedPreferences plane = getSharedPreferences("Plane_"+planeno, MODE_PRIVATE);//获取该飞机的信息
        if((!plane.getString("At_Formation", "").equals("不在编队"))&&(!plane.getString("At_Formation", "").equals(teamno))){//之前在别的编队删掉前编队关于它的信息
            SharedPreferences formation = getSharedPreferences("Formation_" + nametono(plane.getString("At_Formation", "")), MODE_PRIVATE);
            SharedPreferences.Editor editor6 = formation.edit();
            if(formation.getString("Leader_no", "").equals(planeno)){
                editor6.putString("Leader_no","暂不选择");
            }
            if(formation.getString("Leader2_no", "").equals(planeno)){
                editor6.putString("Leader2_no","暂不选择");
            }
            editor6.apply();
        }
        SharedPreferences team = getSharedPreferences("TeamRecord_" + plane.getString("At_Formation", ""), MODE_PRIVATE);
        SharedPreferences.Editor editor5 = team.edit(); // 获得编辑器的对象
        editor5.remove(planeno);
        editor5.apply();
        //改编为新编队的属性
        SharedPreferences newteam = getSharedPreferences("TeamRecord_" + notoname(teamno), MODE_PRIVATE);
        Map<String, String> mapParam = (Map<String, String>) newteam.getAll();
        SharedPreferences.Editor editor = plane.edit();//飞机自己的属性
        editor.putString("At_Formation",notoname(teamno));
        editor.putInt("Location_inteam",mapParam.size()+1);//默认按顺序添加队内位置
        editor.apply();

        SharedPreferences planerecord = getSharedPreferences("PlaneRecord", MODE_PRIVATE);//修改plaanerecord
        SharedPreferences.Editor editor2 = planerecord.edit();
        editor2.putString(planeno,notoname(teamno));
        editor2.apply();

        SharedPreferences team2 = getSharedPreferences("TeamRecord_" + notoname(teamno), MODE_PRIVATE);//写进新的teamrecord
        SharedPreferences.Editor editor3 = team2.edit(); // 获得编辑器的对象
        editor3.putString(planeno, Integer.toString(mapParam.size()+1));
        editor3.apply();

        SharedPreferences formation2 = getSharedPreferences("Formation_" + teamno, MODE_PRIVATE);
        SharedPreferences.Editor editor4 = formation2.edit();
        if(mapParam.size()==0){
            editor4.putString("Leader_no",planeno);
        }else if(mapParam.size()==1){
            editor4.putString("Leader_no",planeno);
        }
        editor4.apply();

    }

    private String notoname(String no) {
        String name = "";
        SharedPreferences formation = getSharedPreferences("FormationRecord", MODE_PRIVATE);
        Map<String, Object> mapParam = (Map<String, Object>) formation.getAll();
        for (Map.Entry<String, Object> item_map : mapParam.entrySet()) {
            final String value = (String) item_map.getValue(); // 获取该配对的键信息
            if (no.equals(item_map.getKey())) {
                name = value;
            }
        }
        return name;
    }

    private void sendformationpacket(String planeno,String teamno){
        ArrayList<String> multibytes=new ArrayList<String>();
        SharedPreferences formation;
        formation = getSharedPreferences("Formation_"+teamno, MODE_PRIVATE);
        SharedPreferences plane;
        plane = getSharedPreferences("Plane_"+planeno, MODE_PRIVATE);
        byte[] bytes=new byte[25];
        bytes[0]=(byte) 0xee;
        bytes[1]=(byte) 0x16;
        bytes[2]=(byte) 0xa5;
        bytes[3]=(byte) 0x15;
        bytes[4]=(byte) 0xa6;
        bytes[5]=(byte) Integer.parseInt(planeno);
        bytes[6]=(byte) Integer.parseInt(teamno);
        bytes[7]=(byte) Integer.parseInt(formation.getString("Formation_size","")) ;
        if(formation.getString("Formation_type", "").equals("三角形")){
            bytes[8]=(byte) 0x00;
        }else {
            bytes[8]=(byte)0x01;
        }
        bytes[9]=(byte)plane.getInt("Location_inteam",0);
        if(plane.getInt("Location_inteam",0)==1||plane.getInt("Location_inteam",0)==2){
            bytes[10]=(byte)plane.getInt("Location_inteam",0);
        }else {
            bytes[10]=(byte)0x00;
        }
        if(formation.getString("Leader_no", "").equals("暂不选择")){
            bytes[11]=(byte)0x00;
        }else{
            bytes[11]=(byte)Integer.parseInt(formation.getString("Leader_no",""));
        }
        if(formation.getString("Leader2_no", "").equals("暂不选择")){
            bytes[12]=(byte)0x00;
        }else{
            bytes[12]=(byte)Integer.parseInt(formation.getString("Leader2_no",""));
        }
        bytes[13]=(byte)Integer.parseInt(formation.getString("distance_x",""));
        bytes[14]=(byte)Integer.parseInt(formation.getString("distance_y",""));
        bytes[15]=(byte)Integer.parseInt(formation.getString("distance_z",""));
        bytes[16]=(byte)Integer.parseInt(formation.getString("launch_delay",""));
        bytes[17]=(byte)Integer.parseInt(formation.getString("escape_delay",""));
        bytes[24]=CheckSum(bytes,25);//校验和
        multibytes.add(bytesToHex(bytes));
        Sendpacket(multibytes);
    }

}
