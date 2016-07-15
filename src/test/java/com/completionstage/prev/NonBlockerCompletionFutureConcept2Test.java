package cc.completionstage.prev;

import static com.completionstage.utils.Commons.log;
import static com.completionstage.utils.Commons.sleepFor;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.junit.Test;

import com.completionstage.prev.SimplifiedCompletionFuture;
import com.completionstage.utils.StringUtils;
import com.completionstage.utils.TypeSignature;

/**
 * Exercising the {@linkplain SimplifiedCompletionFuture}
 * 
 * @author i312366
 */
public class NonBlockerCompletionFutureConcept2Test {

	@Test
	public void simple_thenApply_implementation_should_chain_transformations() throws InterruptedException, ExecutionException {
		@TypeSignature("from:: Callable<T> -> Wrapper<T>")
		final SimplifiedCompletionFuture<String> accessedData = SimplifiedCompletionFuture.from(
				() -> "java-or-clojure");
		
		@TypeSignature("thenApply:: Wrapper<T>, Function<T, R> -> Wrapper<R>")
		SimplifiedCompletionFuture<List<String>> transformedContainer = accessedData.thenApply(
				(data) -> StringUtils.splitBy("-", data));

		assertThat(transformedContainer.get(), is(Arrays.asList("java", "or", "clojure")));
	}

	@Test
	public void simple_thenApply_implementation_should_not_block_and_execute_async() throws InterruptedException, ExecutionException {

		final SimplifiedCompletionFuture<String> accessedData = SimplifiedCompletionFuture.from(
			() -> {
				sleepFor(1, "simulating long string-access computation");
				log("debugging:: executing supplier - string-access, from thread:: " + currentThreadName());
				return "java-or-clojure";
		});

		SimplifiedCompletionFuture<List<String>> transformedData = accessedData.thenApply(
			data -> {
				List<String> tokenized = StringUtils.splitBy("-", data);
				log("debugging:: executing function-transformation from thread:: " 
						+ currentThreadName()
						+ " is:: "
						+ tokenized);

				return tokenized;
		});
		
		log("debugging:: resuming main execution thread:: " + currentThreadName());
		sleepFor(2, "Giving a chance for other threads to complete before JVM exits");
	}
	
	private static final String currentThreadName() {
		return Thread.currentThread().getName();
	}
}