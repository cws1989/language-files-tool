package langfiles.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Locale;

/**
 * Wrapper for PrintStream. Save the log to file also output to System.out.
 * @author Chan Wai Shing <cws1989@gmail.com>
 */
public class LoggingPrintStream extends PrintStream {

  /**
   * Constructor.
   * @param out the {@link java.io.OutputStream} to output the log to
   */
  public LoggingPrintStream(OutputStream out) {
    super(out);
  }

  @Override
  public PrintStream format(Locale l, String format, Object... args) {
    super.format(l, format, args);
    System.out.print(l);
    System.out.print(",");
    System.out.print(format);
    System.out.print(",");
    System.out.print(args);
    return this;
  }

  @Override
  public PrintStream format(String format, Object... args) {
    super.format(format, args);
    System.out.print(format);
    System.out.print(",");
    System.out.print(args);
    return this;
  }

  @Override
  public PrintStream printf(Locale l, String format, Object... args) {
    super.printf(l, format, args);
    System.out.print(l);
    System.out.print(",");
    System.out.print(format);
    System.out.print(",");
    System.out.print(args);
    return this;
  }

  @Override
  public PrintStream printf(String format, Object... args) {
    super.printf(format, args);
    System.out.print(format);
    System.out.print(",");
    System.out.print(args);
    return this;
  }

  @Override
  public PrintStream append(char c) {
    super.append(c);
    System.out.print(c);
    return this;
  }

  @Override
  public PrintStream append(CharSequence csq) {
    super.append(csq);
    System.out.print(csq);
    return this;
  }

  @Override
  public PrintStream append(CharSequence csq, int start, int end) {
    super.append(csq, start, end);
    System.out.print(csq.subSequence(start, end));
    return this;
  }

  @Override
  public void print(boolean s) {
    super.print(s);
    System.out.print(s);
  }

  @Override
  public void print(char s) {
    super.print(s);
    System.out.print(s);
  }

  @Override
  public void print(char[] s) {
    super.print(s);
    System.out.print(new String(s));
  }

  @Override
  public void print(double s) {
    super.print(s);
    System.out.print(s);
  }

  @Override
  public void print(float s) {
    super.print(s);
    System.out.print(s);
  }

  @Override
  public void print(int s) {
    super.print(s);
    System.out.print(s);
  }

  @Override
  public void print(long s) {
    super.print(s);
    System.out.print(s);
  }

  @Override
  public void print(Object s) {
    super.print(s);
    System.out.print(s);
  }

  @Override
  public void print(String s) {
    super.print(s);
    System.out.print(s);
  }

  @Override
  public void println(boolean s) {
    super.println(s);
    System.out.println(s);
  }

  @Override
  public void println(char s) {
    super.println(s);
    System.out.println(s);
  }

  @Override
  public void println(char[] s) {
    super.println(s);
    System.out.println(new String(s));
  }

  @Override
  public void println(double s) {
    super.println(s);
    System.out.println(s);
  }

  @Override
  public void println(float s) {
    super.println(s);
    System.out.println(s);
  }

  @Override
  public void println(int s) {
    super.println(s);
    System.out.println(s);
  }

  @Override
  public void println(long s) {
    super.println(s);
    System.out.println(s);
  }

  @Override
  public void println(Object s) {
    super.println(s);
    System.out.println(s);
  }

  @Override
  public void println(String s) {
    super.println(s);
    System.out.println(s);
  }

  @Override
  public void write(byte[] b) throws IOException {
    super.write(b);
    System.out.print(new String(b));
  }

  @Override
  public void write(byte[] buf, int off, int len) {
    super.write(buf, off, len);
    System.out.print(new String(buf, off, len));
  }

  @Override
  public void write(int b) {
    super.write(b);
    System.out.print(b);
  }
}
