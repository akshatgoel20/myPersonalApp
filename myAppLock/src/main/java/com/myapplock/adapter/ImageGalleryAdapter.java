package com.myapplock.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.myapplock.R;
import com.myapplock.interfaces.ItemClickListner;
import com.myapplock.lazyloading.ImageLoader;
import com.myapplock.models.GalleryPhotoAlbum;

import java.util.ArrayList;

/**
 * Created by Other on 13-07-2015.
 */
public class ImageGalleryAdapter extends RecyclerView.Adapter<ImageGalleryAdapter.CustumView> {

    private ItemClickListner mItemClickListener;
    private ArrayList<GalleryPhotoAlbum> mGalleryPhotoAlbumlist;
    private ImageLoader imageLoader;

    public ImageGalleryAdapter(Context context,ArrayList<GalleryPhotoAlbum> photoAlbumslist){
        mGalleryPhotoAlbumlist=photoAlbumslist;
        imageLoader=new ImageLoader(context,true);
    }


    public class CustumView extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView mAlbumName;
        private ImageView mAlbumCover;

        public CustumView(View itemView) {
            super(itemView);
            mAlbumName = (TextView) itemView.findViewById(R.id.tv_album_name);
            mAlbumCover = (ImageView) itemView.findViewById(R.id.iv_album_cover);
            itemView.setOnClickListener(this);
        }


        @Override
        public void onClick(View v) {
            if(mItemClickListener !=null){
                mItemClickListener.onItemClick(v, getAdapterPosition());
            }
        }
    }

    public void setOnItemClickListner(ItemClickListner itemClickListner) {
        mItemClickListener=itemClickListner;
    }


    @Override
    public CustumView onCreateViewHolder(ViewGroup parent, int viewType) {
        View  v = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_gallery_row, null);
        CustumView mh = new CustumView(v);
        return mh;
    }

    @Override
    public void onBindViewHolder(CustumView holder, int position) {
        GalleryPhotoAlbum photoAlbum=mGalleryPhotoAlbumlist.get(position);
        holder.mAlbumName.setText(photoAlbum.getBucketName() + "(" + photoAlbum.getTotalCount() + ")");
        imageLoader.DisplayImage(photoAlbum.getAlbumCover(), holder.mAlbumCover);
    }

    @Override
    public int getItemCount() {
        return mGalleryPhotoAlbumlist.size();
    }

}
