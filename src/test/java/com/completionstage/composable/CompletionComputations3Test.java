package com.completionstage.composable;

import static com.completionstage.utils.Commons.ensureIsNotNull;
import static com.completionstage.utils.Commons.ensureIsNull;
import static com.completionstage.utils.Commons.failWith;
import static com.completionstage.utils.Commons.log;
import static com.completionstage.utils.Commons.sleepFor;
import static com.completionstage.utils.Commons.typeOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.junit.Test;

import com.completionstage.utils.TypeSignature;
/**
 * 
 * @author i312366
 */
public class CompletionComputations3Test {

	@Test
	public void exceptionally_should_recover_from_error_and_continue_chained_computations() throws InterruptedException, ExecutionException {
		CompletableFuture<String> faultTolerantResult = CompletableFuture.supplyAsync(() -> {
			failWith("a dependent service failed with an exception...");
			return "possible-success-service-result";
		}).exceptionally(error -> {
			log("There was an error during the service invocation:: " + error.getMessage());
			return "default-result";
		}).thenApply(result -> result.toUpperCase());
		
		assertThat(faultTolerantResult.get(), is("DEFAULT-RESULT"));
	}
	
	@Test
	public void exceptionally_should_be_skipped_for_successfull_computation() throws InterruptedException, ExecutionException {
		CompletableFuture<String> successfullResult = CompletableFuture.supplyAsync(() -> {
			return "success-service-result";
		}).exceptionally(error -> {
			log("There was an error during the service invocation:: " + error.getMessage());
			return "default-result";
		}).thenApply(result -> result.toUpperCase());
		
		assertThat(successfullResult.get(), is("SUCCESS-SERVICE-RESULT"));
	}
	
	@Test
	public void exceptionally_should_convert_failing_exception_to_domain_errors() throws InterruptedException, ExecutionException {
		CompletableFuture<String> successfullResult = CompletableFuture.supplyAsync(() -> {
			failWith("third-party-exception");
			return "success-service-result";
		}).exceptionally(error -> {
			log("There was an error during the service invocation:: " + error.getMessage());
			throw new DomainException("a domain-specific-error", error);
		}).handle((result, domainError) -> {
			if(typeOf(domainError, DomainException.class)) 
				log("catch-all should handle domain-specific-errors");
			return "returning-from-domain-error";
		});
		
		assertThat(successfullResult.get(), is("returning-from-domain-error"));
	}


	/* callbacks:: supply & apply but attempt to resolve on success-callback rather than main-thread */
	@Test
	public void whenCompleteAsync_should_resolve_asynchronously() throws InterruptedException, ExecutionException {
		CompletableFuture<String> liftedStringifiedTask = CompletableFuture.supplyAsync(
				() -> "some-string");
		
		CompletableFuture<String> uppercasedStringifiedTask = liftedStringifiedTask.thenApplyAsync(
			stringifiedTask -> {
				sleepFor(1);
				return stringifiedTask.toUpperCase();
		});
		
		@TypeSignature("whenComplete:: BiConsumer<T,E> -> CF<T>")
		CompletableFuture<String> successResult = uppercasedStringifiedTask.whenComplete(
			(upperCasedResult, orError) -> {
				ensureIsNotNull(upperCasedResult);
				ensureIsNull(orError);
				log("logging something from success-callback-thread...on successfull completion...result:: " + upperCasedResult);
		});
		
		sleepFor(1);
	}
	
	static final class DomainException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		public DomainException(String message, Throwable t) {
			super(message, t);
		}
	}
}