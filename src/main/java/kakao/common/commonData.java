package kakao.common;

import java.util.Date;

public final class commonData 
{
	public static final String Host = "127.0.0.1";
	public static final int Port = 6379;
	public static final String BusAddress = "kakao.sungsan.superchangho";
	public static final int dbcount = 10;
	public static final int ServerCount = 1;
	public static final String WebRoot = "/Users/nhn/Documents/workspace/vertx-examples-master/maven-simplest/webroot/";
	public static final int Sorting_Period = 1000 * 60 * 5;
	
	public static long GetCurrentTime()
	{
		Date now = new Date();
		return now.getTime();
	}
}
