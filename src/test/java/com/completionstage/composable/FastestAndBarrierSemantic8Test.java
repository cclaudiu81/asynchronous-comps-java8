package com.completionstage.composable;
import static com.completionstage.utils.Commons.*;
import static java.util.Arrays.asList;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.junit.Test;

/**
 * 
 * @author i312366
 */
public class FastestAndBarrierSemantic8Test {

	@Test
	public void waiting_for_all_should_act_as_a_barrier_for_computation_stages() {
		CompletableFuture<List<String>> tourists = getTourists();
		CompletableFuture<List<String>> cities = getCities();
		
		CompletableFuture<Void> touristsWithCities = CompletableFuture.allOf(tourists, cities);
		
		touristsWithCities.thenRun(() -> {
			log("cities:: " + getOrFail(cities));
			log("tourists:: " + getOrFail(tourists));
		});
	}
	
	private CompletableFuture<List<String>> getTourists() {
		return CompletableFuture.supplyAsync(() -> asList("claudiu", "cosar"));
	}
	
	private CompletableFuture<List<String>> getCities() {
		return CompletableFuture.supplyAsync(() -> asList("timisoara", "deva"));
	}
}