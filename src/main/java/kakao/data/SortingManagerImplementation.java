package kakao.data;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import kakao.redis.listdatahandler;
import java.util.List;
import java.util.Stack;

public class SortingManagerImplementation implements SortingManager, listdatahandler
{
	private Vertx vertx;
	private StorageManager storagemanager;
	private HashMap<String, sortingData> Data = new HashMap<String, sortingData>();
	List<sortingData> list = new ArrayList<sortingData>();
	Stack<JsonObject> recentlist = new Stack<JsonObject>();

	
	@Override
	public void Init(Vertx vertx, StorageManager storagemanager) 
	{
		this.vertx = vertx;
		this.storagemanager = storagemanager;
		
		Sorting();
	}
	
	public class FutureHandler implements Handler<Future<String>>
	{
		private SortingManagerImplementation smi;
		public FutureHandler(SortingManagerImplementation smi) 
		{
			this.smi = smi;
		}
		@Override
		public void handle(Future<String> event) 
		{
			storagemanager.readMasterList(smi);
		}	
	}
	
	public class ResultHandler implements Handler<AsyncResult<String>>
	{
		@Override
		public void handle(AsyncResult<String> event) 
		{
			if(event.succeeded() == true)
			{
				String result = event.result();
				if(result.equals("result") == true)
				{					
				}
			}
			
		}
		
	}
	
	public void Sorting()
	{
		FutureHandler codeHandler = new FutureHandler(this);
		ResultHandler resultHander = new ResultHandler();
		
		vertx.executeBlocking(codeHandler, resultHander);
	}
	
	class ShareCountComparator implements Comparator
	{
		@Override
		public int compare(Object a, Object b) 
		{
			return ((sortingData)a).sharecount > ((sortingData)b).sharecount ? 1: 0;
		}
		
	}	
	
	public void changeToList(HashMap<String, sortingData> map)
	{
		list.clear();
		for(Iterator<Map.Entry<String, sortingData>> i = map.entrySet().iterator(); i.hasNext();)
		{
			Map.Entry<String, sortingData> entry = i.next();
			list.add(entry.getValue());
		}
		
		Collections.sort(list, new ShareCountComparator());
	}
	
	@Override
	public java.util.List<sortingData> getSortedList()
	{
		return list;
	}
	
	
	@Override
	public void OnListDataReadComplete(HashMap<String, String> result) 
	{
		Data.clear();
		for(Iterator<Map.Entry<String, String>> i = result.entrySet().iterator(); i.hasNext();)
		{
			Map.Entry<String, String> entry = i.next();
			sortingData tData = Json.decodeValue(entry.getValue(), sortingData.class);
			Data.put(tData.url, tData);
		}
		
		changeToList(Data);
	}


	@Override
	public void AddRecentList(JsonObject object) 
	{
		recentlist.push(object);
	}
	
	@Override
	public List<sortingData> getRecentList()
	{
		List<sortingData> list = new ArrayList<sortingData>();
		
		for(JsonObject entry : recentlist)
		{
			sortingData test = Json.decodeValue(entry.toString(), sortingData.class);
			list.add(test);
		}
		
		return list;
	}

}
