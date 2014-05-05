package com.lolilake.mizhazha;

import java.util.ArrayList;
import java.util.List;

import android.app.ListFragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;


public class InboxFragment extends ListFragment{
	
	protected List<ParseObject> mMessages;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_inbox, container,
				false);
		return rootView;
	}
	
	
	@Override
	public void onResume() {
		super.onResume();
		
		getActivity().setProgressBarIndeterminateVisibility(true);
		
		ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(ParseConstants.CLASS_MESSAGES);
		query.whereEqualTo(ParseConstants.KEY_RECIPIENT_IDS, ParseUser.getCurrentUser().getObjectId());
		query.addAscendingOrder(ParseConstants.CREATER_AT);
		query.findInBackground(new FindCallback<ParseObject>() {
			
			@Override
			public void done(List<ParseObject> messages, ParseException e) {
				
				getActivity().setProgressBarIndeterminateVisibility(false);
				if(e == null){
					mMessages = messages;
					String[] senderUsername = new String[messages.size()];
					
					int i=0;
					for(ParseObject message : messages){
						senderUsername[i]= message.getString(ParseConstants.KEY_SENDER_NAME);
						i++;
					}
					
					if(getListView().getAdapter() == null){
						MessageAdapter adapter = new MessageAdapter(getListView().getContext(), mMessages);
						setListAdapter(adapter);
					}else {
						//refill the adapter
						MessageAdapter adapter = (MessageAdapter) getListView().getAdapter();
						adapter.refill(messages);
					}
						
				}
			}
		});
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		ParseObject message = mMessages.get(position);
		String messageType = message.getString(ParseConstants.KEY_FILE_TYPE);
		ParseFile file = message.getParseFile(ParseConstants.KEY_FILE);
		Uri fileUri = Uri.parse(file.getUrl());
		
		if(messageType.equals(ParseConstants.TYPE_PHOTO)){
			Intent intent = new Intent(getActivity(), ViewImageActivity.class);
			intent.setData(fileUri);
			startActivity(intent);
		}else{
			Intent intent = new Intent(Intent.ACTION_VIEW, fileUri);
			intent.setDataAndType(fileUri, "video/*");
			startActivity(intent);
		}
		
		//Delete the message
		List<String> recipients = message.getList(ParseConstants.KEY_RECIPIENT_IDS);
		
		if(recipients.size() == 1){
			message.deleteInBackground();
		}else {
			recipients.remove(ParseUser.getCurrentUser().getObjectId());
			
			ArrayList<String> recipientsToRemove = new ArrayList<String>();
			recipientsToRemove.add(ParseUser.getCurrentUser().getObjectId());
			message.removeAll(ParseConstants.KEY_RECIPIENT_IDS, recipientsToRemove);
			message.saveInBackground();
		}
	}
	
	
}
