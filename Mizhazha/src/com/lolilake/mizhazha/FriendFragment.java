package com.lolilake.mizhazha;

import java.util.List;

import android.app.AlertDialog;
import android.app.ListFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

public class FriendFragment extends ListFragment{
	
	public static final String TAG = FriendFragment.class.getSimpleName();
	
	protected List<ParseUser> mFriends;
	protected ParseRelation<ParseUser> mFriendsRelation;
	protected ParseUser mCurrentUser;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_friend, container,
				false);
		return rootView;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		mCurrentUser = ParseUser.getCurrentUser();
		mFriendsRelation = mCurrentUser.getRelation(ParseConstants.KEY_RELATION);
		ParseQuery<ParseUser> query = mFriendsRelation.getQuery();
		query.addAscendingOrder(ParseConstants.KEY_USERNAME);
		
		//sometimes it cause race condition after you just edit your friends.
		//the back-end didn't sync up before I call this method.
		getActivity().setProgressBarIndeterminateVisibility(true);
		query.findInBackground(new FindCallback<ParseUser>() {

			@Override
			public void done(List<ParseUser> friends, ParseException e) {
				
				getActivity().setProgressBarIndeterminateVisibility(false);
				if(e == null){
					mFriends = friends;
					String[] friendsUsername = new String[friends.size()];
					
					int i=0;
					for(ParseUser friend : friends){
						friendsUsername[i++]=friend.getUsername();
					}
					ArrayAdapter<String> adapter = new ArrayAdapter<String>(getListView().getContext(),
							android.R.layout.simple_list_item_1, 
							friendsUsername);
					setListAdapter(adapter);
				}else {
					Log.e(TAG, e.getMessage());
					AlertDialog.Builder builder = new AlertDialog.Builder(getListView().getContext());
					builder.setMessage(e.getMessage())
						.setTitle(R.string.error_title)
						.setPositiveButton(android.R.string.ok, null);
					AlertDialog dialog = builder.create();
					dialog.show();
				}
			}
		});
	}
	
}
