package com.example.thampotter.musiccustom;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class PlayListAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<String> paths;
    private LayoutInflater inflater;

    public PlayListAdapter(Context context, ArrayList<String> paths) {
        this.paths = paths;
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return paths.size();
    }

    @Override
    public String getItem(int i) {
        return paths.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup parent) {
        Holder holder;
        if (view == null) {
            holder = new Holder();
            view = inflater.inflate(R.layout.item_list, parent, false);
            holder.tvTitle = view.findViewById(R.id.tv_title);
            holder.tvArtist = view.findViewById(R.id.tv_artist);
            view.setTag(holder);
        } else {
            holder = (Holder) view.getTag();
        }

        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(paths.get(i));
        String artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        String title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        holder.tvArtist.setText(artist);
        holder.tvTitle.setText(title);
        return view;
    }

    private class Holder {
        TextView tvTitle;
        TextView tvArtist;
    }
}
