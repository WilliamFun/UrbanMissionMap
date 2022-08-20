package buaa.uavswarm.urbanmissionmap.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.Map;

import buaa.uavswarm.urbanmissionmap.R;
import buaa.uavswarm.urbanmissionmap.fragment.FragmentFormationDetail;
import buaa.uavswarm.urbanmissionmap.fragment.FragmentFormationSetting;
import buaa.uavswarm.urbanmissionmap.fragment.FragmentUAVDetail;
import buaa.uavswarm.urbanmissionmap.fragment.FragmentUAVSetting;

public class SettingActivity extends FragmentActivity implements View.OnClickListener, FragmentUAVSetting.planeSelectInterface, FragmentFormationSetting.formationSelectInterface {

    public Fragment[] mFragments;
    private RadioGroup leftRg;
    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;
    private RadioButton rbUAV, rbFormation, rbSystem;
    private ImageButton home;
    FragmentUAVSetting FragmentUAVSetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        mFragments = new Fragment[7];
        fragmentManager = getSupportFragmentManager();
        mFragments[0] = fragmentManager.findFragmentById(R.id.fragement_uavsetting);
        mFragments[1] = fragmentManager.findFragmentById(R.id.fragement_formationsetting);
        mFragments[2] = fragmentManager.findFragmentById(R.id.fragement_systemsetting);
        mFragments[3] = fragmentManager.findFragmentById(R.id.fragement_adduav);
        mFragments[4] = fragmentManager.findFragmentById(R.id.fragement_uavdetail);
        mFragments[5] = fragmentManager.findFragmentById(R.id.fragement_addformation);
        mFragments[6] = fragmentManager.findFragmentById(R.id.fragement_formationdetail);
        leftRg = (RadioGroup) findViewById(R.id.leftRg);
        rbUAV = (RadioButton) findViewById(R.id.rbUAV);
        rbFormation = (RadioButton) findViewById(R.id.rbFormation);
        rbSystem = (RadioButton) findViewById(R.id.rbSystem);
        int fragmentFlag = getIntent().getIntExtra("fragment_flag", 0);
        fragmentTransaction = fragmentManager.beginTransaction().hide(mFragments[0]).hide(mFragments[1]).hide(mFragments[2]).hide(mFragments[3]).hide(mFragments[4]).hide(mFragments[5]).hide(mFragments[6]);
        if(fragmentFlag==3){
            fragmentTransaction.show(mFragments[1]).commit();
            rbFormation.setChecked(true);
        }else{
            fragmentTransaction.show(mFragments[0]).commit();
            rbUAV.setChecked(true);
        }
        setFragmentIndicator();
        home=findViewById(R.id.btn_home);
        home.setOnClickListener(this);
        //隐藏虚拟按键
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
    }

    private void setFragmentIndicator() {
        leftRg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                fragmentTransaction = fragmentManager.beginTransaction()
                        .hide(mFragments[0]).hide(mFragments[1])
                        .hide(mFragments[2]).hide(mFragments[3]).hide(mFragments[4]).hide(mFragments[5]).hide(mFragments[6]);
                switch (checkedId) {
                    case R.id.rbUAV:
                        fragmentTransaction.show(mFragments[0]).commit();
                        break;

                    case R.id.rbFormation:
                        fragmentTransaction.show(mFragments[1]).commit();
                        break;

                    case R.id.rbSystem:
                        fragmentTransaction.show(mFragments[2]).commit();
                        break;

                    default:
                        break;
                }
            }
        });
    }


    @Override
    public void onClick(View v) {
        if(v==home){
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onPlaneSelect(String title) {
        FragmentManager manager = getSupportFragmentManager();
        FragmentUAVDetail fragment2 = (FragmentUAVDetail)manager.findFragmentById(R.id.fragement_uavdetail);
        fragment2.planeno=title;
        SharedPreferences plane = getSharedPreferences("Plane_"+title, MODE_PRIVATE);
        SharedPreferences FormationShared = getSharedPreferences("FormationRecord",MODE_PRIVATE);
        Map<String, String> mapParam = (Map<String, String>) FormationShared.getAll();
        String[] fnameArray=new String[mapParam.size()+1];
        int j=mapParam.size();
        if(mapParam.size()==0){
            fnameArray = new String[]{"不在编队"};
        }else{
            int i=0;
            for (Map.Entry<String, String> item_map : mapParam.entrySet()) {
                final String value = item_map.getValue(); // 获取该配对的键信息
                if(plane.getString("At_Formation", "").equals(value)){
                    j=i;
                }
                fnameArray[i] = value;
                i++;
            }
            fnameArray[i] = "不在编队";
        }
        if(plane.getString("Plane_type", "").equals("固定翼")){
            fragment2.sp_planetype.setSelection(0);
        }else if(plane.getString("Plane_type", "").equals("垂直起降")){
            fragment2.sp_planetype.setSelection(1);
        }else{
            fragment2.sp_planetype.setSelection(2);
        }
        fragment2.et_pid.setText(title);
        fragment2.et_netaddress.setText(plane.getString("Net_address",""));
        fragment2.et_videoaddress.setText(plane.getString("Video_address",""));
        fragment2.sp_atformation.setSelection(j);
        fragment2.et_location.setText(Integer.toString(plane.getInt("Location_inteam",0)));
    }

    @Override
    public void onFormationSelect(String title) {
        FragmentManager manager = getSupportFragmentManager();
        FragmentFormationDetail fragment2 = (FragmentFormationDetail)manager.findFragmentById(R.id.fragement_formationdetail);
        SharedPreferences formation = getSharedPreferences("Formation_"+title, MODE_PRIVATE);
        fragment2.formationno=title;
        fragment2.et_fid.setText(title);
        fragment2.et_name.setText(formation.getString("Formation_Name",""));
        fragment2.et_formationsize.setText(formation.getString("Formation_size",""));
        fragment2.et_escapedelay.setText(formation.getString("escape_delay",""));
        fragment2.et_launchdelay.setText(formation.getString("launch_delay",""));
        fragment2.et_x.setText(formation.getString("distance_x",""));
        fragment2.et_y.setText(formation.getString("distance_y",""));
        fragment2.et_z.setText(formation.getString("distance_z",""));
        SharedPreferences PlaneShared = getSharedPreferences("PlaneRecord", MODE_PRIVATE);
        Map<String, String> mapParam = (Map<String, String>) PlaneShared.getAll();
        String[] planeArray=new String[mapParam.size()+1];
        int j=0,t=0;
        if(mapParam.size()==0){
            planeArray = new String[]{"没有可用飞机"};
        }else{
            int i=0;
            for (Map.Entry<String, String> item_map : mapParam.entrySet()) {
                final String key = item_map.getKey(); // 获取该配对的键信息
                if(formation.getString("Leader_no","")==key){
                    j=i;
                }
                if(formation.getString("Leader2_no","")==key){
                    t=i;
                }
                planeArray[i] = key;
                i++;
            }
            if(formation.getString("Leader_no","")=="暂不选择"){
                j=i;
            }
            if(formation.getString("Leader2_no","")=="暂不选择"){
                t=i;
            }
            planeArray[i] = "暂不选择";
        }
        fragment2.sp_leader.setSelection(j);
        fragment2.sp_leader2.setSelection(t);
        if(formation.getString("Formation_type", "").equals("三角形")){
            fragment2.sp_formationtype.setSelection(0);
        }else if(formation.getString("Formation_type", "").equals("矩形")){
            fragment2.sp_formationtype.setSelection(1);
        }
    }
}
