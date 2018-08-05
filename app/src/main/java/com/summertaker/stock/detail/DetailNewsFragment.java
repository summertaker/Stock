package com.summertaker.stock.detail;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.summertaker.stock.R;
import com.summertaker.stock.common.BaseApplication;
import com.summertaker.stock.common.BaseFragment;
import com.summertaker.stock.common.Config;
import com.summertaker.stock.data.Item;
import com.summertaker.stock.data.News;
import com.summertaker.stock.parser.DaumNewsParser;
import com.summertaker.stock.util.RecyclerTouchListener;

import java.util.ArrayList;

public class DetailNewsFragment extends BaseFragment {

    private Callback mEventListener;

    //private Resources mResources;

    //private int mPosition = -1;

    private String mCode;
    private Item mItem;

    private ArrayList<News> mNewsList = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private DetailNewsAdapter mAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    // Container Activity must implement this interface
    public interface Callback {
        void onDetailNewsFragmentEvent(String event);

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
                mEventListener = (Callback) activity;
            } catch (ClassCastException e) {
                throw new ClassCastException(activity.toString() + " must implement Listener.");
            }
        }
    }

    public DetailNewsFragment() {
    }

    public static DetailNewsFragment newInstance(int position, String code) {
        DetailNewsFragment fragment = new DetailNewsFragment();
        Bundle args = new Bundle();
        args.putInt("position", position);
        args.putString("code", code);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.detail_news_fragment, container, false);

        mContext = getContext();
        initBaseFragment(mContext, rootView);

        //mResources = mContext.getResources();
        Bundle bundle = getArguments();
        if (bundle != null) {
            //mPosition = bundle.getInt("position", 0);
            mCode = bundle.getString("code");
        }

        mItem = BaseApplication.getInstance().getItem();

        mAdapter = new DetailNewsAdapter(mContext, mNewsList, mItem);

        mRecyclerView = rootView.findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mContext, LinearLayoutManager.VERTICAL)); // Divider
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(mContext, mRecyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                News news = mNewsList.get(position);

                Intent intent = new Intent(mContext, ItemNewsActivity.class);
                intent.putExtra("code", mItem.getCode());
                intent.putExtra("title", news.getTitle());
                intent.putExtra("publishedText", news.getPublishedText());
                intent.putExtra("elapsed", news.getElapsed());
                intent.putExtra("url", news.getUrl());
                startActivity(intent);
            }

            @Override
            public void onLongClick(View view, int position) {
                mEventListener.onDetailNewsFragmentEvent(Config.PARAM_FINISH);
            }
        }));

        mSwipeRefreshLayout = rootView.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData();
            }
        });

        showBaseProgress();

        loadData();

        return rootView;
    }

    private void loadData() {
        String url = Config.URL_DAUM_NEWS_LIST + mCode;
        //Log.e(TAG, "url: " + url);

        StringRequest strReq = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //Log.e(TAG, "response:\n" + response);
                parseData(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                parseData("");
            }
        });

        BaseApplication.getInstance().addToRequestQueue(strReq, TAG);
    }

    private void parseData(String response) {
        mNewsList.clear();

        DaumNewsParser daumNewsParser = new DaumNewsParser();
        daumNewsParser.parseList(mItem, response, mNewsList);

        renderData();
    }

    private void renderData() {
        hideBaseProgress();

        mAdapter.notifyDataSetChanged();
        mSwipeRefreshLayout.setRefreshing(false);
    }

    //protected void onToolbarClick() {
    //    //mScrollView.scrollTo(0, 0);
    //}

    public void goToTheTop() {
        LinearLayoutManager manager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
        manager.scrollToPositionWithOffset(0, 0);
    }

    public void refreshFragment() {

    }

    public void updateFragmentItem(Item newItem) {
        //parseData();
    }

    public void notifyDataSetChanged() {

    }

    public int getItemSize() {
        return mNewsList.size();
    }
}
