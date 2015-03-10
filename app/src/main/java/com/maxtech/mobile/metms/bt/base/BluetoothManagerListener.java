package com.maxtech.mobile.metms.bt.base;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

public interface BluetoothManagerListener {

	/**
	 * 蓝牙被打开时调用
	 */
	public void bluetoothOn();

	/**
	 * 蓝牙被关闭时调用
	 */
	public void bluetoothOff();

	/**
	 * 搜索设备时，找到一个新设备时调用
	 */
	public void searchNewDevice(BluetoothDevice device);

	/**
	 * 扫描模式发生了改变
	 *
	 * @param newMode
	 *            新的扫描模式
	 * @param oldMode
	 *            旧的扫描模式
	 */
	public void scanModeChanged(int newMode, int oldMode);

	/**
	 * 开始扫描设备
	 */
	public void startScanDevice();

	/**
	 * 结束扫描设备
	 */
	public void endScanDevice();

	/**
	 * 本地蓝牙设备的名称发生了改变
	 * @param name
	 */
	public void bluetoothNameChanged(String name);

	public void deviceBondStateChanged(BluetoothDevice device, int newState, int oldState);

	public void acceptNewSocket(BluetoothSocket socket);

}
