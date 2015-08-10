package kakao.redis;

public enum RedisResultCode 
{
	UNKNOWN(0, "Unknown error occurred"),
	REDIS_ERROR(1, "Redis Error"),
	EXCEPTION_IN_HANDLER(2, "Exception");
	
	private int errorCode;
	private String message;
	
	public int getHTTPCode()
	{
		return errorCode;
	}
	
	private RedisResultCode(int errorCode, String message)
	{
		this.errorCode = errorCode;
		this.message = message;
	}
	
	public String getMessage()
	{
		return message;
	}
}
