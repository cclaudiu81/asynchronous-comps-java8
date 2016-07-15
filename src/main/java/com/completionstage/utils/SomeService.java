package com.completionstage.utils;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 
 * @author i312366
 */
public class SomeService {
	public static List<Integer> getAllPrices() {
		return getPricesRanging(0, 20);
	}
	
	public static List<Integer> getPricesRanging(int start, int end) {
		return Stream.iterate(start, x -> x + 1).limit(end).collect(Collectors.toList());
	}
}