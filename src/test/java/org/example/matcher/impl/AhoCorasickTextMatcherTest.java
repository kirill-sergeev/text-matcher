package org.example.matcher.impl;

import org.example.matcher.Location;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AhoCorasickTextMatcherTest {

    static Stream<Arguments> provideEdgeCases() {
        return Stream.of(
                Arguments.of("", 0, 0, Set.of("test"), Map.of()),
                Arguments.of("test", 0, 0, Set.of("test"), Map.of("test", List.of(new Location(0, 0L)))),
                Arguments.of("abc", 1, 5, Set.of("test"), Map.of()),
                Arguments.of("test", 1, 10, Set.of("test"), Map.of("test", List.of(new Location(1, 10L))))
        );
    }

    @Test
    @DisplayName("constructor should throw IllegalArgumentException when search terms are null or empty")
    void constructor_shouldThrowWhenSearchTermsInvalid() {
        assertThrows(IllegalArgumentException.class,
                () -> new AhoCorasickTextMatcher(null, false));
        assertThrows(IllegalArgumentException.class,
                () -> new AhoCorasickTextMatcher(Collections.emptySet(), false));
    }

    @Test
    @DisplayName("constructor should throw IllegalArgumentException when search terms contain only blanks")
    void constructor_shouldThrowWhenSearchTermsContainOnlyBlanks() {
        Set<String> searchTerms = new HashSet<>(Arrays.asList(" ", "\t", "\n"));
        assertThrows(IllegalArgumentException.class,
                () -> new AhoCorasickTextMatcher(searchTerms, false));
    }

    @Test
    @DisplayName("constructor should properly initialize matcher for valid search terms")
    void constructor_shouldInitializeForValidSearchTerms() {
        Set<String> searchTerms = new HashSet<>(Arrays.asList("test", "example"));
        AhoCorasickTextMatcher matcher = new AhoCorasickTextMatcher(searchTerms, false);
        assertNotNull(matcher);
    }

    @ParameterizedTest
    @CsvSource({
            "0, 0, true",
            "-1, 0, false",
            "0, -1, false",
            "-1, -1, false"
    })
    @DisplayName("findMatches should throw IllegalArgumentException for invalid offsets")
    void findMatches_shouldThrowForInvalidOffsets(int lineOffset, long charOffset, boolean shouldPass) {
        AhoCorasickTextMatcher matcher = new AhoCorasickTextMatcher(Set.of("test"), false);
        if (shouldPass) {
            assertDoesNotThrow(() -> matcher.findMatches(List.of("This is a test"), lineOffset, charOffset));
        } else {
            assertThrows(IllegalArgumentException.class, () -> matcher.findMatches(List.of("This is a test"), lineOffset, charOffset));
        }
    }

    @Test
    @DisplayName("findMatches should return empty result when no matches are found")
    void findMatches_shouldReturnEmptyWhenNoMatches() {
        AhoCorasickTextMatcher matcher = new AhoCorasickTextMatcher(Set.of("nonexistent"), false);
        Map<String, List<Location>> matches = matcher.findMatches(List.of("This is a test"), 0, 0);
        assertTrue(matches.isEmpty());
    }

    @Test
    @DisplayName("findMatches should return correct matches for single line input")
    void findMatches_shouldReturnMatchesForSingleLine() {
        AhoCorasickTextMatcher matcher = new AhoCorasickTextMatcher(Set.of("test", "example"), false);
        Map<String, List<Location>> matches = matcher.findMatches(List.of("This is a test example."), 0, 0);

        assertEquals(2, matches.size());
        assertTrue(matches.containsKey("test"));
        assertTrue(matches.containsKey("example"));
    }

    @Test
    @DisplayName("findMatches should handle case insensitivity correctly")
    void findMatches_shouldHandleCaseInsensitive() {
        AhoCorasickTextMatcher matcher = new AhoCorasickTextMatcher(Set.of("Test", "EXAMPLE"), true);
        Map<String, List<Location>> matches = matcher.findMatches(List.of("This is a test example."), 0, 0);

        assertEquals(2, matches.size());
        assertTrue(matches.containsKey("test"));
        assertTrue(matches.containsKey("example"));
    }

    @ParameterizedTest
    @MethodSource("provideEdgeCases")
    @DisplayName("findMatches should handle edge cases")
    void findMatches_shouldHandleEdgeCases(String line, int lineOffset, long charOffset, Set<String> searchTerms, Map<String, List<Location>> expected) {
        AhoCorasickTextMatcher matcher = new AhoCorasickTextMatcher(searchTerms, false);
        Map<String, List<Location>> actual = matcher.findMatches(List.of(line), lineOffset, charOffset);
        assertEquals(expected, actual);
    }
}
