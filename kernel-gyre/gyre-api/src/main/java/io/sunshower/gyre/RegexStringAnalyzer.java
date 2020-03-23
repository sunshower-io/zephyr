package io.sunshower.gyre;

import java.util.Iterator;
import java.util.regex.Pattern;

public class RegexStringAnalyzer implements Analyzer<String, String> {
  private final Pattern pattern;

  public RegexStringAnalyzer(String pattern) {
    this.pattern = Pattern.compile(pattern);
  }

  @Override
  public Iterator<String> segments(String key) {
    return new ArrayIterator<>(pattern.split(key));
  }
}
