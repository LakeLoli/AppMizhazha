package com.lolilake.mizhazha;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;

import com.parse.ParseUser;

public class MainActivity extends Activity implements ActionBar.TabListener {

	public static final String TAG = MainActivity.class.getSimpleName();
	
	public static final int ACTION_TAKE_PHOTO = 0;
	public static final int ACTION_TAKE_VIDEO = 1;
	public static final int ACTION_CHOOSE_PHOTO = 2;
	public static final int ACTION_CHOOSE_VIDEO = 3;
	
	public static final int MEDIA_TYPE_IMAGE = 4;
	public static final int MEDIA_TYPE_VIDEO = 5;
	
	public static final int VIDEO_SIZE_LIMIT = 1024*1024*10; //10MB
	
	protected Uri mMediaUri;
	protected Uri mMediaUriBackup;
	
	protected DialogInterface.OnClickListener mDialogListener = 
			new DialogInterface.OnClickListener() {
				
		@Override
		public void onClick(DialogInterface dialog, int which) {
		
			switch (which) {
				case 0:
					//take picture
					Intent takePhotoIntent = new Intent (MediaStore.ACTION_IMAGE_CAPTURE);
					mMediaUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
					mMediaUriBackup = Uri.parse(mMediaUri.toString());
					if(mMediaUri == null){
						Toast.makeText(MainActivity.this, R.string.error_message_media, Toast.LENGTH_LONG).show();
					}else{
						takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mMediaUri);
						if (takePhotoIntent.resolveActivity(getPackageManager()) != null) {
							startActivityForResult(takePhotoIntent, ACTION_TAKE_PHOTO);
						}else {
							//display error message
							AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
							builder.setMessage(R.string.error_photo_handle)
							.setTitle(R.string.error_title)
							.setPositiveButton(android.R.string.ok, null);
							AlertDialog alertdialog = builder.create();
							alertdialog.show();
						}
					}
					break;
				case 1:
					//take video
					Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
					mMediaUri = getOutputMediaFileUri(MEDIA_TYPE_VIDEO);
					mMediaUriBackup = Uri.parse(mMediaUri.toString());
					if(mMediaUri == null){
						Toast.makeText(MainActivity.this, R.string.error_message_media, Toast.LENGTH_LONG).show();
					}else{
						takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mMediaUri);
						takeVideoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 10);
						takeVideoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
						if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
							Log.d(TAG, "DEBUG: mMediaUri in MainActivity line 85: " + mMediaUri);
							startActivityForResult(takeVideoIntent, ACTION_TAKE_VIDEO);
						}else {
							//display error message
							AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
							builder.setMessage(R.string.error_video_handle)
							.setTitle(R.string.error_title)
							.setPositiveButton(android.R.string.ok, null);
							AlertDialog alertdialog = builder.create();
							alertdialog.show();
						}
					}
					break;
				case 2:
					//choose picture
					Intent choosePictureIntent = new Intent(Intent.ACTION_GET_CONTENT);
					choosePictureIntent.setType("image/*");
					startActivityForResult(choosePictureIntent, ACTION_CHOOSE_PHOTO);
					break;
				case 3:
					//choose video
					Intent chooseVideoIntent = new Intent(Intent.ACTION_GET_CONTENT);
					chooseVideoIntent.setType("video/*");
					Toast.makeText(MainActivity.this, R.string.viedo_size_warning, Toast.LENGTH_LONG).show();
					startActivityForResult(chooseVideoIntent, ACTION_CHOOSE_VIDEO);
				default:
					break;
			}
						
		}

		private Uri getOutputMediaFileUri(int mediaTypeImage) {
			
			
			if(isExternalStorageAvailable()){
				//1. get the external storage directory
				File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
						MainActivity.this.getString(R.string.app_name));
				
				//2. create a sub-directory
				if(!mediaStorageDir.exists()){
					if(!mediaStorageDir.mkdirs()){
						Log.e(TAG, "Failed to create a directory");
					}
				}
				
				//3. create the file
				File mediaFile;
				Date date = new Date();
				String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(date);
				
				String path = mediaStorageDir.getPath() + File.separator;
				if(mediaTypeImage == MEDIA_TYPE_IMAGE){
					mediaFile = new File(path + "IMG_" + timeStamp + ".jpg");
				}else if(mediaTypeImage == MEDIA_TYPE_VIDEO){
					mediaFile = new File(path + "VID_" + timeStamp + ".mp4");
				}else{
					return null;
				}
				
				Log.d(TAG, "File: " + Uri.fromFile(mediaFile));
				
				return Uri.fromFile(mediaFile);
				
			}else{
				return null;
			}
		}
		
		private boolean isExternalStorageAvailable(){
			
			String state = Environment.getExternalStorageState();
			
			if(state.equals(Environment.MEDIA_MOUNTED)){
				return true;
			}else{
				return false;
			}
		}
	};
	
		
	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a {@link FragmentPagerAdapter}
	 * derivative, which will keep every loaded fragment in memory. If this
	 * becomes too memory intensive, it may be best to switch to a
	 * {@link android.support.v13.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_main);
		
		ParseUser currentUser = ParseUser.getCurrentUser();
		
		if(currentUser == null){
			navigateToLogin();
		}else {
			Log.i(TAG, currentUser.getUsername());
		}

		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the activity.
		mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager(), this);

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mViewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						actionBar.setSelectedNavigationItem(position);
					}
				});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the callback (listener) for when
			// this tab is selected. 
			actionBar.addTab(actionBar.newTab()
					.setText(mSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		// if there is an error, make a toast, then return
		if(resultCode != RESULT_OK && resultCode != RESULT_CANCELED){
			Toast.makeText(this, getString(R.string.error_carema),  Toast.LENGTH_LONG).show();
			return;
		}
		
		// if it is cancelled, then return
		if(resultCode == RESULT_CANCELED){
			return;
		}
		
		// if it works well
		if(requestCode == ACTION_CHOOSE_PHOTO || requestCode == ACTION_CHOOSE_VIDEO){
			// add data to gallery
			if(data == null){
				// no item selected
				Toast.makeText(this, getString(R.string.error_carema),  Toast.LENGTH_LONG).show();
				return;
			}else {
				mMediaUri = data.getData();
			}
			Log.i(TAG, "Media Uri: " + mMediaUri);
			if(requestCode == ACTION_CHOOSE_VIDEO){
				//make sure the file is less than 10Mb
				int fileSize = 0;
				InputStream inputStream = null;
				try {
					inputStream = getContentResolver().openInputStream(mMediaUri);
					fileSize = inputStream.available();						
				} catch (FileNotFoundException e) {
					Toast.makeText(this, R.string.error_file_not_found,  Toast.LENGTH_LONG).show();
					return;
				} catch (IOException e) {
					Toast.makeText(this, R.string.error_file_not_found,  Toast.LENGTH_LONG).show();
					return;
				} finally{
					try {
						inputStream.close();
					} catch (IOException e) {
						//Intentionally blank
					}
				}
				if(fileSize>=VIDEO_SIZE_LIMIT){
					Toast.makeText(this, R.string.error_video_too_large,  Toast.LENGTH_LONG).show();
					return;
				}
			}
			
		}else {
			// After taking photos and videos, save the data
			Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
			if(mMediaUri == null){
				mMediaUri = mMediaUriBackup;
				AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
				builder.setMessage("no Content attached")
				.setTitle(R.string.error_title)
				.setPositiveButton(android.R.string.ok, null);
				AlertDialog alertdialog = builder.create();
				alertdialog.show();
			}
			mediaScanIntent.setData(mMediaUri);
			sendBroadcast(mediaScanIntent);

		}
		
		// start RecipientsActivity
		Intent recipientsIntent = new Intent(this, RecipientsActivity.class);
		recipientsIntent.setData(mMediaUri);
		
		String fileType;
		if(requestCode == ACTION_TAKE_PHOTO || requestCode == ACTION_CHOOSE_PHOTO){
			fileType = ParseConstants.TYPE_PHOTO;
		}else{
			fileType = ParseConstants.TYPE_VIDEO;
		}
		recipientsIntent.putExtra(ParseConstants.KEY_FILE_TYPE, fileType);
		startActivity(recipientsIntent);
	}
	
	private void navigateToLogin() {
		Intent loginIntent = new Intent(this, LoginActivity.class);
		loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		startActivity(loginIntent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		
		switch (id) {
		case R.id.action_edit_friends:
			Intent intent = new Intent(this, EditFriendsActivity.class);
			startActivity(intent);
			break;
		case R.id.action_logout:
			ParseUser.logOut();
			navigateToLogin();
			break;
		case R.id.action_camera:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setItems(R.array.camera_choices, mDialogListener);
			AlertDialog dialog = builder.create();
			dialog.show();
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}
	
	
	/**
	 * A placeholder fragment containing a simple view.
	 * PlaceholderFragment.java
	 */
	
	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 * SectionsPagerAdapter.java
	 */
	
	/**
	 * 5 Steps to Implement Tabs with Fragments
	 * Create a fragment container in an Activity   PlaceholderFragment.java
	 * Create fragment classes and layouts	
	 * Add the initial fragment to the Activity
	 * Add tabs to the Action Bar
	 * Add a TabListener that loads new fragments
	 */
	

}
