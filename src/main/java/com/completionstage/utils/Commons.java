package com.completionstage.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Commons {

	private Commons() { }
	
	public static final <T> void ensureIsNull(T that) {
		if(that != null) {
			throw new IllegalStateException("Object should be null! object:: " + that);
		}
	}
	
	public static final <T> void ensureIsNotNull(T that) {
		if(that == null) {
			throw new IllegalStateException("Object should not be null!");
		}
	}
	
	public static final long getSecsSinceEpoch() {
		return new Date().getTime() / 1000;
	}
	
	public static final void sleepFor(int sec, String... message) {
		try {
			Thread.sleep(sec * 1000);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
	
	public static final <T extends Object> void log(T message) {
		System.out.println(message.toString());
	}
	
	public static <T> T getOrFail(Future<T> future) {
		try {
			return future.get();
		} catch (InterruptedException | ExecutionException | IllegalStateException e) {
			log("Thread:: " + Thread.currentThread().getName() + " received an exception:: " + e.getMessage());
			throw new IllegalStateException(e.getMessage());
		}
	}
	
	public static <T> T getFromSupplierOrNullForError(Supplier<T> taskSupplier) {
		try {
			return taskSupplier.get();
		} catch (RuntimeException e) {
			log("global-exception-handler:: Handling:: " + e.getMessage());
		}
		return null;
	}

	public static <T> CompletableFuture<T> makePromise() {
		return new CompletableFuture<>();
	}
	
	public static final void failWith(String message) {
		throw new IllegalStateException(message);
	}
	
	public static final <T, E extends RuntimeException> boolean typeOf(T t, Class<E> e) {
		return t.getClass().isAssignableFrom(e);
	}
	
	public static String currentThreadName() {
		return Thread.currentThread().getName();
	}
	
	public static <T> List<T> merge(List<T> coll1, List<T> coll2) {
		return Stream
				.concat(coll1.stream(), coll2.stream())
				.distinct()
				.collect(Collectors.toList());
	}
	
	public static <K, V> Map<K, V> zipMap(List<K> keys, List<V> vals) {
		final Map<K, V> map = new HashMap<>();
		
		for(int i = 0; i < keys.size(); i += 1) {
			if(i >= vals.size()) {
				map.put(keys.get(i), null);
			} else {
				map.put(keys.get(i), vals.get(i));
			}
		}
		return map;
	}
	
	public static void main(String[] args) {
		System.out.println(zipMap(Arrays.asList(1, 2, 3), Arrays.asList("a", "b")));
	}
}