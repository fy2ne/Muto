package me.fy2ne.muto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class MutoLog {
    public static final String MOD_ID = "muto";
    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static final Path LOG_FILE = Paths.get("logs", "muto.log");
    private static BufferedWriter writer;

    static {
        try {
            if (LOG_FILE.getParent() != null) {
                Files.createDirectories(LOG_FILE.getParent());
            }
            writer = Files.newBufferedWriter(LOG_FILE,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND,
                    StandardOpenOption.WRITE);
        } catch (IOException e) {
            LOGGER.warn("failed opening muto.log: {}", e.getMessage());
        }
    }

    private MutoLog() {}

    public static synchronized void info(String msg, Object... args) {
        LOGGER.info(msg, args);
        writeToFile("INFO", msg, args);
    }

    public static synchronized void warn(String msg, Object... args) {
        LOGGER.warn(msg, args);
        writeToFile("WARN", msg, args);
    }

    public static synchronized void error(String msg, Object... args) {
        LOGGER.error(msg, args);
        writeToFile("ERROR", msg, args);
    }

    public static synchronized void error(String msg, Throwable t) {
        LOGGER.error(msg, t);
        writeToFile("ERROR", msg + (t != null ? " | " + t : ""));
    }

    private static void writeToFile(String level, String msg, Object... args) {
        if (writer == null) return;
        try {
            String formatted = formatMessage(msg, args);
            writer.write(String.format("[%s] [%s] %s%n", LocalDateTime.now().format(TIME_FMT), level, formatted));
            writer.flush();
        } catch (IOException ignored) {}
    }

    private static String formatMessage(String pattern, Object... args) {
        if (args == null || args.length == 0) return pattern;
        StringBuilder sb = new StringBuilder(pattern.length() + 32);
        int argIdx = 0;
        int cur = 0;
        while (cur < pattern.length()) {
            int placeholder = pattern.indexOf("{}", cur);
            if (placeholder < 0 || argIdx >= args.length) {
                sb.append(pattern.substring(cur));
                break;
            }
            sb.append(pattern, cur, placeholder);
            sb.append(args[argIdx++]);
            cur = placeholder + 2;
        }
        if (argIdx < args.length && args[argIdx] instanceof Throwable t) {
            sb.append(" | ").append(t);
        }
        return sb.toString();
    }
}
