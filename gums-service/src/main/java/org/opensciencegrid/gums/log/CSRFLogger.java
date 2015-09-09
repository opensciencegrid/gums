
package org.opensciencegrid.log;

import org.apache.log4j.Logger;
import org.owasp.csrfguard.log.ILogger;
import org.owasp.csrfguard.log.LogLevel;

public class CSRFLogger implements ILogger
{

  private final static Logger log = Logger.getLogger("org.glite.security");


  @Override
  public void log(String msg)
  {
    log.info(msg.replaceAll("(\\r|\\n)", ""));
  }


  @Override
  public void log(LogLevel level, String msg)
  {
    // Remove CR and LF characters to prevent CRLF injection
    String sanitizedMsg = msg.replaceAll("(\\r|\\n)", "");

    switch (level)
    {
      case Trace:
        log.trace(sanitizedMsg);
        break;
      case Debug:
        log.debug(sanitizedMsg);
        break;
      case Info:
        log.info(sanitizedMsg);
        break;
      case Warning:
        log.warn(sanitizedMsg);
        break;
      case Error:
        log.error(sanitizedMsg);
        break;
      case Fatal:
        log.fatal(sanitizedMsg);
        break;
      default:
        throw new RuntimeException("unsupported log level " + level);
    }
  }


  @Override
  public void log(Exception exception)
  {
    log.log(org.apache.log4j.Level.WARN, exception.getLocalizedMessage(), exception);
  }


  @Override
  public void log(LogLevel level, Exception exception)
  {
    switch(level)
    {
      case Trace:
        log.log(org.apache.log4j.Level.TRACE, exception.getLocalizedMessage(), exception);
        break;
      case Debug:
        log.log(org.apache.log4j.Level.DEBUG, exception.getLocalizedMessage(), exception);
        break;
      case Info:
        log.log(org.apache.log4j.Level.INFO, exception.getLocalizedMessage(), exception);
        break;
      case Warning:
        log.log(org.apache.log4j.Level.WARN, exception.getLocalizedMessage(), exception);
        break;
      case Error:
        log.log(org.apache.log4j.Level.ERROR, exception.getLocalizedMessage(), exception);
        break;
      case Fatal:
        log.log(org.apache.log4j.Level.FATAL, exception.getLocalizedMessage(), exception);
        break;
      default:
        throw new RuntimeException("unsupported log level " + level);
    }
  }

}

