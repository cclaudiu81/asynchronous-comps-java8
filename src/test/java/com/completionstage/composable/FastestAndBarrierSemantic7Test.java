package com.completionstage.composable;

import static com.completionstage.utils.Commons.log;
import static com.completionstage.utils.Commons.sleepFor;
import static com.completionstage.utils.Commons.zipMap;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import static java.util.Arrays.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.junit.Test;

import com.completionstage.utils.TypeSignature;

/**
 * 
 * @author i312366
 */
public class FastestAndBarrierSemantic7Test {

	@TypeSignature("thenAcceptBoth:: CF<T>, CF<V>, (BiConsumer:: (T,V) -> U) -> CF<U>")
	@Test
	public void acceptBoth_should_wait_for_both_to_complete_async_before_passing_to_bisupplier() {
		CompletableFuture<List<String>> retrieveCustomers = CompletableFuture.supplyAsync(() -> {
			sleepFor(2);
			log("retrieving customers:: " + Util.getCustomers());
			return Util.getCustomers();
		});
		
		CompletableFuture<List<String>> retrieveCountries = CompletableFuture.supplyAsync(() -> {
			sleepFor(1);
			log("retrieving countries:: " + Util.getCountries());
			return Util.getCountries();
		});
		
		CompletableFuture<Void> mappedCustWithCountries = 
				retrieveCustomers.thenAcceptBoth(retrieveCountries, (customers, countries) -> {
					log("zip-mapped customers with countries:: " + zipMap(customers, countries));
				});
		
		sleepFor(3);
		assertThat(mappedCustWithCountries.isDone(), is(true));
	}
	

	@TypeSignature("acceptEither:: CF<T>, CF<T>, (Consumer<T> -> CF<Void>) -> CF<Void>")
	@Test
	public void acceptEither_should_wait_for_fastest_stage_to_complete_only() {
		CompletableFuture<Map<String, List<String>>> retrieveCustomersFromServer1 = CompletableFuture.supplyAsync(() -> {
			sleepFor(2);
			return Util.getCustomers("http://server1/customer");
		});
		
		CompletableFuture<Map<String, List<String>>> retrieveCustomerFromServer2 = CompletableFuture.supplyAsync(() -> {
			sleepFor(1);
			return Util.getCustomers("http://server2/customer");
		});
		
		retrieveCustomersFromServer1.acceptEither(retrieveCustomerFromServer2, (customers) -> {
			log("List of customers:: " + customers.values() + ", from server:: " + customers.keySet());
		});
		
		sleepFor(2);
	}
	
	static class Util {
		static List<String> getCustomers() {
			return asList("george", "peter", "marc", "martin");
		}
		
		static List<String> getCountries() {
			return asList("romania", "uk", "spain");
		}
		
		static Map<String, List<String>> getCustomers(String address) {
			final Map<String, List<String>> customers = new HashMap<>();
				customers.put(address, asList("george", "peter", "marc", "martin"));
			return customers;
		}
	}
}