package org.example.matcher.impl;

import org.ahocorasick.trie.Emit;
import org.ahocorasick.trie.Trie;
import org.example.matcher.TextMatcher;

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
public class AhoCorasickTextMatcher extends AbstractTextMatcher {

    private final Trie trie;

    public AhoCorasickTextMatcher(Set<String> searchTerms, boolean caseInsensitive) {
        super(searchTerms, caseInsensitive);

        Trie.TrieBuilder trieBuilder = Trie.builder();
        trieBuilder.addKeywords(this.orderedSearchTerms);
        trieBuilder.ignoreOverlaps();
        if (caseInsensitive) {
            trieBuilder.ignoreCase();
        }
        this.trie = trieBuilder.build();
    }

    @Override
    protected Map<String, List<Integer>> computeMatches(String lines) {
        Map<String, List<Integer>> matches = new HashMap<>();
        for (Emit match : trie.parseText(lines)) {
            String matchedPattern = match.getKeyword();
            matches.computeIfAbsent(matchedPattern, k -> new ArrayList<>()).add(match.getStart());
        }
        return matches;
    }
}
