package kakao.main;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.util.HashMap;
import java.util.List;

import kakao.common.Log;
import kakao.common.commonData;
import kakao.data.SortingManager;
import kakao.data.SortingManagerImplementation;
import kakao.data.StorageManager;
import kakao.data.StorageManagerImplementation;
import kakao.data.sortingData;
import kakao.redis.RedisReadHandler;
import kakao.redis.RedisResultCode;
import kakao.redis.RedisWriteHandler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.google.gson.Gson;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class KakaoApp extends AbstractVerticle implements RedisReadHandler {
	private HttpServer[] applicationServers;
	private StorageManager storagemanager;
	private SortingManager sortingmanager;

	public void start() {
		initializeLogger();
		Log.Info("Server Initialize");
		initializeStoragemanager();
		initializeSortingmanager();
	}

	void initializeLogger() {
		Logger logger = LoggerFactory.getLogger("kakao");
		Log.Init(logger);
	}

	private class timerHandler implements Handler<Long> {
		@Override
		public void handle(Long timerID) {
			sortingmanager.Sorting();
		}
	}

	private timerHandler timer;

	void initializeSortingmanager() {
		sortingmanager = new SortingManagerImplementation();
		sortingmanager.Init(vertx, storagemanager);

		if (timer == null) {
			timer = new timerHandler();
			vertx.setPeriodic(commonData.Sorting_Period, timer);
		}
	}

	void initializeStoragemanager() {
		Log.Info("Initialize Storagemanager");
		storagemanager = new StorageManagerImplementation();
		try {
			storagemanager.Init(vertx);
		} catch (Exception e) {
			Log.Exception(e);
			Log.Fatal("Fail to initialize storagemanager");
			return;
		}
		createWasServer();
		Log.Info("Storagemanager created");
	}

	private void createWasServer() {
		applicationServers = new HttpServer[commonData.ServerCount];

		for (int i = 0; i < commonData.ServerCount; ++i) {
			applicationServers[i] = vertx.createHttpServer();
			AddHandler(applicationServers[i]);
		}
	}

	private void AddHandler(HttpServer server) {
		server.requestHandler(new Handler<HttpServerRequest>() {

			@Override
			public void handle(HttpServerRequest request) {
				try {
					switch (request.path()) {
						case "/":
							indexpage(request);
							break;
						case "/input.html":
							inputpage(request);
							break;
						case "/saveurl.json":
							savepage(request);
							break;
						case "/getlist.json":
							getList(request);
							break;
						default:
							break;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}).listen(8181);
	}

	private void savepage(HttpServerRequest request) throws Exception {
		MultiMap mMap = request.params();
		Document doc = Jsoup.connect(mMap.get("url")).get();

		String title = doc.title();
		String summary = doc.title();
		String image = doc.title();

		JsonObject data = new JsonObject();
		data.put("title", title);
		data.put("summary", summary);
		data.put("image", image);
		data.put("url", mMap.get("url"));
		data.put("like", 0);

		RedisWriteHandler writeHandler = new RedisWriteHandler() {
			@Override
			public void OnWriteSuccess(Object context) {
				Log.Info("WriteSuccess");
			}

			@Override
			public void OnWriteSuccess(boolean newField, Object context) {
				Log.Info("WriteSuccess");
			}

			@Override
			public void OnWriteError(RedisResultCode errorcode, String errorMessage, Object context) {
				Log.Info(String.format("OnWriteError %s", errorMessage));
			}
		};

		storagemanager.GetMasterDB().WriteIfNotExistInHashMap("list", mMap.get("url"), data.toString(), writeHandler, null);
		
		HttpServerResponse response = request.response();
		response.putHeader("Access-Control-Allow-Origin", "*");
		
		JsonObject result = new JsonObject();
		result.put("result", true);
		response.end(result.toString());
	}

	private void getList(HttpServerRequest request) throws Exception {
		List<sortingData> sortedList = sortingmanager.getSortedList();
		HttpServerResponse response = request.response();
		response.putHeader("Access-Control-Allow-Origin", "*");
		String json = new Gson().toJson(sortedList);
		response.end(json);
	}

	private void inputpage(HttpServerRequest request) {
		Log.Info("Request input page");
		request.response().sendFile("webroot/input.html");
	}

	private void indexpage(HttpServerRequest request) {
		Log.Info("Request index page");
		request.response().sendFile("webroot/index.html");
	}

	@Override
	public void OnReadSuccess(HashMap<String, String> values, Object context) {
		// TODO Auto-generated method stub

	}

	@Override
	public void OnReadError(RedisResultCode errorCode, String errormessage, Object context) {
		// TODO Auto-generated method stub

	}

	public static void main(String[] args) throws Exception {
		io.vertx.core.Starter.main(new String[]{"run", KakaoApp.class.getName(), "vertx.options.blockedThreadCheckInterval", "2147483647"});
	}

}