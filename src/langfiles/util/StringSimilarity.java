/**
 * PHP Version 5
 * 
 * Copyright (c) 1997-2011 The PHP Group
 * 
 * This source file is subject to version 3.01 of the PHP license,
 * that is bundled with this package in the file LICENSE, and is
 * available through the world-wide-web at the following url:
 * http://www.php.net/license/3_01.txt
 * If you did not receive a copy of the PHP license and are unable to
 * obtain it through the world-wide-web, please send a note to
 * license@php.net so we can mail you a copy immediately.
 * 
 * Author: Hartmut Holzgraefe <hholzgra@php.net>
 * 
 * $Id: levenshtein.c 306939 2011-01-01 02:19:59Z felipe $
 * 
 * Adapted from PHP 5.3.8 /ext/standard/levenshtein.c 2011-9-23
 */
package langfiles.util;

public class StringSimilarity {

  protected StringSimilarity() {
  }

  public static int levenshteinDistance(String s1, String s2) {
    return levenshteinDistance(s1, s2, 1, 1, 1);
  }

  public static int levenshteinDistance(String s1, String s2, int cost_ins, int cost_rep, int cost_del) {
    if (s1 == null || s2 == null) {
      throw new NullPointerException("s1 or s2 is null");
    }

    int l1 = s1.length(), l2 = s2.length();
    char[] s1_chars = s1.toCharArray(), s2_chars = s2.toCharArray();

    int c0 = 0, c1, c2;
    int[] tmp;

    if (l1 == 0) {
      return l2 * cost_ins;
    }
    if (l2 == 0) {
      return l1 * cost_del;
    }

    int[] p1 = new int[l2 + 1];
    int[] p2 = new int[l2 + 1];

    for (int i2 = 0; i2 <= l2; i2++) {
      p1[i2] = i2 * cost_ins;
    }
    for (int i1 = 0; i1 < l1; i1++) {
      p2[0] = p1[0] + cost_del;

      for (int i2 = 0; i2 < l2; i2++) {
        c0 = p1[i2] + ((s1_chars[i1] == s2_chars[i2]) ? 0 : cost_rep);
        c1 = p1[i2 + 1] + cost_del;
        if (c1 < c0) {
          c0 = c1;
        }
        c2 = p2[i2] + cost_ins;
        if (c2 < c0) {
          c0 = c2;
        }
        p2[i2 + 1] = c0;
      }
      tmp = p1;
      p1 = p2;
      p2 = tmp;
    }

    return c0;
  }
}
