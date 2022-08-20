package buaa.uavswarm.urbanmissionmap.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import buaa.uavswarm.urbanmissionmap.R;

import static android.content.Context.MODE_PRIVATE;

public class FragmentSystemSetting extends Fragment implements View.OnClickListener {
    protected View mView;
    protected Context mContext;
    private SharedPreferences RecordShared;
    private EditText et_x;
    private EditText et_y;
    private EditText et_z;
    private EditText et_launchdelay;
    private EditText et_escapedelay;
    private Button save;
    private int bid_id;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mContext=getActivity();
        mView=inflater.inflate(R.layout.fragment_systemsetting, container, false);
        et_x=mView.findViewById(R.id.et_x);
        et_y=mView.findViewById(R.id.et_y);
        et_z=mView.findViewById(R.id.et_z);
        et_launchdelay=mView.findViewById(R.id.et_launchdelay);
        et_escapedelay=mView.findViewById(R.id.et_escapedelay);
        save=mView.findViewById(R.id.btn_save);
        save.setOnClickListener(this);
        RecordShared = getActivity().getSharedPreferences("Systemparameter",MODE_PRIVATE);
        et_x.setText(Integer.toString(RecordShared.getInt("x_separation",0)));
        et_y.setText(Integer.toString(RecordShared.getInt("y_separation",0)));
        et_z.setText(Integer.toString(RecordShared.getInt("z_separation",0)));
        et_launchdelay.setText(Integer.toString(RecordShared.getInt("launch_Interval",0)));
        et_escapedelay.setText(Integer.toString(RecordShared.getInt("escape_Interval",0)));
        initTypeSpinner();// 初始化下拉
        return mView;
    }

    private String[] coordinatetypeArray = {"WGS84", "火星坐标","BD坐标"};
    private void initTypeSpinner() {
        int i=0;
        if(RecordShared.getString("system_coordinate", "").equals("WGS84")){
            i=0;
        }else if(RecordShared.getString("system_coordinate", "").equals("火星坐标")){
            i=1;
        }else{
            i=2;
        }
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<String>(mContext,
                R.layout.item_select, coordinatetypeArray);
        typeAdapter.setDropDownViewResource(R.layout.item_dropdown);
        Spinner sp_coordinate = mView.findViewById(R.id.sp_coordinate);
        sp_coordinate.setPrompt("请选择地图坐标系");
        sp_coordinate.setAdapter(typeAdapter);
        sp_coordinate.setSelection(i);
        sp_coordinate.setOnItemSelectedListener(new TypeSelectedListener());
    }

    class TypeSelectedListener implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            bid_id = arg2;
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
        if(v.getId()==R.id.btn_save){
            String x=et_x.getText().toString();
            String y=et_y.getText().toString();
            String z=et_z.getText().toString();
            String launch=et_launchdelay.getText().toString();
            String escape=et_escapedelay.getText().toString();
            String blid=coordinatetypeArray[bid_id];

            SharedPreferences.Editor editor=RecordShared.edit();
            editor.putInt("x_separation",Integer.parseInt(x));
            editor.putInt("y_separation",Integer.parseInt(y));
            editor.putInt("z_separation",Integer.parseInt(z));
            editor.putInt("launch_Interval",Integer.parseInt(launch));
            editor.putInt("escape_Interval",Integer.parseInt(escape));
            editor.putString("system_coordinate",blid);
            editor.apply();
            showToast("数据已保存");
        }

    }
    private void showToast(String desc) {
        Toast.makeText(mContext, desc, Toast.LENGTH_SHORT).show();
    }
}
