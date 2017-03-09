package nohorjo.application;

import android.os.Bundle;
import nohorjo.howl.R;

public class LaunchActivity extends HomeSetter {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		App.setContext(this);
		setContentView(R.layout.activity_launch);
		scheduleBRAndCallHome();
	}

	@Override
	protected void onResume() {
		super.onResume();
		scheduleBRAndCallHome();
	}
}
