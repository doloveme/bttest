package com.maxtech.mobile.metms.bt;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.kingsoft.bttest.app.R;


/**
 * 主菜单
 *
 */
public class BaseMenuActivity extends Activity {

	private MenuAdapter la = null;
	protected ListView menuListView = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.menu_main);
		init();
	}

	// 初始化
	private void init() {

		menuListView = (ListView) findViewById(R.id.menuListView);
		la = new MenuAdapter(menuListView.getContext(),
				initMenuItem());
		menuListView.setAdapter(la);
		menuListView.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view,
									int position, long id) {
				dealOnClick(parent, view, position, id);
			}
		});
	}

	// 处理列表单击事件
	protected void dealOnClick(AdapterView<?> parent, View view, int position,
							   long id) {
		ItemAdapter adapter = (ItemAdapter) view.getTag();
		Class<Object> startClazz = adapter.clazz;
		if (startClazz != null) {
			startActivity(new Intent(this, startClazz));
		} else {
			// 找不到类时，则退出
			finish();
		}
	}

	// 退出程序
	protected void doExit() {
		new AlertDialog.Builder(this)
				.setMessage(getString(R.string.exit_message))
				.setPositiveButton(getString(R.string.confirm),
						new DialogInterface.OnClickListener() {
							public void onClick(
									DialogInterface dialoginterface, int i) {
								finish();
							}
						})
				.setNeutralButton(getString(R.string.cancel),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface arg0, int arg1) {
							}

						}).show();

	}

	// 组装菜单数据
	@SuppressWarnings("unchecked")
	protected List<ItemAdapter> bulidMenuItemList(Object[][] fixedmenu) {
		List<ItemAdapter> list = new ArrayList<ItemAdapter>();
		if(fixedmenu == null)
			return list;
		int count = fixedmenu.length;
		for (int i = 0; i < count; i++) {
			ItemAdapter ia = new ItemAdapter();
			ia.img = (Integer) fixedmenu[i][0];
			if(fixedmenu[i][1] instanceof Integer) {
				ia.title = getString((Integer) fixedmenu[i][1]);
			}else if(fixedmenu[i][1] instanceof String) {
				ia.title = fixedmenu[i][1]+"";
			}
			ia.info = (Integer) fixedmenu[i][2];
			ia.clazz = (Class<Object>) fixedmenu[i][3];
			list.add(ia);
		}
		return list;
	}

	/**
	 * ListView ITEM适配器
	 *
	 * @author Administrator
	 *
	 */

	public final class ItemAdapter {
		public ImageView imgId;
		public TextView titleId;
		public int img;
		public String title;
		public int info;
		public Class<Object> clazz;
	}

	protected int getItemIconVisable() {
		return View.VISIBLE;
	}

	public class MenuAdapter extends BaseAdapter {

		private LayoutInflater mInflater;

		public List<ItemAdapter> itemList;

		@SuppressWarnings("unchecked")
		public MenuAdapter(Context context, Object ItemList) {
			this.mInflater = LayoutInflater.from(context);
			this.itemList = (List<ItemAdapter>) ItemList;
		}

		public void setItems(List<ItemAdapter> items) {
			itemList = items;
		}

		public int getCount() {
			return itemList.size();
		}

		public Object getItem(int position) {
			return position;
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ItemAdapter holder = null;
			if (convertView == null) {
				holder = new ItemAdapter();
				convertView = mInflater.inflate(R.layout.menu_item, null);
				holder.imgId = (ImageView) convertView
						.findViewById(R.id.menu_img);
				holder.titleId = (TextView) convertView
						.findViewById(R.id.menu_title);
				convertView.setTag(holder);
			} else {
				holder = (ItemAdapter) convertView.getTag();
			}

			convertView.findViewById(R.id.menuItemIcon).setVisibility(getItemIconVisable());

			if(itemList.get(position).img == -1) {
				holder.imgId.setVisibility(View.GONE);
			}else {
				holder.img = itemList.get(position).img;
				holder.imgId.setImageResource(holder.img);
			}
			holder.titleId.setText(itemList.get(position).title);
			holder.clazz = itemList.get(position).clazz;
			return convertView;
		}
	}

	// 这里初始化主页面菜单项
	protected List<ItemAdapter> initMenuItem() {
		/**
		 * List Item图片及相关描述
		 */
		Object[][] fixedmenu = null;
		return bulidMenuItemList(fixedmenu);
	}

	protected void refreshMenuItems(Object[][] items) {
		la.setItems(bulidMenuItemList(items));
		la.notifyDataSetChanged();
	}

}
