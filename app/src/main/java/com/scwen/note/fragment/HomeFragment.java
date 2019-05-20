package com.scwen.note.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.scwen.note.NoteBean;
import com.scwen.note.NoteEditorActivity;
import com.scwen.note.R;
import com.scwen.note.adapter.NoteListAdapter;

import org.litepal.LitePal;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by scwen on 2019/4/27.
 * QQ ：811733738
 * 作用：
 */
public class HomeFragment extends Fragment {
    protected View mRootView;

    protected Unbinder mUnbinder;
    @BindView(R.id.tv_home_title)
    TextView mTvHomeTitle;
    @BindView(R.id.iv_search_note)
    ImageView mIvSearchNote;
    @BindView(R.id.toolbar_main)
    Toolbar mToolbarMain;
    @BindView(R.id.rv_home_note)
    RecyclerView mRvHomeNote;
    @BindView(R.id.iv_loading)
    ProgressBar mIvLoading;
    @BindView(R.id.fab_home_new_note)
    FloatingActionButton mFabHomeNewNote;
    private NoteListAdapter mListAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.fragment_home, container, false);
        } else {
            ViewGroup parent = (ViewGroup) mRootView.getParent();
            if (parent != null) {
                parent.removeView(mRootView);
            }
        }
        mUnbinder = ButterKnife.bind(this, mRootView);
        initView();
        return mRootView;
    }

    private void initView() {
        initRy();
    }

    private void initRy() {
        mRvHomeNote.setLayoutManager(new LinearLayoutManager(getActivity()));
        mListAdapter = new NoteListAdapter(null);
        mRvHomeNote.setAdapter(mListAdapter);

    }

    @OnClick({R.id.iv_search_note, R.id.fab_home_new_note})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.iv_search_note:
                break;
            case R.id.fab_home_new_note:
                NoteEditorActivity.startIntent(getActivity());
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    public void loadData() {
        List<NoteBean> noteBeanList = LitePal.order("createtime desc").where("is_del != 1").find(NoteBean.class);

        if (noteBeanList != null) {
            mListAdapter.setNewData(noteBeanList);
            mIvLoading.setVisibility(View.GONE);
        }
    }
}
