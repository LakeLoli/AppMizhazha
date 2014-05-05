package com.lolilake.mizhazha;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class RecipientsActivity extends ListActivity {

	public static final String TAG = RecipientsActivity.class.getSimpleName();
	
	protected List<ParseUser> mFriends;
	protected ParseRelation<ParseUser> mFriendsRelation;
	protected ParseUser mCurrentUser;
	protected MenuItem mSendMenuItem;
	protected Uri mMediaUri;
	protected String mFileType;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_recipients);
		
		getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		mFileType = getIntent().getExtras().getString(ParseConstants.KEY_FILE_TYPE);
		mMediaUri = getIntent().getData();
		Log.d(TAG, "DEBUG: mMediaUri in RecipientsActivity: " + mMediaUri);
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
		setProgressBarIndeterminateVisibility(true);
		query.findInBackground(new FindCallback<ParseUser>() {

			@Override
			public void done(List<ParseUser> friends, ParseException e) {
				
				setProgressBarIndeterminateVisibility(false);
				if(e == null){
					mFriends = friends;
					String[] friendsUsername = new String[friends.size()];
					
					int i=0;
					for(ParseUser friend : friends){
						friendsUsername[i++]=friend.getUsername();
					}
					ArrayAdapter<String> adapter = new ArrayAdapter<String>(getListView().getContext(),
							android.R.layout.simple_list_item_checked, 
							friendsUsername);
					setListAdapter(adapter);
				}else {
					Log.e(TAG, e.getMessage());
					AlertDialog.Builder builder = new AlertDialog.Builder(RecipientsActivity.this);
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
		getMenuInflater().inflate(R.menu.recipients, menu);
		mSendMenuItem = menu.getItem(0);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_send) {
			
			ParseObject message = createMessage();
			if(message == null){
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage(R.string.error_file_message)
					   .setTitle(R.string.error_message_title)
					   .setPositiveButton(android.R.string.ok, null);
				AlertDialog dialog = builder.create();
				dialog.show();
			}else{
				send(message);
				finish();
			}
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void send(ParseObject message) {
		message.saveInBackground(new SaveCallback() {
			
			@Override
			public void done(ParseException e) {
				if(e == null){
					Toast.makeText(RecipientsActivity.this, R.string.success_message, Toast.LENGTH_LONG).show();
				}else{
					AlertDialog.Builder builder = new AlertDialog.Builder(RecipientsActivity.this);
					builder.setMessage(R.string.error_sending_message)
						   .setTitle(R.string.error_message_title)
						   .setPositiveButton(android.R.string.ok, null);
					AlertDialog dialog = builder.create();
					dialog.show();
				}
				
			}
		});
		
	}

	private ParseObject createMessage() {
		
		ParseObject message = new ParseObject(ParseConstants.CLASS_MESSAGES);
		message.put(ParseConstants.KEY_SENDER_ID, mCurrentUser.getObjectId());
		message.put(ParseConstants.KEY_SENDER_NAME, mCurrentUser.getUsername());
		message.put(ParseConstants.KEY_RECIPIENT_IDS, getRecipientIds());
		message.put(ParseConstants.KEY_FILE_TYPE, mFileType);
		
		if(mMediaUri == null){
			AlertDialog.Builder builder = new AlertDialog.Builder(RecipientsActivity.this);
			builder.setMessage("mMediaUri is null")
				   .setTitle(R.string.error_message_title)
				   .setPositiveButton(android.R.string.ok, null);
			AlertDialog dialog = builder.create();
			dialog.show();
		}
		
		byte[] fileBytes  = FileHelper.getByteArrayFromFile(this, mMediaUri);
		
		if(fileBytes == null){
			return null;
		}else{
			if(mFileType == ParseConstants.TYPE_PHOTO){
				fileBytes = FileHelper.reduceImageForUpload(fileBytes);
			}
			
			String fileName = FileHelper.getFileName(this, mMediaUri, mFileType);
			ParseFile parseFile = new ParseFile(fileName, fileBytes);
			message.put(ParseConstants.KEY_FILE, parseFile);		
			return message;
		}
	}

	private ArrayList<String> getRecipientIds() {
		
		ArrayList<String> recipients = new ArrayList<String>();
		
		for(int i=0; i<getListView().getCount(); ++i){
			if(getListView().isItemChecked(i)){
				recipients.add(mFriends.get(i).getObjectId());
			}
		}
		return recipients;
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		if(l.getCount()>0){
			mSendMenuItem.setVisible(true);
		}else{
			mSendMenuItem.setVisible(false);
		}
	}

}
