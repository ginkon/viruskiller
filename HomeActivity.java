package com.all.antivirus.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.all.antivirus.R;

import java.util.Random;

public class HomeActivity extends BaseTouch {
    private GridView gv;
    private ImageView iv_home;
    private TextView tv_home_state;
    private ImageView iv_home_bg;
    // 应用名
    String[] itemName = new String[]{"手机杀毒"};
    int[] itemR = new int[]{R.drawable.baseline_gps_fixed_white_48dp};
    private SharedPreferences shp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 设置整个页面的布局
        setContentView(R.layout.activity_home2);
        // 获取haredPreferences获得配置信息
        shp = getSharedPreferences("config", MODE_PRIVATE);
        // 拿到gridview并设置适配器
        gv = (GridView) findViewById(R.id.gv_home2_home);
        iv_home = (ImageView) findViewById(R.id.iv_home);
        tv_home_state = (TextView) findViewById(R.id.tv_home_state);
        iv_home_bg = (ImageView) findViewById(R.id.iv_home_bg);
        RotateAnimation ra = new RotateAnimation(0, 360, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        ra.setDuration(2000);
        ra.setRepeatCount(-1);
        iv_home.startAnimation(ra);


        gv.setAdapter(new MyAdapter());
        // 设置item的点击事件
        gv.setOnItemClickListener(new MyListener());
        iv_home_bg.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                //finish();
                startActivity(new Intent(HomeActivity.this, HomeActivity.class));
            }
        });

    }





    // item点击监听器
    class MyListener implements OnItemClickListener {

        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            switch (position) {
                case 0:
                    // 手机杀毒
                    startActivity(new Intent(HomeActivity.this,
                            AntivirusActivity.class));
                    overridePendingTransition(R.anim.exit_in, R.anim.exit_out);
                    break;

            }

        }

    }

    /**
     * 自定义网格适配器
     *
     * @author Administrator
     */

    class MyAdapter extends BaseAdapter {
        // 网格数量
        public int getCount() {
            return itemName.length;
        }

        public Object getItem(int position) {
            return itemName[position];
        }

        public long getItemId(int position) {
            return position;
        }

        // 设置展示的每个itemview
        public View getView(int position, View convertView, ViewGroup parent) {
            // 填充item布局xml
            View v = View.inflate(HomeActivity.this, R.layout.gridview_item, null);
            // 设置应用名，和图片
            ImageView iv = (ImageView) v.findViewById(R.id.iv_item);
            TextView tv = (TextView) v.findViewById(R.id.tv_item);
            iv.setBackgroundResource(itemR[position]);
            tv.setText(itemName[position]);
            // 返回设置好的Item
            return v;

        }

    }

    @Override
    public void showNextPage() {

    }

    @Override
    public void showPreviousPage() {

    }
}
