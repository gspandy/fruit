package com.example.xu.myapplication.moduleType.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.example.xu.myapplication.R;
import com.example.xu.myapplication.base.BaseFragment;
import com.example.xu.myapplication.moduleType.adapter.MenuAdapter;
import com.example.xu.myapplication.moduleType.listener.OnItemClickListener;
import com.example.xu.myapplication.moduleType.presenter.MenuListPresenter;
import com.example.xu.myapplication.moduleType.viewInterface.IMenuList;

import java.util.ArrayList;

import butterknife.BindView;
import me.yokeyword.fragmentation.anim.DefaultNoAnimator;
import me.yokeyword.fragmentation.anim.FragmentAnimator;

/**
 * A simple {@link Fragment} subclass.
 */
public class MenuListFragment extends BaseFragment<MenuListPresenter> implements IMenuList {

    public static MenuListFragment newInstance(ArrayList<String> data) {
        Bundle args = new Bundle();
        args.putStringArrayList(ARG_MENUS, data);
        MenuListFragment instance = new MenuListFragment();
        instance.setArguments(args);
        return instance;
    }

    @BindView(R.id.recycler)
    RecyclerView recyclerView;

    private MenuAdapter adapter;
    private ArrayList<String> data;
    private int mCurrentPosition = -1;

    private static final String ARG_MENUS = "arg_menus";
    private static final String SAVE_STATE_POSITION = "save_state_position";

    @Override
    public int getLayout() {
        return R.layout.fragment_menu_list;
    }

    @Override
    public void setPresenter() {
        presenter = new MenuListPresenter(this);
    }

    @Override
    public void initData() {
        Bundle args = getArguments();
        if (args != null)
            data = args.getStringArrayList(ARG_MENUS);
    }

    @Override
    public void initView(Bundle savedInstanceState) {

    }

    @Override
    public void setToolbar() {

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        LinearLayoutManager manager = new LinearLayoutManager(_mActivity);
        recyclerView.setLayoutManager(manager);
        adapter = new MenuAdapter(_mActivity,data);
        recyclerView.setAdapter(adapter);

        presenter.showContent(1);
        adapter.setItemChecked(1);

        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(int position, View view, RecyclerView.ViewHolder vh) {
                presenter.showContent(position);
            }
        });


        presenter.isSave(savedInstanceState);
    }

    @Override
    public FragmentAnimator onCreateFragmentAnimator() {
        return new DefaultNoAnimator();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SAVE_STATE_POSITION, mCurrentPosition);
    }

    @Override
    public void setCurrentPosition(int position) {
        mCurrentPosition = position;
    }

    @Override
    public void setAdapterCheck(int position) {
        adapter.setItemChecked(mCurrentPosition);
    }

    @Override
    public int getCurrentPosition() {
        return mCurrentPosition;
    }

    @Override
    public void switchFragment(MenuContentFragment fragment) {
        ((TypeContentFragment) getParentFragment()).switchContentFragment(fragment);
    }

}
