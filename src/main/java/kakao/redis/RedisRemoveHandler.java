package kakao.redis;

public interface RedisRemoveHandler 
{
	void OnRemoveSuccess(int count, Object context);
	void OnRemoveError(RedisResultCode code, String errorMessage, Object context);
}
