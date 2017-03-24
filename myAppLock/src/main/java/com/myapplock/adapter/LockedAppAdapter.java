package com.myapplock.adapter;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.myapplock.R;
import com.myapplock.application.MyAppLock;
import com.myapplock.models.AppItems;
import com.myapplock.utils.RecyclerViewAnimator;

import java.util.ArrayList;
import java.util.List;

public class LockedAppAdapter extends RecyclerView.Adapter<LockedAppAdapter.CustomViewHolder> {
    private final RecyclerViewAnimator mAnimator;
    private OnItemClickListener mItemClickListener;
    private List<AppItems> mArrayList;

    public LockedAppAdapter(List<AppItems> ModelArrayList,RecyclerView pRecyclerView) {
        mArrayList = new ArrayList<>();
        mArrayList.addAll(ModelArrayList);
        mAnimator = new RecyclerViewAnimator(pRecyclerView);
    }

    @Override
    public void onBindViewHolder(CustomViewHolder viewHolder, int pos) {
        viewHolder.mAppName.setText(mArrayList.get(pos).getAppName());
        viewHolder.mAppIcon.setImageDrawable(mArrayList.get(pos).getAppIcon());

        if (mArrayList.get(pos).isLayoutOpen()) {
            viewHolder.mLockUnLockLayout.setVisibility(View.VISIBLE);
        } else {
            viewHolder.mLockUnLockLayout.setVisibility(View.GONE);
        }
        if (mArrayList.get(pos).isAppLocked()) {
            viewHolder.mLockUnLockIcon.setImageDrawable(ContextCompat.getDrawable(MyAppLock.getAppContext(), R.drawable.ic_action_secure));
            viewHolder.mLockedAppLayout.setVisibility(View.GONE);
            viewHolder.mUnLockedAppLayout.setVisibility(View.VISIBLE);
        } else {
            viewHolder.mLockedAppLayout.setVisibility(View.VISIBLE);
            viewHolder.mUnLockedAppLayout.setVisibility(View.GONE);
            viewHolder.mLockUnLockIcon.setImageDrawable(ContextCompat.getDrawable(MyAppLock.getAppContext(), R.drawable.ic_action_not_secure));
        }
        mAnimator.onBindViewHolder(viewHolder.itemView, pos);

    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int arg1) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_row, viewGroup, false);
        mAnimator.onCreateViewHolder(v);

        return new CustomViewHolder(v);
    }

    @Override
    public int getItemCount() {
        Log.e("TAG", "cpun " + mArrayList.size());
        return mArrayList.size();
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder implements OnClickListener {

        ImageView mAppIcon;
        ImageView mLockUnLockIcon;
        TextView mAppName;
        TextView mAppDesc;
        LinearLayout mLockedAppLayout;
        LinearLayout mUnLockedAppLayout;
        Button mDefaultPass;
        Button mPinPass;
        Button mPatternPass;
        Button mUnLockApp;
        View mLockUnLockLayout;

        public CustomViewHolder(View itemView) {
            super(itemView);

            mAppName = (TextView) itemView.findViewById(R.id.tv_app_name);
            mAppDesc = (TextView) itemView.findViewById(R.id.tv_app_desc);
            mAppIcon = (ImageView) itemView.findViewById(R.id.iv_app_icon);
            mLockUnLockIcon = (ImageView) itemView.findViewById(R.id.iv_app_selected_unselected_icon);
            mLockedAppLayout = (LinearLayout) itemView.findViewById(R.id.ll_locked_app_layout);
            mUnLockedAppLayout = (LinearLayout) itemView.findViewById(R.id.ll_unlocked_app_layout);
            mDefaultPass = (Button) itemView.findViewById(R.id.btn_default_pass);
            mPinPass = (Button) itemView.findViewById(R.id.btn_pin_pass);
            mPatternPass = (Button) itemView.findViewById(R.id.btn_pattern_pass);
            mUnLockApp = (Button) itemView.findViewById(R.id.btn_unlock_app);
            mLockUnLockLayout = itemView.findViewById(R.id.rl_lock_unlock_option);

            itemView.setOnClickListener(this);
            mDefaultPass.setOnClickListener(this);
            mPinPass.setOnClickListener(this);
            mPatternPass.setOnClickListener(this);
            mUnLockApp.setOnClickListener(this);
            mLockUnLockIcon.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mItemClickListener != null) {
                mItemClickListener.onItemClick(v, getLayoutPosition());
            }
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public void SetOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }


}
