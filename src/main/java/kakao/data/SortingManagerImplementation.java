package kakao.data;


import java.util.HashMap;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import kakao.redis.RedisEvalHandler;
import kakao.redis.RedisResultCode;
import kakao.redis.listdatahandler;

public class SortingManagerImplementation implements SortingManager, listdatahandler
{
	private Vertx vertx;
	private StorageManager storagemanager;
	private HashMap<String, String> Result = new HashMap<String, String>();
	
	
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

	@Override
	public void OnListDataReadComplete(HashMap<String, String> result) 
	{
		this.Result = result;
	}

}
