package cc.completionstage.prev;

import static com.completionstage.utils.Commons.sleepFor;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.completionstage.utils.Puzzler;
/**
 * 
 * @author i312366
 */
public class NonBlockerCompletionService1Test {

	@Test
	public void takeAny_should_take_first_completed_computation() throws InterruptedException, ExecutionException {
		ExecutorService execService = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
		ExecutorCompletionService<String> completionService = new ExecutorCompletionService<>(execService);
		
		Future<String> notUsed1 = completionService.submit(() -> {
			sleepFor(1, "simulating long running task-1");
			return "long-running-task-1";
		});
		Future<String> notUsed2 = completionService.submit(() -> {
			sleepFor(2, "simulating long running task-2");
			return "long-running-task-2";
		});

		
		Future<String> first = takeAnyOrTimeout(completionService, 4000);
		assertThat(first.get(), is("long-running-task-1"));
	}
	
	@Puzzler
	@Test
	public void takeAny_should_wait_for_any_dependent_computations() throws InterruptedException, ExecutionException {
		ExecutorService execService = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
		ExecutorCompletionService<String> completionService = new ExecutorCompletionService<>(execService);
		
		Future<String> result1 = completionService.submit(() -> {
			sleepFor(2, "simulating long running task-1");
			return "long-running-task-1";
		});
		Future<String> result2 = completionService.submit(() -> {
			sleepFor(1, "simulating long running task-2");
			return result1.get().toUpperCase();
		});

		Future<String> first = takeAnyOrTimeout(completionService, 1);
		assertThat(first.get(), is("LONG-RUNNING-TASK-1"));
	}
	
	
	private static final <T> Future<T> takeAnyOrTimeout(ExecutorCompletionService<T> completionService, int noMoreTasksTimeout) throws InterruptedException {
		return completionService.poll((noMoreTasksTimeout * 1000), TimeUnit.SECONDS);
	}
}