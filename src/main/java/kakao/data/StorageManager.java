package kakao.data;

import io.vertx.core.Vertx;
import kakao.redis.RedisModule;
import kakao.redis.listdatahandler;

public interface StorageManager 
{
	void Init(Vertx vertx) throws Exception;
	RedisModule GetMasterDB();
	void readMasterList(final listdatahandler handler);
}
