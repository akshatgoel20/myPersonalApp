package com.myapplock.ui;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.myapplock.R;
import com.myapplock.adapter.LockedAppAdapter;
import com.myapplock.adapter.LockedAppAdapter.OnItemClickListener;
import com.myapplock.application.MyAppLock;
import com.myapplock.database.AppInfoDB;
import com.myapplock.database.UpdateDB;
import com.myapplock.interfaces.UpdateListContent;
import com.myapplock.models.AppItems;
import com.myapplock.utils.CommonUtils;

import java.util.Collections;
import java.util.Comparator;

public class LockedAppsLandingFragment extends Fragment implements
		OnClickListener,UpdateListContent {



	private View mView;

	private RecyclerView mAppListView;

	private ProgressDialog loading;

	private LockedAppAdapter adapter;

	private static final int REQ_CREATE_PATTERN = 1;

	private int tempPos = -1;

	private UpdateDB mUpdateDB;

	private MyAppLock myAppLock;
	public LockedAppsLandingFragment()
	{
	}

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
	{
		mView = inflater.inflate(R.layout.fragment_locked_landing, null);
		setRetainInstance(true);
		initView();
		// forImageView();
		return mView;
	}


	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		new LoadApplicationTask().execute();
	}


	@Override
	public void onClick(View v)
	{
		switch (v.getId()) {
			case R.id.btn_selectAll:
				selectAllApp();
				break;
			case R.id.btn_deSelectAll:
				deSelectAllApp();
				break;
			case R.id.btn_refresh:
				refreshApp();
				break;

			default:
				break;
		}
	}

	private void initView()
	{
		mAppListView = (RecyclerView) mView.findViewById(R.id.recycler_view);

	}

	private void refreshApp()
	{
		Comparator<AppItems> cp =
				AppItems.getComparator(AppItems.SortParameter.NAME_DESCENDING, AppItems.SortParameter.SELECTED_ASCENDING);
		Collections.sort(getAppContext().getLockedAppList(), cp);
		if (adapter != null) {
			adapter.notifyDataSetChanged();
		} else {
			adapter = new LockedAppAdapter(getActivity(), getAppContext().getLockedAppList(),CommonUtils.AppStatus.Locked.ordinal());
			mAppListView.setAdapter(adapter);
		}

	}

	private void deSelectAllApp()
	{
		for (AppItems app : getAppContext().getLockedAppList()) {
			app.setStatus(false);
		}
		if (adapter != null) {
			adapter.notifyDataSetChanged();
		} else {
			adapter = new LockedAppAdapter(getActivity(), getAppContext().getLockedAppList(),CommonUtils.AppStatus.Locked.ordinal());
			mAppListView.setAdapter(adapter);
		}
	}

	private void selectAllApp()
	{

		for (AppItems app : getAppContext().getLockedAppList()) {
			app.setStatus(true);
		}
		if (adapter != null) {
			adapter.notifyDataSetChanged();
		} else {
			adapter = new LockedAppAdapter(getActivity(), getAppContext().getLockedAppList(),CommonUtils.AppStatus.Locked.ordinal());
			mAppListView.setAdapter(adapter);
		}
	}

	@Override
	public void updateList() {
		Comparator<AppItems> cp =
				AppItems.getComparator(AppItems.SortParameter.NAME_DESCENDING,
						AppItems.SortParameter.SELECTED_ASCENDING);
		Collections.sort(getAppContext().getLockedAppList(), cp);

		if (adapter != null) {
			adapter.notifyDataSetChanged();
		} else {
			adapter = new LockedAppAdapter(getActivity(), getAppContext().getLockedAppList(),CommonUtils.AppStatus.Locked.ordinal());
			mAppListView.setAdapter(adapter);
		}
	}

	private class LoadApplicationTask extends AsyncTask<Integer, Integer, Integer>
	{
		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();
			loading = ProgressDialog.show(getActivity(), "Please wait", "Gathering application... ");
		}

		@Override
		protected Integer doInBackground(Integer... params)
		{
			getAppContext().getLockedAppList().clear();
			if(getAppContext() !=null && !getAppContext().getLockedAppList().isEmpty()){
				getAppContext().getLockedAppList().addAll(getAppContext().getLockedAppList());
			}
			else if(getDBInstance().getDBCount(AppInfoDB.DB_APP_DETAILS_TABLE) > 0) {
				getAppContext().getLockedAppList().addAll(getDBInstance().getLockedAppList());
			}
			return 0;
		}

		@Override
		protected void onPostExecute(Integer result)
		{
			super.onPostExecute(result);
			if(loading!=null){
				loading.dismiss();
			}

			Comparator<AppItems> cp =
					AppItems.getComparator(AppItems.SortParameter.NAME_DESCENDING,
							AppItems.SortParameter.SELECTED_ASCENDING);
			Collections.sort(getAppContext().getLockedAppList(), cp);


			adapter = new LockedAppAdapter(getActivity(), getAppContext().getLockedAppList(),CommonUtils.AppStatus.Locked.ordinal());
			LinearLayoutManager layoutManager =
					new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
			mAppListView.setLayoutManager(layoutManager);
			mAppListView.setAdapter(adapter);
			mAppListView.setItemAnimator(new DefaultItemAnimator());

			adapter.SetOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(View view, int position) {

					try {


						AppItems model = getAppContext().getLockedAppList().get(position);
						if (view instanceof Button) {
							switch (view.getId()) {
								case R.id.left:
									model.setStatus(false);
									saveToPreference(model);

									Toast.makeText(getActivity(), "Letf: " + position, Toast.LENGTH_SHORT).show();
									break;

								default:
									break;
							}
						} else {

							if (tempPos != -1 ) {
								getAppContext().getLockedAppList().get(tempPos).setOpen(false);
							}
							if (tempPos == position) {
								tempPos = -1;
								model.setOpen(false);
							} else {
								model.setOpen(true);
								tempPos = position;
							}
						}
						adapter.notifyDataSetChanged();
					}
					catch (Exception e){
                      e.printStackTrace();
					}
				}
			});
		}
	}

	private void saveToPreference(AppItems appItems)
	{
		appItems.setOpen(false);
		getAppContext().getLockedAppList().remove(appItems);
		getAppContext().getUnlockedAppList().add(appItems);

		getDBInstance().UpdateAppIntoDB(appItems);
		getDBInstance().updateAppKeyDetailIntoDB(appItems.getAppPackageName());
	}

	private UpdateDB getDBInstance() {

		if (mUpdateDB == null) {
			mUpdateDB = new UpdateDB(getActivity());
		}
		return mUpdateDB;
	}

	private MyAppLock getAppContext(){
		if(myAppLock==null){
			myAppLock=(MyAppLock)getActivity().getApplicationContext();
		}
		return myAppLock;
	}






}
