package buaa.uavswarm.urbanmissionmap.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.io.File;
import java.util.Map;

import buaa.uavswarm.urbanmissionmap.R;
import buaa.uavswarm.urbanmissionmap.activities.SettingActivity;

import static android.content.Context.MODE_PRIVATE;

public class FragmentFormationSetting extends Fragment {
    protected View mView;
    protected Context mContext;
    private SharedPreferences FormationShared;
    private LinearLayout ll_formation;
    private Button Add;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mContext=getActivity();
        mView=inflater.inflate(R.layout.fragment_formationsetting, container, false);
        ll_formation = mView.findViewById(R.id.ll_formation);
        Add = mView.findViewById(R.id.btn_addformation);
        FormationShared = this.getActivity().getSharedPreferences("FormationRecord", MODE_PRIVATE);
        Add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SettingActivity activity = (SettingActivity) getActivity();
                FragmentManager fm = activity.getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.hide(activity.mFragments[0]).hide(activity.mFragments[1]).hide(activity.mFragments[2]).hide(activity.mFragments[3]).
                        hide(activity.mFragments[4]).hide(activity.mFragments[5]).hide(activity.mFragments[6]);
                ft.show(activity.mFragments[5]).commit();
            }
        });
        return mView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private View mContextView;

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        // 保存该IP地址行的视图，以便删除IP时一块从列表移除该行
        mContextView = v;
        // 从menu_planes.xml中构建菜单界面布局
        getActivity().getMenuInflater().inflate(R.menu.menu_formations, menu);
    }
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_modify) { // 点击了菜单项“修改”
            // 跳转到修改页面
            goModify(Integer.toString(mContextView.getId()));
        } else if (id == R.id.menu_delete) { // 点击了菜单项“删除”
            // 删除该编队的XML文件
            File file= new File("/data/data/"+getActivity().getPackageName().toString()+"/shared_prefs","Formation_"+mContextView.getId()+".xml");
            if(file.exists())
            {
                file.delete();
            }
            SharedPreferences team = getActivity().getSharedPreferences("TeamRecord_" + notoname(Integer.toString(mContextView.getId())), MODE_PRIVATE);
            Map<String, String> mapParam = (Map<String, String>) team.getAll();
            for (Map.Entry<String, String> item_map : mapParam.entrySet()) {
                final String key = item_map.getKey();
                SharedPreferences plane = getActivity().getSharedPreferences("Plane_" + key, MODE_PRIVATE);
                SharedPreferences.Editor editor=plane.edit();
                editor.putString("At_Formation","不在编队");
                editor.apply();
                SharedPreferences planerecord = getActivity().getSharedPreferences("PlaneRecord",MODE_PRIVATE);
                SharedPreferences.Editor editor1 = planerecord.edit();
                editor1.putString(key,"不在编队");
                editor1.apply();
                SharedPreferences teamrecord = getActivity().getSharedPreferences("TeamRecord_" + "不在编队", MODE_PRIVATE);
                SharedPreferences.Editor editor3=teamrecord.edit();
                editor3.putString(key,"0");
                editor3.apply();
            }
            File file2= new File("/data/data/"+getActivity().getPackageName().toString()+"/shared_prefs","TeamRecord_"+notoname(Integer.toString(mContextView.getId()))+".xml");
            if(file2.exists())
            {
                file2.delete();
            }
            SharedPreferences.Editor editor2 = FormationShared.edit();
            editor2.remove(Integer.toString(mContextView.getId()));
            editor2.apply();

            // 从列表中删除该行
            ll_formation.removeView(mContextView);
        }
        return true;
    }
    // 跳转到详情页面
    private void goModify(String rowid) {
        mSelectInterface.onFormationSelect(rowid);
        SettingActivity activity = (SettingActivity) getActivity();
        FragmentManager fm = activity.getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.hide(activity.mFragments[0]).hide(activity.mFragments[1]).hide(activity.mFragments[2]).hide(activity.mFragments[3])
                .hide(activity.mFragments[4]).hide(activity.mFragments[5]).hide(activity.mFragments[6]);
        ft.show(activity.mFragments[6]).commit();
    }
    @Override
    public void onResume() {
        super.onResume();
        showformation();
    }

    private void showformation() {
        // 移除线性视图ll_group下面的所有子视图
        ll_formation.removeAllViews();
        // 创建一个标题行的线性视图ll_row
        LinearLayout ll_row = newLinearLayout(LinearLayout.HORIZONTAL, ViewGroup.LayoutParams.WRAP_CONTENT, Color.LTGRAY);
        ll_row.addView(newTextView(2, 2, Gravity.CENTER, "编队名称", Color.BLACK, 18));
        ll_row.addView(newTextView(2, 2, Gravity.CENTER, "编队类型", Color.BLACK, 18));
        ll_row.addView(newTextView(2, 2, Gravity.CENTER, "编队规模", Color.BLACK, 18));
        ll_row.addView(newTextView(2, 2, Gravity.CENTER, "长机地址", Color.BLACK, 18));
        ll_row.addView(newTextView(2, 2, Gravity.CENTER, "备份长机地址", Color.BLACK, 18));
        ll_row.addView(newTextView(0, (float)0.4, Gravity.CENTER, "", Color.BLACK, 18));
        // 把标题行添加到列表
        ll_formation.addView(ll_row);
        Map<String, Object> mapParam = (Map<String, Object>) FormationShared.getAll();
        for (Map.Entry<String, Object> item_map : mapParam.entrySet()) {
            final String key = item_map.getKey(); // 获取该配对的键信息
            SharedPreferences team;
            team = getActivity().getSharedPreferences("Formation_" + key, MODE_PRIVATE);
            // 创建该行的水平线性视图。
            ll_row = newLinearLayout(LinearLayout.HORIZONTAL, ViewGroup.LayoutParams.WRAP_CONTENT,Color.WHITE);
            // 设置该线性视图的编号
            ll_row.setId(Integer.parseInt(key));
            // 添加飞机地址和IP地址
            ll_row.addView(newTextView(10, 2, Gravity.CENTER, "" + team.getString("Formation_Name", ""), Color.BLACK, 18));
            ll_row.addView(newTextView(10, 2, Gravity.CENTER, "" + team.getString("Formation_type", ""), Color.BLACK, 18));
            ll_row.addView(newTextView(10, 2, Gravity.CENTER, "" + team.getString("Formation_size", ""), Color.BLACK, 18));
            if(team.getString("Leader_no", "").equals("")||team.getString("Leader_no", "").equals("暂不选择")){
                ll_row.addView(newTextView(10, 2, Gravity.CENTER, "未选择长机", Color.BLACK, 18));
            }else{ll_row.addView(newTextView(10, 2, Gravity.CENTER, "" + team.getString("Leader_no", ""), Color.BLACK, 18));}
            if(team.getString("Leader2_no", "").equals("")||team.getString("Leader2_no", "").equals("暂不选择")){
                ll_row.addView(newTextView(10, 2, Gravity.CENTER, "未选择备用长机", Color.BLACK, 18));
            }else{
                ll_row.addView(newTextView(10, 2, Gravity.CENTER, "" + team.getString("Leader2_no", ""), Color.BLACK, 18));
            }
            ll_row.addView(newImageView(0,(float)0.4, ContextCompat.getDrawable(mContext,R.drawable.detial_2)));
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
            ll_formation.addView(ll_row);
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

    private formationSelectInterface mSelectInterface;

    public interface formationSelectInterface{
        public void onFormationSelect(String title);
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mSelectInterface = (formationSelectInterface) activity;
        } catch (Exception e) {
            throw new ClassCastException(activity.toString() + "must implement OnArticleSelectedListener");
        }
    }

    private String notoname(String no) {
        String name = "";
        SharedPreferences formation = getActivity().getSharedPreferences("FormationRecord", MODE_PRIVATE);
        Map<String, Object> mapParam = (Map<String, Object>) formation.getAll();
        for (Map.Entry<String, Object> item_map : mapParam.entrySet()) {
            final String value = (String) item_map.getValue(); // 获取该配对的键信息
            if (no.equals(item_map.getKey())) {
                name = value;
            }
        }
        return name;
    }


}
