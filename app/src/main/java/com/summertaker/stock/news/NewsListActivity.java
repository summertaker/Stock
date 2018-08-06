package com.summertaker.stock.news;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.summertaker.stock.R;
import com.summertaker.stock.SearchActivity;
import com.summertaker.stock.common.BaseActivity;
import com.summertaker.stock.common.BaseApplication;
import com.summertaker.stock.common.Config;
import com.summertaker.stock.common.DataManager;
import com.summertaker.stock.data.Site;
import com.summertaker.stock.util.SlidingTabLayout;

import java.util.ArrayList;

public class NewsListActivity extends BaseActivity implements NewsListFragment.Callback {

    //private String mCode;
    //private Item mItem;

    //private View mMenuItemRefreshView;

    //private SwipeRefreshLayout mSwipeRefreshLayout;
    //private android.support.v4.widget.NestedScrollView mScrollView;

    //private boolean mIsLoading = false;

    private ArrayList<Site> mSites = new ArrayList<>();
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.news_list_activity);

        mContext = NewsListActivity.this;
        initBaseActivity(mContext);

        mToolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onToolbarClick();
            }
        });

        if (BaseApplication.getInstance().getItemPrices().size() == 0) {
            showBaseProgress(2); // 전종목 로드하기 (뉴스 내용에서 종목명 찾기 위해)
            setBaseProgressBar(1);
            mDataManager.setOnItemPriceLoaded(new DataManager.ItemPriceCallback() {
                @Override
                public void onParse(int count) {
                    setBaseProgressBar(count + 1);
                }

                @Override
                public void onLoad() {
                    init();
                }
            });
            mDataManager.loadItemPrice();
        } else {
            showBaseProgress(0);
            init();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.news_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_search:
                Intent search = new Intent(this, SearchActivity.class);
                startActivity(search);
                return true;
            //case R.id.action_settings:
            //    Intent wordCategory = new Intent(this, WordCategoryActivity.class);
            //    startActivity(wordCategory);
            //    return true;
            case R.id.action_finish:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void init() {
        hideBaseProgress();

        //mToolbar.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
        //    @Override
        //    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        //        mMenuItemRefreshView = mToolbar.findViewById(R.id.action_refresh);
        //        if (mMenuItemRefreshView != null) {
        //            mMenuItemRefreshView.setVisibility(View.GONE);
        //        }
        //    }
        //});

        mSites = BaseApplication.getInstance().getNewsPagerItems();

        SectionsPagerAdapter pagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = findViewById(R.id.viewpager);
        mViewPager.setAdapter(pagerAdapter);

        //-------------------------------------------------------------------------------------------------------
        // 뷰페이저 간 이동 시 프레그먼트 자동으로 새로고침 방지
        // https://stackoverflow.com/questions/28494637/android-how-to-stop-refreshing-fragments-on-tab-change
        //-------------------------------------------------------------------------------------------------------
        mViewPager.setOffscreenPageLimit(mSites.size());

        SlidingTabLayout slidingTabLayout = findViewById(R.id.slidingTabs);
        slidingTabLayout.setViewPager(mViewPager);
        slidingTabLayout.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                //Toast.makeText(mContext, "onPageSelected(): " + position, Toast.LENGTH_SHORT).show();
                //String tag = "android:switcher:" + R.id.viewpager + ":" + position;
                //RecoFragment fragment = (RecoFragment) getSupportFragmentManager().findFragmentByTag(tag);
                //onFragmentItemSizeChange(position, fragment.getItemSize());
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        hideBaseProgress();
    }

    @Override
    public void onNewsListFragmentEvent(String event) {
        if (event.equals(Config.PARAM_FINISH)) {
            finish();
        }
    }

    @Override
    public void onFragmentItemSizeChanged(int position, int itemSize) {

    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return NewsListFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            return mSites.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mSites.get(position).getTitle();
        }
    }

    public void runFragment(String command) {
        //--------------------------------------------------------------------------------------------
        // 프레그먼트에 이벤트 전달하기
        // https://stackoverflow.com/questions/34861257/how-can-i-set-a-tag-for-viewpager-fragments
        //--------------------------------------------------------------------------------------------
        String tag = "android:switcher:" + R.id.viewpager + ":" + mViewPager.getCurrentItem();
        NewsListFragment fragment = (NewsListFragment) getSupportFragmentManager().findFragmentByTag(tag);

        if (fragment != null) {
            if (command.equals(Config.PARAM_GO_TO_THE_TOP)) {
                fragment.goToTheTop();
            } else if (command.equals(Config.PARAM_DO_REFRESH)) {
                fragment.refreshFragment();
            }
        }
    }

    private void onToolbarClick() {
        runFragment(Config.PARAM_GO_TO_THE_TOP);
    }

    @Override
    protected void onSwipeRight() {

    }

    @Override
    protected void onSwipeLeft() {

    }
}
