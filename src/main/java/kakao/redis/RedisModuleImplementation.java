package kakao.redis;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;


import io.vertx.core.AsyncResult;
import io.vertx.core.AsyncResultHandler;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.RedisClient;
import io.vertx.redis.impl.AbstractRedisClient;
import kakao.common.Log;
import kakao.common.commonData;

public class RedisModuleImplementation implements RedisModule
{
	private RedisClient redisClient;
	private Vertx vertx;
	
	private static Method sendVoid;
	
	@Override
	public void Init(Vertx vertx) throws Exception 
	{
		// TODO Auto-generated method stub
		this.vertx = vertx;
		JsonObject config = new JsonObject();
		config.put("host", commonData.Host);
		config.put("port", commonData.Port);
		config.put("address", commonData.BusAddress);
		
		final RedisModuleImplementation redisModule = this;
		
		redisClient = RedisClient.create(vertx, config);
		
		if(sendVoid == null)
		{
			sendVoid = AbstractRedisClient.class.getDeclaredMethod("sendVoid", String.class, JsonArray.class, Handler.class);
			sendVoid.setAccessible(true);
		}
		
	}

	@Override
	public void Read(String key, RedisJsonReadHandler handler, Object context) 
	{
		Log.Debug("Read");
		
		AsyncResultHandler<String> messageHandler = new AsyncResultHandler<String>() 
		{
			@Override
			public void handle(AsyncResult<String> response) 
			{
				try
				{
					if(response.succeeded() == true)
					{
						String value = response.result();
						JsonObject jsonValue = new JsonObject(value);
						if(handler != null)
						{
							handler.OnReadSuccess(jsonValue, context);
						}
						return;
					}
					if(handler != null)
					{
						handler.OnReadError(RedisResultCode.REDIS_ERROR, response.cause().getMessage(), context);
					}
					Log.Warn(String.format("RedisModule: Error while reading key : key :%s result : %s", key, response.cause().getMessage()));
				}
				catch(Exception e)
				{
					Log.Exception(e);
					if(handler != null)
					{
						handler.OnReadError(RedisResultCode.EXCEPTION_IN_HANDLER, e.getMessage(), context);
					}
					Log.Warn(String.format("RedisModule : Error while reading key: key:%s result:%s", key, e.getMessage()));
				}
			}
		};
		
		redisClient.get(key, messageHandler);
	}
	
	@Override
	public void UpdateHashMap(final String key, final HashMap<String, String> fields, final RedisWriteHandler handler, final Object context)
	{
    	Log.Debug("Update hash map");
		Log.Debug(String.format("RedisModule: Updating hashmap: key:%s", key));
		
		AsyncResultHandler<String> messageHandler = new AsyncResultHandler<String>() 
		{
			public void handle(AsyncResult<String> response) {
				try
				{
					if (response.succeeded())
					{
						if (handler != null)
						{
							handler.OnWriteSuccess(context);
						}
						return;
					}
	
					if (handler != null)
					{
						handler.OnWriteError(RedisResultCode.REDIS_ERROR, response.cause().getMessage(), context);
					}
					Log.Warn(String.format("RedisModule: Error while updating hashmap: key:%s fields:%s result:%s", key, fields.toString(), response.cause().getMessage()));
				}
				catch (Exception e)
				{
					Log.Exception(e);
					if (handler != null)
					{
						handler.OnWriteError(RedisResultCode.EXCEPTION_IN_HANDLER, e.getMessage(), context);
					}
					Log.Warn(String.format("RedisModule: Error while updating hashmap: key:%s fields:%s result:%s", key, fields.toString(), e.getMessage()));
				}
			} 
		};
		
		redisClient.hmset(key, fields, messageHandler);
	}
    
