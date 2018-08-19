package com.summertaker.stock.detail;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.summertaker.stock.R;
import com.summertaker.stock.SearchActivity;
import com.summertaker.stock.common.BaseActivity;
import com.summertaker.stock.common.BaseApplication;
import com.summertaker.stock.common.BaseFragment;
import com.summertaker.stock.common.Config;
import com.summertaker.stock.common.DataManager;
import com.summertaker.stock.data.Item;
import com.summertaker.stock.data.Site;
import com.summertaker.stock.util.SlidingTabLayout;

import java.util.ArrayList;

public class DetailActivity extends BaseActivity implements DetailInfoFragment.Callback,
        DetailReasonFragment.Callback,
        DetailNewsFragment.Callback {

    private String mCode;
    private Item mItem;

    private View mMenuItemRefreshView;

    //private SwipeRefreshLayout mSwipeRefreshLayout;
    private android.support.v4.widget.NestedScrollView mScrollView;

    //private boolean mIsLoading = false;

    private ArrayList<Site> mSites = new ArrayList<>();
    private SectionsPagerAdapter mPagerAdapter;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_activity);

        mContext = DetailActivity.this;

        Intent intent = getIntent();
        mCode = intent.getStringExtra("code");

        initBaseActivity(mContext);
        mToolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onToolbarClick();
            }
        });

        showBaseProgress(0);

        mDataManager.setOnItemLoaded(new DataManager.ItemCallback() {
            @Override
            public void onLoad(Item item) {
                mItem = item;
                BaseApplication.getInstance().setItem(item);
                init();
            }
        });
        mDataManager.loadItem(mCode);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail, menu);
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
        // 액션바 제목
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null && mItem.getName() != null) {
            String title = mItem.getName(); // 종목이름
            if (mItem.getNor() > 0) { // 추천수
                title = title + " (" + mItem.getNor() + ")";
            }
            actionBar.setTitle(title);
        }

        mToolbar.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                mMenuItemRefreshView = mToolbar.findViewById(R.id.action_refresh);
                if (mMenuItemRefreshView != null) {
                    mMenuItemRefreshView.setVisibility(View.GONE);
                }
            }
        });

        mSites = BaseApplication.getInstance().getDetailPagerItems();

        // 추천 사유
        if (mItem.getNor() > 0) {
            if (mSites.size() == 2) {
                mSites.add(new Site(Config.KEY_DETAIL_REASON, getString(R.string.pager_item_detail_reason), ""));
            }
        } else {
            if (mSites.size() == 3) {
                mSites.remove(2);
            }
        }

        mPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = findViewById(R.id.viewpager);
        mViewPager.setAdapter(mPagerAdapter);

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
                //RecommendFragment fragment = (RecommendFragment) getSupportFragmentManager().findFragmentByTag(tag);
                //onFragmentItemSizeChange(position, fragment.getItemSize());
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        hideBaseProgress();
    }

    @Override
    public void onDetailInfoFragmentEvent(String event, String field, String value) {
        if (event.equals(Config.PARAM_DATA_CHANGED)) {
            // 호출 액티비티에 결과 전달하기
            Intent intent = new Intent();
            intent.putExtra(Config.KEY_PARAM, Config.PARAM_DATA_CHANGED);
            intent.putExtra("code", mCode);
            intent.putExtra(field, value);
            setResult(RESULT_OK, intent);
        } else if (event.equals(Config.PARAM_FINISH)) {
            finish();
        }
    }

    @Override
    public void onDetailNewsFragmentEvent(String event) {
        if (event.equals(Config.PARAM_FINISH)) {
            finish();
        }
    }

    @Override
    public void onDetailReasonFragmentEvent(String event) {
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
            Site site = mSites.get(position);
            if (site.getId().equals(Config.KEY_DETAIL_INFO)) {
                return DetailInfoFragment.newInstance(position, mCode);
            } else if (site.getId().equals(Config.KEY_DETAIL_NEWS)) {
                return DetailNewsFragment.newInstance(position, mCode);
            } else {
                return DetailReasonFragment.newInstance(position, mCode);
            }
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
        BaseFragment fragment = (BaseFragment) getSupportFragmentManager().findFragmentByTag(tag);

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

    private void setActivityResult() {
        // 호출 액티비티에 결과 전달하기
        Intent intent = new Intent();
        intent.putExtra(Config.KEY_PARAM, Config.PARAM_DATA_CHANGED);
        intent.putExtra("code", mCode);
        intent.putExtra("tagIds", mItem.getTagIds());
        setResult(RESULT_OK, intent);
    }

    @Override
    protected void onSwipeRight() {

    }

    @Override
    protected void onSwipeLeft() {

    }
}
