package sk.vlakov.exercise1;

import java.math.BigDecimal;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * 
 * Parses one input line at a time, produces CcyHolder
 *
 */
public class CcyParser {
	// ------------------
	// 1 to 2 decimal places.
	// USD -45,67
	// USD 45.67
	// USD 45
	private final String mSPattern;
	private final Pattern mPattern1;

	/**
	 * @param decimalPlaces
	 *        number of decimal palces for sums
	 */
	public CcyParser(int decimPlaces) {
		mSPattern = "^([A-Z]{3}) +([-+]?)(\\d+)([.,](\\d{1," + decimPlaces
		    + "}))? *$";
		mPattern1 = Pattern.compile(mSPattern);
	}

	/**
	 * @param line
	 *        input line
	 * @return holder of currency and sum<br/>
	 *         if input line is invalid, returns null  
	 */
	public CcyHolder parse(String line1) {
		// ----------------
		Matcher matcher1 = mPattern1.matcher(line1);
		if (matcher1.find()) {
			// ----------
			String currency1 = matcher1.group(1);
			StringBuilder sbSum1 = new StringBuilder();
			int grpOrd = 2;
			// ---------------
			// Sign / Znamienko:
			sbSum1.append(matcher1.group(grpOrd));
			grpOrd++;
			String wholeNum = matcher1.group(grpOrd);
			sbSum1.append(wholeNum);
			// ----------
			if (matcher1.group(4) != null) {
				// desatinna cast
				grpOrd += 2;
				String decimalNum = matcher1.group(grpOrd);
				// -----------------------------
				// Moze byt ciarka aj bodka - BigDecimal chce bodku:
				sbSum1.append(".").append(decimalNum);
			}
			return new CcyHolder(currency1, new BigDecimal(sbSum1.toString()));
		} else {
			return null;
		}
	}
}
