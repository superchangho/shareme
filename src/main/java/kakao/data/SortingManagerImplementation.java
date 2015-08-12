package kakao.data;


import java.awt.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import com.hazelcast.nio.serialization.Data;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import kakao.redis.RedisEvalHandler;
import kakao.redis.RedisResultCode;
import kakao.redis.listdatahandler;

public class SortingManagerImplementation implements SortingManager, listdatahandler
{
	private Vertx vertx;
	private StorageManager storagemanager;
	private HashMap<String, String> Result = new HashMap<String, String>();
	private HashMap<String, sortingData> Data = new HashMap<String, sortingData>();
	
	
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
	
	class LikeComparator implements Comparator
	{
		@Override
		public int compare(Object a, Object b) 
		{
			return ((sortingData)a).like > ((sortingData)b).like ? 1: 0;
		}
		
	}
	
	public java.util.List<sortingData> changeToList(HashMap<String, sortingData> map)
	{
		java.util.List<sortingData> list = new ArrayList<sortingData>();
		for(Iterator<Map.Entry<String, sortingData>> i = map.entrySet().iterator(); i.hasNext();)
		{
			Map.Entry<String, sortingData> entry = i.next();
			list.add(entry.getValue());
		}
		
		Collections.sort(list, new LikeComparator());
		
		return list;
	}
	
	
	@Override
	public void OnListDataReadComplete(HashMap<String, String> result) 
	{
		Data.clear();
		for(Iterator<Map.Entry<String, String>> i = result.entrySet().iterator(); i.hasNext();)
		{
			Map.Entry<String, String> entry = i.next();
			JsonObject test = new JsonObject();
			sortingData tData = Json.decodeValue(entry.getValue(), sortingData.class);
			Data.put(tData.url, tData);
		}
		
		changeToList(Data);
	}

}
