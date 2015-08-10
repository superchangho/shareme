package kakao.data;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import io.netty.handler.codec.http.HttpHeaders.Values;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import kakao.common.Log;
import kakao.common.commonData;
import kakao.redis.RedisEvalHandler;
import kakao.redis.RedisJsonReadHandler;
import kakao.redis.RedisModule;
import kakao.redis.RedisModuleImplementation;
import kakao.redis.RedisResultCode;
import kakao.redis.listdatahandler;

public class StorageManagerImplementation implements StorageManager
{
	private RedisModule[] masterDB;
	private Vertx vertx;
	private StorageUser verticle;
	
	private final static String READ_LIST_SCRIPT =
			"return { 0, redis.call('HGETALL', KEYS[1], unpack(ARGV)) }";
	
	@Override
	public void Init(Vertx vertx) throws Exception 
	{
		this.vertx = vertx;
		
		
		initializeMasterDB();
	}
	
	private void initializeMasterDB() throws Exception
	{
		masterDB = new RedisModule[commonData.dbcount];
		for(int i = 0 ; i < commonData.dbcount; ++i)
		{
			RedisModule redisModule = new RedisModuleImplementation();
			redisModule.Init(vertx);
			masterDB[i] = redisModule;
		}
	}

	@Override
	public RedisModule GetMasterDB() 
	{
		return masterDB[0];
	}
	
	public void readMasterList(final listdatahandler handler)
	{
		RedisEvalHandler redisEvalHandler = new RedisEvalHandler() 
		{	
			@Override
			public void OnEvalSuccess(String[] values, Object context) 
			{
				HashMap<String, String> result = new HashMap<String, String>();
				try
				{
					int resultcode = Integer.parseInt(values[0]);
					if(resultcode < 0)
					{
						if(handler != null)
						{
							handler.OnListDataReadComplete(result);
						}
						return;
					}
					JsonArray array = new JsonArray(values[1]);
					String key = null;
					for(int i = 0 ; i < array.size() ; ++i)
					{
						if(i % 2 == 0)
						{
							key = array.getString(i);
						}
						else
						{
							result.put(key, array.getString(i));
						}
					}

				}
				catch(Exception e)
				{
					Log.Exception(e);
					return;
				}	
				if(handler != null)
				{
					handler.OnListDataReadComplete(result);
				}
			}
			
			@Override
			public void OnEvalError(RedisResultCode code, String errorMessage, Object context) 
			{
				Log.Fatal(String.format("OnEvalError message : %s", errorMessage));
			}
		};
		
		String[] keys = new String[1];
		keys[0] = "list";
		GetMasterDB().Eval(keys, READ_LIST_SCRIPT, null, redisEvalHandler, null);
	}
}
