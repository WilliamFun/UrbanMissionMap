package buaa.uavswarm.urbanmissionmap.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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

import java.io.File;
import java.util.Map;

import buaa.uavswarm.urbanmissionmap.R;
import buaa.uavswarm.urbanmissionmap.activities.SettingActivity;

import static android.content.Context.MODE_PRIVATE;

public class FragmentFormationDetail extends Fragment implements View.OnClickListener {
    protected View mView;
    protected Context mContext;
    private SharedPreferences FormationShared;
    private SharedPreferences PlaneShared;
    public String formationno;
    public EditText et_fid;
    public EditText et_name;
    public EditText et_formationsize;
    public EditText et_x;
    public EditText et_y;
    public EditText et_z;
    public EditText et_launchdelay;
    public EditText et_escapedelay;
    public Spinner sp_formationtype;
    public Spinner sp_leader;
    public Spinner sp_leader2;
    public int bid_id1;
    public int bid_id2;
    public int bid_id3;
    private ImageButton back;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mContext=getActivity();
        mView=inflater.inflate(R.layout.fragment_formationdetail, container, false);
        et_fid=mView.findViewById(R.id.et_fid);
        et_name=mView.findViewById(R.id.et_name);
        et_formationsize= mView.findViewById(R.id.et_formationsize);
        et_x=mView.findViewById(R.id.et_x);
        et_y=mView.findViewById(R.id.et_y);
        et_z=mView.findViewById(R.id.et_z);
        et_launchdelay=mView.findViewById(R.id.et_launchdelay);
        et_escapedelay=mView.findViewById(R.id.et_escapedelay);
        back = mView.findViewById(R.id.btn_back);
        back.setOnClickListener(this);
        FormationShared = getActivity().getSharedPreferences("FormationRecord",MODE_PRIVATE);
        PlaneShared = getActivity().getSharedPreferences("PlaneRecord",MODE_PRIVATE);
        initTypeSpinner();// 初始化下拉
        return mView;
    }

    private String[] formationtypeArray = {"三角形", "矩形"};
    private void initTypeSpinner(){
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<String>(mContext,
                R.layout.item_select, formationtypeArray);
        typeAdapter.setDropDownViewResource(R.layout.item_dropdown);
        sp_formationtype = mView.findViewById(R.id.sp_formationtype);
        sp_formationtype.setPrompt("请选择编队类型");
        sp_formationtype.setAdapter(typeAdapter);
        sp_formationtype.setSelection(0);
        sp_formationtype.setOnItemSelectedListener(new TypeSelectedListener());

        Map<String, String> mapParam = (Map<String, String>) PlaneShared.getAll();
        String[] planeArray=new String[mapParam.size()+1];
        if(mapParam.size()==0){
            planeArray = new String[]{"没有可用飞机"};
        }else{
            int i=0;
            for (Map.Entry<String, String> item_map : mapParam.entrySet()) {
                final String key = item_map.getKey(); // 获取该配对的键信息
                planeArray[i] = key;
                i++;
            }
            planeArray[i] = "暂不选择";
        }
        ArrayAdapter<String> nameAdapter = new ArrayAdapter<String>(mContext,
                R.layout.item_select, planeArray);
        nameAdapter.setDropDownViewResource(R.layout.item_dropdown);
        sp_leader = mView.findViewById(R.id.sp_leader);
        sp_leader.setAdapter(nameAdapter);
        sp_leader.setSelection(0);
        sp_leader.setOnItemSelectedListener(new FormationSelectedListener());

        ArrayAdapter<String> locAdapter = new ArrayAdapter<String>(mContext,
                R.layout.item_select, planeArray);
        locAdapter.setDropDownViewResource(R.layout.item_dropdown);
        sp_leader2 = mView.findViewById(R.id.sp_leader2);
        sp_leader2.setAdapter(locAdapter);
        //sp_Location.setSelection(plane.getInt("Location_inteam",0));
        sp_leader2.setOnItemSelectedListener(new LocSelectedListener());
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

    class LocSelectedListener implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            bid_id3=arg2;
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
        if(v.getId()==R.id.btn_back){
            String fid =et_fid.getText().toString();
            String name = et_name.getText().toString();
            String formationsize = et_formationsize.getText().toString();
            String x =et_x.getText().toString();
            String y = et_y.getText().toString();
            String z = et_z.getText().toString();
            String launchdelay =et_launchdelay.getText().toString();
            String escapedelay = et_escapedelay.getText().toString();
            String blid1 = formationtypeArray[bid_id1];
            Map<String, String> mapParam = (Map<String, String>) PlaneShared.getAll();
            String[] planeArray=new String[mapParam.size()+1];
            if(mapParam.size()==0){
                planeArray = new String[]{"没有可用飞机"};
            }else{
                int i=0;
                for (Map.Entry<String, String> item_map : mapParam.entrySet()) {
                    final String key = item_map.getKey(); // 获取该配对的键信息
                    planeArray[i] = key;
                    i++;
                }
                planeArray[i] = "暂不选择";
            }
            String blid2 = planeArray[bid_id2];
            String blid3 = planeArray[bid_id3];
            //删除旧的飞机
            File file= new File("/data/data/"+getActivity().getPackageName().toString()+"/shared_prefs","Formation_"+formationno+".xml");
            if(file.exists())
            {
                file.delete();
            }
            //更新
            SharedPreferences formation;
            formation = getActivity().getSharedPreferences("Formation_"+fid, MODE_PRIVATE);
            SharedPreferences.Editor editor = formation.edit(); // 获得编辑器的对象
            editor.putString("Formation_No", fid);
            editor.putString("Formation_Name",name);
            editor.putString("Formation_type",blid1);
            editor.putString("Formation_size",formationsize);
            editor.putString("distance_x",x);
            editor.putString("distance_y",y);
            editor.putString("distance_z",z);
            editor.putString("launch_delay",launchdelay);
            editor.putString("escape_delay",escapedelay);
            editor.putString("Leader_no",blid2);
            editor.putString("Leader2_no",blid3);
            editor.apply(); // 提交编辑器中的修改
            SharedPreferences.Editor editor2 = FormationShared.edit();
            editor2.remove(formationno);
            editor2.putString(fid,name);
            editor2.apply();
            if(!blid2.equals("暂不选择")&&!blid2.equals("没有可用飞机")){
                SharedPreferences plane;
                SharedPreferences team1;
                plane = getActivity().getSharedPreferences("Plane_"+blid2, MODE_PRIVATE);
                team1=getActivity().getSharedPreferences("TeamRecord_"+plane.getString("At_Formation",""),MODE_PRIVATE);
                if((!plane.getString("At_Formation", "").equals("不在编队")) && (!plane.getString("At_Formation", "").equals(name))){
                    SharedPreferences formation1;
                    formation1 = getActivity().getSharedPreferences("Formation_"+nametono(plane.getString("At_Formation","")),MODE_PRIVATE);
                    SharedPreferences.Editor editor9 = formation1.edit();
                    if(blid2.equals(formation1.getString("Leader_no", ""))){
                        editor9.putString("Leader_no","暂不选择");
                    }
                    if(blid2.equals(formation1.getString("Leader2_no", ""))){
                        editor9.putString("Leader2_no","暂不选择");
                    }
                    editor9.apply();
                }
                SharedPreferences.Editor editor7 = team1.edit();
                editor7.remove(blid2);
                editor7.apply();
                SharedPreferences.Editor editor4 = plane.edit();
                editor4.putString("At_Formation",name);
                editor4.putInt("Location_inteam",1);
                editor4.apply();
            }
            if(!blid3.equals("暂不选择")&&!blid3.equals("没有可用飞机")){
                SharedPreferences plane2;
                plane2 = getActivity().getSharedPreferences("Plane_"+blid3, MODE_PRIVATE);
                SharedPreferences team2;
                team2=getActivity().getSharedPreferences("TeamRecord_"+plane2.getString("At_Formation",""),MODE_PRIVATE);
                if((!plane2.getString("At_Formation", "").equals("不在编队")) && (!plane2.getString("At_Formation", "").equals(name))){
                    SharedPreferences formation2;
                    formation2 = getActivity().getSharedPreferences("Formation_"+nametono(plane2.getString("At_Formation","")),MODE_PRIVATE);
                    SharedPreferences.Editor editor10 = formation2.edit();
                    if(blid3.equals(formation2.getString("Leader_no", ""))){
                        editor10.putString("Leader_no","暂不选择");
                    }
                    if(blid3.equals(formation2.getString("Leader2_no", ""))){
                        editor10.putString("Leader2_no","暂不选择");
                    }
                    editor10.apply();
                }
                SharedPreferences.Editor editor8 = team2.edit();
                editor8.remove(blid3);
                editor8.apply();
                SharedPreferences.Editor editor5 = plane2.edit();
                editor5.putString("At_Formation",name);
                editor5.putInt("Location_inteam",2);
                editor5.apply();
            }
            SharedPreferences planerecord;
            planerecord = getActivity().getSharedPreferences("PlaneRecord", MODE_PRIVATE);
            SharedPreferences.Editor editor6 = planerecord.edit();
            if(!blid2.equals("暂不选择")&&!blid2.equals("没有可用飞机")){
                editor6.putString(blid2,name);
            }
            if(!blid3.equals("暂不选择")&&!blid3.equals("没有可用飞机")){
                editor6.putString(blid3,name);
            }
            editor6.apply();
            SharedPreferences team;
            team = getActivity().getSharedPreferences("TeamRecord_"+name, MODE_PRIVATE);
            SharedPreferences.Editor editor3 = team.edit();
            if(!blid2.equals("暂不选择")&&!blid2.equals("没有可用飞机")){
                editor3.putString(blid2,"1");
            }
            if(!blid3.equals("暂不选择")&&!blid3.equals("没有可用飞机")){
                editor3.putString(blid3,"2");
            }
            editor3.apply();
            Intent intent = new Intent(mContext, SettingActivity.class);
            intent.putExtra("fragment_flag", 3);
            startActivity(intent);
        }
    }

    private void showToast(String desc) {
        Toast.makeText(mContext, desc, Toast.LENGTH_SHORT).show();
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

}
