package org.example.matcher.impl;

import org.ahocorasick.trie.Emit;
import org.ahocorasick.trie.Trie;
import org.example.matcher.Location;
import org.example.matcher.TextMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Implements {@link TextMatcher} using the Aho-Corasick algorithm for efficient multi-pattern string matching.
 * This implementation utilizes the <a href="https://github.com/robert-bor/aho-corasick">ahocorasick-java</a> library,
 * which provides a robust and efficient implementation of the Aho-Corasick algorithm.
 *
 * <p>The Aho-Corasick algorithm is chosen for its ability to search for multiple patterns simultaneously
 * in a given text, with a time complexity of <code>O(n + m + z)</code>, where:
 * <ul>
 *   <li><code>n</code> is the length of the text,</li>
 *   <li><code>m</code> is the total length of all patterns,</li>
 *   <li><code>z</code> is the number of pattern occurrences in the text.</li>
 * </ul>
 */
public class AhoCorasickTextMatcher implements TextMatcher {

    private static final Logger logger = LoggerFactory.getLogger(AhoCorasickTextMatcher.class);

    private final Trie trie;
    private final int shortestKeywordLength;

    public AhoCorasickTextMatcher(Set<String> searchTerms, boolean caseInsensitive) {
        if (searchTerms == null || searchTerms.isEmpty()) {
            throw new IllegalArgumentException("Search terms must be specified.");
        }

        int minLength = Integer.MAX_VALUE;
        Trie.TrieBuilder trieBuilder = Trie.builder();
        for (String word : searchTerms) {
            if (word == null || word.isBlank()) {
                throw new IllegalArgumentException("Invalid search term provided: " + word);
            }

            minLength = Math.min(minLength, word.length());
            if (caseInsensitive) {
                trieBuilder.addKeywords(word.toLowerCase());
                trieBuilder.ignoreCase();
            } else {
                trieBuilder.addKeywords(word);
            }
        }

        this.trie = trieBuilder.build();
        this.shortestKeywordLength = minLength;
    }

    @Override
    public Map<String, List<Location>> findMatches(List<String> lines, int initialLineOffset, long initialCharOffset) {
        if (lines == null || initialLineOffset < 0 || initialCharOffset < 0) {
            throw new IllegalArgumentException("List of lines must not be null, offsets must be positive.");
        }

        logger.debug("Processing file chunk of size {} lines, line offset: {}, char offset: {}",
                lines.size(), initialCharOffset, initialCharOffset);

        Map<String, List<Location>> occurrences = new HashMap<>();
        long currentCharOffset = initialCharOffset;

        for (int lineIndex = 0; lineIndex < lines.size(); lineIndex++) {
            String line = lines.get(lineIndex);

            if (line.length() >= shortestKeywordLength) {
                for (Emit match : trie.parseText(line)) {
                    String matchedPattern = match.getKeyword();
                    Location location = new Location(initialLineOffset + lineIndex, currentCharOffset + match.getStart());
                    occurrences.computeIfAbsent(matchedPattern, k -> new ArrayList<>()).add(location);
                }
            }

            currentCharOffset += line.length() + System.lineSeparator().length();
        }
        return occurrences;
    }
}
