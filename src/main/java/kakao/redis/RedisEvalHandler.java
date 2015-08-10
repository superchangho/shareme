package kakao.redis;

public interface RedisEvalHandler 
{
	void OnEvalSuccess(String[] value, Object context);
	void OnEvalError(RedisResultCode code, String errorMessage, Object context);
}
