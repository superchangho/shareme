package kakao.redis;

import java.util.HashMap;

import io.vertx.core.Vertx;

public interface RedisModule 
{
	void Init(Vertx vertx) throws Exception;
	void Read(final String key, final RedisJsonReadHandler handler, final Object context);
	void RemoveFromHashMap(final String key, final String[] hashKeys, final RedisRemoveHandler handler, final Object context);
	void ReadFromHashMap(final String key, final String[] hashKeys, final RedisReadHandler handler, final Object context);
	void WriteIfNotExistInHashMap(final String key, final String hashKey, final String data, final RedisWriteHandler handler, final Object context);
	void UpdateHashMap(final String key, final HashMap<String, String> fields, final RedisWriteHandler handler, final Object context);
	void Eval(String[] keys, String script, String[] parameters, RedisEvalHandler handler, Object context);
}
