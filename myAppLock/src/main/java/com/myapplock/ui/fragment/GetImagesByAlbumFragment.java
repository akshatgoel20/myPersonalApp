package com.myapplock.ui.fragment;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.myapplock.R;
import com.myapplock.adapter.AllGalleryImageAdapter;
import com.myapplock.framework.api.ItemClickListner;
import com.myapplock.models.MediaObject;
import com.myapplock.utils.MediaType;
import com.myapplock.utils.Utils;

import java.util.ArrayList;

public class GetImagesByAlbumFragment extends Fragment
{

    private RecyclerView mImageRecyclerView;
    private AllGalleryImageAdapter AllImageAdapter;
    private Cursor mPhotoCursor = null;
    private String mAlbumName;
    private View mView;

    public ArrayList<MediaObject> getImagesList() {
        if(imagesList==null){
            imagesList=new ArrayList<>();
        }
        return imagesList;
    }

    public void setImagesList(ArrayList<MediaObject> imagesList) {
        this.imagesList = imagesList;
    }

    private ArrayList<MediaObject> imagesList;

    public GetImagesByAlbumFragment(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        mView = inflater.inflate(R.layout.fragment_galley, null);
        initViews();
        return mView;
    }

    private void initViews(){

        mImageRecyclerView=(RecyclerView)mView.findViewById(R.id.recycler_view_image_gallery);
        setAllImageAdapter();
    }

    private void setAllImageAdapter(){
        mAlbumName=getArguments().getString("AlbumName");
        getAllPhotoFromAlbum(mAlbumName);

        GridLayoutManager layoutManager =new GridLayoutManager(getActivity(),2);
        mImageRecyclerView.setLayoutManager(layoutManager);
        mImageRecyclerView.setItemAnimator(new DefaultItemAnimator());
//        mImageRecyclerView.addItemDecoration(new GridSpacingDecoration());
        AllImageAdapter = new AllGalleryImageAdapter(getActivity(), getImagesList());
        mImageRecyclerView.setAdapter(AllImageAdapter);

        AllImageAdapter.setOnItemClickListner(new ItemClickListner() {
            @Override
            public void onItemClick(View view, int pos) {
                MediaObject mediaObject=getImagesList().get(pos);
                if(mediaObject.isSelected()){
                    mediaObject.setSelected(false);
                }else{
                    mediaObject.setSelected(true);
                }
                AllImageAdapter.notifyDataSetChanged();
            }
        });

    }



    /**
     * find image list for given bucket name
     *
     * @param bucketName
     */
    private ArrayList<MediaObject> getAllPhotoFromAlbum(String bucketName) {

        try {
            getImagesList().clear();
            final String orderBy = MediaStore.Images.Media.DATE_TAKEN;
            String bucket = bucketName;
            String whereAlbumName = "bucket_display_name = \"" + bucket + "\"";

            // final String[] columns = { MediaStore.Images.Media.DATA,
            // MediaStore.Images.Media._ID };
            mPhotoCursor = getActivity().getContentResolver().query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null,
                    whereAlbumName, null, orderBy + " ASC");

            if (mPhotoCursor.getCount() > 0) {
                getImagesList().addAll(Utils.extractMediaList(mPhotoCursor,
                        MediaType.PHOTO));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return  getImagesList();
    }

}
