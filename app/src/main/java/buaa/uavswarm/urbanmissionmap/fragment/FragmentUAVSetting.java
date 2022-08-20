package buaa.uavswarm.urbanmissionmap.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.duke.udp.UDPReceiveHelper;
import com.duke.udp.UDPSendHelper;
import com.duke.udp.util.UDPListener;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import buaa.uavswarm.urbanmissionmap.util.MapValueComparator;
import buaa.uavswarm.urbanmissionmap.R;
import buaa.uavswarm.urbanmissionmap.activities.SettingActivity;

import static android.content.Context.MODE_PRIVATE;
import static buaa.uavswarm.urbanmissionmap.util.ByteTransFormUtil.bytesToHex;
import static buaa.uavswarm.urbanmissionmap.util.ByteTransFormUtil.hexToByteArray;

public class FragmentUAVSetting extends Fragment {
    public static final String TAG = "UDP_TAG";
    protected View mView;
    protected Context mContext;
    private SharedPreferences PlaneShared;
    private LinearLayout ll_IP;
    private Button Add;
    private Button Upload;
    private planeSelectInterface mSelectInterface;
    private int sendPort = 20005;
    private String ip = "224.1.1.1";
    private UDPSendHelper udpSendHelper;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContext=getActivity();
        mView=inflater.inflate(R.layout.fragment_uavsetting, container, false);
        ll_IP = mView.findViewById(R.id.ll_IP);
        Add = mView.findViewById(R.id.btn_adduav);
        Upload = mView.findViewById(R.id.btn_upload);
        PlaneShared = this.getActivity().getSharedPreferences("PlaneRecord", MODE_PRIVATE);
        Add.setOnClickListener(v -> {
            SettingActivity activity = (SettingActivity) getActivity();
            FragmentManager fm = activity.getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.hide(activity.mFragments[0]).hide(activity.mFragments[1]).hide(activity.mFragments[2]).hide(activity.mFragments[3]).
                    hide(activity.mFragments[4]).hide(activity.mFragments[5]).hide(activity.mFragments[6]);
            ft.show(activity.mFragments[3]).commit();
        });
        Upload.setOnClickListener(v -> {
            Map<String, String> mapPlane = (Map<String, String>) PlaneShared.getAll();
            ArrayList<String> multibytes=new ArrayList<String>();
            for (Map.Entry<String, String> item_map : mapPlane.entrySet()) {
                final String key = item_map.getKey(); // 获取该配对的键信息
                final String value = item_map.getValue();
                if(!value.equals("不在编队")){
                    SharedPreferences formation;
                    formation = getActivity().getSharedPreferences("Formation_"+nametono(value), MODE_PRIVATE);
                    SharedPreferences plane;
                    plane = getActivity().getSharedPreferences("Plane_"+Integer.parseInt(key), MODE_PRIVATE);
                    byte[] bytes=new byte[25];
                    bytes[0]=(byte) 0xee;
                    bytes[1]=(byte) 0x16;
                    bytes[2]=(byte) 0xa5;
                    bytes[3]=(byte) 0x15;
                    bytes[4]=(byte) 0xa6;
                    bytes[5]=(byte) Integer.parseInt(key);
                    bytes[6]=(byte) Integer.parseInt(nametono(value));
                    bytes[7]=(byte) Integer.parseInt(formation.getString("Formation_size","")) ;
                    if(formation.getString("Formation_type", "").equals("三角形")){
                        bytes[8]=(byte) 0x00;
                    }else {
                        bytes[8]=(byte)0x01;
                    }
                    bytes[9]=(byte)plane.getInt("Location_inteam",0);
                    if(plane.getInt("Location_inteam",0)==1){
                        bytes[10]=(byte)0x00;//长机
                    }else if(plane.getInt("Location_inteam",0)==2){
                        bytes[10]=(byte)0x02;//备份长机
                    }else{
                        bytes[10]=(byte)0x01;//僚机
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
                }
            }
            Sendpacket(multibytes);
            showToast("编队配置已上传");
        });


        return mView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        showplane();
    }
    private View mContextView;

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        // 保存该IP地址行的视图，以便删除IP时一块从列表移除该行
        mContextView = v;
        // 从menu_planes.xml中构建菜单界面布局
        getActivity().getMenuInflater().inflate(R.menu.menu_planes, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_modify) { // 点击了菜单项“修改”
            // 跳转到修改页面
            goModify(Integer.toString(mContextView.getId()));
        } else if (id == R.id.menu_delete) { // 点击了菜单项“删除”
            SharedPreferences plane;
            plane = getActivity().getSharedPreferences("Plane_" + mContextView.getId(), MODE_PRIVATE);
            if((!plane.getString("At_Formation", "").equals("不在编队"))){
                SharedPreferences formation;
                formation = getActivity().getSharedPreferences("Formation_" + nametono(plane.getString("At_Formation", "")), MODE_PRIVATE);
                SharedPreferences.Editor editor6 = formation.edit();
                if(formation.getString("Leader_no", "").equals(Integer.toString(mContextView.getId()))){
                    editor6.putString("Leader_no","暂不选择");
                }
                if(formation.getString("Leader2_no", "").equals(Integer.toString(mContextView.getId()))){
                    editor6.putString("Leader2_no","暂不选择");
                }
                editor6.apply();
            }
            SharedPreferences team;
            team = getActivity().getSharedPreferences("TeamRecord_" + plane.getString("At_Formation", ""), MODE_PRIVATE);
            SharedPreferences.Editor editor = team.edit();
            editor.remove(Integer.toString(mContextView.getId()));
            editor.apply();
            // 删除该飞机的XML文件
            File file= new File("/data/data/"+getActivity().getPackageName().toString()+"/shared_prefs","Plane_"+mContextView.getId()+".xml");
            if(file.exists())
            {
                file.delete();
            }
            SharedPreferences.Editor editor2 = PlaneShared.edit();
            editor2.remove(Integer.toString(mContextView.getId()));
            editor2.apply();
            // 从列表中删除该行
            ll_IP.removeView(mContextView);
        }
        return true;
    }
    // 跳转到详情页面
    private void goModify(String rowid) {
        mSelectInterface.onPlaneSelect(rowid);
        SettingActivity activity = (SettingActivity) getActivity();
        FragmentManager fm = activity.getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.hide(activity.mFragments[0]).hide(activity.mFragments[1]).hide(activity.mFragments[2]).hide(activity.mFragments[3]).hide(activity.mFragments[4]).hide(activity.mFragments[5]).hide(activity.mFragments[6]);
        ft.show(activity.mFragments[4]).commit();
    }
    @Override
    public void onResume() {
        super.onResume();

    }
    private void showplane() {
        // 移除线性视图ll_group下面的所有子视图
        ll_IP.removeAllViews();
        // 创建一个标题行的线性视图ll_row
        LinearLayout ll_row = newLinearLayout(LinearLayout.HORIZONTAL, ViewGroup.LayoutParams.WRAP_CONTENT,Color.LTGRAY);
        ll_row.addView(newTextView(2, 2, Gravity.CENTER, "编号", Color.BLACK, 18));
        ll_row.addView(newTextView(2, 2, Gravity.CENTER, "飞行器类型", Color.BLACK, 18));
        ll_row.addView(newTextView(2, 4, Gravity.CENTER, "载荷视频地址", Color.BLACK, 18));
        ll_row.addView(newTextView(2, 2, Gravity.CENTER, "所处编队", Color.BLACK, 18));
        ll_row.addView(newTextView(2, 2, Gravity.CENTER, "队内位置", Color.BLACK, 18));
        ll_row.addView(newTextView(0, (float)0.5, Gravity.CENTER, "", Color.BLACK, 18));
        // 把标题行添加到列表
        ll_IP.addView(ll_row);
        Map<String, String> mapParam = (Map<String, String>) PlaneShared.getAll();
        if(mapParam.size()>2){
            mapParam = sortMapByValue(mapParam);
        }
        for (Map.Entry<String, String> item_map : mapParam.entrySet()) {
            final String key = item_map.getKey(); // 获取该配对的键信息
            SharedPreferences plane;
            plane = getActivity().getSharedPreferences("Plane_" + key, MODE_PRIVATE);
            // 创建该行的水平线性视图。
            ll_row = newLinearLayout(LinearLayout.HORIZONTAL, ViewGroup.LayoutParams.WRAP_CONTENT,Color.WHITE);
            // 设置该线性视图的编号
            ll_row.setId(Integer.parseInt(key));
            // 添加飞机地址和IP地址
            ll_row.addView(newTextView(10, 2, Gravity.CENTER, "" + plane.getInt("Plane_No", 0), Color.BLACK, 18));
            ll_row.addView(newTextView(10, 2, Gravity.CENTER, "" + plane.getString("Plane_type", ""), Color.BLACK, 18));
            ll_row.addView(newTextView(10, 4, Gravity.CENTER, "" + plane.getString("Video_address", ""), Color.BLACK, 18));
            ll_row.addView(newTextView(10, 2, Gravity.CENTER, "" + plane.getString("At_Formation", ""), Color.BLACK, 18));
            ll_row.addView(newTextView(10, 2, Gravity.CENTER, "" + plane.getInt("Location_inteam", 0), Color.BLACK, 18));
            ll_row.addView(newImageView(0,(float)0.5, ContextCompat.getDrawable(mContext,R.drawable.detial_2)));
            // 给行添加点击事件
            ll_row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goModify(key);
                }
            });
            // 给行注册上下文菜单，为防止重复注册，这里先注销再注册
            unregisterForContextMenu(ll_row);
            registerForContextMenu(ll_row);
            // 往列表添加该行
            ll_IP.addView(ll_row);
        }
    }

    // 创建一个线性视图的框架
    private LinearLayout newLinearLayout(int orientation, int height,int color) {
        LinearLayout ll_new = new LinearLayout(mContext);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, height);
        ll_new.setLayoutParams(params);
        ll_new.setOrientation(orientation);
        ll_new.setBackgroundColor(color);
        return ll_new;
    }

    // 创建一个文本视图的模板
    private TextView newTextView(int height, float weight, int gravity, String text, int textColor, int textSize) {
        TextView tv_new = new TextView(mContext);
        if (height == -3) {  // 垂直排列
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, 0, weight);
            tv_new.setLayoutParams(params);
        } else {  // 水平排列
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    0, (height == 0) ? ViewGroup.LayoutParams.WRAP_CONTENT : ViewGroup.LayoutParams.MATCH_PARENT, weight);
            tv_new.setLayoutParams(params);
        }
        tv_new.setText(text);
        tv_new.setTextColor(textColor);
        tv_new.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
        tv_new.setGravity(Gravity.CENTER | gravity);
        return tv_new;
    }

    private ImageView newImageView(int height, float weight, Drawable drawable) {
        ImageView tv_new = new ImageView(mContext);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    0,(height == 0) ? ViewGroup.LayoutParams.WRAP_CONTENT : ViewGroup.LayoutParams.MATCH_PARENT, weight);
            tv_new.setLayoutParams(params);
        tv_new.setBackground(drawable);
        return tv_new;
    }

    public interface planeSelectInterface{
        public void onPlaneSelect(String title);
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mSelectInterface = (planeSelectInterface) activity;
        } catch (Exception e) {
            throw new ClassCastException(activity.toString() + "must implement OnArticleSelectedListener");
        }
    }

    public static Map<String, String> sortMapByValue(Map<String, String> oriMap) {
        if (oriMap == null || oriMap.isEmpty()) {
            return null;
        }
        Map<String, String> sortedMap = new LinkedHashMap<String, String>();
        List<Map.Entry<String, String>> entryList = new ArrayList<Map.Entry<String, String>>(
                oriMap.entrySet());
        Collections.sort(entryList, new MapValueComparator());
        Iterator<Map.Entry<String, String>> iter = entryList.iterator();
        Map.Entry<String, String> tmpEntry = null;
        while (iter.hasNext()) {
            tmpEntry = iter.next();
            sortedMap.put(tmpEntry.getKey(), tmpEntry.getValue());
        }
        return sortedMap;
    }

    private String nametono(String name){
        String no = "";
        SharedPreferences formation = getActivity().getSharedPreferences("FormationRecord", MODE_PRIVATE);
        Map<String, Object> mapParam = (Map<String, Object>) formation.getAll();
        for (Map.Entry<String, Object> item_map : mapParam.entrySet()) {
            final String key = item_map.getKey(); // 获取该配对的键信息
            if(name.equals(item_map.getValue())){
                no=key;
            }
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

    private UDPListener udpListener = new UDPListener() {
        @Override
        public void onError(String error) {
            Toast.makeText(mContext, "错误：" + error, Toast.LENGTH_SHORT).show();
            Log.v(TAG, error);
        }

        @Override
        public void onReceive(String content) {//UI线程
        }

        @Override
        public void onSend(String content) {
            //Toast.makeText(mContext, "发送成功：" + content, Toast.LENGTH_SHORT).show();
            Log.v(TAG, content);
        }
    };

    private void showToast(String desc) {
        Toast.makeText(mContext, desc, Toast.LENGTH_SHORT).show();
    }

}
