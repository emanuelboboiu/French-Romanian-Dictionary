package ro.pontes.dictfrro;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class PremiumVersionActivity extends Activity {

	private final Context mFinalContext = this;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_premium_version);

		// Some initial things like background:
		GUITools.setLayoutInitial(this, 2);

	} // end onCreate() method.

	public void onResume() {
		super.onResume();
		SoundPlayer.playSimple(this, "bought_premium");
		goToMainActivity();
	} // end onResume() method.

	public void goToMainActivity() {
		final Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				// Do something after 5000 milliseconds:
				Intent intent = new Intent(mFinalContext, MainActivity.class);
				startActivity(intent);
				finish();
			}
		}, 3500);

	} // end goToMainActivity() method.

	@Override
	public void onBackPressed() {
		this.finish();
		GUITools.goToDictionary(this);
	} // end onBackPressed()

} // end PremiumVersionActivity class.
