package ro.pontes.dictfrro;

import java.util.Locale;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.speech.tts.TextToSpeech;

public class SpeakText2 {

	private Context context;
	private String curEngine = null;
	private TextToSpeech mTTS;
	private Locale curTTSLocale = null;
	private float ttsRate = 1.0F;
	private float ttsPitch = 1.0F;

	// The constructor:
	public SpeakText2(Context context) {
		this.context = context;

		/*
		 * First of all, charge current engine, the saved one, the default would
		 * be Google TTS:
		 */
		Settings set = new Settings(this.context);
		curEngine = set.getStringSettings("curEngine2");
		// Check if a setting was saved:
		if (curEngine == null || curEngine.equals("")) {
			curEngine = "com.google.android.tts";
		} // end if no engine was saved.

		// For TextToSpeech:
		mTTS = new TextToSpeech(this.context,
				new TextToSpeech.OnInitListener() {
					@Override
					public void onInit(int status) {
						if (status != TextToSpeech.ERROR) {
							// It means no error was found, we can set the
							// language:
							setSavedLanguage();
						}
					}
				}, curEngine);
		// end for TextToSpeech.
	} // end constructor.

	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	public void say(final String toSay, final boolean interrupt) {
		if (MainActivity.isSpeech) {

			int speakMode = 0;
			if (interrupt) {
				speakMode = TextToSpeech.QUEUE_FLUSH;
			} else {
				speakMode = TextToSpeech.QUEUE_ADD;
			} // end if is not interruption.
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				mTTS.speak(toSay, speakMode, null, null);
			} else {
				mTTS.speak(toSay, speakMode, null);
			}
		} // end if isSpeech.
	} // end say method.

	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	public void sayUsingLanguage(final String toSay, final boolean interrupt) {
		if (MainActivity.isSpeech) {

			// First check if there is this language:
			if (mTTS.isLanguageAvailable(curTTSLocale) != TextToSpeech.LANG_NOT_SUPPORTED) {

				int speakMode = 0;
				if (interrupt) {
					speakMode = TextToSpeech.QUEUE_FLUSH;
				} else {
					speakMode = TextToSpeech.QUEUE_ADD;
				} // end if is not interruption.
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
					mTTS.speak(toSay, speakMode, null, null);
				} else {
					mTTS.speak(toSay, speakMode, null);
				}

			} // end if this language exists as country and variant.
			else {
				GUITools.alert(context, context.getString(R.string.warning),
						context.getString(R.string.warning_no_tts_available_ro));
			} // end if the language doesn't exist.
		} // end if isSpeech.
	} // end sayUsingLanguage() method.

	// A method to spell a string:
	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	public void spellUsingLanguage(String toSpell) {
		if (MainActivity.isSpeech) {

			// First check if there is this language:
			if (mTTS.isLanguageAvailable(curTTSLocale) != TextToSpeech.LANG_NOT_SUPPORTED) {

				for (int i = 0; i < toSpell.length(); i++) {
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
						mTTS.speak("" + toSpell.charAt(i),
								TextToSpeech.QUEUE_ADD, null, null);
					} else {
						mTTS.speak("" + toSpell.charAt(i),
								TextToSpeech.QUEUE_ADD, null);
					}
				} // end for each character.

			} // end if this language exists as country and variant.
			else {
				GUITools.alert(context, context.getString(R.string.warning),
						context.getString(R.string.warning_no_tts_available_ro));
			} // end if the language doesn't exist.
		} // end if isSpeech.
	} // end spellUsingLanguage() method.

	public void stop() {
		mTTS.stop();
	} // end stop method of the SpeakText class.

	// A method to set the language of this instance, make a locale:
	private void setSavedLanguage() {
		Settings set = new Settings(context);
		String language = set.getStringSettings("ttsLanguage2");
		if (language == null || language.equals("")) {
			language = "ro";
		}
		String country = set.getStringSettings("ttsCountry2");
		if (country == null || country.equals("")) {
			country = Locale.getDefault().getCountry();
		}
		String variant = set.getStringSettings("ttsVariant2");
		if (variant == null) {
			variant = "";
		}

		curTTSLocale = new Locale(language, country, variant);

		mTTS.setLanguage(curTTSLocale);

		// See about rate and pitch:
		if (set.preferenceExists("ttsRate")) {
			ttsRate = set.getFloatSettings("ttsRate");
		} // end if TTS rate was saved.
		mTTS.setSpeechRate(ttsRate);

		if (set.preferenceExists("ttsPitch")) {
			ttsPitch = set.getFloatSettings("ttsPitch");
		} // end if TTS rate was saved.
		mTTS.setPitch(ttsPitch);
	} // end setCurrentLanguage() method.

	// A method to release resources:
	public void close() {
		mTTS.shutdown();
	} // end close() method.

} // end SpeakText class.
