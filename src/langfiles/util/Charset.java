package langfiles.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.mozilla.universalchardet.UniversalDetector;
import org.mozilla.universalchardet.Constants;

public class Charset {

  protected static final Map<String, String> charsetCodeToDisplayName;
  protected static final List<CharsetEntry> charsetDisplayNameToCode;

  static {
    charsetCodeToDisplayName = new TreeMap<String, String>();
    charsetDisplayNameToCode = new ArrayList<CharsetEntry>();

    int c = 0, total = 75;
    String[][] set = new String[total][2];
    set[c++] = new String[]{"Big5", "Traditional Chinese"};
    set[c++] = new String[]{"Big5-HKSCS", "Traditional Chinese (Hong Kong special character)"};
    set[c++] = new String[]{"EUC-JP", "Japanese"};
    set[c++] = new String[]{"EUC-KR", "Korean"};
    set[c++] = new String[]{"GB18030", "Simplified Chinese"};
    set[c++] = new String[]{"GB2312", "Simplified Chinese"};
    set[c++] = new String[]{"GBK", "Simplified Chinese"};
    set[c++] = new String[]{"ISO-2022-CN", "Simplified Chinese"};
    set[c++] = new String[]{"ISO-2022-JP", "Japanese"};
    set[c++] = new String[]{"ISO-2022-JP-2", "Japanese"};
    set[c++] = new String[]{"ISO-2022-KR", "Korean"};
    set[c++] = new String[]{"ISO-8859-1", "Western European (Latin-1)"};
    set[c++] = new String[]{"ISO-8859-13", "Baltic Rim (Latin-7)"};
    set[c++] = new String[]{"ISO-8859-15", "Western European (Latin-9)"};
    set[c++] = new String[]{"ISO-8859-2", "Eastern European (Latin-2)"};
    set[c++] = new String[]{"ISO-8859-3", "South European (Latin-3)"};
    set[c++] = new String[]{"ISO-8859-4", "North European (Latin-4)"};
    set[c++] = new String[]{"ISO-8859-5", "Cyrillic (Latin)"};
    set[c++] = new String[]{"ISO-8859-6", "Arabic (Latin)"};
    set[c++] = new String[]{"ISO-8859-7", "Greek (Latin)"};
    set[c++] = new String[]{"ISO-8859-8", "Hebrew (Latin"};
    set[c++] = new String[]{"ISO-8859-9", "Turkish (Latin-5)"};
    set[c++] = new String[]{"JIS_X0201", "Japanese"};
    set[c++] = new String[]{"JIS_X0212-1990", "Japanese"};
    set[c++] = new String[]{"KOI8-R", "Korean"};
    set[c++] = new String[]{"KOI8-U", "Korean"};
    set[c++] = new String[]{"Shift_JIS", "Japanese"};
    set[c++] = new String[]{"TIS-620", "Thai"};
    set[c++] = new String[]{"US-ASCII", "ASCII"};
    set[c++] = new String[]{"UTF-16", "UTF-16"};
    set[c++] = new String[]{"UTF-16BE", "UTF-16BE"};
    set[c++] = new String[]{"UTF-16LE", "UTF-16LE"};
    set[c++] = new String[]{"UTF-32", "UTF-32"};
    set[c++] = new String[]{"UTF-32BE", "UTF-32BE"};
    set[c++] = new String[]{"UTF-32LE", "UTF-32LE"};
    set[c++] = new String[]{"UTF-8", "UTF-8"};
    set[c++] = new String[]{"windows-1250", "Central European/Eastern European"};
    set[c++] = new String[]{"windows-1251", "Cyrillic"};
    set[c++] = new String[]{"windows-1252", "English/Western"};
    set[c++] = new String[]{"windows-1253", "Greek"};
    set[c++] = new String[]{"windows-1254", "Turkish"};
    set[c++] = new String[]{"windows-1255", "Hebrew"};
    set[c++] = new String[]{"windows-1256", "Arabic"};
    set[c++] = new String[]{"windows-1257", "Estonian/Latvian/Lithuanian"};
    set[c++] = new String[]{"windows-1258", "Vietnamese"};
    set[c++] = new String[]{"windows-31j", "Japanese Extension"};
    set[c++] = new String[]{"x-Big5-HKSCS-2001", "Traditional Chinese (Hong Kong special character 2001)"};
    set[c++] = new String[]{"x-ISCII91", "Indian"};
    set[c++] = new String[]{"x-iso-8859-11", "Thai"};
    set[c++] = new String[]{"x-JIS0208", "Japanese"};
    set[c++] = new String[]{"x-JISAutoDetect", "Japanese"};
    set[c++] = new String[]{"x-Johab", "Korean"};
    set[c++] = new String[]{"x-MacArabic", "Arabic"};
    set[c++] = new String[]{"x-MacCentralEurope", "Central European"};
    set[c++] = new String[]{"x-MacCroatian", "Croatian"};
    set[c++] = new String[]{"x-MacCyrillic", "Cyrillic"};
    set[c++] = new String[]{"x-MacDingbat", "Dingbat"};
    set[c++] = new String[]{"x-MacGreek", "Greek"};
    set[c++] = new String[]{"x-MacIceland", "Iceland"};
    set[c++] = new String[]{"x-MacRoman", "Roman"};
    set[c++] = new String[]{"x-MacRomania", "Romania"};
    set[c++] = new String[]{"x-MacSymbol", "Symbol"};
    set[c++] = new String[]{"x-MacThai", "Thai"};
    set[c++] = new String[]{"x-MacTurkish", "Turkish"};
    set[c++] = new String[]{"x-MacUkraine", "Ukraine"};
    set[c++] = new String[]{"x-SJIS_0213", "Japanese"};
    set[c++] = new String[]{"x-UTF-16LE-BOM", "UTF-16LE-BOM"};
    set[c++] = new String[]{"X-UTF-32BE-BOM", "UTF-32BE-BOM"};
    set[c++] = new String[]{"X-UTF-32LE-BOM", "UTF-32LE-BOM"};
    set[c++] = new String[]{"x-windows-50220", "Windows 50220"};
    set[c++] = new String[]{"x-windows-50221", "Windows 50221"};
    set[c++] = new String[]{"x-windows-874", "Windows 874"};
    set[c++] = new String[]{"x-windows-949", "Windows 949"};
    set[c++] = new String[]{"x-windows-950", "Windows 950"};
    set[c++] = new String[]{"x-windows-iso2022jp", "Japanese"};

    for (int i = 0, iEnd = c; i < iEnd; i++) {
      charsetCodeToDisplayName.put(set[i][0].toLowerCase(), set[i][1]);
    }


    Map<String, java.nio.charset.Charset> charsetsMap = java.nio.charset.Charset.availableCharsets();
    for (java.nio.charset.Charset charset : charsetsMap.values()) {
      String charsetCode = charset.displayName().toLowerCase();
      if (charsetCode.startsWith("ibm") || charsetCode.startsWith("x-ibm")) {
        continue;
      }

      String charsetDisplayName = charsetCodeToDisplayName.get(charsetCode);
      if (charsetDisplayName != null) {
        charsetDisplayNameToCode.add(new CharsetEntry(charsetDisplayName, charsetCode));
      } else {
        charsetDisplayName = charsetCode.startsWith("x-") ? charsetCode.substring(2) : charsetCode;
        charsetDisplayNameToCode.add(new CharsetEntry(charsetDisplayName, charsetCode));
      }
    }
    Collections.sort(charsetDisplayNameToCode);
  }

