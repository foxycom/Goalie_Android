package com.github.q115.goalie_android.ui.friends;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.github.q115.goalie_android.R;
import com.github.q115.goalie_android.models.User;

/**
 * Created by Qi on 8/4/2017.
 */

public class FriendsListFragment extends Fragment implements FriendsListView {
    private FriendsListPresenter mPresenter;
    private RecyclerView mFriendsList;
    private ProgressDialog mProgressDialog;

    public FriendsListFragment() {
    }

    public static FriendsListFragment newInstance() {
        return new FriendsListFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_friends_list, container, false);

        mFriendsList = rootView.findViewById(R.id.friends_list);
        mFriendsList.setLayoutManager(new LinearLayoutManager(getContext()));
        mFriendsList.setAdapter(new FriendsListRecycler(getActivity()));

        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setMessage(getString(R.string.updating));
        mProgressDialog.setCancelable(false);

        return rootView;
    }


    @Override
    public void onResume() {
        super.onResume();
        mPresenter.start();
        reload(false);
    }

    @Override
    public void setPresenter(FriendsListPresenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        User user = ((FriendsListRecycler) mFriendsList.getAdapter()).getItem(item.getOrder());
        if (user == null)
            return super.onContextItemSelected(item);

        switch (item.getItemId()) {
            case R.string.refresh:
                mProgressDialog.show();
                mPresenter.refresh(user.username);
                return true;
            case R.string.delete:
                mPresenter.delete(user.username);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onAddContactDialog(User user) {
        if (getView() != null) {
            RecyclerView friendsList = getView().findViewById(R.id.friends_list);
            FriendsListRecycler friendsRecycler = (FriendsListRecycler) friendsList.getAdapter();
            friendsRecycler.addUserToList(user);
        }
    }

    @Override
    public void reload(boolean shouldReloadList) {
        if (getView() != null) {
            if(mProgressDialog.isShowing())
                mProgressDialog.cancel();

            RecyclerView friendsList = getView().findViewById(R.id.friends_list);

            if (shouldReloadList)
                ((FriendsListRecycler)friendsList.getAdapter()).notifyDataSetHasChanged();

            if (friendsList.getAdapter().getItemCount() == 0) {
                friendsList.setVisibility(View.GONE);
                getView().findViewById(R.id.empty).setVisibility(View.VISIBLE);
            } else {
                friendsList.setVisibility(View.VISIBLE);
                getView().findViewById(R.id.empty).setVisibility(View.GONE);
            }
        }
    }
}
