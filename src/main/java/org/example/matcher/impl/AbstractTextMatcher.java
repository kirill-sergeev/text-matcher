package org.example.matcher.impl;

import org.example.matcher.Location;
import org.example.matcher.TextMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * An abstract base class for text matchers that provides common functionality for processing
 * and matching search terms in input text. It supports case-insensitive matching, mapping
 * matched substrings to their original search terms, and finding matches across multiple lines.
 *
 * <h3>Features:</h3>
 * <ul>
 *   <li>Optional case-insensitive matching.</li>
 *   <li>Handling of multi-line input text.</li>
 *   <li>Support for mapping matches to their original search terms.</li>
 * </ul>
 */
public abstract class AbstractTextMatcher implements TextMatcher {

    protected final boolean caseInsensitive;
    protected final List<String> orderedSearchTerms;
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Map<String, String> matchToSearchTerm;

    protected AbstractTextMatcher(Set<String> searchTerms, boolean caseInsensitive) {
        if (searchTerms == null || searchTerms.isEmpty()) {
            throw new IllegalArgumentException("Search terms must be specified.");
        }

        Map<String, String> sortedSearchTerms = caseInsensitive
                ? new TreeMap<>(String.CASE_INSENSITIVE_ORDER.reversed())
                : new TreeMap<>(Comparator.reverseOrder());

        for (String word : searchTerms) {
            if (word == null || word.isBlank()) {
                throw new IllegalArgumentException("Invalid search term provided: " + word);
            }
            sortedSearchTerms.put(caseInsensitive ? word.toLowerCase() : word, word);
        }

        this.caseInsensitive = caseInsensitive;
        this.orderedSearchTerms = List.copyOf(sortedSearchTerms.keySet());
        this.matchToSearchTerm = caseInsensitive ? sortedSearchTerms : null;
    }

    @Override
    public Map<String, List<Location>> findMatches(List<String> lines, int initialLineOffset, long initialCharOffset) {
        if (lines == null || initialLineOffset < 0 || initialCharOffset < 0) {
            throw new IllegalArgumentException("List of lines must not be null, offsets must be positive.");
        }

        logger.debug("Processing file chunk of size {} lines, line offset: {}, char offset: {}",
                lines.size(), initialLineOffset, initialCharOffset);

        Map<String, List<Location>> matches = new HashMap<>();
        List<Integer> lineStartPositions = new ArrayList<>();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < lines.size(); i++) {
            lineStartPositions.add(sb.length());
            sb.append(lines.get(i));
            if (i < lines.size() - 1) {
                sb.append('\n');
            }
        }

        String combinedLines = sb.toString();
        for (Map.Entry<String, List<Integer>> entry : computeMatches(combinedLines).entrySet()) {
            String matchedPattern = entry.getKey();
            for (int matchStart : entry.getValue()) {

                long charOffset = initialCharOffset + matchStart;
                int lineNumber = Collections.binarySearch(lineStartPositions, matchStart);
                if (lineNumber < 0) {
                    lineNumber = -lineNumber - 2;
                }
                Location location = new Location(initialLineOffset + lineNumber, charOffset);
                matches.computeIfAbsent(mapToSearchTerm(matchedPattern), k -> new ArrayList<>()).add(location);
            }
        }
        return matches;
    }

    protected abstract Map<String, List<Integer>> computeMatches(String lines);

    private String mapToSearchTerm(String match) {
        return caseInsensitive ? matchToSearchTerm.get(match) : match;
    }
}
