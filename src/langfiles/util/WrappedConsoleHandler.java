package langfiles.util;

import java.util.logging.ConsoleHandler;

/**
 * Redirect logger output to System.out instead of System.err because System.err is logged into log file and logger is also logged into log file.
 * @author Chan Wai Shing <cws1989@gmail.com>
 */
public class WrappedConsoleHandler extends ConsoleHandler {

    public WrappedConsoleHandler() {
        super();
        // redirect to System.out
        setOutputStream(System.out);
    }
}
