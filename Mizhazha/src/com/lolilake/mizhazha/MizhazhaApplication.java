package com.lolilake.mizhazha;

import android.app.Application;

import com.parse.Parse;

public class MizhazhaApplication extends Application{
	@Override
	public void onCreate() {
		super.onCreate();
		Parse.initialize(this, "smE9cKjI5HXihcQx3ijZ7bV0qq0GGWeeuwqw5d7S", "vyGMDTfcHE6PydjgbOQ6DmGkA8YR17dxgYdAUMD2");
	}
}
