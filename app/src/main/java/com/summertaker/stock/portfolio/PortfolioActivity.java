package com.summertaker.stock.portfolio;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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
import com.summertaker.stock.data.Item;
import com.summertaker.stock.data.Tag;
import com.summertaker.stock.util.SlidingTabLayout;

import java.util.ArrayList;

public class PortfolioActivity extends BaseActivity implements PortfolioFragment.Callback {

    private ArrayList<Tag> mTags = new ArrayList<>();
    private SectionsPagerAdapter mPagerAdapter;
    private ViewPager mViewPager;
    private FloatingActionButton mFab;

    private boolean mIsFirstLoading = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.portfolio_activity);

        mContext = PortfolioActivity.this;

        initBaseActivity(mContext);

        mToolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                runFragment(Config.PARAM_GO_TO_THE_TOP);
            }
        });

        //mToolbar.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
        //    @Override
        //    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        //        mMenuItemRefreshView = mToolbar.findViewById(R.id.action_refresh);
        //    }
        //});

        mFab = findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                //ViewCompat.animate(mFab).rotation(360f).withLayer().setDuration(3000L).setInterpolator(new OvershootInterpolator()).start();
                onFabClick();
            }
        });

        loadData();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.portfolio, menu);
        mMenuItemChart = menu.findItem(R.id.action_chart);
        //setMenuItemChart();
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
            case R.id.action_chart:
                onActionChartClick();
                return true;
            case R.id.action_refresh:
                onActionRefreshClick();
                return true;
            //case R.id.action_finish:
            //    finish();
            //    return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadData() {
        showBaseProgress(2);
        setBaseProgressBar(1);
        mDataManager.setOnItemPriceLoaded(new DataManager.ItemPriceCallback() {
            @Override
            public void onParse(int count) {
                setBaseProgressBar(count + 1);
            }

            @Override
            public void onLoad() {
                renderData();
            }
        });
        mDataManager.loadItemPrice();
    }

    @SuppressLint("RestrictedApi")
    private void renderData() {
        hideBaseProgress();
        mFab.setVisibility(View.VISIBLE);

        if (mIsFirstLoading) {
            mTags = BaseApplication.getInstance().getTags();

            mPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
            mViewPager = findViewById(R.id.viewpager);
            mViewPager.setAdapter(mPagerAdapter);

            //-------------------------------------------------------------------------------------------------------
            // 뷰페이저 간 이동 시 프레그먼트 자동으로 새로고침 방지
            // https://stackoverflow.com/questions/28494637/android-how-to-stop-refreshing-fragments-on-tab-change
            //-------------------------------------------------------------------------------------------------------
            mViewPager.setOffscreenPageLimit(mTags.size());

            SlidingTabLayout slidingTabLayout = findViewById(R.id.slidingTabs);
            slidingTabLayout.setViewPager(mViewPager);
            slidingTabLayout.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    //Toast.makeText(mContext, "onPageSelected(): " + position, Toast.LENGTH_SHORT).show();
                    String pageId = "android:switcher:" + R.id.viewpager + ":" + position;
                    PortfolioFragment fragment = (PortfolioFragment) getSupportFragmentManager().findFragmentByTag(pageId);
                    if (fragment != null) {
                        onFragmentItemSizeChanged(position, fragment.getItemSize());
                        mChartMode = fragment.getChartMode();
                        setMenuItemChart();
                    }
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
        } else {
            refreshAllFragment();
        }

        mIsFirstLoading = false;
    }

    @Override
    public void onPortfolioFragmentEvent(String event) {
        if (event.equals(Config.PARAM_FINISH)) {
            finish();
        } else if (event.equals(Config.PARAM_REFRESH_ALL_FRAGMENT)) {
            refreshAllFragment();
        }
    }

    @Override
    public void onFragmentItemSizeChanged(int position, int itemSize) {
        if (mViewPager != null && position == mViewPager.getCurrentItem()) {
            setActionBarTitleCount(itemSize);
        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            //Tag tag = mTags.get(position);
            return PortfolioFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            return mTags.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTags.get(position).getName();
        }
    }

    public void runFragment(String command) {
        //--------------------------------------------------------------------------------------------
        // 프레그먼트에 이벤트 전달하기
        // https://stackoverflow.com/questions/34861257/how-can-i-set-a-tag-for-viewpager-fragments
        //--------------------------------------------------------------------------------------------
        int i = mViewPager.getCurrentItem();
        String tag = "android:switcher:" + R.id.viewpager + ":" + i;
        PortfolioFragment fragment = (PortfolioFragment) getSupportFragmentManager().findFragmentByTag(tag);

        if (fragment != null) {
            if (command.equals(Config.PARAM_GO_TO_THE_TOP)) {
                fragment.goToTheTop();
            } else if (command.equals(Config.PARAM_DO_REFRESH)) {
                fragment.refreshFragment();
            } else if (command.equals(Config.PARAM_DATA_CHANGED)) {
                fragment.notifyDataSetChanged();
            } else if (command.equals(Config.PARAM_TOGGLE_CHART)) {
                fragment.toggleChart();
            }
        }
    }

    public void refreshFragment() {
        //--------------------------------------------------------------------------------------------
        // 프레그먼트에 이벤트 전달하기
        // https://stackoverflow.com/questions/34861257/how-can-i-set-a-tag-for-viewpager-fragments
        //--------------------------------------------------------------------------------------------
        // 개별 프레그먼트 새로 고침
        String tag = "android:switcher:" + R.id.viewpager + ":" + mViewPager.getCurrentItem();
        PortfolioFragment f = (PortfolioFragment) getSupportFragmentManager().findFragmentByTag(tag);
        if (f != null) {
            f.refreshFragment();
        }
    }

    public void refreshAllFragment() {
        //--------------------------------------------------------------------------------------------
        // 프레그먼트에 이벤트 전달하기
        // https://stackoverflow.com/questions/34861257/how-can-i-set-a-tag-for-viewpager-fragments
        //--------------------------------------------------------------------------------------------
        // 모든 프레그먼트 새로 고침
        for (int i = 0; i < mPagerAdapter.getCount(); i++) {
            String tag = "android:switcher:" + R.id.viewpager + ":" + i;
            PortfolioFragment f = (PortfolioFragment) getSupportFragmentManager().findFragmentByTag(tag);
            if (f != null) {
                f.refreshFragment();
            }
        }
    }

    public void updateFragmentItem(Item item) {
        //--------------------------------------------------------------------------------------------
        // 프레그먼트의 아이템 새로 고침
        // https://stackoverflow.com/questions/34861257/how-can-i-set-a-tag-for-viewpager-fragments
        //--------------------------------------------------------------------------------------------
        for (int i = 0; i < mPagerAdapter.getCount(); i++) {
            String tag = "android:switcher:" + R.id.viewpager + ":" + i;
            PortfolioFragment f = (PortfolioFragment) getSupportFragmentManager().findFragmentByTag(tag);
            if (f != null) {
                f.updateFragmentItem(item);
            }
        }
    }

    /*
    private void startRefreshAnimation() {
        if (mMenuItemRefreshView != null) {
            mMenuItemRefreshView.startAnimation(mRotateAnimation);
        }
    }

    private void stopRefreshAnimation() {
        if (mMenuItemRefreshView != null) {
            mMenuItemRefreshView.clearAnimation();
        }
    }
    */

    private void onActionRefreshClick() {
        loadData();
        //refreshFragment();
    }

    private void onFabClick() {
        refreshFragment();
    }

    @Override
    protected void onSwipeRight() {

    }

    @Override
    protected void onSwipeLeft() {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            String param = data.getStringExtra(Config.KEY_PARAM);
            if (param != null) {
                //Toast.makeText(mContext, param, Toast.LENGTH_SHORT).show();

                if (param.equals(Config.PARAM_DATA_CHANGED)) {
                    // 종목 상세 화면에서 내용이 변경되어 돌아 온 경우
                    String code = data.getStringExtra("code");
                    String tagIds = data.getStringExtra("tagIds");
                    //Toast.makeText(mContext, code + ": " + tagIds, Toast.LENGTH_LONG).show();

                    // 변경된 포트폴리오 태그 정보 업데이트
                    Item item = null;
                    for (Item bi : BaseApplication.getInstance().getItemPrices()) {
                        if (bi.getCode().equals(code)) {
                            bi.setTagIds(tagIds);
                            item = bi;
                            break;
                        }
                    }

                    if (item != null) {
                        updateFragmentItem(item);
                    }
                }
            }
        }
    }
}
