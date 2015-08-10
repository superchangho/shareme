package kakao.redis;

import java.util.HashMap;

public interface RedisReadHandler 
{
	void OnReadSuccess(HashMap<String, String> values, Object context);
	void OnReadError(RedisResultCode errorCode, String errormessage, Object context);
}
