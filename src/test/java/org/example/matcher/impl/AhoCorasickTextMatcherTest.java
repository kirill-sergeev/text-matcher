package org.example.matcher.impl;

import org.example.matcher.AbstractTextMatcherTest;
import org.example.matcher.TextMatcher;

import java.util.Set;

class AhoCorasickTextMatcherTest extends AbstractTextMatcherTest {

    @Override
    protected TextMatcher createMatcher(Set<String> searchTerms, boolean caseInsensitive) {
        return new AhoCorasickTextMatcher(searchTerms, caseInsensitive);
    }
}
