package org.example.matcher;

import org.example.matcher.impl.AhoCorasickTextMatcher;
import org.example.matcher.impl.NaiveTextMatcher;
import org.example.matcher.impl.RegexTextMatcher;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Fork(value = 1, warmups = 1)
@Warmup(iterations = 1)
@Measurement(iterations = 3)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class TextMatcherBenchmark {

    public static void main(String[] args) throws Exception {
        new Runner(new OptionsBuilder()
                .include(TextMatcherBenchmark.class.getSimpleName())
                .shouldFailOnError(true)
                .build())
                .run();
    }

    @Benchmark
    public Map<String, List<Location>> benchmarkAhoCorasickMatcher(BenchmarkState state) {
        return state.ahoCorasickMatcher.findMatches(state.lines, 0, 0);
    }

    @Benchmark
    public Map<String, List<Location>> benchmarkAhoCorasickMatcherIgnoreCase(BenchmarkState state) {
        return state.ahoCorasickMatcherIgnoreCase.findMatches(state.lines, 0, 0);
    }

    @Benchmark
    public Map<String, List<Location>> benchmarkRegexMatcher(BenchmarkState state) {
        return state.regexMatcher.findMatches(state.lines, 0, 0);
    }

    @Benchmark
    public Map<String, List<Location>> benchmarkRegexMatcherIgnoreCase(BenchmarkState state) {
        return state.regexMatcherIgnoreCase.findMatches(state.lines, 0, 0);
    }

    @Benchmark
    public Map<String, List<Location>> benchmarkNaiveMatcher(BenchmarkState state) {
        return state.naiveMatcher.findMatches(state.lines, 0, 0);
    }

    @Benchmark
    public Map<String, List<Location>> benchmarkNaiveMatcherIgnoreCase(BenchmarkState state) {
        return state.naiveMatcherIgnoreCase.findMatches(state.lines, 0, 0);
    }

    @State(Scope.Benchmark)
    public static class BenchmarkState {

        List<String> lines;
        Set<String> searchTerms;
        TextMatcher ahoCorasickMatcher;
        TextMatcher ahoCorasickMatcherIgnoreCase;
        TextMatcher regexMatcher;
        TextMatcher regexMatcherIgnoreCase;
        TextMatcher naiveMatcher;
        TextMatcher naiveMatcherIgnoreCase;

        @Setup(Level.Trial)
        public void setup() throws Exception {
            lines = Files.readAllLines(Paths.get("src/test/resources/big.txt"));
            searchTerms = Set.of("James", "John", "Robert", "Michael", "William", "David", "Richard", "Charles",
                    "Joseph", "Thomas", "Christopher", "Daniel", "Paul", "Mark", "Donald", "George", "Kenneth",
                    "Steven", "Edward", "Brian", "Ronald", "Anthony", "Kevin", "Jason", "Matthew", "Gary", "Timothy",
                    "Jose", "Larry", "Jeffrey", "Frank", "Scott", "Eric", "Stephen", "Andrew", "Raymond", "Gregory",
                    "Joshua", "Jerry", "Dennis", "Walter", "Patrick", "Peter", "Harold", "Douglas", "Henry", "Carl",
                    "Arthur", "Ryan", "Roger");

            ahoCorasickMatcher = new AhoCorasickTextMatcher(searchTerms, false);
            ahoCorasickMatcherIgnoreCase = new AhoCorasickTextMatcher(searchTerms, true);
            regexMatcher = new RegexTextMatcher(searchTerms, false);
            regexMatcherIgnoreCase = new RegexTextMatcher(searchTerms, true);
            naiveMatcher = new NaiveTextMatcher(searchTerms, false);
            naiveMatcherIgnoreCase = new NaiveTextMatcher(searchTerms, true);
        }
    }
}