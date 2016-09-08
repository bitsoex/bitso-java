package com.bitso.util;

public class Utilities {
	public interface Constants {
		public final static String RESPONSE_PAYLOAD = "payload";
		/**
		 * CURRENCY & TIME
		 */
		public final static String ISO_8601_ADD = ".000";
		public final static int ISO_8601_SUBSTRING_POS = 19;
	}
	
	/**
	 * CURRENCY & TIME METHODS
	 */
	public static String insert(String date, String addition, int index) {
		String begin = date.substring(0, index);
		String end = date.substring(index);
		return begin + addition + end;
	}
}
