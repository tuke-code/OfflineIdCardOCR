package com.hh.myocr;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.ocr.decode.OcrDecodeFactory;
import com.tomcat.ocr.idcard.R;
import com.tomcat.ocr.idcard.databinding.ActivityMainBinding;

import java.util.List;

import com.hh.myocr.permissions.*;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements EasyPermission.PermissionCallback {
    private Context context;
    private ActivityMainBinding binding;
    private String broadcastAction = "com.idCard.result";

    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        context = this;
        try {
            context.getPackageManager().getApplicationInfo("", PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        Intent intent = getIntent();
        String result = intent.getStringExtra("result");
        if (result != null) {
            Toast.makeText(context, "解析成功: " + result, Toast.LENGTH_LONG).show();
            Log.e("TAG", "解析成功: " + result);

            try {

                JSONObject jo = new JSONObject(result);
                if (jo.getString("type") != null) {
                    StringBuffer sb = new StringBuffer();
                    sb.append(String.format("正面 = %s\n", getField(jo, "type")));
                    sb.append(String.format("姓名 = %s\n", getField(jo, "name")));
                    sb.append(String.format("性别 = %s\n", getField(jo, "sex")));
                    sb.append(String.format("民族 = %s\n", getField(jo, "folk")));
                    sb.append(String.format("日期 = %s\n", getField(jo, "birt")));
                    sb.append(String.format("号码 = %s\n", getField(jo, "num")));
                    sb.append(String.format("住址 = %s\n", getField(jo, "addr")));
                    sb.append(String.format("签发机关 = %s\n", getField(jo, "issue")));
                    sb.append(String.format("有效期限 = %s\n", getField(jo, "valid")));
                    sb.append(String.format("整体照片 = %s\n", getField(jo, "imgPath")));
                    sb.append(String.format("头像路径 = %s\n", getField(jo, "headPath")));
                    sb.append("\n驾照专属字段\n");
                    sb.append(String.format("国家 = %s\n", getField(jo, "nation")));
                    sb.append(String.format("初始领证 = %s\n", getField(jo, "startTime")));
                    sb.append(String.format("准驾车型 = %s\n", getField(jo, "drivingType")));
                    sb.append(String.format("有效期限 = %s\n", getField(jo, "registerDate")));
                    binding.textview.setText(sb.toString());
                }


            } catch (Exception e) {
                JSONObject jo = null;
                try {
                    jo = new JSONObject(result);
                    StringBuffer sb = new StringBuffer();
                    sb.append(String.format("正面 = %s\n", getField(jo, "type")));
                    sb.append(String.format("姓名 = %s\n", getField(jo, "Name")));
                    sb.append(String.format("中文姓名 = %s\n", getField(jo, "NameCh")));
                    sb.append(String.format("英文姓 = %s\n", getField(jo, "EnFir")));
                    sb.append(String.format("英文名 = %s\n", getField(jo, "EnSen")));
                    sb.append(String.format("性别 = %s\n", getField(jo, "SexCH")));
                    sb.append(String.format("生日 = %s\n", getField(jo, "Birthday")));
                    sb.append(String.format("号码 = %s\n", getField(jo, "CardNo")));
                    sb.append(String.format("住址 = %s\n", getField(jo, "AddressCH")));
                    sb.append(String.format("国家 = %s\n", getField(jo, "Nation")));
                    binding.textview.setText(sb.toString());
                } catch (JSONException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
//        OcrDecodeFactory.initOCR(context);  //全局初始化一次即可.
//        binding.button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                //Bundle bundle = new Bundle();
//                //bundle.putBoolean("saveImage", binding.saveImage.getSelectedItemPosition() == 0 ? true : false); // 是否保存识别图片
//                //bundle.putBoolean("showSelect", true);                          // 是否显示选择图片
//                //bundle.putBoolean("showCamera", true);                          // 显示图片界面是否显示拍照(驾照选择图片识别率比扫描高)
//                //bundle.putInt("requestCode", REQUEST_CODE);                     // requestCode
//                //bundle.putInt("type", binding.type.getSelectedItemPosition());    // 0身份证, 1驾驶证, 2护照
//
//                //broadcastAction 将扫描结果广播出去, 注意增加 intent.addCategory(context.getPackageName());
//                //如果不需要广播,就不会传这个参数
//                //bundle.putString("broadcastAction", broadcastAction);
//                OcrDecodeFactory.newBuilder(context)
//                        .requestCode(REQUEST_CODE)
//                        .ocrType(binding.type.getSelectedItemPosition())    //0身份证, 1驾驶证, 2护照
//                        .broadcastAction(broadcastAction)                   //扫描结果发送到这个广播上
//                        .startOcrActivity();                                //使用内置的扫描界面
//            }
//        });
//
//
//
//        binding.button.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                binding.tip.setVisibility(View.GONE);
//                return true;
//            }
//        });
        binding.button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, SimpleCameraActivity.class);
                intent.putExtra("saveImage", false);
                intent.putExtra("type", binding.type.getSelectedItemPosition());
                startActivity(intent);
            }
        });


        //扫描之前,提前申请权限..
        requestPermission();


        binding.saveImage.setSelection(1);  //默认不保存.
        //注册扫描广播
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(broadcastAction);
        intentFilter.addCategory(getPackageName());
        resultReceiver = new ResultReceiver();
        registerReceiver(resultReceiver, intentFilter);
    }

    private ResultReceiver resultReceiver;

    private class ResultReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (broadcastAction.equals(intent.getAction())) {
                String result = intent.getStringExtra("OCRResult");

//                Toast.makeText(context, "从广播中接收到扫描数据: " + result, Toast.LENGTH_LONG).show();
                try {
                    JSONObject jo = new JSONObject(result);
                    StringBuffer sb = new StringBuffer();
                    sb.append(String.format("正面 = %s\n", getField(jo, "type")));
                    sb.append(String.format("姓名 = %s\n", getField(jo, "name")));
                    sb.append(String.format("性别 = %s\n", getField(jo, "sex")));
                    sb.append(String.format("民族 = %s\n", getField(jo, "folk")));
                    sb.append(String.format("日期 = %s\n", getField(jo, "birt")));
                    sb.append(String.format("号码 = %s\n", getField(jo, "num")));
                    sb.append(String.format("住址 = %s\n", getField(jo, "addr")));
                    sb.append(String.format("签发机关 = %s\n", getField(jo, "issue")));
                    sb.append(String.format("有效期限 = %s\n", getField(jo, "valid")));
                    sb.append(String.format("整体照片 = %s\n", getField(jo, "imgPath")));
                    sb.append(String.format("头像路径 = %s\n", getField(jo, "headPath")));
                    sb.append("\n驾照专属字段\n");
                    sb.append(String.format("国家 = %s\n", getField(jo, "nation")));
                    sb.append(String.format("初始领证 = %s\n", getField(jo, "startTime")));
                    sb.append(String.format("准驾车型 = %s\n", getField(jo, "drivingType")));
                    sb.append(String.format("有效期限 = %s\n", getField(jo, "registerDate")));
                    binding.textview.setText(result);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // 可以提取公共方法简化代码
    private String getField(JSONObject jo, String key) {
        // 先尝试首字母大写形式
        String capitalizedKey = key.substring(0, 1).toUpperCase() + key.substring(1);
        Object value = jo.opt(capitalizedKey);  // 直接访问JSON对象，而不是递归调用

        // 如果大写形式不存在，则尝试原始小写形式
        return value != null ? value.toString() : jo.optString(key);
    }

    String[] permissions = new String[]{
            Manifest.permission.CAMERA
    };

    private boolean isHavePermission = true;

    private void requestPermission() {
        if (EasyPermission.hasPermissions(context, permissions)) {
            isHavePermission = true;
        } else {
            //TODO 请求相机权限
//            EasyPermission.with(this)
//                    .rationale(getString(com.msd.ocr.idcard.R.string.rationale_camera))
//                    .addRequestCode(REQUEST_PERMISS)
//                    .permissions(permissions)
//                    .request();
        }
    }

    private final int REQUEST_CODE = 1;
    private final int REQUEST_PERMISS = 2;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            String result = data.getStringExtra("OCRResult");
            try {
                JSONObject jo = new JSONObject(result);
                StringBuffer sb = new StringBuffer();
                sb.append(String.format("正面 = %s\n", getField(jo, "type")));
                sb.append(String.format("姓名 = %s\n", getField(jo, "name")));
                sb.append(String.format("性别 = %s\n", getField(jo, "sex")));
                sb.append(String.format("民族 = %s\n", getField(jo, "folk")));
                sb.append(String.format("日期 = %s\n", getField(jo, "birt")));
                sb.append(String.format("号码 = %s\n", getField(jo, "num")));
                sb.append(String.format("住址 = %s\n", getField(jo, "addr")));
                sb.append(String.format("签发机关 = %s\n", getField(jo, "issue")));
                sb.append(String.format("有效期限 = %s\n", getField(jo, "valid")));
                sb.append(String.format("整体照片 = %s\n", getField(jo, "imgPath")));
                sb.append(String.format("头像路径 = %s\n", getField(jo, "headPath")));
                sb.append("\n驾照专属字段\n");
                sb.append(String.format("国家 = %s\n", getField(jo, "nation")));
                sb.append(String.format("初始领证 = %s\n", getField(jo, "startTime")));
                sb.append(String.format("准驾车型 = %s\n", getField(jo, "drivingType")));
                sb.append(String.format("有效期限 = %s\n", getField(jo, "registerDate")));
                binding.textview.setText(result);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onPermissionGranted(int requestCode, List<String> perms) {
        isHavePermission = true;
    }

    @Override
    public void onPermissionDenied(int requestCode, List<String> perms) {
        Toast.makeText(context, "没有相机权限", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(resultReceiver); //记得要注销
    }
}
