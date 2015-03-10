package com.maxtech.mobile.metms.bt;

import android.content.Context;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;

/**
 * @author zhanghaitao
 * @date 2011-7-6
 * @version 1.0
 */
public class Util {

	private final String PREFERENCE_NAME = "gtalk";
	private Context mContext;
	private SharedPreferences mSharedPreferences;

	public static final String network[] = new String[] { "http://", "/" };

	public Util(Context context) {
		mContext = context;
		mSharedPreferences = context.getSharedPreferences(PREFERENCE_NAME,
				Context.MODE_PRIVATE);
		// Editor et = mSharedPreferences.edit();
		// et.clear();
		// et.commit();
	}

	// 显示提示信息
	public void showMsg(String msg) {
		Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
	}

	public void showMsg(int msg) {
		Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
	}

	// 保存配置信息
	public void saveString(String key, String value) {
		mSharedPreferences.edit().putString(key, value).commit();
	}

	// 获得配置信息
	public String getString(String key, String... defValue) {
		if (defValue.length > 0)
			return mSharedPreferences.getString(key, defValue[0]);
		else
			return mSharedPreferences.getString(key, "");

	}

	// 截取指定分隔符前面的字符串
	public static String getLeftString(String s, String separator) {
		int index = s.indexOf(separator);
		if (index > -1)
			return s.substring(0, index);
		else
			return s;
	}

	// 根据Uri获取实际文件名
	public String getRealPathFromURI(Uri contentUri) {
		String[] proj = { MediaStore.Images.Media.DATA };
		Cursor cursor = mContext.getContentResolver().query(contentUri, proj,
				null, null, null);
		int column_index = cursor
				.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();

		return cursor.getString(column_index);
	}


	/**
	 // 上传文件
	 public void sendFile(Uri contentUri, String toUser) throws Exception {
	 String fromUser = ConnectionUtils.connection.getUser();
	 String fileName = getRealPathFromURI(contentUri);
	 UploadResponse response = FileUtil.uploadFile(fileName, fromUser,
	 toUser);
	 // 上传成功
	 if (response != null && response.getReturnCode() == 0) {
	 CommonIQ iq = new CommonIQ();
	 iq.setNameSpace("com.maxtech.filetrans");
	 iq.addAttribute("fileId", response.getFileId() + "");
	 iq.addAttribute("fromUser", fromUser);
	 iq.addAttribute("toUser", toUser);
	 iq.addAttribute("fileName", new File(fileName).getName());
	 } else {
	 throw new Exception("文件发送失败！");
	 }
	 }

	 // 下载文件
	 public String getFile(String savePath, CommonIQ iq) throws Exception {
	 String fileId = iq.getAttribute("fileId");
	 String fromUser = iq.getAttribute("fromUser");
	 String toUser = iq.getAttribute("toUser");
	 String fileName = iq.getAttribute("fileName");
	 fileName = new Date().getTime() + "_" + fileName;
	 FileUtil.downloadFile(savePath, Long.valueOf(fileId), fileName,
	 fromUser, toUser);
	 return savePath + File.separator + fileName;
	 }

	 **/

	public final static void main(String[] args){
		char ch = 80;

		System.out.println(ch);

	}
}
