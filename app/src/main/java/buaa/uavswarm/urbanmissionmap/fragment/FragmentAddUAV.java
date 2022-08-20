package buaa.uavswarm.urbanmissionmap.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.Map;

import buaa.uavswarm.urbanmissionmap.R;
import buaa.uavswarm.urbanmissionmap.activities.SettingActivity;
import buaa.uavswarm.urbanmissionmap.dialog.ConfirmUAVDialog;

import static android.content.Context.MODE_PRIVATE;

public class FragmentAddUAV extends Fragment implements View.OnClickListener {
    protected View mView;
    protected Context mContext;
    private SharedPreferences RecordShared;
    private SharedPreferences FormationShared;
    private EditText et_pid;
    private EditText et_netaddress;
    private EditText et_videoaddress;
    private EditText et_location;
    private int bid_id1;
    private int bid_id2;
    private ImageButton confirm;
    private ImageButton back;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mContext=getActivity();
        mView=inflater.inflate(R.layout.fragment_adduav, container, false);
        et_pid=mView.findViewById(R.id.et_pid);
        et_netaddress=mView.findViewById(R.id.et_netaddress);
        et_videoaddress=mView.findViewById(R.id.et_videoaddress);
        et_location=mView.findViewById(R.id.et_location);
        confirm = mView.findViewById(R.id.btn_confirm);
        confirm.setOnClickListener(this);
        back = mView.findViewById(R.id.btn_back);
        back.setOnClickListener(this);
        RecordShared = getActivity().getSharedPreferences("PlaneRecord",MODE_PRIVATE);
        FormationShared = getActivity().getSharedPreferences("FormationRecord",MODE_PRIVATE);
        initTypeSpinner();// 初始化下拉
        return mView;
    }

    private String[] planetypeArray = {"固定翼", "垂直起降","多旋翼"};
    private void initTypeSpinner(){
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<String>(mContext,
                R.layout.item_select, planetypeArray);
        typeAdapter.setDropDownViewResource(R.layout.item_dropdown);
        Spinner sp_planetype = mView.findViewById(R.id.sp_planetype);
        sp_planetype.setPrompt("请选择飞行器类型");
        sp_planetype.setAdapter(typeAdapter);
        sp_planetype.setSelection(0);
        sp_planetype.setOnItemSelectedListener(new TypeSelectedListener());

        Map<String, String> mapParam = (Map<String, String>) FormationShared.getAll();
        String[] fnameArray=new String[mapParam.size()+1];
        if(mapParam.size()==0){
            fnameArray = new String[]{"不在编队"};
        }else{
        int i=0;
        for (Map.Entry<String, String> item_map : mapParam.entrySet()) {
            final String value = item_map.getValue(); // 获取该配对的键信息
            fnameArray[i] = value;
            i++;
        }
            fnameArray[i] = "不在编队";
        }
        ArrayAdapter<String> nameAdapter = new ArrayAdapter<String>(mContext,
                R.layout.item_select, fnameArray);
        nameAdapter.setDropDownViewResource(R.layout.item_dropdown);
        Spinner sp_atformation = mView.findViewById(R.id.sp_atformation);
        sp_atformation.setAdapter(nameAdapter);
        sp_atformation.setSelection(0);
        sp_atformation.setOnItemSelectedListener(new FormationSelectedListener());
    }
    class TypeSelectedListener implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            bid_id1 = arg2;
        }

        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }

    class FormationSelectedListener implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            bid_id2=arg2;
        }

        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.btn_confirm){
            String pid =et_pid.getText().toString();
            String netaddress = et_netaddress.getText().toString();
            String videoaddress = et_videoaddress.getText().toString();
            String location = et_location.getText().toString();
            String blid1 = planetypeArray[bid_id1];

            Map<String, String> mapParam = (Map<String, String>) FormationShared.getAll();
            String[] fnameArray=new String[mapParam.size()+1];
            if(mapParam.size()==0){
                fnameArray = new String[]{"不在编队"};
            }else{
                int i=0;
                for (Map.Entry<String, String> item_map : mapParam.entrySet()) {
                    final String value = item_map.getValue(); // 获取该配对的键信息
                    fnameArray[i] = value;
                    i++;
                }
                fnameArray[i] = "不在编队";
            }
            String blid2 = fnameArray[bid_id2];
            if (TextUtils.isEmpty(pid)) {
                showToast("请先填写编号");
                return;
            } else if (TextUtils.isEmpty(netaddress)) {
                showToast("请先填写网络地址");
                return;
            } else if (TextUtils.isEmpty(videoaddress)) {
                showToast("请先填写载荷视频地址");
                return;
            }else if (TextUtils.isEmpty(location)) {
                showToast("请先填写队内位置");
                return;
            }
            showMakeSureDialog(pid,netaddress,videoaddress,blid1,blid2,location);
        }
        if(v.getId()==R.id.btn_back){
            SettingActivity activity = (SettingActivity) getActivity();
            FragmentManager fm = activity.getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.hide(activity.mFragments[0]).hide(activity.mFragments[1]).hide(activity.mFragments[2]).hide(activity.mFragments[3])
                    .hide(activity.mFragments[4]).hide(activity.mFragments[5]).hide(activity.mFragments[6]);
            ft.show(activity.mFragments[0]).commit();
        }
    }

    private void showToast(String desc) {
        Toast.makeText(mContext, desc, Toast.LENGTH_SHORT).show();
    }

    private void showMakeSureDialog(final String pid, final String netaddress, final String videoaddress, final String blid1, final String blid2, final String blid3) {
        final ConfirmUAVDialog dialog = new ConfirmUAVDialog();
        dialog.setContent(pid);
        dialog.setDialogClickListener(new  ConfirmUAVDialog.onDialogClickListener() {
            @Override
            public void onSureClick() {
                SharedPreferences plane;
                plane = getActivity().getSharedPreferences("Plane_"+pid, MODE_PRIVATE);
                SharedPreferences.Editor editor = plane.edit(); // 获得编辑器的对象
                editor.putInt("Plane_No", Integer.parseInt(pid));
                editor.putString("Net_address",netaddress);
                editor.putString("Video_address",videoaddress);
                editor.putString("At_Formation",blid2);
                editor.putString("Plane_type",blid1);
                editor.putInt("Location_inteam", Integer.parseInt(blid3));
                editor.apply(); // 提交编辑器中的修改
                SharedPreferences.Editor editor2 = RecordShared.edit();
                editor2.putString(pid,blid2);
                editor2.apply();
                SharedPreferences team;
                team = getActivity().getSharedPreferences("TeamRecord_" + blid2, MODE_PRIVATE);
                SharedPreferences.Editor editor3 = team.edit(); // 获得编辑器的对象
                if(blid2=="不在编队"){
                    editor3.putString(pid,pid);
                }
                editor3.putString(pid, blid3);
                editor3.apply();
                Intent intent = new Intent(mContext, SettingActivity.class);
                startActivity(intent);
            }

            @Override
            public void onCancelClick() {
            //这里是取消操作
                SettingActivity activity = (SettingActivity) getActivity();
                FragmentManager fm = activity.getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.hide(activity.mFragments[0]).hide(activity.mFragments[1]).hide(activity.mFragments[2]).hide(activity.mFragments[3])
                        .hide(activity.mFragments[4]).hide(activity.mFragments[5]).hide(activity.mFragments[6]);
                ft.show(activity.mFragments[3]).commit();
            }
        });
        dialog.show(getFragmentManager(),"");
    }


}
