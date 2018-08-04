package com.summertaker.stock;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.summertaker.stock.R;
import com.summertaker.stock.common.BaseActivity;
import com.summertaker.stock.common.BaseApplication;
import com.summertaker.stock.common.Config;
import com.summertaker.stock.data.Item;
import com.summertaker.stock.data.Tag;
import com.summertaker.stock.helper.OnStartDragListener;
import com.summertaker.stock.helper.SimpleItemTouchHelperCallback;
import com.summertaker.stock.util.RecyclerTouchListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class TagListActivity extends BaseActivity implements OnStartDragListener {

    //private View mMenuItemRefreshView;

    private ArrayList<Tag> mTags = new ArrayList<>();
    private TagListAdapter mAdapter;
    private RecyclerView mRecyclerView;

    private ItemTouchHelper mItemTouchHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tag_list_activity);

        mContext = TagListActivity.this;

        initBaseActivity(mContext);

        /*
        mToolbar.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                mMenuItemRefreshView = mToolbar.findViewById(R.id.action_refresh);
                if (mMenuItemRefreshView != null) {
                    mMenuItemRefreshView.setVisibility(View.GONE);
                }
            }
        });
        */

        mAdapter = new TagListAdapter(mContext, mTags, this);
        mAdapter.setHasStableIds(true);

        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mContext, LinearLayoutManager.VERTICAL)); // Divider
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(mContext, mRecyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Tag tag = mTags.get(position);
                //Toast.makeText(mContext, tag.getId() + "", Toast.LENGTH_SHORT).show();

                Intent edit = new Intent(mContext, TagEditActivity.class);
                edit.putExtra("id", tag.getId());
                edit.putExtra("name", tag.getName());
                edit.putExtra("bgc", tag.getBgc());
                edit.putExtra("fgc", tag.getFgc());
                startActivityForResult(edit, Config.ACTIVITY_REQUEST_CODE);
            }

            @Override
            public void onLongClick(View view, int position) {
                //Tag tag = mTags.get(position);
            }
        }));

        // 드래그 정렬
        // Part 1. https://medium.com/@ipaulpro/drag-and-swipe-with-recyclerview-b9456d2b1aaf
        // Part 2. https://medium.com/@ipaulpro/drag-and-swipe-with-recyclerview-6a6f0c422efd
        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mAdapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(mRecyclerView);

        loadData();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.tag_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_add:
                Intent edit = new Intent(this, TagEditActivity.class);
                startActivityForResult(edit, Config.ACTIVITY_REQUEST_CODE);
                return true;
            case R.id.action_finish:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadData() {
        mTags.clear();

        for (Tag bt : BaseApplication.getInstance().getTags()) {
            Tag tag = new Tag();
            tag.setId(bt.getId());
            tag.setName(bt.getName());
            tag.setBgc(bt.getBgc());
            tag.setFgc(bt.getFgc());
            mTags.add(tag);
        }

        renderData();
    }

    private void renderData() {
        mAdapter.notifyDataSetChanged();
        setActionBarTitleCount(mTags.size());
    }

    private void refresh() {
        loadData();
    }

    public void goToTheTop() {
        LinearLayoutManager manager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
        if (manager != null) {
            manager.scrollToPositionWithOffset(0, 0);
        }
    }

    public void onToolbarClick() {
        goToTheTop();
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder, long id) {
        mItemTouchHelper.startDrag(viewHolder);
    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        // 드래그 끝난 후 순서 저장하기
        if (actionState == ItemTouchHelper.ACTION_STATE_IDLE) {
            BaseApplication.getInstance().getTags().clear();
            BaseApplication.getInstance().getTags().addAll(mTags);
            long id = 1;
            for (Tag tag : BaseApplication.getInstance().getTags()) {
                tag.setId(id);
                id++;
            }
            mDataManager.writeTags();

            /*
            List<String> list = new LinkedList<>();
            for (Tag tag : mTags) {
                list.add(String.valueOf(tag.getId()));
            }
            String ids = TextUtils.join(",", list);
            Log.e(TAG, "ids: " + ids);
            //postData(ids);
            */
        }
    }

    /*
    private void postData(final String ids) {
        mMenuItemRefreshView.setVisibility(View.VISIBLE);
        //mMenuItemRefreshView.startAnimation(mRotateAnimation);

        StringRequest strReq = new StringRequest(Request.Method.POST, Config.URL_TAG_SORT, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //Log.e(TAG, "response\n" + response);
                BaseApplication.getInstance().getTags().clear();

                try {
                    JSONObject data = new JSONObject(response);
                    mDataManager.parseTags(data);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                mMenuItemRefreshView.clearAnimation();
                mMenuItemRefreshView.setVisibility(View.GONE);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(mContext, error.getMessage(), Toast.LENGTH_LONG).show();
                Log.e(TAG, "ERROR: " + error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("ids", ids);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                return params;
            }
        };

        BaseApplication.getInstance().addToRequestQueue(strReq, TAG);
    }
    */

    @Override
    protected void onSwipeRight() {
        finish();
    }

    @Override
    protected void onSwipeLeft() {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            refresh();
        }
    }
}
