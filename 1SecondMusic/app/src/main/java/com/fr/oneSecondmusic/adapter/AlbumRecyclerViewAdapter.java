package com.fr.oneSecondmusic.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fr.oneSecondmusic.MyApplication;
import com.fr.oneSecondmusic.R;
import com.fr.oneSecondmusic.Utils;
import com.fr.oneSecondmusic.object.Album;

import java.util.List;

public class AlbumRecyclerViewAdapter extends RecyclerView.Adapter<AlbumRecyclerViewAdapter.ViewHolder> {

    private final List<Album> mData;
    private final LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    private int selectedPos = RecyclerView.NO_POSITION;

    // data is passed into the constructor
    public AlbumRecyclerViewAdapter(Context context, List<Album> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    // inflates the row layout from xml when needed
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recyclerview_row_album, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Album album = mData.get(position);
        holder.bind(album);
        album.setHolder(holder);

        holder.itemView.setSelected(selectedPos == position);
        holder.switchAlbumIsSelected.setOnClickListener(view -> holder.switchAlbumIsSelected(album));
        holder.switchAlbumIsSelected.setChecked(album.getNumberOfSelectedTracks()>0);

        holder.itemView.setOnClickListener(v -> {
            // Get the current state of the item
            boolean expanded = album.isExpanded();
            // Change the state
            album.setExpanded(!expanded);
            // Notify the adapter that item has changed
            notifyItemChanged(position);
        });
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, TrackRecyclerViewAdapter.ItemClickListener {

        TextView albumTitle;
        TextView albumInfo;
        LinearLayout subItem;
        ImageView imageView;
        SwitchCompat switchAlbumIsSelected;

        RecyclerView trackRecyclerView;
        TrackRecyclerViewAdapter adapter;

        ViewHolder(View itemView) {
            super(itemView);
            albumTitle = itemView.findViewById(R.id.albumTitle);
            albumInfo = itemView.findViewById(R.id.albumInfo);
            trackRecyclerView = itemView.findViewById(R.id.rvTrack);
            subItem = itemView.findViewById(R.id.subItem);
            imageView = itemView.findViewById(R.id.imageView);
            switchAlbumIsSelected = itemView.findViewById(R.id.switchAlbumIsSelected);

            switchAlbumIsSelected.setChecked(true);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
            notifyItemChanged(selectedPos);
            selectedPos = getLayoutPosition();
            notifyItemChanged(selectedPos);
        }

        public void switchAlbumIsSelected(Album album){
            album.checkAllSongs(switchAlbumIsSelected.isChecked());
        }

        public void updateUI(Album album){
            albumTitle.setText(album.getName());
            StringBuilder info = new StringBuilder();
            for(String artist : album.getArtists()){
                info.append(artist).append(", ");
            }
            info.append(album.getNumberOfSelectedTracks()).append("/").append(album.getNumberOfTracks());

            Utils.setImgViewFromURL(imageView, album.getImgURL());
            albumInfo.setText(info.toString());

            switchAlbumIsSelected.setChecked(album.getNumberOfSelectedTracks()>0);
        }

        public void bind(Album album) {
            // Get the state
            boolean expanded = album.isExpanded();
            // Set the visibility based on state
            subItem.setVisibility(expanded ? View.VISIBLE : View.GONE);
            intiSubItemRV(album);

            updateUI(album);
        }

        private void intiSubItemRV(Album album){
            // set up the RecyclerView
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MyApplication.getContext());
            trackRecyclerView.setLayoutManager(linearLayoutManager);

            //add divider between row
            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(trackRecyclerView.getContext(), linearLayoutManager.getOrientation());
            trackRecyclerView.addItemDecoration(dividerItemDecoration);

            // populate the RecyclerView
            adapter = new TrackRecyclerViewAdapter(MyApplication.getContext(), album.getTracks());
            adapter.setClickListener(this);
            trackRecyclerView.setAdapter(adapter);
        }

        @Override
        public void onItemClick(View view, int position) {
        }
    }

    // convenience method for getting data at click position
    public Album getItem(int id) {
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
