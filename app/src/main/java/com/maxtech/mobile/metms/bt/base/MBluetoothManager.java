package com.maxtech.mobile.metms.bt.base;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class MBluetoothManager {

	private Activity context;
	private BluetoothManagerListener listener = null;
	public BluetoothAdapter bluetoothAdapter = null;
	public BluetoothServerSocket serverSocket = null;

	@SuppressWarnings("unused")
	private final String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";

	public MBluetoothManager(Activity context, BluetoothManagerListener listener) {
		this.context = context;
		this.listener = listener;
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	}

	/**
	 * 打开蓝牙后，获得已经配对的设备
	 */
	public List<BluetoothDevice> getBondDevices() {
		Set<BluetoothDevice> set = bluetoothAdapter.getBondedDevices();
		List<BluetoothDevice> bondDevices = new ArrayList<BluetoothDevice>();

		if (set.size() == 0)
			return null;

		Iterator<BluetoothDevice> i = set.iterator();
		while (i.hasNext())
			bondDevices.add(i.next());

		return bondDevices;
	}

	public boolean isEnabled() {
		return bluetoothAdapter.isEnabled();
	}

	/**
	 * 是否可以发送文件
	 *
	 * @return
	 */
	public boolean isSend() {
		if (!bluetoothAdapter.isEnabled())
			return false;
		return true;
	}

	/**
	 * 是否可以搜索设备
	 *
	 * @return
	 */
	public boolean isSearch() {
		if (!bluetoothAdapter.isEnabled())
			return false;

		return true;
	}

	/**
	 * 是否可以被检测
	 *
	 * @return true:当前menuitem可以操作
	 */
	public boolean isDiscovery() {
		/**
		 * 蓝牙状态不是STATE_ON，返回false 如果正在查找，返回true
		 */

		if (!bluetoothAdapter.isEnabled())
			return false;

		// 可连接不可发现
		if (bluetoothAdapter.getScanMode() == BluetoothAdapter.SCAN_MODE_CONNECTABLE)
			return true;

		// 可连接可发现
		if (bluetoothAdapter.getScanMode() == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE)
			return false;

		return true;
	}

	/**
	 * 打开蓝牙，不提示用户操作
	 *
	 * @return
	 */
	public boolean enable() {
		return bluetoothAdapter.enable();
	}

	/**
	 * 调用Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)请求用户打开并获得结果
	 * 调用startActivityForResult(intent, int)打开蓝牙 如果蓝牙被打开，则resultCode以resule_ok返回
	 * 用户拒绝，则resultCode以resule_canceled返回
	 * 每当蓝牙的状态发生变化，都可以在BroadcastReceiver中监听ACTION_STATE_CHANGED的值变化
	 */
	public void enableByTip() {
		Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		context.startActivityForResult(intent, 0);
	}

	/**
	 * 关闭蓝牙
	 *
	 * @return
	 */
	public boolean disable() {
		return bluetoothAdapter.disable();
	}

	/**
	 * 设置bluetooth为可被其他设备检测
	 *
	 * @param duration
	 *            可被检测持续时间
	 * @return
	 */
	public boolean setBluetoothDiscovering(int duration) {
		if (!isDiscovery())
			return false;

		Intent setDiscovery = new Intent(
				BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		setDiscovery.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,
				duration);
		context.startActivity(setDiscovery);
		return true;
	}

	/**
	 * 搜索可见的其他蓝牙设备
	 *
	 * @return
	 */
	public boolean searchDevice() {
		if (!isSearch())
			return false;
		/**
		 * 调用的是一个异步的线程查找周围可见设备 整个过程持续大概12秒
		 */
		return bluetoothAdapter.startDiscovery();
	}

	/**
	 * 如果正在查询扫描设备，则停止扫描
	 *
	 * @return
	 */
	public boolean cancelDiscovery() {
		if (bluetoothAdapter.isDiscovering())
			return bluetoothAdapter.cancelDiscovery();
		return false;
	}

	/**
	 * 建立配对 通过隐藏API经过反射配对远程蓝牙设备
	 *
	 * @param remoteDevice
	 * @return
	 */
	public boolean pair(BluetoothDevice remoteDevice) throws Exception {

		if (remoteDevice.getBondState() == BluetoothDevice.BOND_NONE) {
			Method createBond = BluetoothDevice.class.getMethod("createBond");
			return (Boolean) createBond.invoke(remoteDevice);
		}

		return false;
	}

	/**
	 * 取消配对 通过隐藏API经过反射取消配对远程蓝牙设备
	 *
	 * @param remoteDevice
	 * @return
	 * @throws Exception
	 */
	public boolean unpair(BluetoothDevice remoteDevice) throws Exception {

		if (remoteDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
			Method createBond = BluetoothDevice.class.getMethod("removeBond");
			return (Boolean) createBond.invoke(remoteDevice);
		}

		return false;
	}

	/**
	 * 跟这个设备建立一个socket连接
	 *
	 * @param remoteDevice
	 * @return
	 */
	public BluetoothSocket connectBySPPUUID(BluetoothDevice remoteDevice) {
		BluetoothSocket socket = null;
		try {
			// socket =
			// remoteDevice.createRfcommSocketToServiceRecord(UUID.fromString(SPP_UUID));
			socket = remoteDevice.createRfcommSocketToServiceRecord(UUID
					.fromString("6c84fc17-1b86-4695-86b8-41106014b4f8"));
			return socket;
		} catch (IOException e) {
			return null;
		}
	}

	private ServerSocketThread thread = null;
	public void initServer() throws IOException {
		serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(
				"test server",
				UUID.fromString("6c84fc17-1b86-4695-86b8-41106014b4f8"));
		thread = new ServerSocketThread();
		thread.start();
	}

	public void destroyServer() {
		if(thread != null) {
			if(thread.isInterrupted())
				thread.interrupt();
		}
		thread = null;
		serverSocket = null;
	}

	private class ServerSocketThread extends Thread {

		@Override
		public void run() {
			while(true) {
				try {
					BluetoothSocket socket = serverSocket.accept();
					if(listener != null)
						listener.acceptNewSocket(socket);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

	public void sendFiles() {
	}

	public void destory() {
		bluetoothAdapter = null;
	}

	/**
	 * 注册蓝牙事件广播监听
	 */
	public void registerBluetooth() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(BluetoothDevice.ACTION_FOUND);
		filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		context.registerReceiver(bluetoothReceiver, filter);
	}

	/**
	 * 反注册蓝牙事件广播监听
	 */
	public void unregisterBluetooth() {
		context.unregisterReceiver(bluetoothReceiver);
	}

	private BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (listener == null)
				return;
			String action = intent.getAction();

			/**
			 * 调用异步的线程查找周围设备 每次找到设备都会发布广播
			 */
			if (action.equals(BluetoothDevice.ACTION_FOUND)) {
				BluetoothDevice newDevice = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				listener.searchNewDevice(newDevice);
			}

			/**
			 * 蓝牙的状态发生了改变 包含两个属性EXTRA_STATE和EXTRA_PREVIOUS_STATE
			 * EXTRA_PREVIOUS_STATE是上一次的状态 EXTRA_STATE可能值有STATE_OFF,
			 * STATE_TURNING_ON, STATE_ON, STATE_TURNING_OFF
			 */
			if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
				switch (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)) {
					case BluetoothAdapter.STATE_ON:
						listener.bluetoothOn();
						break;
					case BluetoothAdapter.STATE_OFF:
						listener.bluetoothOff();
						break;
				}
			}

			/**
			 * 蓝牙设备开始查询扫描周边设备
			 */
			if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)) {
				listener.startScanDevice();
			}

			/**
			 * 蓝牙设备结束查询扫描周边设备
			 */
			if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
				listener.endScanDevice();
			}

			/**
			 * 蓝牙设备名称发生改变
			 */
			if (action.equals(BluetoothAdapter.ACTION_LOCAL_NAME_CHANGED)) {
				listener.bluetoothNameChanged(intent
						.getStringExtra(BluetoothAdapter.EXTRA_LOCAL_NAME));
			}

			/**
			 * SCAN_MODE_CONNECTABLE：可以被曾经连接过的设备连接，但是不能被未曾连接的设备扫描到
			 * SCAN_MODE_CONNECTABLE_DISCOVERABLE：既可以被扫描到，还能被连接
			 * SCAN_MODE_NONE：既不能被扫描到，也不能被连接
			 */
			if (action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {
				listener.scanModeChanged(intent.getIntExtra(
						BluetoothAdapter.EXTRA_SCAN_MODE, -1), intent
						.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_SCAN_MODE,
								-1));
			}

			/**
			 * 当一个设备的绑定状态发生改变时 总是包含EXTRA_DEVICE, EXTRA_BOND_STATE和
			 * EXTRA_PREVIOUS_BOND_STATE这些域值
			 */
			if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
				BluetoothDevice device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				int newState = intent.getIntExtra(
						BluetoothDevice.EXTRA_BOND_STATE, -1);
				int oldState = intent.getIntExtra(
						BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, -1);
				listener.deviceBondStateChanged(device, newState, oldState);
			}

		}
	};

}
