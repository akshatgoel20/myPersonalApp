package com.myapplock.adapter;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.myapplock.R;
import com.myapplock.models.AppItems;
import com.myapplock.utils.CommonUtils;

import java.util.ArrayList;
import java.util.List;

public class AppAdapter extends RecyclerView.Adapter<AppAdapter.CustumViewHolder>
{
    private OnItemClickListener mItemClickListener;

    private List<AppItems> mArrayList;

    private Context mContext;

    private int listType;
    public AppAdapter(Context context, List<AppItems> ModelArrayList, int pListType)
    {
        mArrayList = new ArrayList<AppItems>();
        mArrayList = ModelArrayList;
        mContext = context;
        listType=pListType;
    }

    public class CustumViewHolder extends RecyclerView.ViewHolder implements OnClickListener
    {
        CardView cv;

        TextView personName;

        TextView personAge;

        LinearLayout layout;

        Button mLeft, mMiddel, mRight;

        ImageView personPhoto;

        private ToggleButton mSelected;

        public CustumViewHolder(View itemView)
        {
            super(itemView);
            cv = (CardView) itemView.findViewById(R.id.cv);
            personName = (TextView) itemView.findViewById(R.id.person_name);
            personAge = (TextView) itemView.findViewById(R.id.person_age);
            personPhoto = (ImageView) itemView.findViewById(R.id.person_photo);
//            mSelected = (ToggleButton) itemView.findViewById(R.id.app_selected);
            layout = (LinearLayout) itemView.findViewById(R.id.btn_view);
            mLeft = (Button) itemView.findViewById(R.id.left);

            itemView.setOnClickListener(this);
            mLeft.setOnClickListener(this);

            if(listType== CommonUtils.AppStatus.UnLocked.ordinal()){
                mMiddel = (Button) itemView.findViewById(R.id.right);
                mRight = (Button) itemView.findViewById(R.id.right);
                mMiddel.setOnClickListener(this);
                mRight.setOnClickListener(this);
            }
        }

        @Override
        public void onClick(View v)
        {
            if (mItemClickListener != null) {
                mItemClickListener.onItemClick(v, getPosition());
            }
        }
    }

    public interface OnItemClickListener
    {
         void onItemClick(View view, int position);
    }

    public void SetOnItemClickListener(final OnItemClickListener mItemClickListener)
    {
        this.mItemClickListener = mItemClickListener;
    }

    @Override
    public int getItemCount()
    {
        return mArrayList.size();
    }

    @Override
    public void onBindViewHolder(CustumViewHolder viewHolder, int pos)
    {
        viewHolder.personName.setText(mArrayList.get(pos).getAppName());
        viewHolder.personPhoto.setImageDrawable(mArrayList.get(pos).getAppIcon());
//        viewHolder.mSelected.setChecked(mArrayList.get(pos).isAppLocked());

        if (mArrayList.get(pos).isLayoutOpen()) {
            viewHolder.layout.setVisibility(View.VISIBLE);
        } else {
            viewHolder.layout.setVisibility(View.GONE);
        }
    }

    @Override
    public CustumViewHolder onCreateViewHolder(ViewGroup viewGroup, int arg1)
    {
        View v=null;
        if(listType== CommonUtils.AppStatus.UnLocked.ordinal()){
            v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_row, viewGroup, false);
        }else{
            v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.locked_app_list_row, viewGroup, false);
        }

        CustumViewHolder mh = new CustumViewHolder(v);

        return mh;
    }


}
