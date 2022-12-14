package ro.pontes.dictfrro;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import android.content.Context;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodSubtype;

public class StringTools {

	private Context context;
	private int keyboardLanguage = 0;

	// A constructor:
	@SuppressWarnings("deprecation")
	public StringTools(Context context) {
		this.context = context;
		InputMethodManager imm = (InputMethodManager) this.context
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		InputMethodSubtype ims = imm.getCurrentInputMethodSubtype();
		String locale = "xx"; // an unknown one.
		if (ims != null) {
			locale = ims.getLocale();
		}

		if (locale.startsWith("ro")) {
			// Romanian keyboard is value 1:
			keyboardLanguage = 1;
		} else if (locale.startsWith("ru")) {
			// Russian keyboard is value 2:
			keyboardLanguage = 2;
		}
		// GUITools.alert(context, "", locale);
	} // end constructor for context.

	/*
	 * A method to replace the string with special letters with corresponding
	 * Latin English character:
	 */
	public String replaceCharacters(String str) {
		// Make it lower case:
		if (keyboardLanguage == 2) {
			// It is the Russian keyboard:
			str = str.toLowerCase(new Locale("ru"));
		} else {
			str = str.toLowerCase(Locale.getDefault());
		}

		StringBuilder sb = new StringBuilder(str);

		switch (keyboardLanguage) {
		case 2:
			// Russian language:
			// Code here to make Kyrillic into Latin:
			// The two arrays:
			// The Kyrillic one:
			char[] kyrillic = new char[] { 'а', 'б', 'в', 'г', 'д', 'е', 'ё',
					'ж', 'з', 'и', 'й', 'к', 'л', 'м', 'н', 'о', 'п', 'р', 'с',
					'т', 'у', 'ф', 'х', 'ц', 'ч', 'ш', 'щ', 'ъ', 'ы', 'ь', 'э',
					'ю', 'я' };

			// The Latin one:
			String[] latin = new String[] { "a", "b", "v", "g", "d", "e", "yo",
					"zh", "z", "i", "j", "k", "l", "m", "n", "o", "p", "r",
					"s", "t", "u", "f", "h", "ts", "ch", "sh", "shch", "'",
					"y", "'", "e", "yu", "ya" };

			/*
			 * Create the new string replacing the Kyrillic letters with Latin
			 * correspondents.
			 */
			// A new string builder for new word:
			StringBuilder sbNew = new StringBuilder();
			for (int i = 0; i < sb.length(); i++) {
				boolean wasKyrillic = false;
				innerloop: for (int j = 0; j < kyrillic.length; j++) {
					if (sb.charAt(i) == kyrillic[j]) {
						sbNew.append(latin[j]);
						wasKyrillic = true;
						break innerloop;
					} // end if a Kyrillic character was found.
				} // end inner for.
				if (!wasKyrillic) {
					/*
					 * It means we hadn't a Kyrillic character, then we add the
					 * original one found in that position [i]
					 */
					sbNew.append(sb.charAt(i));
				} // end if was not Kyrillic character.
			} // end outer for.

			sb = sbNew;
			break;

		default:
			/*
			 * Romanian and French language which is case 1 in fact. It be
			 * possible to make copy paste for romanian words and the keyboard
			 * to be another locale, we need to replace anyway the diacritics.
			 */
			StringBuilder diacritics = new StringBuilder("çéàèùâêîôûëïüÿăşţșț");
			StringBuilder nonDiacritics = new StringBuilder(
					"ceaeuaeioueiuyastst");

			for (int i = 0; i < sb.length(); i++) {
				innerloop: for (int j = 0; j < diacritics.length(); j++) {
					if (sb.charAt(i) == diacritics.charAt(j)) {
						sb.setCharAt(i, nonDiacritics.charAt(j));
						break innerloop;
					}
				} // end inner for.
			} // end outer for.

			break;
		} // end switch for keyboardLanguage.

		String toReturn = sb.toString();

		// Make also realEscapeString::
		toReturn = realEscapeString(toReturn);

		return toReturn;
	} // end replace special letters.

	// A method instead RealEscapeString:
	public String realEscapeString(String str) {
		String toReturn = str.replaceAll("'", "''");

		return toReturn;
	} // end realEscapeString() method.

	// A method to polish a string:
	public String polishString(String str) {

		String toReturn = str.replaceAll("''", "'");

		return toReturn;
	} // end polishString() method.

	// Convert to hex:
	private static String convertToHex(byte[] data) {
		StringBuilder buf = new StringBuilder();
		for (byte b : data) {
			int halfbyte = (b >>> 4) & 0x0F;
			int two_halfs = 0;
			do {
				buf.append((0 <= halfbyte) && (halfbyte <= 9) ? (char) ('0' + halfbyte)
						: (char) ('a' + (halfbyte - 10)));
				halfbyte = b & 0x0F;
			} while (two_halfs++ < 1);
		}
		return buf.toString();
	} // end convertToHex() method.

	public static String SHA1(String text) throws NoSuchAlgorithmException,
			UnsupportedEncodingException {
		MessageDigest md = MessageDigest.getInstance("SHA-1");
		md.update(text.getBytes("iso-8859-1"), 0, text.length());
		byte[] sha1hash = md.digest();
		return convertToHex(sha1hash);
	} // end SHA1() method.

	/* A method which sanitises the string for clipboard and other things: */
	public String cleanString(String str) {
		String toReturn = str.trim();

		return toReturn;
	} // end cleanString() method.

} // end StringTools class.
