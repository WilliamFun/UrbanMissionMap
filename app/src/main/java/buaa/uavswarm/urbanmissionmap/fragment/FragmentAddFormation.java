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
import buaa.uavswarm.urbanmissionmap.dialog.ConfirmFormationDialog;

import static android.content.Context.MODE_PRIVATE;

public class FragmentAddFormation extends Fragment implements View.OnClickListener {
    protected View mView;
    protected Context mContext;
    private SharedPreferences RecordShared;
    private SharedPreferences PlaneShared;
    private EditText et_fid;
    private EditText et_name;
    private EditText et_formationsize;
    private EditText et_x;
    private EditText et_y;
    private EditText et_z;
    private EditText et_launchdelay;
    private EditText et_escapedelay;
    private int bid_id;
    private int bid_id2;
    private int bid_id3;
    private ImageButton confirm;
    private ImageButton back;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mContext = getActivity();
        mView = inflater.inflate(R.layout.fragment_addformation, container, false);
        et_fid = mView.findViewById(R.id.et_fid);
        et_name = mView.findViewById(R.id.et_name);
        et_formationsize = mView.findViewById(R.id.et_formationsize);
        et_x = mView.findViewById(R.id.et_x);
        et_y = mView.findViewById(R.id.et_y);
        et_z = mView.findViewById(R.id.et_z);
        et_launchdelay = mView.findViewById(R.id.et_launchdelay);
        et_escapedelay = mView.findViewById(R.id.et_escapedelay);
        confirm = mView.findViewById(R.id.btn_confirm);
        confirm.setOnClickListener(this);
        back = mView.findViewById(R.id.btn_back);
        back.setOnClickListener(this);
        RecordShared = getActivity().getSharedPreferences("FormationRecord", MODE_PRIVATE);
        PlaneShared = getActivity().getSharedPreferences("PlaneRecord", MODE_PRIVATE);
        initTypeSpinner();// 初始化下拉
        return mView;
    }

    private String[] formationtypeArray = {"三角形", "矩形"};

    private void initTypeSpinner() {
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<String>(mContext,
                R.layout.item_select, formationtypeArray);
        typeAdapter.setDropDownViewResource(R.layout.item_dropdown);
        Spinner sp_formationtype = mView.findViewById(R.id.sp_formationtype);
        sp_formationtype.setPrompt("请选择飞行器类型");
        sp_formationtype.setAdapter(typeAdapter);
        sp_formationtype.setSelection(0);
        sp_formationtype.setOnItemSelectedListener(new TypeSelectedListener());

        Map<String, String> mapParam = (Map<String, String>) PlaneShared.getAll();
        String[] planeArray = new String[mapParam.size() + 1];
        if (mapParam.size() == 0) {
            planeArray = new String[]{"没有可用飞机"};
        } else {
            int i = 0;
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
        Spinner sp_leader = mView.findViewById(R.id.sp_leader);
        sp_leader.setAdapter(nameAdapter);
        sp_leader.setSelection(0);
        sp_leader.setOnItemSelectedListener(new FormationSelectedListener());

        ArrayAdapter<String> locAdapter = new ArrayAdapter<String>(mContext,
                R.layout.item_select, planeArray);
        locAdapter.setDropDownViewResource(R.layout.item_dropdown);
        Spinner sp_leader2 = mView.findViewById(R.id.sp_leader2);
        sp_leader2.setAdapter(locAdapter);
        sp_leader2.setSelection(0);
        sp_leader2.setOnItemSelectedListener(new LocSelectedListener());

    }

    class TypeSelectedListener implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            bid_id = arg2;
        }

        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }

    class FormationSelectedListener implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            bid_id2 = arg2;
        }

        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }

    class LocSelectedListener implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            bid_id3 = arg2;
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
        if (v.getId() == R.id.btn_confirm) {
            String fid = et_fid.getText().toString();
            String name = et_name.getText().toString();
            String formationsize = et_formationsize.getText().toString();
            String x = et_x.getText().toString();
            String y = et_y.getText().toString();
            String z = et_z.getText().toString();
            String launchdelay = et_launchdelay.getText().toString();
            String escapedelay = et_escapedelay.getText().toString();
            String blid = formationtypeArray[bid_id];
            Map<String, String> mapParam = (Map<String, String>) PlaneShared.getAll();
            String[] planeArray = new String[mapParam.size() + 1];
            if (mapParam.size() == 0) {
                planeArray = new String[]{"没有可用飞机"};
            } else {
                int i = 0;
                for (Map.Entry<String, String> item_map : mapParam.entrySet()) {
                    final String key = item_map.getKey(); // 获取该配对的键信息
                    planeArray[i] = key;
                    i++;
                }
                planeArray[i] = "暂不选择";
            }
            String blid2 = planeArray[bid_id2];
            String blid3 = planeArray[bid_id3];
            if (TextUtils.isEmpty(fid)) {
                showToast("请先填写编号");
                return;
            } else if (TextUtils.isEmpty(name)) {
                showToast("请先填写编队名称");
                return;
            } else if (TextUtils.isEmpty(formationsize)) {
                showToast("请先填写编队规模");
                return;
            } else if (TextUtils.isEmpty(x)) {
                showToast("请先填写X方向编队间距");
                return;
            } else if (TextUtils.isEmpty(y)) {
                showToast("请先填写Y方向编队间距");
                return;
            } else if (TextUtils.isEmpty(z)) {
                showToast("请先填写Z方向编队间距");
                return;
            } else if (TextUtils.isEmpty(launchdelay)) {
                showToast("请先填写发射延迟");
                return;
            } else if (TextUtils.isEmpty(escapedelay)) {
                showToast("请先填写脱离延迟");
                return;
            }
            showMakeSureDialog(fid, name, formationsize, x, y, z, launchdelay, escapedelay, blid, blid2, blid3);
        }
        if (v.getId() == R.id.btn_back) {
            SettingActivity activity = (SettingActivity) getActivity();
            FragmentManager fm = activity.getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.hide(activity.mFragments[0]).hide(activity.mFragments[1]).hide(activity.mFragments[2]).hide(activity.mFragments[3])
                    .hide(activity.mFragments[4]).hide(activity.mFragments[5]).hide(activity.mFragments[6]);
            ft.show(activity.mFragments[1]).commit();
        }

    }

    private void showToast(String desc) {
        Toast.makeText(mContext, desc, Toast.LENGTH_SHORT).show();
    }

    private void showMakeSureDialog(final String fid, final String name, final String formationsize, final String x, final String y, final String z, final String launchdelay, final String escapedelay, final String blid, final String blid2, final String blid3) {
        final ConfirmFormationDialog dialog = new ConfirmFormationDialog();
        dialog.setContent(fid);
        dialog.setDialogClickListener(new ConfirmFormationDialog.onDialogClickListener() {
            @Override
            public void onSureClick() {
                SharedPreferences formation;
                formation = getActivity().getSharedPreferences("Formation_" + fid, MODE_PRIVATE);
                SharedPreferences.Editor editor = formation.edit(); // 获得编辑器的对象
                editor.putString("Formation_No", fid);
                editor.putString("Formation_Name", name);
                editor.putString("Formation_type", blid);
                editor.putString("Formation_size", formationsize);
                editor.putString("distance_x", x);
                editor.putString("distance_y", y);
                editor.putString("distance_z", z);
                editor.putString("launch_delay", launchdelay);
                editor.putString("escape_delay", escapedelay);
                editor.putString("Leader_no", blid2);
                editor.putString("Leader2_no", blid3);
                editor.apply(); // 提交编辑器中的修改
                SharedPreferences.Editor editor2 = RecordShared.edit();
                editor2.putString(fid, name);
                editor2.apply();
                SharedPreferences team;
                team = getActivity().getSharedPreferences("TeamRecord_" + name, MODE_PRIVATE);
                SharedPreferences.Editor editor3 = team.edit();
                if (!blid2.equals("暂不选择") && !blid2.equals("没有可用飞机")) {
                    editor3.putString(blid2, "1");
                }
                if (!blid3.equals("暂不选择") && !blid3.equals("没有可用飞机")) {
                    editor3.putString(blid3, "2");
                }
                editor3.apply();

                if (!blid2.equals("暂不选择") && !blid2.equals("没有可用飞机")) {
                    SharedPreferences plane;
                    SharedPreferences team1;
                    plane = getActivity().getSharedPreferences("Plane_" + blid2, MODE_PRIVATE);
                    team1 = getActivity().getSharedPreferences("TeamRecord_" + plane.getString("At_Formation", ""), MODE_PRIVATE);
                    if ((!plane.getString("At_Formation", "").equals(name)) && (!plane.getString("At_Formation", "").equals("不在编队"))) {
                        SharedPreferences formation1;
                        formation1 = getActivity().getSharedPreferences("Formation_" + nametono(plane.getString("At_Formation", "")), MODE_PRIVATE);
                        SharedPreferences.Editor editor9 = formation1.edit();
                        if (blid2.equals(formation1.getString("Leader_no", ""))) {
                            editor9.putString("Leader_no", "暂不选择");
                        }
                        if (blid2.equals(formation1.getString("Leader2_no", ""))) {
                            editor9.putString("Leader2_no", "暂不选择");
                        }
                        editor9.apply();
                    }
                    SharedPreferences.Editor editor7 = team1.edit();
                    editor7.remove(blid2);
                    editor7.apply();
                    SharedPreferences.Editor editor4 = plane.edit();
                    editor4.putString("At_Formation", name);
                    editor4.putInt("Location_inteam", 1);
                    editor4.apply();
                }
                if (!blid3.equals("暂不选择") && !blid2.equals("没有可用飞机")) {
                    SharedPreferences plane2;
                    plane2 = getActivity().getSharedPreferences("Plane_" + blid3, MODE_PRIVATE);
                    SharedPreferences team2;
                    team2 = getActivity().getSharedPreferences("TeamRecord_" + plane2.getString("At_Formation", ""), MODE_PRIVATE);
                    if ((!plane2.getString("At_Formation", "").equals(name)) && (!plane2.getString("At_Formation", "").equals("不在编队"))) {
                        SharedPreferences formation2;
                        formation2 = getActivity().getSharedPreferences("Formation_" + nametono(plane2.getString("At_Formation", "")), MODE_PRIVATE);
                        SharedPreferences.Editor editor10 = formation2.edit();
                        if (blid3.equals(formation2.getString("Leader_no", ""))) {
                            editor10.putString("Leader_no", "暂不选择");
                        }
                        if (blid3.equals(formation2.getString("Leader2_no", ""))) {
                            editor10.putString("Leader2_no", "暂不选择");
                        }
                        editor10.apply();
                    }
                    SharedPreferences.Editor editor8 = team2.edit();
                    editor8.remove(blid3);
                    editor8.apply();
                    SharedPreferences.Editor editor5 = plane2.edit();
                    editor5.putString("At_Formation", name);
                    editor5.putInt("Location_inteam", 2);
                    editor5.apply();
                }
                SharedPreferences planerecord;
                planerecord = getActivity().getSharedPreferences("PlaneRecord", MODE_PRIVATE);
                SharedPreferences.Editor editor6 = planerecord.edit();
                if (!blid2.equals("暂不选择") && !blid2.equals("没有可用飞机")) {
                    editor6.putString(blid2, name);
                }
                if (!blid3.equals("暂不选择") && !blid2.equals("没有可用飞机")) {
                    editor6.putString(blid3, name);
                }
                editor6.apply();
                Intent intent = new Intent(mContext, SettingActivity.class);
                intent.putExtra("fragment_flag", 3);
                startActivity(intent);
            }

            @Override
            public void onCancelClick() {
                //这里是取消操作
                SettingActivity activity = (SettingActivity) getActivity();
                FragmentManager fm = activity.getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.hide(activity.mFragments[0]).hide(activity.mFragments[1]).hide(activity.mFragments[2]).hide(activity.mFragments[3]).hide(activity.mFragments[4]).hide(activity.mFragments[5]).hide(activity.mFragments[6]);
                ft.show(activity.mFragments[5]).commit();
            }
        });
        dialog.show(getFragmentManager(), "");
    }

    private String nametono(String name) {
        String no = "";
        SharedPreferences formation = getActivity().getSharedPreferences("FormationRecord", MODE_PRIVATE);
        Map<String, Object> mapParam = (Map<String, Object>) formation.getAll();
        for (Map.Entry<String, Object> item_map : mapParam.entrySet()) {
            final String key = item_map.getKey(); // 获取该配对的键信息
            if (name.equals(item_map.getValue())) {
                no = key;
            }
        }
        return no;
    }

}
