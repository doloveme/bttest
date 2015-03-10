package com.maxtech.mobile.metms.bt;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.kingsoft.bttest.app.R;
import com.maxtech.mobile.metms.bt.base.BluetoothManagerListener;
import com.maxtech.mobile.metms.bt.base.MBluetoothManager;

public class BluetoothActivity extends BaseMenuActivity {

	private MBluetoothManager bluetoothM = null;
	private BluetoothSocket connectServerSocket = null;

	private final int SEND_IMAGE_REQUEST_CODE = 200;

	private final int MENU_OPEN = Menu.FIRST;
	private final int MENU_CLOSE = Menu.FIRST + 1;
	private final int MENU_DISCOVERY = Menu.FIRST + 2;
	private final int MENU_SEARCH = Menu.FIRST + 3;
	private final int MENU_SEND = Menu.FIRST + 4;

	private ProgressDialog dialog = null;
	private List<BluetoothDevice> searchedDevices = new ArrayList<BluetoothDevice>();

	private ImageView image = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		init();
	}

	@Override
	protected void dealOnClick(AdapterView<?> parent, View view, int position,
							   long id) {
		if (!bluetoothM.isEnabled()) {
			Toast
					.makeText(BluetoothActivity.this, "蓝牙没有开启",
							Toast.LENGTH_SHORT).show();
			return;
		}

		if (searchedDevices.size() == 0)
			return;

		final BluetoothDevice device = searchedDevices.get(position);

		if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
			new AlertDialog.Builder(BluetoothActivity.this).setItems(
					new String[] { "进入聊天室", "发送图片" },
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							switch (which) {
								// 发送文本
								case 0:
									//sendText(device);
									Intent intent1 = new Intent();
									//intent.putExtra("testIntent", "123");
									intent1.setClass(BluetoothActivity.this, BluetoothChat.class);//设置从Activity01跳转到OtherActivity
									BluetoothActivity.this.startActivity(intent1);
									break;
								// 发送图片
								case 1:
									sendPicDevice = device;
									Intent intent = new Intent(
											Intent.ACTION_GET_CONTENT);
									intent.setType("image/*");
									startActivityForResult(Intent.createChooser(
													intent, "请选择文件..."),
											SEND_IMAGE_REQUEST_CODE);
									break;
							}
						}
					}).setPositiveButton("返回", null).setTitle("请选择操作").show();
		} else {
			Toast.makeText(BluetoothActivity.this, "配对成功后再连接...",
					Toast.LENGTH_SHORT).show();
		}
	}



	/**
	 * 发送图片
	 *
	 * @param uri
	 */
	private BluetoothDevice sendPicDevice = null;

	private void sendPicture(final Uri uri) {
		new AsyncTask<Void, Void, Boolean>() {
			private ProgressDialog progressDialog;

			@Override
			protected Boolean doInBackground(Void... arg0) {
				File file = new File(new Util(BluetoothActivity.this)
						.getRealPathFromURI(uri));
				connectServerSocket = bluetoothM
						.connectBySPPUUID(sendPicDevice);
				if (connectServerSocket != null) {
					byte[] fileLength = null;
					try {
						connectServerSocket.connect();
						OutputStream out = connectServerSocket
								.getOutputStream();
						BufferedOutputStream bufferOut = new BufferedOutputStream(
								out);

						fileLength = int2bytes(new Long(file.length())
								.intValue());

						FileInputStream in = new FileInputStream(file);
						BufferedInputStream bufferIn = new BufferedInputStream(
								in);
						bufferOut.write(1);
						bufferOut.write(fileLength);

						byte[] b = new byte[512];
						int le = -1;
						while((le = bufferIn.read(b)) != -1) {
							bufferOut.write(b, 0, le);
							bufferOut.flush();
						}
//						bufferIn.read(buffer);
//						bufferOut.write(buffer);
//						bufferOut.flush();
					} catch (Exception e) {
						e.printStackTrace();
						return false;
					}
					return true;
				}
				return false;
			}

			@Override
			protected void onPostExecute(Boolean result) {
				progressDialog.dismiss();
				if (result) {
					Toast.makeText(BluetoothActivity.this, "文件发送成功！",
							Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(BluetoothActivity.this, "文件发送失败！",
							Toast.LENGTH_SHORT).show();
				}
				super.onPostExecute(result);
			}

			@Override
			protected void onPreExecute() {
				progressDialog = ProgressDialog.show(BluetoothActivity.this,
						"提示", "正在发送文件...");
				progressDialog.setCancelable(true);
				super.onPreExecute();
			}

		}.execute();
	}

	private int bytes2int(byte[] bytes) {
		int num = bytes[0] & 0xFF;
		num |= ((bytes[1] << 8) & 0xFF00);
		num |= ((bytes[2] << 16) & 0xFF0000);
		num |= ((bytes[3] << 24) & 0xFF000000);
		return num;
	}

	private byte[] int2bytes(int i) {
		byte[] b = new byte[4];

		b[0] = (byte) (0xff & i);
		b[1] = (byte) ((0xff00 & i) >> 8);
		b[2] = (byte) ((0xff0000 & i) >> 16);
		b[3] = (byte) ((0xff000000 & i) >> 24);
		return b;
	}

	@Override
	protected int getItemIconVisable() {
		return View.GONE;
	}

	@Override
	protected List<ItemAdapter> initMenuItem() {
		Object[][] fixedmenu = { { -1, "没有蓝牙设备", -1, null } };
		return bulidMenuItemList(fixedmenu);
	}

	private void init() {
		image = (ImageView) findViewById(R.id.image);

		bluetoothM = new MBluetoothManager(BluetoothActivity.this,
				new BluetoothListener());

		menuListView
				.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

					public boolean onItemLongClick(AdapterView<?> arg0,
												   View view, int position, long arg3) {
						BluetoothDevice device = null;
						if (position < searchedDevices.size()) {
							device = searchedDevices.get(position);
						}
						if (device != null) {
							if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
								try {
									Toast.makeText(BluetoothActivity.this,
											"取消配对...", Toast.LENGTH_SHORT)
											.show();
									bluetoothM.unpair(device);
								} catch (Exception e) {
								}
							} else if (device.getBondState() == BluetoothDevice.BOND_NONE) {
								try {
									Toast.makeText(BluetoothActivity.this,
											"开始配对...", Toast.LENGTH_SHORT)
											.show();
									bluetoothM.pair(device);
								} catch (Exception e) {
								}
							} else if (device.getBondState() == BluetoothDevice.BOND_BONDING) {
								Toast.makeText(BluetoothActivity.this,
										"正在配对...", Toast.LENGTH_SHORT).show();
							}
						}
						return true;
					}
				});

		dialog = new ProgressDialog(BluetoothActivity.this);

		if (bluetoothM.isEnabled()) {
			initServer();
		}

		searchedDevices = bluetoothM.getBondDevices();
		if (searchedDevices == null) {
			searchedDevices = new ArrayList<BluetoothDevice>();
		} else {
			refreshMenuItems(doAdapter());
		}
	}

	private Object[][] doAdapter() {
		if (searchedDevices.size() == 0)
			return null;
		Object[][] items = new Object[searchedDevices.size()][4];
		for (int i = 0; i < searchedDevices.size(); i++) {
			BluetoothDevice device = searchedDevices.get(i);
			items[i][0] = -1;
			String bondState = "";
			switch (device.getBondState()) {
				case BluetoothDevice.BOND_BONDING:
					bondState = "正在配对...";
					break;
				case BluetoothDevice.BOND_BONDED:
					bondState = "已配对";
					break;
				case BluetoothDevice.BOND_NONE:
					bondState = "没有配对";
					break;
			}
			items[i][1] = device.getName() + "\n" + bondState;
			items[i][2] = -1;
			items[i][3] = null;
		}
		return items;
	}

	/**
	 * 创建菜单
	 *
	 * @param menu
	 */
	private void createMenu(Menu menu) {
		menu.clear();
		// 打开蓝牙
		MenuItem openItem = menu.add(0, MENU_OPEN, 0, R.string.open_bluetooth);

		// 关闭蓝牙
		MenuItem closeItem = menu.add(0, MENU_CLOSE, 0,
				R.string.close_bluetooth);

		// 蓝牙打开状态
		if (bluetoothM.isEnabled()) {
			openItem.setVisible(false);
		}
		// 蓝牙关闭状态
		else {
			closeItem.setVisible(false);
		}

		// 搜索设备菜单
		MenuItem searchItem = menu.add(0, MENU_SEARCH, 0,
				R.string.search_device);
		searchItem.setEnabled(bluetoothM.isSearch());

		// 发送文件
		// MenuItem sendItem = menu.add(0, MENU_SEND, 0, R.string.send_file);
		// sendItem.setEnabled(bluetoothM.isSend());

		// 可检测性
		MenuItem checkedItem = menu.add(0, MENU_DISCOVERY, 0,
				R.string.checked_enable);
		checkedItem.setEnabled(bluetoothM.isDiscovery());

	}

	/**
	 * 菜单项被点击
	 *
	 * @param item
	 */
	private void menuItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			// 打开蓝牙
			case MENU_OPEN:
				dialog.setCancelable(false);
				dialog.setTitle("请稍等");
				dialog.setMessage("正在打开蓝牙...");
				dialog.show();
				bluetoothM.enable();
				break;
			// 关闭蓝牙
			case MENU_CLOSE:
				dialog.setCancelable(false);
				dialog.setTitle("请稍等");
				dialog.setMessage("正在关闭蓝牙...");
				dialog.show();
				bluetoothM.disable();
				break;
			// 设置可检测性
			case MENU_DISCOVERY:
				bluetoothM.setBluetoothDiscovering(120);
				break;
			// 搜索设备
			case MENU_SEARCH:
				searchedDevices.clear();
				refreshMenuItems(null);
				bluetoothM.searchDevice();
				break;
			// 发送文件
			case MENU_SEND:
				// bluetoothM.sendFiles();
				new Thread() {
					public void run() {
						if (connectServerSocket != null) {
							try {
								connectServerSocket.connect();
								OutputStream out = connectServerSocket
										.getOutputStream();
								String name = "hello world";
								out.write(name.getBytes());
								out.flush();
								out.close();
								connectServerSocket.close();
							} catch (IOException e) {
								e.printStackTrace();
							}

						}
					}
				}.start();
				break;
		}
	}

	@Override
	protected void onPause() {
		bluetoothM.unregisterBluetooth();
		super.onPause();
	}

	@Override
	protected void onResume() {
		bluetoothM.registerBluetooth();
		super.onResume();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			doExit();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		menuItemSelected(item);
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		createMenu(menu);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		/**
		 * 调用startActivityForResult(intent, int)打开蓝牙
		 * 如果蓝牙被打开，则resultCode以resule_ok返回 用户拒绝，则resultCode以resule_canceled返回
		 * 每当蓝牙的状态发生变化，都可以在BroadcastReceiver中监听ACTION_STATE_CHANGED的值变化
		 */
		if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
			Toast.makeText(BluetoothActivity.this, "打开成功", Toast.LENGTH_SHORT)
					.show();
		} else if (requestCode == SEND_IMAGE_REQUEST_CODE
				&& resultCode == Activity.RESULT_OK) {
			sendPicture(data.getData());
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	private void initServer() {
		try {
			bluetoothM.initServer();
			Toast.makeText(BluetoothActivity.this, "服务初始化", Toast.LENGTH_SHORT)
					.show();
		} catch (IOException e1) {
			Toast.makeText(BluetoothActivity.this, "服务初始化失败",
					Toast.LENGTH_SHORT).show();
		}
	}

	private void destroyServer() {
		Toast.makeText(BluetoothActivity.this, "服务销毁", Toast.LENGTH_SHORT)
				.show();
		bluetoothM.destroyServer();
	}

	private class BluetoothListener implements BluetoothManagerListener {

		public void bluetoothOn() {
			dialog.dismiss();
			initServer();
		}

		public void bluetoothOff() {
			dialog.dismiss();
			destroyServer();
		}

		public void searchNewDevice(BluetoothDevice device) {
			for (BluetoothDevice d : searchedDevices) {
				if (d.getName() != null && d.getName().equals(device.getName()))
					searchedDevices.remove(d);
				searchedDevices.add(device);
				refreshMenuItems(doAdapter());
				return;
			}
			searchedDevices.add(device);
			refreshMenuItems(doAdapter());
		}

		public void scanModeChanged(int newMode, int oldMode) {

		}

		public void startScanDevice() {
			dialog.setCancelable(true);
			dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

				public void onCancel(DialogInterface dialog) {
					dialog.dismiss();
					bluetoothM.cancelDiscovery();
				}
			});
			dialog.setMessage("正在扫描...");
			dialog.show();
		}

		public void endScanDevice() {
			dialog.dismiss();
		}

		public void bluetoothNameChanged(String name) {

		}

		public void deviceBondStateChanged(BluetoothDevice device,
										   int newState, int oldState) {
			for (BluetoothDevice searchedDevice : searchedDevices) {
				if (device.getAddress().equals(searchedDevice.getAddress())) {
					searchedDevice = device;
					refreshMenuItems(doAdapter());
					return;
				}
			}
			searchedDevices.add(device);
			refreshMenuItems(doAdapter());
		}

		public void acceptNewSocket(final BluetoothSocket socket) {
			new Thread() {
				public void run() {
					InputStream in = null;
					FileOutputStream out = null;
					try {
						in = socket.getInputStream();

						int type = in.read();
						switch (type) {
							case 0:
								byte[] buffer = new byte[512];
								in.read(buffer);
								List<Byte> list = new ArrayList<Byte>();
								for (int i = 0; i < buffer.length; i++) {
									if (buffer[i] != 0) {
										list.add(buffer[i]);
									} else {
										break;
									}
								}
								buffer = new byte[list.size()];
								for (int i = 0; i < list.size(); i++) {
									buffer[i] = list.get(i);
								}

								Message msg = handler.obtainMessage();
								msg.what = 0;
								msg.obj = new String(buffer);

								msg.sendToTarget();
								break;
							case 1:
								byte[] fileLength = new byte[4];
								in.read(fileLength);

								byte[] b = new byte[512];
								int le = -1;
								ByteArrayOutputStream o = new ByteArrayOutputStream();
								int readle = 0;
								while((le = in.read(b)) != -1) {
									o.write(b, 0, le);
									readle += le;
									if(readle == bytes2int(fileLength))
										break;
								}
								o.flush();

								msg = handler.obtainMessage();
								msg.what = 1;
								msg.obj = o.toByteArray();
								msg.sendToTarget();
								break;
						}

					} catch (IOException e) {
						e.printStackTrace();
					} finally {
						try {
							if (in != null)
								in.close();
							if (out != null)
								out.close();
							socket.close();
						} catch (Exception e2) {
						}
					}
				}
			}.start();
		}

	}

	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case 0:
					Toast.makeText(BluetoothActivity.this, msg.obj.toString(),
							Toast.LENGTH_SHORT).show();
					break;
				case 1:
					byte[] imagebytes = (byte[]) msg.obj;
					Bitmap bitmap = BitmapFactory.decodeByteArray(imagebytes, 0,
							imagebytes.length);
					image.setImageBitmap(bitmap);
					File file = new File("/sdcard/heihei.jpg");
					FileOutputStream out;
					try {
						out = new FileOutputStream(file);
						out.write(imagebytes);
						out.flush();
						out.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
					Toast.makeText(BluetoothActivity.this, "接收成功",
							Toast.LENGTH_SHORT).show();
					break;
			}
			super.handleMessage(msg);
		}

	};

}