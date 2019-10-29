package io.sunshower.kernel.log;

import java.util.ResourceBundle;
import java.util.function.Supplier;
import java.util.logging.Filter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public interface Logger {

  ResourceBundle getResourceBundle();

  String getResourceBundleName();

  void setFilter(Filter newFilter) throws SecurityException;

  Filter getFilter();

  void log(LogRecord record);

  void log(Level level, String msg);

  void log(Level level, Supplier<String> msgSupplier);

  void log(Level level, String msg, Object param1);

  void log(Level level, String msg, Object[] params);

  void log(Level level, String msg, Object fst, Object... params);

  void log(Level level, String msg, Throwable thrown);

  void log(Level level, Throwable thrown, Supplier<String> msgSupplier);

  void logp(Level level, String sourceClass, String sourceMethod, String msg);

  void logp(Level level, String sourceClass, String sourceMethod, Supplier<String> msgSupplier);

  void logp(Level level, String sourceClass, String sourceMethod, String msg, Object param1);

  void logp(Level level, String sourceClass, String sourceMethod, String msg, Object[] params);

  void logp(Level level, String sourceClass, String sourceMethod, String msg, Throwable thrown);

  void logp(
      Level level,
      String sourceClass,
      String sourceMethod,
      Throwable thrown,
      Supplier<String> msgSupplier);

  void logrb(
      Level level,
      String sourceClass,
      String sourceMethod,
      ResourceBundle bundle,
      String msg,
      Object... params);

  void logrb(Level level, ResourceBundle bundle, String msg, Object... params);

  void logrb(
      Level level,
      String sourceClass,
      String sourceMethod,
      ResourceBundle bundle,
      String msg,
      Throwable thrown);

  void logrb(Level level, ResourceBundle bundle, String msg, Throwable thrown);

  void entering(String sourceClass, String sourceMethod);

  void entering(String sourceClass, String sourceMethod, Object param1);

  void entering(String sourceClass, String sourceMethod, Object[] params);

  void exiting(String sourceClass, String sourceMethod);

  void exiting(String sourceClass, String sourceMethod, Object result);

  void throwing(String sourceClass, String sourceMethod, Throwable thrown);

  void severe(String msg);

  void warning(String msg);

  void info(String msg);

  void config(String msg);

  void fine(String msg);

  void finer(String msg);

  void finest(String msg);

  void severe(Supplier<String> msgSupplier);

  void warning(Supplier<String> msgSupplier);

  void info(Supplier<String> msgSupplier);

  void config(Supplier<String> msgSupplier);

  void fine(Supplier<String> msgSupplier);

  void finer(Supplier<String> msgSupplier);

  void finest(Supplier<String> msgSupplier);

  void setLevel(Level newLevel) throws SecurityException;

  Level getLevel();

  boolean isLoggable(Level level);

  String getName();

  void addHandler(Handler handler) throws SecurityException;

  void removeHandler(Handler handler) throws SecurityException;

  Handler[] getHandlers();

  void setUseParentHandlers(boolean useParentHandlers);

  boolean getUseParentHandlers();

  void setResourceBundle(ResourceBundle bundle);

  java.util.logging.Logger getParent();

  void setParent(java.util.logging.Logger parent);
}