    @Override
	public void WriteIfNotExistInHashMap(final String key, final String hashKey, final String data, final RedisWriteHandler handler, final Object context)
	{
    	Log.Debug("Write if not exist");
		Log.Debug(String.format("RedisModule: Writing to hashmap: key:%s hashkey:%s", key, hashKey));
		
		AsyncResultHandler<Long> messageHandler = new AsyncResultHandler<Long>() 
		{
			public void handle(AsyncResult<Long> response) {
				try
				{
					if (response.succeeded())
					{
						long result = response.result();
						if (handler != null)
						{
							handler.OnWriteSuccess(result == 1, context);
						}
						return;
					}
					if (handler != null)
					{
						handler.OnWriteError(RedisResultCode.REDIS_ERROR, response.cause().getMessage(), context);
					}
					Log.Warn(String.format("RedisModule: Error while writing to hashmap: key:%s hashkey:%s result:%s", key, hashKey, response.cause().getMessage()));
				}
				catch (Exception e)
				{
					Log.Exception(e);
					if (handler != null)
					{
						handler.OnWriteError(RedisResultCode.EXCEPTION_IN_HANDLER, e.getMessage(), context);
					}
					Log.Warn(String.format("RedisModule: Error while writing to hashmap: key:%s hashkey:%s result:%s", key, hashKey, e.getMessage()));
				}
			} 
		};
		
		redisClient.hsetnx(key, hashKey, data, messageHandler);
	}
 
    @Override
	public void RemoveFromHashMap(final String key, final String[] hashKeys, final RedisRemoveHandler handler, final Object context)
	{
    	Log.Debug("Remove from hashmap");
    	
		Log.Debug(String.format("RedisModule: Removing hashkeys: key:%s", key));
 
		AsyncResultHandler<Long> messageHandler = new AsyncResultHandler<Long>() 
		{
			public void handle(AsyncResult<Long> response) {
				try
				{
					if (response.succeeded())
					{
						long count = response.result();
						if (handler != null)
						{
							handler.OnRemoveSuccess((int) count, context);
						}
						return;
					}
					if (handler != null)
					{
						handler.OnRemoveError(RedisResultCode.REDIS_ERROR, response.cause().getMessage(), context);
					}
					Log.Warn(String.format("RedisModule: Error while removing fields from hashmap: key:%s hashkeys:%s result:%s", key, Arrays.toString(hashKeys), response.cause().getMessage()));
				}
				catch (Exception e)
				{
					Log.Exception(e);
					if (handler != null)
					{
						handler.OnRemoveError(RedisResultCode.EXCEPTION_IN_HANDLER, e.getMessage(), context);
					}
					Log.Warn(String.format("RedisModule: Error while removing fields from hashmap: key:%s hashkeys:%s result:%s", key, Arrays.toString(hashKeys), e.getMessage()));
				}
			} 
		};
		redisClient.hdelMany(key, Arrays.asList((String[]) hashKeys), messageHandler);
	}
 
    @Override
	public void ReadFromHashMap(final String key, final String[] hashKeys, final RedisReadHandler handler, final Object context)
	{
    	Log.Debug("Remove from hashmap");
    	
		Log.Debug(String.format("RedisModule: Reading from hashmap: key:%s fields:%s", key, Arrays.toString(hashKeys)));
 
		AsyncResultHandler<JsonArray> messageHandler = new AsyncResultHandler<JsonArray>() 
		{
			public void handle(AsyncResult<JsonArray> response) {
				try
				{
					if (response.succeeded())
					{
						JsonArray values = response.result();
						
						HashMap<String, String> result = new HashMap<String, String>();
						
						for (int index = 0; index < values.size(); ++index)
						{
							String value = values.getString(index);
							result.put(hashKeys[index], value);
						}
						if (handler != null)
						{
							handler.OnReadSuccess(result, context);
						}
						return;
					}
					if (handler != null)
					{
						handler.OnReadError(RedisResultCode.REDIS_ERROR, response.cause().getMessage(), context);
					}
					Log.Warn(String.format("RedisModule: Error while reading from hashmap: key:%s hashKey:%s result:%s", key, Arrays.toString(hashKeys), response.cause().getMessage()));
				}
				catch (Exception e)
				{
					Log.Exception(e);
					if (handler != null)
					{
						handler.OnReadError(RedisResultCode.EXCEPTION_IN_HANDLER, e.getMessage(), context);
					}
					Log.Warn(String.format("RedisModule: Error while reading from hashmap: key:%s hashKey:%s result:%s", key, Arrays.toString(hashKeys), e.getMessage()));
				}
			} 
		};
		
		redisClient.hmget(key, Arrays.asList((String[]) hashKeys), messageHandler);
	}
    
