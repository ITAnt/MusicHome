package com.itant.musichome.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.itant.musichome.R;
import com.itant.musichome.bean.Music;

import java.util.List;

/**
 * Created by Jason on 2016/11/13.
 */
public class MusicAdapter extends BaseAdapter {
    private Context context;
    private List<Music> musics;

    public void setMusics(List<Music> musics) {
        this.musics = musics;
    }

    public MusicAdapter(Context context, List<Music> musics) {
        this.context = context;
        this.musics = musics;
    }

    @Override
    public int getCount() {
        return musics == null ? 0 : musics.size();
    }

    @Override
    public Object getItem(int position) {
        return musics == null ? null : musics.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final Music music = musics.get(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.adapter_music, null);
            viewHolder = new ViewHolder();
            viewHolder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
            viewHolder.tv_size = (TextView) convertView.findViewById(R.id.tv_size);
            viewHolder.tv_bitrate = (TextView) convertView.findViewById(R.id.tv_bitrate);
            viewHolder.tv_singer = (TextView) convertView.findViewById(R.id.tv_singer);
            viewHolder.ll_download = (LinearLayout) convertView.findViewById(R.id.ll_download);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.tv_name.setText(music.getName());
        viewHolder.tv_bitrate.setText(music.getBitrate());
        viewHolder.tv_singer.setText(music.getSinger() + " " +music.getAlbum());

        viewHolder.ll_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onDownloadClickListener != null) {
                    onDownloadClickListener.onIconClick(position);
                }
            }
        });

        return convertView;
    }

    private ViewHolder viewHolder;
    private static class ViewHolder {
        private TextView tv_name;
        private TextView tv_size;
        private TextView tv_bitrate;
        private TextView tv_singer;
        private LinearLayout ll_download;
    }

    private OnDownloadClickListener onDownloadClickListener;

    public OnDownloadClickListener getOnDownloadClickListener() {
        return onDownloadClickListener;
    }

    public void setOnDownloadClickListener(OnDownloadClickListener onDownloadClickListener) {
        this.onDownloadClickListener = onDownloadClickListener;
    }

    public interface OnDownloadClickListener {
        void onIconClick(int position);
    }
}
