package com.test.common.util;

public class MailUtils {
	public static String randomNumBuilder() {
		String result="";
		for(int i=0;i<12;i++)result+=Math.round(Math.random()*9);
		return result;
	}
}
