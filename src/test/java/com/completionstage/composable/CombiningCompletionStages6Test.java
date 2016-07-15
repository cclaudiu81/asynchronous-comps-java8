package cc.completionstage.composable;

import static com.completionstage.utils.Commons.getOrFail;
import static com.completionstage.utils.Commons.log;
import static com.completionstage.utils.Commons.sleepFor;
import static com.completionstage.utils.Commons.zipMap;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.junit.Test;

import com.completionstage.utils.TypeSignature;

/**
 * 
 * @author i312366
 */
public class CombiningCompletionStages6Test {

	@TypeSignature("thenCombine:: CF<T>, CF<V>, (BiFunction:: (T,V) -> U) -> CF<U>")
	@Test
	public void combine_stages_should_process_in_parallel_and_wait_for_both_to_complete_before_applying_function() {
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
		
		CompletableFuture<Map<String,String>> mappedCustWithCountries = 
				retrieveCustomers.thenCombine(retrieveCountries, (customers, countries) -> {
					log("zip-mapped customers with countries:: " + zipMap(customers, countries));
					return zipMap(customers, countries);
				});
		
		assertThat(getOrFail(mappedCustWithCountries).size(), is(4));
	}
	
	static class Util {
		static List<String> getCustomers() {
			return Arrays.asList("george", "peter", "marc", "martin");
		}
		
		static List<String> getCountries() {
			return Arrays.asList("romania", "uk", "spain");
		}

	}
}