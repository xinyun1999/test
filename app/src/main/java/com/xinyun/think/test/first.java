package com.xinyun.think.test;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;


import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;



public class first extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {
    private BluetoothAdapter bAdapter ;//声明蓝牙适配器
    private EditText nameView;//声明edittext
    private String blueName;//声明用户输入的蓝牙设备名称变量
    private TextView showRssi;//声明textview用于显示信号强度信息
    private MqttAsyncClient mqttClient;
    private final static String host = "123.206.127.199:1883";
    private final static String username = "Nw6sTc3Uo9MomuT";
    private final static String Topic = "inTopic";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.first_layout);
        Switch switch1 = findViewById(R.id.switch1);
        switch1.setOnCheckedChangeListener(this);
        connectBroker();
        bAdapter = BluetoothAdapter.getDefaultAdapter();//获取蓝牙适配器
        //设置过滤器，过滤因远程蓝牙设备被找到而发送的广播 BluetoothDevice.ACTION_FOUND
        IntentFilter iFilter = new IntentFilter();
        iFilter.addAction(BluetoothDevice.ACTION_FOUND);
        //设置广播接收器和安装过滤器
        registerReceiver(new foundReceiver(), iFilter);
        //获取控件对象
        nameView = (EditText) findViewById(R.id.bluetoothName);
        showRssi = (TextView) findViewById(R.id.showRssi);
    }

    private void connectBroker() {
        try {
            mqttClient = new MqttAsyncClient("tcp://" + host,
                    "ClientID" + username, new MemoryPersistence());
            mqttClient.connect(getOptions(), null);

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private MqttConnectOptions getOptions() {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);//重连不保持状态
        options.setConnectionTimeout(10);//设置连接超时时间
        options.setKeepAliveInterval(30);//设置保持活动时间，超过时间没有消息收发将会触发ping消息确认
        return options;
    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            try {
                mqttClient.publish(Topic, "1".getBytes(), 1, false);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        } else {
            try {
                mqttClient.publish(Topic, "0".getBytes(), 1, false);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 打开蓝牙
     */
    public void open(View v) {
        if (!bAdapter.isEnabled()) {
            bAdapter.enable();
            Toast.makeText(getApplicationContext(), "蓝牙打开成功", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "蓝牙已经打开", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 关闭蓝牙
     */
    public void close(View v) {
        if (bAdapter.isEnabled()) {
            bAdapter.disable();
            Toast.makeText(getApplicationContext(), "蓝牙关闭成功", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "蓝牙已经关闭", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 搜索远程蓝牙设备，获取editview的值
     */
    public void show(View v) {
        if (bAdapter.isEnabled()) {
            blueName = nameView.getText().toString().trim();
            bAdapter.startDiscovery();
        } else {
            Toast.makeText(getApplicationContext(), "蓝牙未打开", Toast.LENGTH_SHORT).show();
            ;
        }
    }


    class foundReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);//获取此时找到的远程设备对象
            if (blueName.equals(device.getName())) {//判断远程设备是否与用户目标设备相同
                short rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,Short.MIN_VALUE);//获取额外rssi值
                showRssi.setText(rssi);//显示rssi到控件上
                bAdapter.cancelDiscovery();//关闭搜索
            } else {
                showRssi.setText("未发现设备");
            }
        }
    }
}
