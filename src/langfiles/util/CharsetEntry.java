package langfiles.util;

public class CharsetEntry implements Comparable<Object> {

  protected String displayName;
  protected String charsetCode;

  public CharsetEntry(String displayName, String charsetCode) {
    if (displayName == null) {
      throw new NullPointerException("argument 'displayName' cannot be null");
    }
    if (charsetCode == null) {
      throw new NullPointerException("argument 'charsetCode' cannot be null");
    }
    this.displayName = displayName;
    this.charsetCode = charsetCode;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getCharsetCode() {
    return charsetCode;
  }

  @Override
  public int compareTo(Object o) {
    if (o == null) {
      return 1;
    }
    if (o instanceof CharsetEntry) {
      CharsetEntry entry = (CharsetEntry) o;

      if (this == entry) {
        return 0;
      } else {
        return getDisplayName().compareTo(entry.getDisplayName());
      }
    } else {
      throw new ClassCastException();
    }
  }
}