package com.itant.musichome.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.dinuscxj.progressbar.CircleProgressBar;
import com.itant.musichome.MusicApplication;
import com.itant.musichome.R;
import com.itant.musichome.bean.Music;
import com.itant.musichome.utils.ToastTool;
import com.umeng.analytics.MobclickAgent;
import com.wuxiaolong.pullloadmorerecyclerview.PullLoadMoreRecyclerView;

import org.xutils.ex.DbException;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Jason on 2016/11/13.
 */
public class TaskActivity extends BaseActivity {
    private PullLoadMoreRecyclerView pmrv_task;
    private RecyclerViewAdapter mRecyclerViewAdapter;
    private List<Music> musics;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitleBar("任务");
        setBackable(true);

        pmrv_task = (PullLoadMoreRecyclerView) findViewById(R.id.pmrv_task);
        musics = new ArrayList<>();
        mRecyclerViewAdapter = new RecyclerViewAdapter(musics);

        pmrv_task.setLinearLayout();
        pmrv_task.setPullRefreshEnable(true);// 不需要下拉刷新
        pmrv_task.setPushRefreshEnable(false);// 不需要上拉刷新
        pmrv_task.setFooterViewText("正在刷新");
        pmrv_task.setAdapter(mRecyclerViewAdapter);
        pmrv_task.setOnPullLoadMoreListener(new PullLoadMoreRecyclerView.PullLoadMoreListener() {
            @Override
            public void onRefresh() {
                MobclickAgent.onEvent(TaskActivity.this, "Refresh");// 统计刷新次数
                getMusicFromDb();
            }

            @Override
            public void onLoadMore() {

            }
        });
        getMusicFromDb();
    }

    private void getMusicFromDb() {

        try {
            List<Music> dbMusics = MusicApplication.db.selector(Music.class).findAll();
            if (musics == null) {
                return;
            }
            musics.clear();
            musics.addAll(dbMusics);
            Collections.reverse(musics);
            mRecyclerViewAdapter.notifyDataSetChanged();

        } catch (Throwable e) {
            e.printStackTrace();
        }

        pmrv_task.setPullLoadMoreCompleted();
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_task;
    }

    class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

        private List<Music> musics;

        public RecyclerViewAdapter(List<Music> musics) {
            this.musics = musics;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // inflate方法的第二个参数一定要为null，否则只能显示第一行
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_task, null, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            // 这里设置数据

            final Music music = musics.get(position);
            holder.tv_name.setText(music.getName());
            holder.tv_bitrate.setText(music.getBitrate());
            holder.tv_singer.setText(music.getSinger() + " " + music.getAlbum());
            holder.cpb_task.setProgress(music.getProgress());

            switch (music.getMusicType()) {
                case 0:
                    holder.tv_from.setText("小狗");
                    break;
                case 1:
                    holder.tv_from.setText("凉窝");
                    break;
                case 2:
                    holder.tv_from.setText("企鹅");
                    break;
                case 3:
                    holder.tv_from.setText("白云");
                    break;
                case 4:
                    holder.tv_from.setText("熊掌");
                    break;
                case 5:
                    holder.tv_from.setText("龙虾");
                    break;
                default:
                    break;
            }

            holder.iv_play.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 点击播放
                    if (music.getProgress() != 100) {
                        ToastTool.toastShort(getApplicationContext(), "请等待歌曲下载完毕");
                        return;
                    }

                    File file = new File(music.getFilePath());
                    if (!file.exists()) {
                        ToastTool.toastShort(getApplicationContext(), "该文件已丢失，请从列表中删除");
                        return;
                    }

                    // 播放
                    try {
                        MobclickAgent.onEvent(TaskActivity.this, "Play");// 统计播放次数
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.parse("file://" + music.getFilePath()), "audio/MP3");
                        startActivity(intent);
                    } catch (Exception e) {
                        ToastTool.toastShort(getApplicationContext(), "找不到文件");
                    }
                }
            });

            holder.iv_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 点击删除(提示：你真的要删除这首歌曲吗？)========

                    final AlertDialog dialog = new AlertDialog.Builder(TaskActivity.this).create();
                    dialog.show();
                    dialog.setContentView(R.layout.dialog_delete_song);
                    dialog.setCancelable(false);
                    TextView tv_song_name = (TextView) dialog.findViewById(R.id.tv_song_name);
                    tv_song_name.setText("歌曲名：" + music.getName());

                    BootstrapButton bb_confirm = (BootstrapButton) dialog.findViewById(R.id.bb_confirm);
                    bb_confirm.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.cancel();
                            MobclickAgent.onEvent(TaskActivity.this, "Delete");// 统计删除次数
                            // 删除
                            File file = new File(music.getFilePath());
                            if (file.exists()) {
                                file.delete();
                            }

                            // 从数据库中删除
                            try {
                                MusicApplication.db.deleteById(Music.class, music.getId());
                            } catch (DbException e) {
                                e.printStackTrace();
                            }

                            ToastTool.toastShort(getApplicationContext(), "删除成功");

                            // 刷新数据
                            getMusicFromDb();
                        }
                    });
                    BootstrapButton bb_cancel = (BootstrapButton) dialog.findViewById(R.id.bb_cancel);
                    bb_cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // 取消
                            dialog.cancel();
                        }
                    });
                }
            });

            holder.ll_song.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 播放
                    try {
                        MobclickAgent.onEvent(TaskActivity.this, "Play");// 统计播放次数
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.parse("file://" + music.getFilePath()), "audio/MP3");
                        startActivity(intent);
                    } catch (Exception e) {
                        ToastTool.toastShort(getApplicationContext(), "找不到文件");
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return musics == null ? 0 : musics.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            private TextView tv_name;
            private TextView tv_size;
            private TextView tv_bitrate;
            private TextView tv_singer;
            private CircleProgressBar cpb_task;
            private ImageView iv_play;
            private ImageView iv_delete;
            private LinearLayout ll_song;
            private TextView tv_from;// 歌曲来源

            public ViewHolder(View itemView) {
                super(itemView);

                // 这里查找控件
                tv_name = (TextView) itemView.findViewById(R.id.tv_name);
                tv_size = (TextView) itemView.findViewById(R.id.tv_size);
                tv_bitrate = (TextView) itemView.findViewById(R.id.tv_bitrate);
                tv_singer = (TextView) itemView.findViewById(R.id.tv_singer);
                cpb_task = (CircleProgressBar) itemView.findViewById(R.id.cpb_task);
                iv_play = (ImageView) itemView.findViewById(R.id.iv_play);
                iv_delete = (ImageView) itemView.findViewById(R.id.iv_delete);
                ll_song = (LinearLayout) itemView.findViewById(R.id.ll_song);
                tv_from = (TextView) itemView.findViewById(R.id.tv_from);
            }
        }
    }
}
