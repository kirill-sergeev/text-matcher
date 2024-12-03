package org.example.matcher;

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

public abstract class AbstractTextMatcherTest {

    static Stream<Arguments> provideEdgeCases() {
        return Stream.of(
                Arguments.of("", 0, 0, Set.of("test"), Map.of()),
                Arguments.of("test", 0, 0, Set.of("test"), Map.of("test", List.of(new Location(0, 0L)))),
                Arguments.of("abc", 1, 5, Set.of("test"), Map.of()),
                Arguments.of("test", 1, 10, Set.of("test"), Map.of("test", List.of(new Location(1, 10L))))
        );
    }

    static Stream<Arguments> provideCasesForOverlaps() {
        return Stream.of(
                // Case 1: Simple Match
                Arguments.of(
                        Set.of("Jose", "Joseph"),
                        List.of("Joseph is a name. Jose is another."),
                        Map.of(
                                "Joseph", List.of(new Location(0, 0)),
                                "Jose", List.of(new Location(0, 19))
                        ),
                        false
                ),
                // Case 2: Overlapping without match order change
                Arguments.of(
                        Set.of("Jose", "Joseph"),
                        List.of("JoseJoseph"),
                        Map.of(
                                "Jose", List.of(new Location(0, 0)),
                                "Joseph", List.of(new Location(0, 5))
                        ),
                        false
                ),
                // Case 3: Case-insensitive match
                Arguments.of(
                        Set.of("jose", "joseph"),
                        List.of("JOSEPH and JOSE"),
                        Map.of(
                                "joseph", List.of(new Location(0, 0)),
                                "jose", List.of(new Location(0, 11))
                        ),
                        true
                )
        );
    }

    protected abstract TextMatcher createMatcher(Set<String> searchTerms, boolean caseInsensitive);

    @Test
    @DisplayName("constructor should throw IllegalArgumentException when search terms are null or empty")
    void constructor_shouldThrowWhenSearchTermsInvalid() {
        assertThrows(IllegalArgumentException.class,
                () -> createMatcher(null, false));
        assertThrows(IllegalArgumentException.class,
                () -> createMatcher(Collections.emptySet(), false));
    }

    @Test
    @DisplayName("constructor should throw IllegalArgumentException when search terms contain only blanks")
    void constructor_shouldThrowWhenSearchTermsContainOnlyBlanks() {
        Set<String> searchTerms = new HashSet<>(Arrays.asList(" ", "\t", "\n"));
        assertThrows(IllegalArgumentException.class,
                () -> createMatcher(searchTerms, false));
    }

    @Test
    @DisplayName("constructor should properly initialize matcher for valid search terms")
    void constructor_shouldInitializeForValidSearchTerms() {
        Set<String> searchTerms = new HashSet<>(Arrays.asList("test", "example"));
        TextMatcher matcher = createMatcher(searchTerms, false);
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
        TextMatcher matcher = createMatcher(Set.of("test"), false);
        if (shouldPass) {
            assertDoesNotThrow(() -> matcher.findMatches(List.of("This is a test"), lineOffset, charOffset));
        } else {
            assertThrows(IllegalArgumentException.class, () -> matcher.findMatches(List.of("This is a test"), lineOffset, charOffset));
        }
    }

    @Test
    @DisplayName("findMatches should return empty result when no matches are found")
    void findMatches_shouldReturnEmptyWhenNoMatches() {
        TextMatcher matcher = createMatcher(Set.of("nonexistent"), false);
        Map<String, List<Location>> matches = matcher.findMatches(List.of("This is a test"), 0, 0);
        assertTrue(matches.isEmpty());
    }

    @Test
    @DisplayName("findMatches should return correct matches for single line input")
    void findMatches_shouldReturnMatchesForSingleLine() {
        TextMatcher matcher = createMatcher(Set.of("test", "example"), false);
        Map<String, List<Location>> matches = matcher.findMatches(List.of("This is a test example."), 0, 0);

        assertEquals(2, matches.size());
        assertTrue(matches.containsKey("test"));
        assertTrue(matches.containsKey("example"));
    }

    @Test
    @DisplayName("findMatches should handle case insensitivity correctly")
    void findMatches_shouldHandleCaseInsensitive() {
        TextMatcher matcher = createMatcher(Set.of("Test", "EXAMPLE"), true);
        Map<String, List<Location>> matches = matcher.findMatches(List.of("This is a test example."), 0, 0);

        assertEquals(2, matches.size());
        assertTrue(matches.containsKey("Test"));
        assertTrue(matches.containsKey("EXAMPLE"));
    }

    @DisplayName("findMatches should correctly handle non-overlapping matching")
    @ParameterizedTest
    @MethodSource("provideCasesForOverlaps")
    void findMatches_shouldHandleOverlaps(Set<String> searchTerms, List<String> inputLines,
                                          Map<String, List<Location>> expectedMatches, boolean caseInsensitive) {
        TextMatcher matcher = createMatcher(searchTerms, caseInsensitive);
        Map<String, List<Location>> actualMatches = matcher.findMatches(inputLines, 0, 0);

        assertEquals(expectedMatches.size(), actualMatches.size());

        expectedMatches.forEach((key, expectedLocations) -> {
            assertTrue(actualMatches.containsKey(key));
            assertEquals(expectedLocations.size(), actualMatches.get(key).size());
        });
    }

    @ParameterizedTest
    @MethodSource("provideEdgeCases")
    @DisplayName("findMatches should handle edge cases")
    void findMatches_shouldHandleEdgeCases(String line, int lineOffset, long charOffset, Set<String> searchTerms, Map<String, List<Location>> expected) {
        TextMatcher matcher = createMatcher(searchTerms, false);
        Map<String, List<Location>> actual = matcher.findMatches(List.of(line), lineOffset, charOffset);
        assertEquals(expected, actual);
    }
}
