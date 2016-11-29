package com.itant.musichome.fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.beardedhen.androidbootstrap.BootstrapButton;
import com.itant.musichome.MusicApplication;
import com.itant.musichome.R;
import com.itant.musichome.activity.AboutActivity;
import com.itant.musichome.activity.TaskActivity;
import com.itant.musichome.adapter.MusicAdapter;
import com.itant.musichome.bean.Music;
import com.itant.musichome.common.Constants;
import com.itant.musichome.music.advanced.AdvancedMusic;
import com.itant.musichome.music.classic.DogMusic;
import com.itant.musichome.music.classic.KmeMusic;
import com.itant.musichome.music.classic.QieMusic;
import com.itant.musichome.music.classic.XiaMusic;
import com.itant.musichome.music.classic.XiongMusic;
import com.itant.musichome.music.classic.YunMusic;
import com.itant.musichome.utils.ActivityTool;
import com.itant.musichome.utils.FileTool;
import com.itant.musichome.utils.ToastTool;
import com.umeng.analytics.MobclickAgent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.xutils.common.Callback;
import org.xutils.ex.DbException;
import org.xutils.x;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AdvancedFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemClickListener, RadioGroup.OnCheckedChangeListener {
	private ListView lv_music;
	private MusicAdapter musicAdapter;
	private List<Music> musics;

	private int index = 0;// 0小狗 1凉窝 2企鹅 3白云 4熊掌 5龙虾
	private String keyWords;// 搜索的关键字，一般为歌曲名
	private EditText et_key;

	private InputMethodManager inputMethodManager;
	private AlertDialog loadingDialog;
	private String parent;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_main, container, false);

		// 初始化文件夹目录
		initDirectory();
		parent = Constants.PATH_ADVANCED_DOG;

		inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		//Constants.MUSIC_TASKS = new HashMap<>();

		et_key = (EditText) view.findViewById(R.id.et_key);
		// 不能输入空格
		InputFilter filter = new InputFilter() {
			@Override
			public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
				//返回null表示接收输入的字符,返回空字符串表示不接受输入的字符
                /*if (TextUtils.equals(source, " ")) {
                    return "";
                }*/

				if (TextUtils.equals(source, "  ")) {
					return "";
				}
				return null;
			}
		};
		et_key.setFilters(new InputFilter[]{filter});
		// 点击回车则搜索(onSearchClicked方法有隐藏键盘)
		et_key.setOnKeyListener(new View.OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {

				if (keyCode == KeyEvent.KEYCODE_ENTER) {
					// 搜索
					onSearchClicked();
				}
				return false;
			}
		});

		lv_music = (ListView) view.findViewById(R.id.lv_music);
		musics = new ArrayList<>();
		musicAdapter = new MusicAdapter(getActivity(), musics);
		lv_music.setAdapter(musicAdapter);
		lv_music.setOnItemClickListener(this);
		musicAdapter.setOnDownloadClickListener(new MusicAdapter.OnDownloadClickListener() {
			@Override
			public void onIconClick(int position) {
				//onDownloadClick(musics.get(position));
				chooseBitRate(musics.get(position));
			}
		});

		RadioGroup rg_type = (RadioGroup) view.findViewById(R.id.rg_type);
		rg_type.setOnCheckedChangeListener(this);

		BootstrapButton bb_search = (BootstrapButton) view.findViewById(R.id.bb_search);
		bb_search.setOnClickListener(this);

		initDialog();
		return view;
	}

	private void initDialog() {
		loadingDialog = new AlertDialog.Builder(getActivity()).create();
		loadingDialog.show();
		loadingDialog.setContentView(R.layout.dialog_loading);
		loadingDialog.setCancelable(true);
		loadingDialog.setCanceledOnTouchOutside(true);
		loadingDialog.cancel();
	}

	/**
	 * 初始化文件夹目录
	 */
	private void initDirectory() {
		Constants.PATH_ADVANCED_DOG = Constants.PATH_DOWNLOAD + "advanced/dog/";
		Constants.PATH_ADVANCED_KWO = Constants.PATH_DOWNLOAD + "advanced/lwo/";
		Constants.PATH_ADVANCED_QIE = Constants.PATH_DOWNLOAD + "advanced/qie/";
		Constants.PATH_ADVANCED_YUN = Constants.PATH_DOWNLOAD + "advanced/yun/";
		Constants.PATH_ADVANCED_XIONG = Constants.PATH_DOWNLOAD + "advanced/xiong/";
		Constants.PATH_ADVANCED_XIA = Constants.PATH_DOWNLOAD + "advanced/xia/";

		File file = new File(Constants.PATH_ADVANCED_DOG);
		if (!file.exists()) {
			file.mkdirs();
		}

		file = new File(Constants.PATH_ADVANCED_KWO);
		if (!file.exists()) {
			file.mkdirs();
		}

		file = new File(Constants.PATH_ADVANCED_QIE);
		if (!file.exists()) {
			file.mkdirs();
		}

		file = new File(Constants.PATH_ADVANCED_YUN);
		if (!file.exists()) {
			file.mkdirs();
		}

		file = new File(Constants.PATH_ADVANCED_XIONG);
		if (!file.exists()) {
			file.mkdirs();
		}

		file = new File(Constants.PATH_ADVANCED_XIA);
		if (!file.exists()) {
			file.mkdirs();
		}
	}


	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.rl_about:
				// 在父类做动画
				MobclickAgent.onEvent(getActivity(), "About");// 统计关于
				ActivityTool.startActivity(getActivity(), new Intent(getActivity(), AboutActivity.class));
				break;

			case R.id.rl_task:
				MobclickAgent.onEvent(getActivity(), "Task");// 查看下载列表
				ActivityTool.startActivity(getActivity(), new Intent(getActivity(), TaskActivity.class));
				break;

			case R.id.bb_search:
				onSearchClicked();
				break;
			default:
				break;
		}
	}

	private void onSearchClicked() {
		MobclickAgent.onEvent(getActivity(), "Search");// 统计搜索次数
		// 收起软键盘并搜索
		inputMethodManager.hideSoftInputFromWindow(et_key.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS); //强制隐藏键盘
		keyWords = et_key.getText().toString().replaceAll(" ", "");
		if (TextUtils.isEmpty(keyWords)) {
			ToastTool.toastShort(getActivity(), "关键字不能为空");
			return;
		}

		// 加载中
		if (musics != null) {
			musics.clear();
		}
		musicAdapter.notifyDataSetChanged();
		loadingDialog.show();

		try {
			switch (index) {
				case 0:
					// 搜索小狗
					MobclickAgent.onEvent(getActivity(), "Dog_AD");// 搜索小狗
					AdvancedMusic.getInstance().getAdvancedSongs(musics, parent, 0, "kg", 1, keyWords);
					break;
				case 1:
					// 搜索凉我
					MobclickAgent.onEvent(getActivity(), "Kwo_AD");// 统计凉窝
					AdvancedMusic.getInstance().getAdvancedSongs(musics, parent, 1, "kw", 1, keyWords);
					break;
				case 2:
					// 搜索企鹅
					MobclickAgent.onEvent(getActivity(), "Qie_AD");// 统计企鹅
					AdvancedMusic.getInstance().getAdvancedSongs(musics, parent, 2, "qq", 1, keyWords);
					break;
				case 3:
					// 搜索白云
					MobclickAgent.onEvent(getActivity(), "Yun_AD");// 统计白云
					AdvancedMusic.getInstance().getAdvancedSongs(musics, parent, 3, "wy", 1, keyWords);
					break;
				case 4:
					// 搜索熊掌
					MobclickAgent.onEvent(getActivity(), "Xiong_AD");// 统计熊掌
					AdvancedMusic.getInstance().getAdvancedSongs(musics, parent, 4, "bd", 1, keyWords);
					break;

				case 5:
					// 搜索龙虾
					MobclickAgent.onEvent(getActivity(), "Xia_AD");// 统计龙虾
					AdvancedMusic.getInstance().getAdvancedSongs(musics, parent, 5, "xm", 1, keyWords);
					break;
				default:
					// 搜索小狗
					break;
			}
		} catch (Exception e) {
			e.printStackTrace();
			ToastTool.toastShort(getActivity(), "歌曲有误");
			loadingDialog.dismiss();
		}
	}


	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		switch (checkedId) {
			case R.id.rb_dog:
				index = 0;
				parent = Constants.PATH_ADVANCED_DOG;
				break;
			case R.id.rb_lwo:
				parent = Constants.PATH_ADVANCED_KWO;
				index = 1;
				break;
			case R.id.rb_qie:
				index = 2;
				parent = Constants.PATH_ADVANCED_QIE;
				break;
			case R.id.rb_yun:
				index = 3;
				parent = Constants.PATH_ADVANCED_YUN;
				break;

			case R.id.rb_xiong:
				index = 4;
				parent = Constants.PATH_ADVANCED_XIONG;
				break;

			case R.id.rb_xia:
				index = 5;
				parent = Constants.PATH_ADVANCED_XIA;
				break;
			default:
				index = 0;
				parent = Constants.PATH_ADVANCED_YUN;
				break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		MobclickAgent.onEvent(getActivity(), "Download");// 统计下载次数
		Music music = musics.get(position);

		chooseBitRate(music);// 选择音质

		// 弹出对话框选择地址
		//onDownloadClick(music);
	}

	/**
	 * 下载音乐
	 *
	 * @param music
	 */
	private void downloadMusic(final Music music) {
		File localFile = new File(music.getFilePath());
		if (localFile != null && localFile.exists()) {
			ToastTool.toastShort(getActivity(), "该歌曲已下载完成");
			return;
		}

		ToastTool.toastShort(getActivity(), "下载" + music.getName());
		org.xutils.http.RequestParams params = new org.xutils.http.RequestParams(music.getMp3Url());
		params.setAutoResume(true);
		params.setAutoRename(false);
		params.setSaveFilePath(music.getFilePath());
		params.setExecutor(Constants.EXECUTOR_MUSIC);
		params.setCancelFast(true);

		x.http().get(params, new Callback.ProgressCallback<File>() {

			@Override
			public void onSuccess(File result) {
				ToastTool.toastShort(MusicApplication.applicationContext, music.getName() + "下载成功");
				music.setProgress(100);
				try {
					MusicApplication.db.update(music, "progress");
				} catch (DbException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onError(Throwable ex, boolean isOnCallback) {
				ToastTool.toastShort(getActivity(), "错误：" + ex.toString());
			}

			@Override
			public void onCancelled(CancelledException cex) {

			}

			@Override
			public void onFinished() {

			}

			@Override
			public void onWaiting() {

			}

			@Override
			public void onStarted() {

			}

			@Override
			public void onLoading(long total, long current, boolean isDownloading) {
				// 更新进度
				int progress = (int) (current * 100 / total);
				music.setProgress(progress);
				try {
					MusicApplication.db.update(music, "progress");
				} catch (DbException e) {
					e.printStackTrace();
				}
			}
		});
	}

	@Override
	public void onStart() {
		super.onStart();
		EventBus.getDefault().register(this);
	}

	@Override
	public void onStop() {
		super.onStop();
		EventBus.getDefault().unregister(this);
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onMessageEvent(String event) {
		if (TextUtils.equals(event, Constants.EVENT_LOAD_COMPLETE_AD)) {
			// 停止加载动画
			loadingDialog.dismiss();
		}

		if (TextUtils.equals(event, Constants.EVENT_UPDATE_MUSICS_AD)) {
			// 刷新音乐列表
			musicAdapter.notifyDataSetChanged();
		}
	}

	/**
	 * 选择音质
	 */
	private void chooseBitRate(final Music music) {

		final AlertDialog dialog = new AlertDialog.Builder(getActivity()).create();
		dialog.getWindow().setBackgroundDrawable(new ColorDrawable());
		dialog.show();
		dialog.setContentView(R.layout.dialog_bitrate);
		dialog.setCancelable(false);
		dialog.setCanceledOnTouchOutside(false);

		LinearLayout ll_flac = (LinearLayout) dialog.findViewById(R.id.ll_flac);
		if (!TextUtils.isEmpty(music.getFlacUrl())) {
			ll_flac.setVisibility(View.VISIBLE);
			ll_flac.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {

					dialog.dismiss();
					onBitrateClicked(music, music.getFlacUrl(), "无损", ".flac");
				}
			});
		} else {
			ll_flac.setVisibility(View.GONE);
		}


		LinearLayout ll_ape = (LinearLayout) dialog.findViewById(R.id.ll_ape);
		if (!TextUtils.isEmpty(music.getApeUrl())) {
			ll_ape.setVisibility(View.VISIBLE);
			ll_ape.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					dialog.dismiss();
					onBitrateClicked(music, music.getApeUrl(), "无损", ".ape");
				}
			});
		} else {
			ll_ape.setVisibility(View.GONE);
		}


		LinearLayout ll_sq = (LinearLayout) dialog.findViewById(R.id.ll_sq);
		if (!TextUtils.isEmpty(music.getSqUrl())) {
			ll_sq.setVisibility(View.VISIBLE);
			ll_sq.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					dialog.dismiss();
					onBitrateClicked(music, music.getSqUrl(), "320", ".mp3");
				}
			});
		} else {
			ll_sq.setVisibility(View.GONE);
		}

		LinearLayout ll_hq = (LinearLayout) dialog.findViewById(R.id.ll_hq);
		if (!TextUtils.isEmpty(music.getSqUrl())) {
			ll_hq.setVisibility(View.VISIBLE);
			ll_hq.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					dialog.dismiss();
					onBitrateClicked(music, music.getHqUrl(), "192", ".mp3");
				}
			});
		} else {
			ll_hq.setVisibility(View.GONE);
		}

		LinearLayout ll_lq = (LinearLayout) dialog.findViewById(R.id.ll_lq);
		if (!TextUtils.isEmpty(music.getSqUrl())) {
			ll_hq.setVisibility(View.VISIBLE);
			ll_lq.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					dialog.dismiss();
					onBitrateClicked(music, music.getLqUrl(), "128", ".mp3");
				}
			});
		} else {
			ll_hq.setVisibility(View.GONE);
		}

		LinearLayout ll_cancel = (LinearLayout) dialog.findViewById(R.id.ll_cancel);
		ll_cancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
	}

	private void onBitrateClicked(Music music, String url, String bitrate, String extension) {
		try {
			Music dbMusic = MusicApplication.db.selector(Music.class).where("id", "=", music.getId()).findFirst();
			if (dbMusic != null) {
				ToastTool.toastShort(getActivity(), "这首歌曲已经在下载列表中了");
				return;
			}
		} catch (Exception e) {
			ToastTool.toastShort(getActivity(), "这首歌曲已经在下载列表中了");
			return;
		}

		music.setMp3Url(url);
		music.setBitrate(bitrate);
		String fileName = music.getName() + "-" + music.getSinger() + extension;

		String uniFileName = null;
		switch (music.getMusicType()) {
			case 0:
				uniFileName = FileTool.getUniqueFileName(Constants.PATH_ADVANCED_DOG, fileName, 1);
				break;
			case 1:
				uniFileName = FileTool.getUniqueFileName(Constants.PATH_ADVANCED_KWO, fileName, 1);
				break;
			case 2:
				uniFileName = FileTool.getUniqueFileName(Constants.PATH_ADVANCED_QIE, fileName, 1);
				break;
			case 3:
				uniFileName = FileTool.getUniqueFileName(Constants.PATH_ADVANCED_YUN, fileName, 1);
				break;
			case 4:
				uniFileName = FileTool.getUniqueFileName(Constants.PATH_ADVANCED_XIONG, fileName, 1);
				break;
			case 5:
				uniFileName = FileTool.getUniqueFileName(Constants.PATH_ADVANCED_XIA, fileName, 1);
				break;
			default:
				uniFileName = FileTool.getUniqueFileName(Constants.PATH_ADVANCED_DOG, fileName, 1);
				break;
		}

		music.setFileName(uniFileName);// 文件名
		// 文件路径
		String realPath = music.getFilePath() + music.getFileName();
		music.setFilePath(realPath);
		try {
			MusicApplication.db.save(music);
		} catch (DbException e) {
			e.printStackTrace();
		}

		downloadMusic(music);
	}
}
