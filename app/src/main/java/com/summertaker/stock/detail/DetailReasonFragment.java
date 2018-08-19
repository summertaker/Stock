package com.summertaker.stock.detail;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.summertaker.stock.R;
import com.summertaker.stock.common.BaseFragment;
import com.summertaker.stock.common.Config;
import com.summertaker.stock.common.DataManager;
import com.summertaker.stock.data.Item;
import com.summertaker.stock.data.Reason;
import com.summertaker.stock.util.RecyclerTouchListener;

import java.util.ArrayList;

public class DetailReasonFragment extends BaseFragment {

    private DetailReasonFragment.Callback mEventListener;

    private Resources mResources;

    private int mPosition = -1;
    private String mCode;

    private ArrayList<Reason> mReasons = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private DetailReasonAdapter mAdapter;

    // Container Activity must implement this interface
    public interface Callback {
        void onDetailReasonFragmentEvent(String event);

        void onFragmentItemSizeChanged(int position, int itemSize);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof Activity) {
            Activity activity = (Activity) context;

            // This makes sure that the container activity has implemented
            // the callback interface. If not, it throws an exception
            try {
                mEventListener = (DetailReasonFragment.Callback) activity;
            } catch (ClassCastException e) {
                throw new ClassCastException(activity.toString() + " must implement Listener.");
            }
        }
    }

    public DetailReasonFragment() {
    }

    public static DetailReasonFragment newInstance(int position, String code) {
        DetailReasonFragment fragment = new DetailReasonFragment();
        Bundle args = new Bundle();
        args.putInt("position", position);
        args.putString("code", code);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.detail_reason_fragment, container, false);

        mContext = getContext();
        initBaseFragment(mContext, rootView);

        mResources = mContext.getResources();

        Bundle bundle = getArguments();
        if (bundle != null) {
            mPosition = bundle.getInt("position", 0);
            mCode = bundle.getString("code");
        }

        mRecyclerView = rootView.findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mContext, LinearLayoutManager.VERTICAL)); // Divider
        mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(mContext, mRecyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                //Reason reason = mReasons.get(position);
            }

            @Override
            public void onLongClick(View view, int position) {
                mEventListener.onDetailReasonFragmentEvent(Config.PARAM_FINISH);
            }
        }));

        showBaseProgress();

        mDataManager.setOnRecommendReasonLoaded(new DataManager.RecommendReasonCallback() {
            @Override
            public void onLoad(ArrayList<Reason> reasons) {
                mReasons = reasons;
                //Log.e(TAG, "reasons.size(): " + reasons.size());
                renderData();
            }
        });
        mDataManager.loadRecommendReason((Activity) mContext, mCode);

        return rootView;
    }

    private void renderData() {
        hideBaseProgress();

        mAdapter = new DetailReasonAdapter(mContext, mReasons);
        mRecyclerView.setAdapter(mAdapter);
        //mAdapter.notifyDataSetChanged();
    }

    protected void onToolbarClick() {
        //mScrollView.scrollTo(0, 0);
    }

    public void refreshFragment() {

    }

    public void updateFragmentItem(Item newItem) {
        //parseData();
    }

    public void notifyDataSetChanged() {

    }

    public int getItemSize() {
        return mReasons.size();
    }
}
