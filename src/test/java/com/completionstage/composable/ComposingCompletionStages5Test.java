package cc.completionstage.composable;

import static com.completionstage.utils.Commons.getOrFail;
import static com.completionstage.utils.Commons.log;
import static com.completionstage.utils.Commons.sleepFor;
import static com.completionstage.utils.StringUtils.splitBy;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import com.completionstage.utils.TypeSignature;

/**
 * Exercising composition:: that is the really monadic-bind operation
 * 
 * @author i312366
 */
public class ComposingCompletionStages5Test {

	/* monadic bind operation!! */
	@TypeSignature("thenCompose:: CF<T>, (Function:: T -> CF<R>) -> CF<R>")
	@Test
	public void composing_as_many_stages_should_execute_sequentially_while_each_computation_finishes() {
		final String givenText = "functional-composition-is-a-very-cool-aspect";
		
		@TypeSignature("supplyAsync:: (Supplier<String>) -> CF<String>" +
					   "thenApply::   (CF<String> -> (Func<String> -> List<String>)) -> CF<List<String>>" +
					   "thenApply::   (CF<List<String>> -> (Func<List<String>> -> List<String>)) -> CF<List<String>")
		CompletableFuture<Stream<String>> upperCasedParts = CompletableFuture
			.supplyAsync(() -> givenText)
				.thenApply(token -> splitBy("-", token))
				.thenApply(parts -> parts.stream().map(part -> part.toUpperCase()));
		
		
		@TypeSignature("thenCompose:: CF<Stream>, (Function:: Stream -> CF<List>) -> CF<List>")
		CompletableFuture<List<String>> computed = upperCasedParts.thenCompose(this::filterHigherThan3Chars);
		CompletableFuture.runAsync(() -> log(getOrFail(computed)));

		sleepFor(1, "Giving a chance for other computations complete before exiting the JVM");
	}
	
	// CF<CF<T>> ?
	private CompletableFuture<List<String>> filterHigherThan3Chars(Stream<String> parts) {
		return CompletableFuture.supplyAsync(() -> filterPartsGreaterThanXCharsFunc(3).apply(parts));
	}
	
	private Function<Stream<String>, List<String>> filterPartsGreaterThanXCharsFunc(int lowerBound) {
		return (stream) -> stream.filter(part -> part.length() > lowerBound).collect(Collectors.toList());
	}
	
	@Test
	public void a_more_realistic_example_of_composing_async_computations() {
		CompletableFuture<Programmer> retrieveProgrammer = CompletableFuture.supplyAsync(() -> {
			log("retrieving the programmer...");
			return Initializer.getAProgrammer();
		});
		
		Function<Programmer, CompletableFuture<List<KnownLanguage>>> extractExpertise = 
				(programmer) -> CompletableFuture.supplyAsync(() -> {
			log("extracting expertise...");
			return programmer.knownLanguages;
		});
		
		Function<List<KnownLanguage>, CompletableFuture<Stream<Language>>> extractFunctionalLanguages = (expertise) -> CompletableFuture.supplyAsync(() -> {
			log("extracting languages...");
			return expertise
					.stream()
					.filter(knownLanguage -> knownLanguage.lang.type == "functional")
					.map(knownLanguage -> knownLanguage.lang);
		});
		
		Function<Stream<Language>, CompletableFuture<Void>> processLanguages = (knownFunctionalLanguages) -> CompletableFuture.runAsync(() -> {
			log("knownLanguages processed async are:: " + knownFunctionalLanguages.collect(Collectors.toList()));
		});
			
		retrieveProgrammer
			.thenCompose(extractExpertise)
			.thenCompose(extractFunctionalLanguages)
			.thenCompose(processLanguages);
		
		sleepFor(1, "Being async computations happen in other threads therefore main thread is not blocked...");
	}
	
	@Test
	public void any_stage_failure_should_stop_the_propagation_of_further_stages() {
		CompletableFuture<Programmer> retrieveProgrammer = CompletableFuture.supplyAsync(() -> {
			log("retrieving the programmer...");
			return Initializer.getAProgrammer();
		});
		
		Function<Programmer, CompletableFuture<List<KnownLanguage>>> extractExpertise = (programmer) -> CompletableFuture.supplyAsync(() -> {
			log("extracting expertise...");
			return programmer.knownLanguages;
		});
		
		Function<List<KnownLanguage>, CompletableFuture<Stream<Language>>> extractFunctionalLanguages = (expertise) -> CompletableFuture.supplyAsync(() -> {
			throw new IllegalStateException("some exception occured!");
		});
		
		Function<Stream<Language>, CompletableFuture<Void>> processLanguages = (knownFunctionalLanguages) -> CompletableFuture.runAsync(() -> {
			log("knownLanguages processed async are:: " + knownFunctionalLanguages.collect(Collectors.toList()));
		});
			
		retrieveProgrammer
			.thenCompose(extractExpertise)
			.thenCompose(extractFunctionalLanguages)
			.thenCompose(processLanguages);
		
		sleepFor(1, "Being async computations happen in other threads therefore main thread is not blocked...");
	}
	
	
	/* ;;;;;;;;;;;;;;;;; setup ;;;;;;;;;;;;;;;;;;; */
	private static class Initializer {
		static Programmer getAProgrammer() {
			final Programmer programmer = new Programmer();
			programmer.knownLanguages = Arrays.asList(buildLanguage("java", "imperative", 5), 
													  buildLanguage("clojure", "functional", 3),
													  buildLanguage("javascript", "imperative", 4));
			return programmer;
		}

		private static KnownLanguage buildLanguage(String name, String type, int level) {
			Language lang = new Language(name, type);
			return new KnownLanguage(lang, level);
		}
	}
	
	static class Programmer {
		List<KnownLanguage> knownLanguages;
	}
	
	static class KnownLanguage {
		KnownLanguage(Language lang, int level) {
			this.lang = lang;
			this.level = level;
		}
		Language lang;
		int level;
		@Override public String toString() {
			return "lang:: " + lang + ", level:: " + level;
		}
	}
	
	static class Language {
		Language(String name, String type) {
			this.name = name;
			this.type = type;
		}
		String name;
		String type;
		@Override public String toString() {
			return "name:: " + name + ", type:: " + type;
		}
	}
}