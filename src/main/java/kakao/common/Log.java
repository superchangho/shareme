package kakao.common;

import io.vertx.core.logging.Logger;

import io.vertx.core.logging.Logger;
import java.io.StringWriter;
import java.io.PrintWriter;
 
public class Log
{
	private static Logger logger;
	public static final int TRACE = 0;
	public static final int DEBUG = 1;
	public static final int INFO = 2;
	public static final int WARN = 3;
	public static final int ERROR = 4;
	public static final int FATAL = 5;
	
	public static void Init(Logger lgr)
	{
		logger = lgr;
	}
	
	public static void Trace(String logMessage)
	{
		logger.trace(logMessage);
	}
 
	public static void Debug(String logMessage)
	{
		logger.debug(logMessage);
	}
 
	public static void Info(String logMessage)
	{
		logger.info(logMessage);
	}
 
	public static void Warn(String logMessage)
	{
		logger.warn(logMessage);
	}
 
	public static void Error(String logMessage)
	{
		logger.error(logMessage);
	}
 
	public static void Exception(Throwable e)
	{
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		String stacktrace = sw.toString();
		
		logger.error(String.format("Exception: %s", e.getMessage()));
		logger.error(stacktrace);
	}
 
	public static void Fatal(String logMessage)
	{
		logger.fatal(logMessage);
	}
 
	public static void Print(int level, String logMessage)
	{
		switch (level)
		{
		case TRACE:
			Trace(logMessage);
			break;
		case DEBUG:
			Debug(logMessage);
			break;
		case INFO:
			Info(logMessage);
			break;
		case WARN:
			Warn(logMessage);
			break;
		case ERROR:
			Error(logMessage);
			break;
		case FATAL:
			Fatal(logMessage);
			break;
		default:
			break;
		}
	}
}
