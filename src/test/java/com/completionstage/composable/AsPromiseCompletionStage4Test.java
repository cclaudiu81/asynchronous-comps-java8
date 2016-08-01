package com.completionstage.composable;

import static com.completionstage.utils.Commons.currentThreadName;
import static com.completionstage.utils.Commons.failWith;
import static com.completionstage.utils.Commons.getOrFail;
import static com.completionstage.utils.Commons.log;
import static com.completionstage.utils.Commons.makePromise;
import static com.completionstage.utils.Commons.sleepFor;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.junit.Test;

import com.completionstage.utils.Commons;
import com.completionstage.utils.Recursive;
import com.completionstage.utils.SomeService;
import com.completionstage.utils.TypeSignature;

/**
 * promises
 * 
 * @author i312366
 */
public class AsPromiseCompletionStage4Test {
	
	@Test
	public void promise_can_only_be_completed_once_and_should_ignore_any_further_complete_invocations() {
		CompletableFuture<String> promised = makePromise();
		promised.complete("1");
		promised.complete("2");
		
		assertThat(getOrFail(promised), is("1"));
	}

	@Test
	public void promise_should_make_dependent_parties_wait_for_the_promised_thing_until_some_party_delivers() throws InterruptedException, ExecutionException {
		
		CompletableFuture<Long> promised = makePromise();
		
		CompletableFuture
			.runAsync(() -> {
				log(currentThreadName() + ":: computing fibonnaci...waiting for the promised number...");
				BigInteger computedFib = fib(BigInteger.valueOf(getOrFail(promised)));
				log(currentThreadName() + ":: fibonnaci of:: " + getOrFail(promised) + " computed to:: " + computedFib); 
			})
			.thenRun(() -> log(currentThreadName() + ":: logging the computed fibonnaci of:: " + getOrFail(promised)));

		log(currentThreadName() + " thread completes delivering what the dependent parties expected!");
		
		promised.complete(30L);
		
		sleepFor(2);
	}

	
	@Recursive
	private static BigInteger fib(BigInteger n) {
		if(n.compareTo(BigInteger.valueOf(2L)) == -1) return n;
		return n.multiply(fib(n.subtract(BigInteger.valueOf(1L))))
						.add(fib(n.subtract(BigInteger.valueOf(2L))));
	}
	
	/* errors:: propagating to all the dependent clients that wait for dereferencing promise-delivering-error */
	@Test(expected = IllegalStateException.class)
	public void dependent_future_parties_should_all_receive_error_when_promise_delivers_error1() {

		@TypeSignature("supplyAsync:: (Supplier<String>) -> Error")
		CompletableFuture<Void> maybeError = CompletableFuture.runAsync(() -> { 
			failWith("failing would make dependent clients 'receive' the error");
		});
		
		CompletableFuture.runAsync(() -> getOrFail(maybeError));
		getOrFail(maybeError);
	}
	
	@Test
	public void dependent_future_parties_should_all_receive_error_when_promise_delivers_error2() {

		CompletableFuture<Void> promised = makePromise();
		
		CompletableFuture.runAsync(() -> {
			log("client:: " + currentThreadName() + " depending on this promise received error" + getOrFail(promised));
		});
		
		promised.completeExceptionally(new IllegalStateException("parties attempting to dereference this promise shall receive this error"));
	}
	
	@Test
	public void resolving_a_promise_with_error_should_skips_transformations_before_catchAll() {
		CompletableFuture<List<Integer>> promised = Commons.makePromise();

		promised
			.thenApply(this::applyTax)
			.handle((nums, error) -> (error == null) ? nums : SomeService.getPricesRanging(0, 10))
			.thenAccept(safePrices -> log("prices are:: " + safePrices));
		
		if(lastDayBeforeSalary()) {
			promised.completeExceptionally(new IllegalStateException("missing-funds..."));
		} else {
			promised.complete(SomeService.getAllPrices());
		}

		sleepFor(1);
	}

	private boolean lastDayBeforeSalary() {
		return false;
	}

	private List<Integer> applyTax(List<Integer> prices) {
		return prices.stream().map(price -> price + 100).collect(Collectors.toList());
	}

	@Test
	public void attached_success_error_completion_callbacks_are_controlled_by_closure_state() {
		final CompletableFuture<String> promised = makePromise();

		promised.whenCompleteAsync((result, error) -> {
			if(error == null) 
				log("Success callback invoked:: " + result);
			else 
				log("Failure callback will be invoked!");
		});
				
		promised.complete("success");
	}
}