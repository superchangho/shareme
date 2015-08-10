package kakao.data;

import io.vertx.core.Vertx;

public interface SortingManager 
{
	void Init(Vertx vertx, StorageManager storagemanager);
	void Sorting();
}
