package kakao.main;

import java.util.HashMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import kakao.common.Log;
import kakao.common.commonData;
import kakao.data.SortingManager;
import kakao.data.SortingManagerImplementation;
import kakao.data.StorageManager;
import kakao.data.StorageManagerImplementation;
import kakao.redis.RedisReadHandler;
import kakao.redis.RedisResultCode;
import kakao.redis.RedisWriteHandler;


/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class KakaoApp extends AbstractVerticle implements RedisReadHandler 
{
	private HttpServer[] applicationServers;
	private StorageManager storagemanager;
	private SortingManager sortingmanager;
	
		
	public void start()
	{			
		initializeLogger();
		Log.Info("Server Initialize");
		initializeStoragemanager();
		initializeSortingmanager();
	}
	
	void initializeLogger()
	{
		Logger logger = LoggerFactory.getLogger("kakao");
		Log.Init(logger);
	}
	
	private class timerHandler implements Handler<Long>
	{
		@Override
		public void handle(Long timerID) 
		{
			sortingmanager.Sorting();
		}
	}
	
	private timerHandler timer;
	
	void initializeSortingmanager()
	{
		sortingmanager = new SortingManagerImplementation();
		sortingmanager.Init(vertx, storagemanager);
		
		if(timer == null)
		{
			timer = new timerHandler();
			vertx.setPeriodic(commonData.Sorting_Period, timer);
		}
	}
	
	void initializeStoragemanager()
	{
		Log.Info("Initialize Storagemanager");
		storagemanager = new StorageManagerImplementation();
		try
		{
			storagemanager.Init(vertx);
		}
		catch(Exception e)
		{
			Log.Exception(e);
			Log.Fatal("Fail to initialize storagemanager");
			return;
		}
		createWasServer();
		Log.Info("Storagemanager created");
	}
	
	private void createWasServer()
	{
		applicationServers = new HttpServer[commonData.ServerCount];
		
		for(int i = 0 ; i < commonData.ServerCount ; ++i)
		{
			applicationServers[i] = vertx.createHttpServer();
			AddHandler(applicationServers[i]);
		}
	}
	
	private void AddHandler(HttpServer server)
	{
		server.requestHandler(new Handler<HttpServerRequest>() 
		{

			@Override
			public void handle(HttpServerRequest request) 
			{
				switch (request.path()) 
				{
				case "/":
					indexpage(request);
				break;
				case "/input.html":
					inputpage(request);
				break;
				case "/saveurl.json":
					try 
					{
						savepage(request);
					} 
					catch (Exception e) 
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				break;

				default:
					break;
				}
			}
			
		}).listen(8181);
	}
	
	private void savepage(HttpServerRequest request) throws Exception
	{
		MultiMap mMap = request.params();
		Document doc = Jsoup.connect(mMap.get("url")).get();
		

		String title = doc.title();
		
		JsonObject data = new JsonObject();
		data.put("title", title);
		data.put("url", mMap.get("url"));
		data.put("like", 0);

		RedisWriteHandler writeHandler = new RedisWriteHandler() 
		{
			@Override
			public void OnWriteSuccess(Object context) 
			{
				Log.Info("WriteSuccess");
			}
			
			@Override
			public void OnWriteSuccess(boolean newField, Object context) 
			{
				Log.Info("WriteSuccess");
			}
			
			@Override
			public void OnWriteError(RedisResultCode errorcode, String errorMessage, Object context) 
			{
				Log.Info(String.format("OnWriteError %s", errorMessage));
			}
		};
		
		storagemanager.GetMasterDB().WriteIfNotExistInHashMap("list", mMap.get("url"), data.toString(), writeHandler, null);
	}
	
	private void inputpage(HttpServerRequest request)
	{
		request.response().sendFile("webroot/input.html");
	}
	
	private void indexpage(HttpServerRequest request)
	{
		request.response().sendFile("webroot/index.html");
	}

	@Override
	public void OnReadSuccess(HashMap<String, String> values, Object context) 
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void OnReadError(RedisResultCode errorCode, String errormessage, Object context) 
	{
		// TODO Auto-generated method stub
		
	}

}