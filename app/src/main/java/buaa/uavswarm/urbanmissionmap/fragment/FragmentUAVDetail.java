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

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.util.Map;

import buaa.uavswarm.urbanmissionmap.R;
import buaa.uavswarm.urbanmissionmap.activities.SettingActivity;

import static android.content.Context.MODE_PRIVATE;

public class FragmentUAVDetail extends Fragment implements View.OnClickListener {
    protected View mView;
    protected Context mContext;
    private SharedPreferences RecordShared;
    private SharedPreferences FormationShared;
    public String planeno;
    public EditText et_pid;
    public EditText et_netaddress;
    public EditText et_videoaddress;
    public EditText et_location;
    public Spinner sp_atformation;
    public Spinner sp_planetype;
    public int bid_id1;
    public int bid_id2;
    private ImageButton back;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContext=getActivity();
        mView=inflater.inflate(R.layout.fragment_uavdetail, container, false);
        et_pid=mView.findViewById(R.id.et_pid);
        et_netaddress=mView.findViewById(R.id.et_netaddress);
        et_videoaddress=mView.findViewById(R.id.et_videoaddress);
        et_location=mView.findViewById(R.id.et_location);
        back = mView.findViewById(R.id.btn_back);
        back.setOnClickListener(this);
        RecordShared = getActivity().getSharedPreferences("PlaneRecord",MODE_PRIVATE);
        FormationShared = getActivity().getSharedPreferences("FormationRecord",MODE_PRIVATE);
        initTypeSpinner();// ???????????????
        return mView;
    }

    private String[] planetypeArray = {"?????????", "????????????","?????????"};
    private void initTypeSpinner(){
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<String>(mContext,
                R.layout.item_select, planetypeArray);
        typeAdapter.setDropDownViewResource(R.layout.item_dropdown);
        sp_planetype = mView.findViewById(R.id.sp_planetype);
        sp_planetype.setPrompt("????????????????????????");
        sp_planetype.setAdapter(typeAdapter);
        sp_planetype.setSelection(0);
        sp_planetype.setOnItemSelectedListener(new TypeSelectedListener());

        Map<String, String> mapParam = (Map<String, String>) FormationShared.getAll();
        String[] fnameArray=new String[mapParam.size()+1];
        if(mapParam.size()==0){
            fnameArray = new String[]{"????????????"};
        }else{
            int i=0;
            for (Map.Entry<String, String> item_map : mapParam.entrySet()) {
                final String value = item_map.getValue(); // ???????????????????????????
                fnameArray[i] = value;
                i++;
            }
            fnameArray[i] = "????????????";
        }
        ArrayAdapter<String> nameAdapter = new ArrayAdapter<String>(mContext,
                R.layout.item_select, fnameArray);
        nameAdapter.setDropDownViewResource(R.layout.item_dropdown);
        sp_atformation = mView.findViewById(R.id.sp_atformation);
        sp_atformation.setAdapter(nameAdapter);
        //sp_atformation.setSelection(0);
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
        if(v.getId()==R.id.btn_back){
            String pid =et_pid.getText().toString();
            String netaddress = et_netaddress.getText().toString();
            String videoaddress = et_videoaddress.getText().toString();
            String location = et_location.getText().toString();
            String blid1 = planetypeArray[bid_id1];

            Map<String, String> mapParam = (Map<String, String>) FormationShared.getAll();
            String[] fnameArray=new String[mapParam.size()+1];
            if(mapParam.size()==0){
                fnameArray = new String[]{"????????????"};
            }else{
                int i=0;
                for (Map.Entry<String, String> item_map : mapParam.entrySet()) {
                    final String value = item_map.getValue(); // ???????????????????????????
                    fnameArray[i] = value;
                    i++;
                }
                fnameArray[i] = "????????????";
            }
            String blid2 = fnameArray[bid_id2];
            if (TextUtils.isEmpty(pid)) {
                showToast("??????????????????");
                return;
            } else if (TextUtils.isEmpty(netaddress)) {
                showToast("????????????????????????");
                return;
            } else if (TextUtils.isEmpty(videoaddress)) {
                showToast("??????????????????????????????");
                return;
            }else if (TextUtils.isEmpty(location)) {
                showToast("????????????????????????");
                return;
            }
            //??????????????????
            SharedPreferences.Editor editor4 = RecordShared.edit();
            editor4.remove(planeno);
            editor4.apply();
            SharedPreferences plane = getActivity().getSharedPreferences("Plane_"+planeno, MODE_PRIVATE);
            SharedPreferences team;
            if((!plane.getString("At_Formation", "").equals("????????????"))&&(!plane.getString("At_Formation", "").equals(blid2))){
                SharedPreferences formation;
                formation = getActivity().getSharedPreferences("Formation_" + nametono(plane.getString("At_Formation", "")), MODE_PRIVATE);
                SharedPreferences.Editor editor6 = formation.edit();
                if(formation.getString("Leader_no", "").equals(planeno)){
                    editor6.putString("Leader_no","????????????");
                }
                if(formation.getString("Leader2_no", "").equals(planeno)){
                    editor6.putString("Leader2_no","????????????");
                }
                editor6.apply();
            }
            team = getActivity().getSharedPreferences("TeamRecord_" + plane.getString("At_Formation", ""), MODE_PRIVATE);
            SharedPreferences.Editor editor5 = team.edit(); // ????????????????????????
            editor5.remove(planeno);
            editor5.apply();
            File file= new File("/data/data/"+getActivity().getPackageName().toString()+"/shared_prefs","Plane_"+planeno+".xml");
            if(file.exists())
            {
                file.delete();
            }
            //??????????????????
            SharedPreferences plane2;
            plane2 = getActivity().getSharedPreferences("Plane_"+pid, MODE_PRIVATE);
            SharedPreferences.Editor editor = plane2.edit(); // ????????????????????????
            editor.putInt("Plane_No", Integer.parseInt(pid));
            editor.putString("Net_address",netaddress);
            editor.putString("Video_address",videoaddress);
            editor.putString("At_Formation",blid2);
            editor.putString("Plane_type",blid1);
            editor.putInt("Location_inteam", Integer.parseInt(location));
            editor.apply(); // ???????????????????????????
            SharedPreferences.Editor editor2 = RecordShared.edit();
            editor2.putString(pid,blid2);
            editor2.apply();
            SharedPreferences team2;
            team2 = getActivity().getSharedPreferences("TeamRecord_" + blid2, MODE_PRIVATE);
            SharedPreferences.Editor editor3 = team2.edit(); // ????????????????????????
            editor3.putString(pid, location);
            editor3.apply();
            Intent intent = new Intent(mContext, SettingActivity.class);
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
            final String key = item_map.getKey(); // ???????????????????????????
            if(name.equals(item_map.getValue())){
                no=key;
            }
        }
        return no;
    }
}
