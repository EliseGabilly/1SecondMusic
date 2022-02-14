package com.fr.oneSecondmusic.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.fr.oneSecondmusic.R;
import com.fr.oneSecondmusic.object.Song;

import java.util.List;

public class TrackRecyclerViewAdapter extends RecyclerView.Adapter<TrackRecyclerViewAdapter.ViewHolder> {

    private final List<Song> mData;
    private final LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    private int selectedPos = RecyclerView.NO_POSITION;

    // data is passed into the constructor
    public TrackRecyclerViewAdapter(Context context, List<Song> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    // inflates the row layout from xml when needed
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.reciclerview_row_track, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Song song = mData.get(position);

        holder.myTextView.setText(song.getName());

        song.setHolder(holder);
        holder.switchTrackIsSelected.setOnClickListener(view -> holder.switchSongIsSelected(song));
        holder.switchTrackIsSelected.setChecked(song.isSelected());

        holder.itemView.setSelected(selectedPos == position);
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView myTextView;
        SwitchCompat switchTrackIsSelected;

        ViewHolder(View itemView) {
            super(itemView);
            myTextView = itemView.findViewById(R.id.tvTrackName);
            switchTrackIsSelected = itemView.findViewById(R.id.switchTrackIsSelected);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
            selectedPos = getLayoutPosition();
            notifyItemChanged(selectedPos);
        }
        public void switchSongIsSelected(Song song){
            song.changeSelected();
        }
        public void updateSwitch(boolean isOn){
            switchTrackIsSelected.setChecked(isOn);
        }
    }

    // convenience method for getting data at click position
    public Song getItem(int id) {
        return mData.get(id);
    }

    // allows clicks events to be caught
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

}
