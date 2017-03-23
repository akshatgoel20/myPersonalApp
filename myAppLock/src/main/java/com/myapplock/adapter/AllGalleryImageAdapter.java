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

public class AllGalleryImageAdapter extends RecyclerView.Adapter<AllGalleryImageAdapter.CustomView> {

    private ItemClickListner mItemClickListener;
    private ArrayList<MediaObject> mGalleryPhotoAlbumlist;
    private ImageLoader imageLoader;

    public AllGalleryImageAdapter(Context context, ArrayList<MediaObject> photoAlbumslist){
        mGalleryPhotoAlbumlist=photoAlbumslist;
        imageLoader=new ImageLoader(context,false);
    }


     class CustomView extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView mAlbumCover;
        private ImageView selectedImage;

         CustomView(View itemView) {
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
    public CustomView onCreateViewHolder(ViewGroup parent, int viewType) {
        View  v = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_image, null);
        CustomView mh = new CustomView(v);
        return mh;
    }

    @Override
    public void onBindViewHolder(CustomView holder, int position) {

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
