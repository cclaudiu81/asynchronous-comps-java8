package com.completionstage.prev;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.function.Function;

import com.completionstage.utils.TypeSignature;

import cc.jcip.commons.ThreadSafe;

/**
 * 
 * simple Functor-ish style / non-blocking implementation for the thenApply()
 * transformation
 * 
 * @author i312366
 *
 * @param <T>
 */
@ThreadSafe
public class SimplifiedCompletionFuture<T> {
	private final ExecutorService execService = Executors.newCachedThreadPool();
	private final FutureTask<T> initialFutureTask;

	private SimplifiedCompletionFuture(Callable<T> taskSupplier) {
		this.initialFutureTask = new FutureTask<>(taskSupplier);
	}

	private SimplifiedCompletionFuture(FutureTask<T> futureTask) {
		this.initialFutureTask = futureTask;
	}
	
	/** static factory constructor-function */
	public static final <R> SimplifiedCompletionFuture<R> from(Callable<R> taskSupplier) {
		return new SimplifiedCompletionFuture<>(taskSupplier);
	}

	@TypeSignature("thenApply:: (Function<IN, OUT>, M<IN>) -> M<OUT>")
	public <R> SimplifiedCompletionFuture<R> thenApply(Function<T, R> transformationFunc) {
		execService.execute(initialFutureTask);

		Future<T> accessedInitialComputation = execService.submit(
				() -> initialFutureTask.get());
		Future<R> appliedComputation = execService.submit(
				() -> transformationFunc.apply(accessedInitialComputation.get()));

		return new SimplifiedCompletionFuture<>((FutureTask<R>) appliedComputation);
	}

	/* can make it default-pack */
	public T get() throws InterruptedException, ExecutionException {
		return initialFutureTask.get();
	}

}