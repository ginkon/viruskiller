package com.all.antivirus.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.all.antivirus.R;
import com.all.antivirus.util.IOUtil;
import com.all.antivirus.util.ServiceState;
import com.all.touch.RocketService;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class SplashActivity extends Activity {
    // 用来处理的常量
    protected static final int CODE_IN_HOME = 0;
    protected static final int CODE_NOT_UPDATE = 1;
    private DevicePolicyManager mDPM;
    private ComponentName mDeviceAdminSample;
    private Long startT;
    private long endT;
    private String versionName;
    private int versionCode;
    private String description;
    private String downloadUrl;
    private RelativeLayout rl;
    private SharedPreferences spf;
    // handler处理相应的行为
    Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
               // 进入主页
                case CODE_IN_HOME:
                    enterHome();
                    break;
                case CODE_NOT_UPDATE:
                    enterHome();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        // 拿到设备管理器
        mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);// 获取设备策略服务
        mDeviceAdminSample = new ComponentName(this,
                com.all.antivirus.receiver.AdminReceiver.class);// 设备管理组件
        // 激活设备管理器
        activeDevice();
        // splash设置渐变效果
        AlphaAnimation aa = new AlphaAnimation(0.2f, 1);
        rl = findViewById(R.id.rl_splash);
        aa.setDuration(5000);
        rl.startAnimation(aa);
        //启动touch
        if (!ServiceState.serviceRunning(this, "com.all.touch.RocketService")) {
            Intent intent = new Intent(this, RocketService.class);
            startService(intent);
        }

        // 拷贝病毒数据库到files文件夹
        copy("antivirus.db");

        // 获得SharedPreferences然后进行相应处理
        spf = getSharedPreferences("config", Context.MODE_PRIVATE);
        // 创建快捷图标
        shortcut();
        // 发送延时消息确保整splash页面停留至少2秒
            Message msg = handler.obtainMessage();
           msg.what = CODE_NOT_UPDATE;
            handler.handleMessage(msg);



    }

    /**
     * 创建快捷图标
     */
    public void shortcut() {
        // 查看配置文件中是否已经设置快捷图标
        boolean install = spf.getBoolean("install", false);
        // 设置了就跳出
        if (install) {
            return;
        }
        // 设置安装快捷图标的意图
        Intent intent = new Intent(
                "com.android.launcher.action.INSTALL_SHORTCUT");
        // 设置名字
        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "杀毒卫士");
        // 设置图标
        intent.putExtra(Intent.EXTRA_SHORTCUT_ICON,
                BitmapFactory.decodeResource(getResources(), R.drawable.ic_main));
        // 设置意图
        Intent it = new Intent("com.all.home");
        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, it);
        // 发送广播，创建快捷图标
        sendBroadcast(intent);
        // 创建后就讲配置文件设置印记
        spf.edit().putBoolean("install", true).commit();
    }

    /**
     * 激活设备管理器
     */
    public void activeDevice() {
        if (!mDPM.isAdminActive(mDeviceAdminSample)) {// 判断设备管理器是否已经激活
            // 如果未激活就激活弹出激活页面
            Intent intent = new Intent(
                    DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                    mDeviceAdminSample);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    "开启功能必须激活此设备管理器");
            startActivity(intent);
        }
    }


    /**
     * 复制assets里的内容到内部存储的files文件夹
     *
     * @param assetname
     */
    public void copy(String assetname) {
        // 目标文件位置
        File locFile = new File(getFilesDir(), assetname);
        if (locFile.exists()) {
            return;
        }
        OutputStream out = null;
        InputStream in = null;
        try {
            // assets的文件变成读取流
            in = getAssets().open(assetname);
            // 输出流
            out = new FileOutputStream(locFile);
            byte[] buf = new byte[1024];
            int num = 0;
            // 开始拷贝
            while ((num = in.read(buf)) != -1) {
                out.write(buf, 0, num);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    // 如果读取流不为空则关闭
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    // 关闭报错就设置为空，让虚拟机自己回收
                    in = null;
                }
            }
            if (out != null) {
                try {
                    // 如果输出流不为空则关闭
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    // 关闭报错就设置为空，让虚拟机自己回收
                    out = null;
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);

        // 安装界面被关闭，强制进入主页
        enterHome();
    }

    /**
     * 进入主界面
     */
    public void enterHome() {

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(SplashActivity.this, HomeActivity.class);
                        startActivity(intent);
                        // 关闭splash
                        finish();
                        // splash退出效果
                        overridePendingTransition(R.anim.splansh_in, R.anim.splansh_out);
                    }
                });
            }
        }, 7000);


    }

    public boolean checkIsUpdate() {
        boolean isupdate = spf.getBoolean("isupdate", false);
        return isupdate;
    }

}
