package com.completionstage.utils;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class StringUtils {

	private StringUtils() { }
	
	public static final List<String> splitBy(String splitter, String someString) {
		return Stream.of(someString.split(splitter)).collect(Collectors.toList());
	}
}
