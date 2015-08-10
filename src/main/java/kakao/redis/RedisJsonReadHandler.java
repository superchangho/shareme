package kakao.redis;

import io.vertx.core.json.JsonObject;

public interface RedisJsonReadHandler 
{
	void OnReadSuccess(JsonObject value, Object context);
	void OnReadError(RedisResultCode resultCode, String errorMessage, Object context);
}
