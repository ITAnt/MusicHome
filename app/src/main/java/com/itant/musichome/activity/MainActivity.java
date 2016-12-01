package com.itant.musichome.activity;

import java.util.ArrayList;
import java.util.List;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.itant.musichome.R;
import com.itant.musichome.adapter.FragmentAdapter;
import com.itant.musichome.fragment.AdvancedFragment;
import com.itant.musichome.fragment.ClassicFragment;
import com.itant.musichome.utils.ActivityTool;
import com.itant.musichome.utils.UITool;
import com.shizhefei.view.indicator.Indicator;
import com.shizhefei.view.indicator.IndicatorViewPager;
import com.shizhefei.view.indicator.slidebar.LayoutBar;
import com.shizhefei.view.indicator.slidebar.ScrollBar;
import com.shizhefei.view.indicator.transition.OnTransitionTextListener;
import com.umeng.analytics.MobclickAgent;

public class MainActivity extends FragmentActivity implements View.OnClickListener {
	private ViewPager ssvp;
	private FragmentAdapter mAdapter;
	private List<Fragment> mFragments;

	private LayoutInflater inflate;
	private IndicatorViewPager indicatorViewPager;

	private RelativeLayout rl_about;
	private RelativeLayout rl_task;

	private static String[] REQUIRED_PERMISSIONS = {
			Manifest.permission.READ_EXTERNAL_STORAGE,
			Manifest.permission.WRITE_EXTERNAL_STORAGE
	};

	/**
	 * 初始化权限
	 */
	private void initPermission() {
		int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
		if (permission != PackageManager.PERMISSION_GRANTED) {
			// We don't have permission so prompt the user
			ActivityCompat.requestPermissions(
					this,
					REQUIRED_PERMISSIONS,
					1
			);
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == 1) {
			int grantResult = grantResults[0];
			boolean granted = grantResult == PackageManager.PERMISSION_GRANTED;

			if (!granted) {
				MobclickAgent.onEvent(this, "Permission");// 统计权限拒绝次数

				final AlertDialog dialog = new AlertDialog.Builder(this).create();
				dialog.show();
				dialog.setContentView(R.layout.dialog_permission);
				dialog.setCancelable(false);
				dialog.setCanceledOnTouchOutside(false);

				BootstrapButton bb_confirm = (BootstrapButton) dialog.findViewById(R.id.bb_confirm);
				bb_confirm.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						dialog.cancel();
						// 退出
						System.exit(0);
					}
				});
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);


		// 申请6.0的权限，如果拒绝了，则退出应用
		if (android.os.Build.VERSION.SDK_INT >= 23) {
			initPermission();
		}

		inflate = LayoutInflater.from(this);
		rl_about = (RelativeLayout) findViewById(R.id.rl_about);
		rl_about.setOnClickListener(this);
		rl_task = (RelativeLayout) findViewById(R.id.rl_task);
		rl_task.setOnClickListener(this);

		ssvp = (ViewPager) findViewById(R.id.ssvp);
		mFragments = new ArrayList<>();
		mFragments.add(new AdvancedFragment());
		mFragments.add(new ClassicFragment());

		mAdapter = new FragmentAdapter(getSupportFragmentManager(), mFragments);
		ssvp.setAdapter(mAdapter);
		ssvp.setOffscreenPageLimit(2);


		Indicator fiv_first_fragment = (Indicator) findViewById(R.id.fiv_first_fragment);
		fiv_first_fragment.setScrollBar(new LayoutBar(this, R.layout.layout_slide_bar, ScrollBar.Gravity.BOTTOM_FLOAT));
		float unSelectSize = UITool.dpToPx(this, 5);
		float selectSize = unSelectSize * 1.05f;

		int selectColor = getResources().getColor(R.color.bg_btn);
		int unSelectColor = getResources().getColor(R.color.white);
		fiv_first_fragment.setOnTransitionListener(new OnTransitionTextListener().setColor(selectColor, unSelectColor).setSize(selectSize, unSelectSize));
		indicatorViewPager = new IndicatorViewPager(fiv_first_fragment, ssvp);
		indicatorViewPager.setAdapter(new MyAdapter(getSupportFragmentManager()));
		indicatorViewPager.setOnIndicatorPageChangeListener(new IndicatorViewPager.OnIndicatorPageChangeListener() {
			@Override
			public void onIndicatorPageChange(int preItem, int currentItem) {
				if (preItem != currentItem) {
					switch (currentItem) {
						case 0:

							break;
						case 1:

							break;
						default:
							break;
					}
				}
			}
		});

		TextView tv_classic = (TextView) findViewById(R.id.tv_classic);
		tv_classic.setOnClickListener(this);
		TextView tv_advanced = (TextView) findViewById(R.id.tv_advanced);
		tv_advanced.setOnClickListener(this);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		moveTaskToBack(true);// true对任何Activity都适用
        /*Intent mHomeIntent = new Intent(Intent.ACTION_MAIN, null);
        mHomeIntent.addCategory(Intent.CATEGORY_HOME);
        mHomeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        startActivity(mHomeIntent);*/
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.rl_about:
				// 在父类做动画
				MobclickAgent.onEvent(this, "About");// 统计关于
				ActivityTool.startActivity(this, new Intent(this, AboutActivity.class));
				break;

			case R.id.rl_task:
				MobclickAgent.onEvent(this, "Task");// 查看下载列表
				ActivityTool.startActivity(this, new Intent(this, TaskActivity.class));
				break;

			case R.id.tv_advanced:
				indicatorViewPager.setCurrentItem(0, true);
				break;

			case R.id.tv_classic:
				indicatorViewPager.setCurrentItem(1, true);
				break;

			default:
				break;
		}
	}

	String[] topTabTitles = {"", ""};
	/**
	 * 没有标题，只有箭头的viewpagerindicator
	 */
	private class MyAdapter extends IndicatorViewPager.IndicatorFragmentPagerAdapter {

		public MyAdapter(FragmentManager fragmentManager) {
			super(fragmentManager);
		}

		@Override
		public int getCount() {
			return topTabTitles.length;
		}

		@Override
		public View getViewForTab(int position, View convertView, ViewGroup container) {
			try {
				if (convertView == null) {
					convertView = inflate.inflate(R.layout.tab_top, container, false);
				}
				TextView textView = (TextView) convertView;
				textView.setText(topTabTitles[position]);


				/*int witdh = UITool.getTextWidth(textView);
				int padding = UITool.dip2px(getApplicationContext(), 8);
				//因为wrap的布局 字体大小变化会导致textView大小变化产生抖动，这里通过设置textView宽度就避免抖动现象
				//1.3f是根据上面字体大小变化的倍数1.3f设置
				textView.setWidth((int) (witdh * 1.3f) + padding);*/
			} catch (Exception e) {
				e.printStackTrace();
			}

			return convertView;
		}

		@Override
		public Fragment getFragmentForPage(int position) {
			return mFragments.get(position);
		}
	}
}