    @Override
	public void Eval(final String[] keys, final String script, final String[] parameters, final RedisEvalHandler handler, final Object context)
	{
    	Log.Debug("Eval in");
    	Log.Debug(script);
    	Log.Debug(context == null ? "Empty context":context.toString());
		
		Log.Debug(String.format("RedisModule: Eval: Script:%s", script));
 
		AsyncResultHandler<Void> messageHandler = new AsyncResultHandler<Void>() 
		{
			public void handle(AsyncResult<Void> response) {
				try
				{
					if (response.succeeded())
					{
						JsonArray array = (JsonArray)((Object)response.result());
 
						String[] result = new String[array.size()];
						for (int index = 0; index < result.length; ++index)
						{
							Object temp = array.getValue(index);
							String value = temp == null ? null : temp.toString();
							result[index] = value;
						}
						if (handler != null)
						{
							handler.OnEvalSuccess(result, context);
						}
						return;
					}
					if (handler != null)
					{
						handler.OnEvalError(RedisResultCode.REDIS_ERROR, response.cause().getMessage(), context);
					}
					Log.Warn(String.format("RedisModule: Error %4$s while eval: script:%1$s keys:%2$s parameters:%3$s", script, Arrays.toString(keys), Arrays.toString(parameters), response.cause().getMessage()));
				}
				catch (Exception e)
				{
					Log.Exception(e);
					if (handler != null)
					{
						handler.OnEvalError(RedisResultCode.EXCEPTION_IN_HANDLER, e.getMessage(), context);
					}
					Log.Warn(String.format("RedisModule: Error %4$s while eval: script:%1$s keys:%2$s parameters:%3$s", script, Arrays.toString(keys), Arrays.toString(parameters), e.getMessage()));
				}
			} 
		};
		try
		{
			eval(script, Arrays.asList((String[])keys), parameters == null ? null : Arrays.asList((String[])parameters), messageHandler);
		}
		catch (Exception e)
		{
			Log.Exception(e);
			if (handler != null)
			{
				handler.OnEvalError(RedisResultCode.EXCEPTION_IN_HANDLER, e.getMessage(), context);
			}
			Log.Warn(String.format("RedisModule: Error %4$s while eval: script:%1$s keys:%2$s parameters:%3$s", script, Arrays.toString(keys), Arrays.toString(parameters), e.getMessage()));
		}
	}
    
    private static JsonArray toPayload(Object ... parameters) 
	{ 
		JsonArray result = new JsonArray(); 
		for (Object param: parameters) 
		{ 
			if (param instanceof JsonArray) 
			{ 
				param = ((JsonArray) param).getList(); 
			} 
			if (param instanceof Collection) 
			{ 
				for (Object el : (Collection) param) 
				{ 
					if (el != null) 
					{ 
						result.add(el); 
					} 
				} 
			} 
			else if (param instanceof Map) 
			{ 
				for (Map.Entry<?, ?> pair : ((Map<?, ?>) param).entrySet()) 
				{ 
					result.add(pair.getKey()); 
					result.add(pair.getValue()); 
				} 
			} 
			else if (param instanceof Stream) 
			{ 
				((Stream) param).forEach(e -> 
				{ 
					if (e instanceof Object[]) 
					{ 
						for (Object item : (Object[]) e) 
						{ 
							result.add(item); 
						} 
					} 
					else 
					{ 
						result.add(e); 
					} 
				}); 
			} 
			else if (param != null) 
			{ 
				result.add(param); 
			} 
		} 
		return result; 
	}

    
    private void eval(String script, List<String> keys, List<String> args, Handler<AsyncResult<Void>> handler) throws Exception
    {
    	keys = (keys != null) ? keys : Collections.emptyList();
    	args = (args != null) ? args : Collections.emptyList();
    	sendVoid.invoke(redisClient, "EVAL", toPayload(script, keys.size(), keys, args), handler);
    }
	
}
