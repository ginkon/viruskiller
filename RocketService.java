package com.all.touch;


import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.all.antivirus.R;
import com.all.antivirus.receiver.AdminReceiver;

import java.util.Timer;
import java.util.TimerTask;

public class RocketService extends Service {
	private long startT;
	private WindowManager wm;
	private int startX;
	private int startY;
	private WindowManager.LayoutParams params;
	private int wDwidth;
	private int wDhight;
	private View view;
	private DevicePolicyManager mDPM;
	private ComponentName mDeviceAdminSample;
	long[] mHits = new long[1];// 数组长度表示要点击的次数
	long[] mHits2 = new long[1];// 数组长度表示要点击的次数
	private SharedPreferences spf;
	private ImageView iv_touch;
	private TextView tv_prossess;
	private boolean isOpen = true;
	private Timer timer;
	private TimerTask task;
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	Handler handler = new Handler(){
		public void handleMessage(Message msg) {
			wm.removeView(view);
			spf.edit().putInt("x", params.x).commit();
			spf.edit().putInt("y", params.y).commit();
			
			show();
			
		};
	};
	@Override
	public void onCreate() {
		super.onCreate();
		mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);// 获取设备策略服务
		mDeviceAdminSample = new ComponentName(this, AdminReceiver.class);// 设备管理组件
		spf = getSharedPreferences("config", Context.MODE_PRIVATE);
		show();
		
		timer= new Timer();
		task = new TimerTask() {
			
			@Override
			public void run() {
				handler.sendEmptyMessage(0);
				
			}
		};
		
		timer.schedule(task, 0, 10000);
		
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
		if (timer!=null&&task!=null) {
			timer.cancel();
			task.cancel();
			timer=null;
			task=null;
			
		}
		wm.removeView(view);
		spf.edit().putInt("x", params.x).commit();
		spf.edit().putInt("y", params.y).commit();
	}

	/**
	 * 自定义浮窗
	 */
	public void show() {
		// 拿到配置中心存储的显示坐标
		int x = spf.getInt("x", 0);
		int y = spf.getInt("y", 0);
		// 获得窗口管理器
		// 闹到屏幕的宽高
		wm = (WindowManager) getSystemService(WINDOW_SERVICE);
		wDwidth = wm.getDefaultDisplay().getWidth();
		wDhight = wm.getDefaultDisplay().getHeight();
		params = new WindowManager.LayoutParams();
		params.height = WindowManager.LayoutParams.WRAP_CONTENT;
		params.width = WindowManager.LayoutParams.WRAP_CONTENT;
		params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
				| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
		params.format = PixelFormat.TRANSLUCENT;
		params.type = WindowManager.LayoutParams.TYPE_PHONE;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
		}
		// 将默认的重心位置从中心，设置到左上方，与设置位置的页面相同。变异量才能吻合
		params.gravity = Gravity.LEFT + Gravity.TOP;
		// 将配置文件中存储的坐标设置上去
		params.setTitle("Toast");
		// 将配置文件中存储的坐标设置上去
		params.x = x;
		params.y = y;
		// 自定义View
		view = View.inflate(this, R.layout.view_touch, null);
		iv_touch = (ImageView) view.findViewById(R.id.iv_touch);
		tv_prossess = (TextView) view.findViewById(R.id.tv_prossess);
		


		
		// 设置监听
		view.setOnTouchListener(new MyOnTouchListener());
		// 设置到window上
		wm.addView(view, params);
	}


	/**
	 * 触碰监听
	 * 
	 * @author Administrator
	 * 
	 */
	class MyOnTouchListener implements OnTouchListener {


		public boolean onTouch(View v, MotionEvent event) {
			// 点击事件
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				startT = System.currentTimeMillis();
				// 起始的坐标
				startX = (int) event.getRawX();
				startY = (int) event.getRawY();
				break;
			case MotionEvent.ACTION_MOVE:
				// 移动的位置
				int endX = (int) event.getRawX();
				int endY = (int) event.getRawY();

				// 移动的偏移量
				int x = endX - startX;
				int y = endY - startY;
				// 设置编译后的位置
				params.x += x;
				params.y += y;
				// 防止越界
				
				if (params.x < 0) {
					params.x = 0;
				}
				if (params.y < 0) {
					params.y = 0;
				}
				if (params.x > wDwidth - view.getWidth()) {
					params.x = wDwidth - view.getWidth();
				}
				if (params.y > wDhight - view.getHeight()) {
					params.y = wDhight - view.getHeight();
				}
				// 更新位置
				wm.updateViewLayout(view, params);
				
				// 将这次的坐标记录
				startX = (int) event.getRawX();
				startY = (int) event.getRawY();

				break;
			case MotionEvent.ACTION_UP:
				long endT = System.currentTimeMillis();
				if (endT - startT < 200) {
					Intent i = new Intent(RocketService.this,
							TouchActivity.class);
					i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(i);
					stopSelf();
				}
				if (params.x>=wDwidth-200) {
					params.x=wDwidth;
				}
				
				if (params.x<=200) {
					params.x = 0;
				}
				if (params.y<=200) {
					params.y=0;
				}
				if (params.y>=wDhight-250) {
					params.y=wDhight;
				}
				wm.updateViewLayout(view, params);
				
			default:
				break;
			}
			return true;
		}
	}
	

}
