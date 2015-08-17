package kakao.data;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import java.util.*;

public interface SortingManager 
{
	void Init(Vertx vertx, StorageManager storagemanager);
	void Sorting();
	List<sortingData> getSortedList();
	void AddRecentList(JsonObject object);
	List<sortingData> getRecentList();
}
