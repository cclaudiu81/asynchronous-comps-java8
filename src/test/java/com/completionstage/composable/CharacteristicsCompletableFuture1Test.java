package com.completionstage.composable;

import static com.completionstage.utils.Commons.log;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import org.junit.Assert;
import org.junit.Test;

import com.completionstage.utils.StringUtils;
import com.completionstage.utils.TypeSignature;
/**
 * 
 * @author i312366
 */
public class CharacteristicsCompletableFuture1Test {
	
	@Test
	public void declaratively_defining_completable_futures_should_apply_transformations() throws InterruptedException, ExecutionException {
		// declare FIRST the task to apply transformations to
		CompletableFuture<String> firstTransformation = new CompletableFuture<String>();

		firstTransformation
				.thenApply(deliveredInput -> deliveredInput.toUpperCase());
			
		CompletableFuture<List<String>> secondTransformation = firstTransformation
				.thenApply(deliveredInput -> StringUtils.splitBy("-", deliveredInput));
		
		@TypeSignature("whenComplete:: BiConsumer<R, E> -> CF<R> ")
		CompletableFuture<List<String>> lastTransformation = secondTransformation.whenComplete((endResult, err) -> {
			log(endResult);
		});
		
		firstTransformation.complete("java-or-clojure");
		assertThat(lastTransformation.get(), is(Arrays.asList("java", "or", "clojure")));
	}
	
	@Test
	public void declarative_completable_future_can_launch_a_missile_if_required_by_your_manager_in_the_future() {
		// may encourage for declaratively defining the working-tasks, prior any triggering-start
		final CompletableFuture<String> launcher = new CompletableFuture<>();
		
		launcher.thenAccept(missileName -> log("launching a missile of type:: " + missileName));
		
		// ...at a later point in time...
		launcher.complete("air-to-air");
		
		Assert.assertTrue(launcher.isDone());
	}

	@Test 
	public void composable_style_completable_futures_composed() throws InterruptedException, ExecutionException {
		CompletableFuture<String> cf1 = CompletableFuture
			.supplyAsync(() -> "completable-future")
			.thenApply(some -> some.toUpperCase());
		
		Function<String, CompletionStage<List<String>>> cf2 = (str) -> CompletableFuture.supplyAsync(() -> StringUtils.splitBy("-", str));
			
		CompletableFuture<List<String>> cf3 = cf1.thenCompose(cf2);
		
		assertThat(cf3.get(), is(Arrays.asList("COMPLETABLE", "FUTURE")));
	}
}