  protected Charset() {
  }

  public static Map<String, String> getCodeToDisplayName() {
    return new TreeMap<String, String>(charsetCodeToDisplayName);
  }

  public static List<CharsetEntry> getDisplayNameToCode() {
    return new ArrayList<CharsetEntry>(charsetDisplayNameToCode);
  }

  public static boolean isSupported(String charsetCode) {
    if (charsetCode == null) {
      throw new NullPointerException("argument 'charsetCode' cannot be null");
    }
    return java.nio.charset.Charset.isSupported(charsetCode);
  }

  public static String detect(byte[] b, int offset, int length) {
    if (b == null) {
      throw new NullPointerException("argument 'b' cannot be null");
    }
    if (offset < 0 || offset > b.length || offset + length > b.length) {
      return null;
    }
    UniversalDetector detector = new UniversalDetector(null);
    detector.handleData(b, offset, length);
    detector.dataEnd();
    String charset = detector.getDetectedCharset();
    if (charset == null
            || (charset.equals(Constants.CHARSET_HZ_GB_2312)
            || charset.equals(Constants.CHARSET_X_ISO_10646_UCS_4_3412)
            || charset.equals(Constants.CHARSET_X_ISO_10646_UCS_4_2143))) {
      return null;
    }
    return charset;
  }
}
