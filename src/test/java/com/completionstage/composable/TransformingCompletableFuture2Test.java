package com.completionstage.composable;

import static com.completionstage.utils.Commons.getSecsSinceEpoch;
import static com.completionstage.utils.Commons.log;
import static com.completionstage.utils.Commons.sleepFor;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.junit.Test;

import com.completionstage.utils.StringUtils;
import com.completionstage.utils.TypeSignature;

/**
 * 
 * @author i312366
 */
public class TransformingCompletableFuture2Test {
	
	@TypeSignature(
			   "supplyAsync:: Supplier<T>    -> CF<T>" + 
			   "thenApply::   Function<T, R> -> CF<R>" +
			   "thenApply::   Function<R, V> -> CF<V>" +
			   "thenAccept::  Consumer<V>    -> CF<Void>" +
			   "thenRun::     Runnable<Void> -> CF<Void>")
	@Test
	public void supplied_completable_future_transforms_chained_lifted_computations_without_blocking_when_not_forced() throws InterruptedException, ExecutionException {
		
	CompletableFuture<Void> sideEffectVoidResult = CompletableFuture.supplyAsync(() -> "some-string")
					.thenApply(someString -> someString.toUpperCase())
					.thenApply(someString -> StringUtils.splitBy("", someString))
					.thenAccept(splittedChars -> log("logging somewhere the computation:: " + splittedChars))
					.thenRun(() -> log("running some logging/side-effect operation"));
	}

	/* supply + apply:: but block on .get() -> the usual case */
	@Test
	public void supplied_completable_future_blocks_on_FINAL_get_call_and_each_task_is_awaiting_for_previous_to_complete() throws InterruptedException, ExecutionException {
		long startTime = getSecsSinceEpoch();
		
		@TypeSignature("string -> CF<string>")
		CompletableFuture<String> liftedStringifiedTask = CompletableFuture.supplyAsync(() -> {
			sleepFor(1);
			return "some-string";
		});
		
		@TypeSignature("string -> CF<string>")
		CompletableFuture<String> uppercasedStringifiedTask = liftedStringifiedTask.thenApply(stringifiedTask -> {
			sleepFor(1);
			return stringifiedTask.toUpperCase();
		});
		
		// in the end the get() still blocks but the idea is to attach computations rather than blocking the main thread
		final String computedStr = uppercasedStringifiedTask.get();
		final long endTime = getSecsSinceEpoch();
		
		assertThat(computedStr, is("SOME-STRING"));
		assertThat((endTime - startTime), is(2L));
	}
	
	@Test
	public void supplied_completable_future_will_not_wait_for_task_but_return_a_fallback_result() {
		CompletableFuture<String> aTask = CompletableFuture.supplyAsync(() -> {
			sleepFor(1);
			return "some-string";
		});
		
		String fallbackResult = aTask.getNow("fallback result:: IDE helps identifying the 'dereferenced' result");

		assertThat(fallbackResult, is("fallback result:: IDE helps identifying the 'dereferenced' result"));
	}
}