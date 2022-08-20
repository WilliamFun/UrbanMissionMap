package buaa.uavswarm.urbanmissionmap.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.Map;
import java.util.Objects;

import buaa.uavswarm.urbanmissionmap.R;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private int swarmname;
    private SharedPreferences FormationShared;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar tl_main=findViewById(R.id.wellcome_toolbar);
        tl_main.setBackgroundResource(R.color.blackblue);
        setSupportActionBar(tl_main);
        FormationShared = getSharedPreferences("FormationRecord", MODE_PRIVATE);
        initTypeSpinner();// 初始化下拉
        findViewById(R.id.btn_start).setOnClickListener(this);
        findViewById(R.id.btn_setting).setOnClickListener(this);
        //隐藏虚拟按键
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
    }

    private void initTypeSpinner() {
        Map<String, String> mapParam = (Map<String, String>) FormationShared.getAll();
        String[] fnameArray=new String[mapParam.size()];
        int i=0;
        for (Map.Entry<String, String> item_map : mapParam.entrySet()) {
            final String value = item_map.getValue(); // 获取该配对的键信息
            fnameArray[i] = value;
            i++;
        }
        if(mapParam.size()==0){
            fnameArray = new String[]{"无编队"};
        }
        ArrayAdapter<String> nameAdapter = new ArrayAdapter<String>(this,
                R.layout.item_select, fnameArray);
        nameAdapter.setDropDownViewResource(R.layout.item_dropdown);
        Spinner sp_lid = findViewById(R.id.sp_baseformation);
        sp_lid.setAdapter(nameAdapter);
        sp_lid.setSelection(0);
        sp_lid.setOnItemSelectedListener(new MySelectedListener());
    }

    class MySelectedListener implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            swarmname=arg2;
        }

        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_setting) { // 点击了添加按钮
            // 跳转到添加页面
            Intent intent = new Intent(this, SettingActivity.class);
            startActivity(intent);
        } else if (v.getId() == R.id.btn_start) { // 点击了添加按钮
            // 跳转到添加页面
            Intent intent = new Intent(this, FlightActivity.class);
            startActivity(intent);
        }
    }
    
}