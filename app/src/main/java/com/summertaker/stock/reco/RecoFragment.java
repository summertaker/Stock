package com.summertaker.stock.reco;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.summertaker.stock.R;
import com.summertaker.stock.common.BaseApplication;
import com.summertaker.stock.common.BaseFragment;
import com.summertaker.stock.common.Config;
import com.summertaker.stock.common.DataManager;
import com.summertaker.stock.data.Item;
import com.summertaker.stock.data.Site;
import com.summertaker.stock.detail.DetailActivity;
import com.summertaker.stock.util.RecyclerTouchListener;

import java.util.ArrayList;

public class RecoFragment extends BaseFragment {

    private Callback mEventListener;

    private int mPosition = -1;

    private boolean mIsLoading = false;

    private Site mSite;

    private ArrayList<Item> mItems = new ArrayList<>();
    private RecoAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private DividerItemDecoration mDividerItemDecoration;

    private int mLowestPrice; // 최저가
    private int mHighestPrice; // 최고가
    private float mRateOfFluctuation; // 등락률

    private boolean mChartMode = false;

    // Container Activity must implement this interface
    public interface Callback {
        void onRecoFragmentEvent(String event);

        void onFragmentItemSizeChange(int position, int itemSize);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof Activity) {
            Activity activity = (Activity) context;

            // This makes sure that the container activity has implemented
            // the callback interface. If not, it throws an exception
            try {
                mEventListener = (Callback) activity;
            } catch (ClassCastException e) {
                throw new ClassCastException(activity.toString() + " must implement Listener.");
            }
        }
    }

    public RecoFragment() {
    }

    public static RecoFragment newInstance(int position) {
        RecoFragment fragment = new RecoFragment();
        Bundle args = new Bundle();
        args.putInt("position", position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.reco_fragment, container, false);

        mContext = getContext();
        initBaseFragment(mContext, rootView);

        Bundle bundle = getArguments();
        if (bundle != null) {
            mPosition = getArguments().getInt("position", 0);
            mSite = BaseApplication.getInstance().getRecoPagerItems().get(mPosition);
        }

        mAdapter = new RecoAdapter(mContext, mSite.getId(), mItems);
        mAdapter.setHasStableIds(true);

        mRecyclerView = rootView.findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(mContext, mRecyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Item item = mItems.get(position);
                //Toast.makeText(mContext, item.getName(), Toast.LENGTH_SHORT).show();
                BaseApplication.getInstance().setItem(item);

                Intent intent = new Intent(mContext, DetailActivity.class);
                intent.putExtra("code", item.getCode());
                startActivityForResult(intent, Config.ACTIVITY_REQUEST_CODE);
            }

            @Override
            public void onLongClick(View view, int position) {
                mEventListener.onRecoFragmentEvent(Config.PARAM_FINISH);
                //Util.startKakaoStockDeepLink(mContext, mItems.get(position).getCode());
                //mDataManager.updateMyItem(mItems.get(position).getCode(), Config.KEY_FAVORITES);
                //mEventListener.onFragmentEvent(Config.PARAM_DATA_CHANGED);
            }
        }));

        mDividerItemDecoration = new DividerItemDecoration(mContext, LinearLayoutManager.VERTICAL);

        mLowestPrice = BaseApplication.getInstance().getIntSetting(Config.SETTING_RECO_LOWEST_PRICE); // 최저가
        mHighestPrice = BaseApplication.getInstance().getIntSetting(Config.SETTING_RECO_HIGHEST_PRICE); // 최고가
        mRateOfFluctuation = BaseApplication.getInstance().getFloatSetting(Config.SETTING_RECO_RATE_OF_FLUCTUATION); // 등락률
        //Log.e(TAG, mLowestPrice + " ~ " + mHighestPrice + ", " + mRateOfFluctuation);

        showBaseProgress();

        //final DataManager dm = new DataManager(mContext);

        if (mSite.getId().equals(Config.KEY_RECO_RETURN)) { // 추천 종목 수익률
            mDataManager.setOnRecoReturnItemLoaded(new DataManager.RecoReturnItemCallback() {
                @Override
                public void onLoad(ArrayList<Item> items) {
                    parseData(items);
                }
            });
            mDataManager.loadRecoReturnItem((Activity) mContext);
        } else if (mSite.getId().equals(Config.KEY_RECO_TOP)) { // 추천수 상위
            mDataManager.setOnRecoTopItemLoaded(new DataManager.RecoTopItemCallback() {
                @Override
                public void onLoad() {
                    parseData(BaseApplication.getInstance().getRecoTopItems());
                }
            });
            mDataManager.loadRecoTopItem((Activity) mContext);
        } else if (mSite.getId().equals(Config.KEY_RECO_CURRENT)) { // 현재 추천 종목
            mDataManager.setOnRecoCurrentItemLoaded(new DataManager.RecoCurrentItemCallback() {
                @Override
                public void onLoad(ArrayList<Item> items) {
                    parseData(items);
                }
            });
            mDataManager.loadRecoCurrentItem((Activity) mContext);
        }

        return rootView;
    }

    /*
    private void parseRecoTop() {
        for (Item bi : BaseApplication.getInstance().getItemPrices()) {
            if (bi.isRecoTopItem() && isValidItem(bi)) {
                mItems.add(bi);
            }
        }

        // 추천수로 정렬
        Collections.sort(mItems, new Comparator<Item>() {
            @Override
            public int compare(Item a, Item b) {
                if (a.getNor() < b.getNor()) {
                    return 1;
                } else if (a.getNor() > b.getNor()) {
                    return -1;
                }
                return 0;
            }
        });

        long id = 1;
        for (Item item : mItems) {
            item.setId(id);
            id++;
        }

        renderData();
    }
    */

    private void parseData(ArrayList<Item> items) {
        mItems.clear();

        long id = 1;
        for (Item item : items) {
            for (Item bi : BaseApplication.getInstance().getItemPrices()) {
                if (bi.getCode().equals(item.getCode())) {
                    if (isValidItem(bi)) {
                        item.setId(id);
                        item.setPrice(bi.getPrice());   // 현재가
                        item.setPof(bi.getPof());       // 전일비
                        item.setRof(bi.getRof());       // 등락률
                        item.setTagIds(bi.getTagIds()); // 태그
                        //item.setNor(bi.getNor());       // 추천수
                        mItems.add(item);
                        id++;

                        //if (mSite.getId().equals(Config.KEY_RECO_RETURN)) {
                        //    Log.e(TAG, item.getName() + " " + item.getRor());
                        //}
                    }
                }
            }
        }

        /*
        if (mSite.getId().equals(Config.KEY_RECO_TOP)) {
            // 추천수 정렬
            Collections.sort(mItems, new Comparator<Item>() {
                @Override
                public int compare(Item a, Item b) {
                    if (a.getNor() < b.getNor()) {
                        return 1;
                    } else if (a.getNor() > b.getNor()) {
                        return -1;
                    }
                    return 0;
                }
            });

            //long id = 1;
            //for (Item item : mItems) {
            //    item.setId(id);
            //    id++;
            //}
        }
        */

        renderData();
    }

    private void renderData() {
        for (Item item : mItems) {
            item.setChart(mChartMode);
        }

        mRecyclerView.removeItemDecoration(mDividerItemDecoration);
        if (!mChartMode) {
            mRecyclerView.addItemDecoration(mDividerItemDecoration); // Divider
        }

        hideBaseProgress();

        mAdapter.notifyDataSetChanged();
        mEventListener.onFragmentItemSizeChange(mPosition, mItems.size());
        //mEventListener.onFragmentEvent(Config.PARAM_LOAD_FINISHED);
        mIsLoading = false;
    }

    private boolean isValidItem(Item item) {
        if (mLowestPrice > 0 && item.getPrice() < mLowestPrice) { // 최저가
            return false;
        }

        if (mHighestPrice > 0 && item.getPrice() > mHighestPrice) { // 최고가
            return false;
        }

        if (mRateOfFluctuation > 0 && item.getPrice() < mRateOfFluctuation) { // 등락률
            return false;
        }

        return true;
    }

    public void goToTheTop() {
        LinearLayoutManager manager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
        manager.scrollToPositionWithOffset(0, 0);
    }

    public void refreshFragment(boolean isActionRefresh) {
        if (!mIsLoading) {
            mEventListener.onRecoFragmentEvent(Config.PARAM_LOAD_STARTED);
            //loadData();
        }
    }

    public void updateFragmentItem(Item newItem) {
        for (Item item : mItems) {
            if (item.getCode().equals(newItem.getCode())) {
                item.setTagIds(newItem.getTagIds());
                //break; // 중복 추천 종목이 있기에 주석 처리
            }
        }

        mAdapter.notifyDataSetChanged();
    }

    public void toggleChart() {
        mChartMode = !mChartMode;
        renderData();
    }

    public void notifyDataSetChanged() {
        mAdapter.notifyDataSetChanged();
    }

    public int getItemSize() {
        return mItems.size();
    }
}
