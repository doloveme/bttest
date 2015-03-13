package com.kingsoft.bttest.app;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;


public class MainActivity extends ActionBarActivity implements View.OnClickListener  {

    Button openBtn;
    Button closeBtn;
    Button searchBtn;
    Button cleanBtn;
    Button pairBtn;
    Button send;
    TextView info;
    BluetoothAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        openBtn = (Button) findViewById(R.id.open);
        closeBtn = (Button) findViewById(R.id.close);
        searchBtn = (Button) findViewById(R.id.search);
        cleanBtn = (Button) findViewById(R.id.clean);
        pairBtn = (Button) findViewById(R.id.cleanpair);
        send = (Button) findViewById(R.id.send);

        send.setOnClickListener(this);
        pairBtn.setOnClickListener(this);
        searchBtn.setOnClickListener(this);
        cleanBtn.setOnClickListener(this);
        openBtn.setOnClickListener(this);
        closeBtn.setOnClickListener(this);
        info = (TextView) findViewById(R.id.info);
        initBlueTooth();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter); // 不要忘了之后解除绑定
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    private void initBlueTooth(){
        adapter = BluetoothAdapter.getDefaultAdapter();
        Intent discoveryIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoveryIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);//设置持续时间（最多300秒）
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initSocket() {
        BluetoothSocket temp = null;
        try {
            Method m = device.getClass().getMethod(
                    "createRfcommSocket", new Class[] { int.class });
            temp = (BluetoothSocket) m.invoke(device, 1);//这里端口为1
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        socket = temp;
    }
    BluetoothSocket socket;
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case  R.id.open:
                adapter.enable();
                break;
            case  R.id.close:
                adapter.disable();
                break;
            case  R.id.search:
                adapter.startDiscovery();
                break;
            case  R.id.cleanpair:
                // 固定的UUID
                Thread thread  = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        connect();
//                        connectDevice();
                    }
                });
                thread.start();
                break;
            case  R.id.clean:
                info.setText("");
                count = 0;
                break;
            case  R.id.send:
                count = 0;
                break;
        }
    }
    boolean connected = false;
    boolean connecting = false;
    int connetTime = 0;

    private void connect(){
        try {
            adapter.cancelDiscovery();
            String uuid = "00001101-0000-1000-8000-00805F9B34FB";
//            String uuid = "fa87c0d0-afac-11de-8a39-0800200c9a66";
            socket = device.createRfcommSocketToServiceRecord(UUID.fromString(uuid));
            socket.connect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void send(){
    }


    protected void connectDevice() {
        try {
            // 连接建立之前的先配对
            if (device.getBondState() == BluetoothDevice.BOND_NONE) {
                Method creMethod = BluetoothDevice.class
                        .getMethod("createBond");
                Log.e("TAG", "开始配对");
                creMethod.invoke(device);
            } else {
            }
        } catch (Exception e) {
            // TODO: handle exception
            //DisplayMessage("无法配对！");
            e.printStackTrace();
        }
        adapter.cancelDiscovery();
        try {
            socket.connect();
            //DisplayMessage("连接成功!");
            //connetTime++;
            connected = true;
        } catch (IOException e) {
            // TODO: handle exception
            //DisplayMessage("连接失败！");
            connetTime++;
            connected = false;
            try {
                socket.close();
                socket = null;
            } catch (IOException e2) {
                // TODO: handle exception
            }
        } finally {
            connecting = false;
        }
    }
    BluetoothDevice device;
    private int count = 0;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            // 发现设备

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {

                // 从Intent中获取设备对象

                BluetoothDevice d = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                // 将设备名称和地址放入array adapter，以便在ListView中显示
                String strPsw = "275271";
                if("Nexus 7".equals(d.getName())) {
                    device = d;
                    info.setText(info.getText().toString() + "\t" + (count++) + "\t" + device.getName() + "\t" + device.getAddress() + "\n");
                    try {
                        ClsUtils.setPin(device.getClass(), device, strPsw); // 手机和蓝牙采集器配对
                        ClsUtils.createBond(device.getClass(), device);
                        ClsUtils.cancelPairingUserInput(device.getClass(), device);
                        adapter.cancelDiscovery();
                        initSocket();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }
}
