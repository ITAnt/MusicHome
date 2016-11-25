package com.itant.musichome.fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
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
import android.widget.ListView;
import android.widget.RadioGroup;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.beardedhen.androidbootstrap.BootstrapButton;
import com.itant.musichome.MusicApplication;
import com.itant.musichome.R;
import com.itant.musichome.adapter.MusicAdapter;
import com.itant.musichome.bean.Music;
import com.itant.musichome.common.Constants;
import com.itant.musichome.music.classic.DogMusic;
import com.itant.musichome.music.classic.KmeMusic;
import com.itant.musichome.music.classic.QieMusic;
import com.itant.musichome.music.classic.XiaMusic;
import com.itant.musichome.music.classic.XiongMusic;
import com.itant.musichome.music.classic.YunMusic;
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

public class ClassicFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemClickListener, RadioGroup.OnCheckedChangeListener {

	private ListView lv_music;
	private MusicAdapter musicAdapter;
	private List<Music> musics;



	private int index = 0;// 0小狗 1凉窝 2企鹅 3白云 4熊掌 5龙虾
	private String keyWords;// 搜索的关键字，一般为歌曲名
	private EditText et_key;

	private InputMethodManager inputMethodManager;
	private AlertDialog loadingDialog;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_main, container, false);

		// 初始化文件夹目录
		initDirectory();

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
				onDownloadClick(musics.get(position));
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
		Constants.PATH_CLASSIC_DOG = Constants.PATH_DOWNLOAD + "classic/dog/";
		Constants.PATH_CLASSIC_KWO = Constants.PATH_DOWNLOAD + "classic/lwo/";
		Constants.PATH_CLASSIC_QIE = Constants.PATH_DOWNLOAD + "classic/qie/";
		Constants.PATH_CLASSIC_YUN = Constants.PATH_DOWNLOAD + "classic/yun/";
		Constants.PATH_CLASSIC_XIONG = Constants.PATH_DOWNLOAD + "classic/xiong/";
		Constants.PATH_CLASSIC_XIA = Constants.PATH_DOWNLOAD + "classic/xia/";

		File file = new File(Constants.PATH_CLASSIC_DOG);
		if (!file.exists()) {
			file.mkdirs();
		}

		file = new File(Constants.PATH_CLASSIC_KWO);
		if (!file.exists()) {
			file.mkdirs();
		}

		file = new File(Constants.PATH_CLASSIC_QIE);
		if (!file.exists()) {
			file.mkdirs();
		}

		file = new File(Constants.PATH_CLASSIC_YUN);
		if (!file.exists()) {
			file.mkdirs();
		}

		file = new File(Constants.PATH_CLASSIC_XIONG);
		if (!file.exists()) {
			file.mkdirs();
		}

		file = new File(Constants.PATH_CLASSIC_XIA);
		if (!file.exists()) {
			file.mkdirs();
		}
	}


	@Override
	public void onClick(View v) {
		switch (v.getId()) {


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
					MobclickAgent.onEvent(getActivity(), "Dog");// 搜索小狗
					DogMusic.getInstance().getDogSongs(musics, keyWords);
					break;
				case 1:
					// 搜索凉我
					MobclickAgent.onEvent(getActivity(), "Kwo");// 统计凉窝
					KmeMusic.getInstance().getDogSongs(musics, keyWords);
					break;
				case 2:
					// 搜索企鹅
					MobclickAgent.onEvent(getActivity(), "Qie");// 统计企鹅
					QieMusic.getInstance().getQieSongs(musics, keyWords);
					break;
				case 3:
					// 搜索白云
					MobclickAgent.onEvent(getActivity(), "Yun");// 统计白云
					YunMusic.getInstance().getYunSongs(musics, keyWords);
					break;
				case 4:
					// 搜索熊掌
					MobclickAgent.onEvent(getActivity(), "Xiong");// 统计熊掌
					XiongMusic.getInstance().getXiongSongs(musics, keyWords);
					break;

				case 5:
					// 搜索龙虾
					MobclickAgent.onEvent(getActivity(), "Xia");// 统计龙虾
					XiaMusic.getInstance().getXiaSongs(musics, keyWords);
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
				break;
			case R.id.rb_lwo:
				index = 1;
				break;
			case R.id.rb_qie:
				index = 2;
				break;
			case R.id.rb_yun:
				index = 3;
				break;

			case R.id.rb_xiong:
				index = 4;
				break;

			case R.id.rb_xia:
				index = 5;
				break;
			default:
				index = 0;
				break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		MobclickAgent.onEvent(getActivity(), "Download");// 统计下载次数
		Music music = musics.get(position);
		onDownloadClick(music);
	}

	private void onDownloadClick(final Music music) {
		if (TextUtils.isEmpty(music.getMp3Url())) {
			ToastTool.toastShort(getActivity(), "没有相应的下载地址");
			return;
		}

		File localFile = new File(music.getFilePath());
		if (localFile != null && localFile.exists()) {
			ToastTool.toastShort(getActivity(), "该歌曲已下载完成");
			return;
		}

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

		try {
			MusicApplication.db.save(music);
		} catch (DbException e) {
			e.printStackTrace();
		}

		ToastTool.toastShort(getActivity(), "下载" + music.getName());
		switch (music.getMusicType()) {
			case 0:
				// 小狗，步骤多一步，必须先获取真正的下载地址
				org.xutils.http.RequestParams params = new org.xutils.http.RequestParams(music.getMp3Url());
				params.setExecutor(Constants.EXECUTOR_MUSIC);
				params.setCancelFast(true);

				x.http().get(params, new Callback.CommonCallback<String>() {

					@Override
					public void onSuccess(String result) {
						JSONObject jsonObject = JSON.parseObject(result);
						if (jsonObject == null) {
							ToastTool.toastShort(getActivity(), "没有相应的下载地址");
							return;
						}

						String url = jsonObject.getString("url");
						if (!TextUtils.isEmpty(url)) {
							music.setMp3Url(url);
							try {
								MusicApplication.db.update(music, "mp3Url");
							} catch (DbException e) {
								e.printStackTrace();
							}
							downloadMusic(music);
						}
					}

					@Override
					public void onError(Throwable ex, boolean isOnCallback) {
						ToastTool.toastShort(getActivity(), "这首歌不能下载了");
					}

					@Override
					public void onCancelled(CancelledException cex) {

					}

					@Override
					public void onFinished() {

					}
				});
				break;

			case 1:
				// 凉窝
				// 需要多一步，获取真正的mp3地址
				org.xutils.http.RequestParams kmeParams = new org.xutils.http.RequestParams(music.getMp3Url());
				kmeParams.setExecutor(Constants.EXECUTOR_MUSIC);
				kmeParams.setCancelFast(true);

				x.http().get(kmeParams, new Callback.CommonCallback<String>() {

					@Override
					public void onSuccess(String result) {
						music.setMp3Url(result.trim().replaceAll(" ", ""));
						try {
							MusicApplication.db.update(music, "mp3Url");
						} catch (DbException e) {
							e.printStackTrace();
						}
						downloadMusic(music);
					}

					@Override
					public void onError(Throwable ex, boolean isOnCallback) {
						ToastTool.toastShort(getActivity(), "这首歌不能下载了");
					}

					@Override
					public void onCancelled(CancelledException cex) {

					}

					@Override
					public void onFinished() {

					}
				});
				break;

			case 2:
				// 企鹅，直接下
				downloadMusic(music);
				break;

			case 3:
				// 白云，直接下
				downloadMusic(music);
				break;

			case 4:
				// 熊掌，直接下
				downloadMusic(music);
				break;

			case 5:
				// 龙虾，直接下
				downloadMusic(music);
				break;
			default:
				break;
		}
	}

	/**
	 * 下载音乐
	 *
	 * @param music
	 */
	private void downloadMusic(final Music music) {

		org.xutils.http.RequestParams params = new org.xutils.http.RequestParams(music.getMp3Url());
		params.setAutoResume(true);
		params.setAutoRename(false);
		params.setSaveFilePath(music.getFilePath());
		params.setExecutor(Constants.EXECUTOR_MUSIC);
		params.setCancelFast(true);

		x.http().get(params, new Callback.ProgressCallback<File>() {

			@Override
			public void onSuccess(File result) {
				ToastTool.toastShort(getActivity(), music.getName() + "下载成功");
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
		if (TextUtils.equals(event, Constants.EVENT_LOAD_COMPLETE)) {
			// 停止加载动画
			loadingDialog.dismiss();
		}

		if (TextUtils.equals(event, Constants.EVENT_UPDATE_MUSICS)) {
			// 刷新音乐列表
			musicAdapter.notifyDataSetChanged();
		}
	}
}
