package com.redhat.topicindex.utils;

public class LineEndFormatter 
{
	public static String convertToLinuxLineEndings(final String input)
	{
		if (input == null) return "";
		return input.replaceAll("\\r", "");
	}
	
	public static String convertToWindowsLineEndings(final String input)
	{
		if (input == null) return "";
		return input.replaceAll("(?<!\\r)\\n", "\\r\\n");
	}
}
