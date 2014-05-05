package com.lolilake.mizhazha;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.ImageView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class ViewImageActivity extends Activity {
	
	public static final String TAG = ViewImageActivity.class.getSimpleName();
	
	protected Uri mUri;
	protected ImageView imageView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_view_image);
		
		imageView = (ImageView)findViewById(R.id.viewImage);
		mUri = getIntent().getData();
		
		setProgressBarIndeterminateVisibility(true);
		Picasso.with(this).load(mUri.toString()).into(imageView, new Callback() {
			
			@Override
			public void onSuccess() {
				setProgressBarIndeterminateVisibility(false);
				Timer timer = new Timer();
				timer.schedule(new TimerTask() {
					
					@Override
					public void run() {
						finish();
					}
				}, 10*1000);
			}
			
			@Override
			public void onError() {
				setProgressBarIndeterminateVisibility(false);
				Log.e(TAG, getString(R.string.error_fail_to_load_image));
				AlertDialog.Builder builder = new AlertDialog.Builder(ViewImageActivity.this);
				builder.setMessage(getString(R.string.error_fail_to_load_image))
					.setTitle(R.string.error_title)
					.setPositiveButton(android.R.string.ok, null);
				AlertDialog dialog = builder.create();
				dialog.show();
			}
		});
		

	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "Picasso, cancelRequest is called");
		Picasso.with(this).cancelRequest(imageView);
	}
}
