package com.myapplock.ui.fragment;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.myapplock.R;
import com.myapplock.models.GalleryPhotoAlbum;

import java.util.ArrayList;

public class ImageVaultFragment extends Fragment
{
    private View mView;
    private RecyclerView mImageRecyclerView;
    private FloatingActionButton mAddImage;
    private ArrayList<GalleryPhotoAlbum> mPhotoAlbum;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        // TODO Auto-generated method stub
        mView = inflater.inflate(R.layout.fragment_image_vault, null);
        initViews();
        return mView;
    }


    private void initViews(){
        mImageRecyclerView=(RecyclerView)mView.findViewById(R.id.recycler_view_image_gallery);
        mAddImage=(FloatingActionButton)mView.findViewById(R.id.fab_add_images);
        mPhotoAlbum=new ArrayList<>();
        mAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
            }
        });
    }

    private void openGallery(){
        changeFragment(new GetImageGalleryFragment(), "GetImageGalleryFragment");
    }
    private void setImageAdapter(){

    }

    private void changeFragment(Fragment pFragment, String pFragmentName) {
        android.support.v4.app.FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.addToBackStack(pFragmentName);
        ft.replace(R.id.container, pFragment, pFragmentName).commit();
    }

    /**
     * retrieve image album and set
     */
    private void getPhotoList() {

        // which image properties are we querying
        String[] PROJECTION_BUCKET = { MediaStore.Images.ImageColumns.BUCKET_ID,
                MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME, MediaStore.Images.ImageColumns.DATE_TAKEN,
                MediaStore.Images.ImageColumns.DATA };
        // We want to order the albums by reverse chronological order. We abuse
        // the
        // "WHERE" parameter to insert a "GROUP BY" clause into the SQL
        // statement.
        // The template for "WHERE" parameter is like:
        // SELECT ... FROM ... WHERE (%s)
        // and we make it look like:
        // SELECT ... FROM ... WHERE (1) GROUP BY 1,(2)
        // The "(1)" means true. The "1,(2)" means the first two columns
        // specified
        // after SELECT. Note that because there is a ")" in the template, we
        // use
        // "(2" to match it.
        String BUCKET_GROUP_BY = "1) GROUP BY 1,(2";
        String BUCKET_ORDER_BY = "MAX(datetaken) DESC";

        // Get the base URI for the People table in the Contacts content
        // provider.
        Uri images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        Cursor cur = getActivity().getContentResolver().query(images, PROJECTION_BUCKET,
                BUCKET_GROUP_BY, null, BUCKET_ORDER_BY);

        Log.v("ListingImages", " query count=" + cur.getCount());

        GalleryPhotoAlbum album;

        if (cur.moveToFirst()) {
            String bucket;
            String date;
            String data;
            long bucketId;

            int bucketColumn = cur
                    .getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);

            int dateColumn = cur
                    .getColumnIndex(MediaStore.Images.Media.DATE_TAKEN);
            int dataColumn = cur.getColumnIndex(MediaStore.Images.Media.DATA);

            int bucketIdColumn = cur
                    .getColumnIndex(MediaStore.Images.Media.BUCKET_ID);

            do {
                // Get the field values
                bucket = cur.getString(bucketColumn);
                date = cur.getString(dateColumn);
                data = cur.getString(dataColumn);
                bucketId = cur.getInt(bucketIdColumn);

                if (bucket != null && bucket.length() > 0) {
                    album = new GalleryPhotoAlbum();
                    album.setBucketId(bucketId);
                    album.setBucketName(bucket);
                    album.setDateTaken(date);
                    // album.setAlbumCover(data);
                    album.setTotalCount(photoCountByAlbum(bucket));
                    mPhotoAlbum.add(album);
                    // Do something with the values.
                    Log.v("ListingImages", " bucket=" + bucket
                            + "  date_taken=" + date + "  _data=" + data
                            + " bucket_id=" + bucketId);
                }

            } while (cur.moveToNext());
        }
        cur.close();
        setImageAdapter();

    }

    /**
     * photo count find based on bucket name(album name)
     *
     * @param bucketName
     * @return
     */
    private int photoCountByAlbum(String bucketName) {
        try {
            final String orderBy = MediaStore.Images.Media.DATE_TAKEN;
            String searchParams = null;
            String bucket = bucketName;
            searchParams = "bucket_display_name = \"" + bucket + "\"";

            // final String[] columns = { MediaStore.Images.Media.DATA,
            // MediaStore.Images.Media._ID };
            Cursor mPhotoCursor = getActivity().getContentResolver().query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null,
                    searchParams, null, orderBy + " DESC");

            if (mPhotoCursor.getCount() > 0) {
                return mPhotoCursor.getCount();
            }
            mPhotoCursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;

    }



}
