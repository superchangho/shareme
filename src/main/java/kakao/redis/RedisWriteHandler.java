package kakao.redis;

public interface RedisWriteHandler 
{
	void OnWriteSuccess(boolean newField, Object context);
	void OnWriteSuccess(Object context);
	void OnWriteError(RedisResultCode errorcode, String errorMessage, Object context);
}
