package com.lolilake.mizhazha;

import java.util.HashMap;
import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class EditFriendsActivity extends ListActivity {
	
	public static final String TAG = EditFriendsActivity.class.getSimpleName();
	
	protected List<ParseUser> mUsers;
	protected ParseRelation<ParseUser> mFriendsRelation;
	protected ParseUser mCurrentUser;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_edit_friends);
		
		getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		if(getListView().isItemChecked(position)){
			mFriendsRelation.add(mUsers.get(position));
		}else {
			mFriendsRelation.remove(mUsers.get(position));			
		}
		mCurrentUser.saveInBackground(new SaveCallback() {
			
			@Override
			public void done(ParseException e) {
				if(e == null){
					//Success
				}else{
					Log.e(TAG, e.getMessage());
				}
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		mCurrentUser = ParseUser.getCurrentUser();
		mFriendsRelation = mCurrentUser.getRelation(ParseConstants.KEY_RELATION);
		
		ParseQuery<ParseUser> query = ParseUser.getQuery();
		query.orderByAscending(ParseConstants.KEY_USERNAME);
		query.setLimit(1000);
		
		setProgressBarIndeterminateVisibility(true);
		query.findInBackground(new FindCallback<ParseUser>() {
			
			@Override
			public void done(List<ParseUser> users, ParseException e) {
				setProgressBarIndeterminateVisibility(false);
				if(e == null){
					mUsers = users;
					
					String[] usernames = new String[users.size()];
					int i=0;
					for(ParseUser user: mUsers){
						usernames[i++]=user.getUsername();
					}
					ArrayAdapter<String> adapter = new ArrayAdapter<String>(EditFriendsActivity.this,
							android.R.layout.simple_list_item_checked, usernames);
					setListAdapter(adapter);
					
					addFriendCheckMarks();
					
				}else{
					Log.e(TAG, e.getMessage());
					AlertDialog.Builder builder = new AlertDialog.Builder(EditFriendsActivity.this);
					builder.setMessage(e.getMessage())
						.setTitle(R.string.error_title)
						.setPositiveButton(android.R.string.ok, null);
					AlertDialog dialog = builder.create();
					dialog.show();
				}
				
			}
		});
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		
		//int id = item.getItemId();
		return super.onOptionsItemSelected(item);
	}

	private void addFriendCheckMarks() {
		
		ParseQuery<ParseUser> query = mFriendsRelation.getQuery();
		query.findInBackground(new FindCallback<ParseUser>() {

			@Override
			public void done(List<ParseUser> friends, ParseException e) {
				if (e == null) {
					
					HashMap<String, Boolean> map = new HashMap<String, Boolean>();
					for(ParseUser friend: friends){
						map.put(friend.getObjectId(), true);
					}
					
					int i=0;
					for(ParseUser user: mUsers){
						if(map.containsKey(user.getObjectId())==true){
							getListView().setItemChecked(i, true);
						}
						i++;
					}
					map.clear();
				}else{
					Log.e(TAG, e.getMessage());
					AlertDialog.Builder builder = new AlertDialog.Builder(EditFriendsActivity.this);
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
