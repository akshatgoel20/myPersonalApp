package com.myapplock.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.myapplock.R;
import com.myapplock.interfaces.ItemClickListner;
import com.myapplock.lazyloading.ImageLoader;
import com.myapplock.models.MediaObject;

import java.util.ArrayList;

/**
 * Created by Other on 13-07-2015.
 */
public class AllGalleryImageAdapter extends RecyclerView.Adapter<AllGalleryImageAdapter.CustumView> {

    private ItemClickListner mItemClickListener;
    private ArrayList<MediaObject> mGalleryPhotoAlbumlist;
    private ImageLoader imageLoader;

    public AllGalleryImageAdapter(Context context, ArrayList<MediaObject> photoAlbumslist){
        mGalleryPhotoAlbumlist=photoAlbumslist;
        imageLoader=new ImageLoader(context,false);
    }


    public class CustumView extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView mAlbumCover;
        private ImageView selectedImage;

        public CustumView(View itemView) {
            super(itemView);
            mAlbumCover = (ImageView) itemView.findViewById(R.id.iv_img_name);
            selectedImage = (ImageView) itemView.findViewById(R.id.selected_img);
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
        View  v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_image, null);
        CustumView mh = new CustumView(v);
        return mh;
    }

    @Override
    public void onBindViewHolder(CustumView holder, int position) {

        holder.mAlbumCover.setScaleType(ImageView.ScaleType.CENTER_CROP);
        holder.mAlbumCover.setPadding(8, 8, 8, 8);
        imageLoader.DisplayImage(mGalleryPhotoAlbumlist.get(position).getPath(), holder.mAlbumCover);
        if(mGalleryPhotoAlbumlist.get(position).isSelected()){
            holder.selectedImage.setVisibility(View.VISIBLE);
        }else{
            holder.selectedImage.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mGalleryPhotoAlbumlist.size();
    }

}
