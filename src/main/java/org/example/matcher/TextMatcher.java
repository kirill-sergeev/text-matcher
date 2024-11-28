package org.example.matcher;

import java.util.List;
import java.util.Map;

/**
 * Interface representing a text pattern matcher that finds occurrences of specific patterns within a set of lines.
 */
public interface TextMatcher {

    Map<String, List<Location>> findMatches(List<String> lines, int startingLineOffset, long startingCharOffset);
}
