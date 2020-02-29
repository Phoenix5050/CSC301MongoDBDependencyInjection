package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import com.sun.net.httpserver.HttpServer;

import javax.inject.Inject;

import com.mongodb.client.MongoClient;


public class App
{
    static int port = 8080;
    public static MongoClient db;
    
    public static void main(String[] args) throws IOException
    {
    	Dagger service = DaggerDaggerComponent.create().buildMongoHttp();
    	HttpServer httpServer = service.getServer();
    	db = service.getDb();
    	//Create your server context here
    	service.getServer().start();
    	httpServer.createContext("/api/v1/post", new BlogPost(db));
    	
    	System.out.printf("Server started on port %d", port);
    }
}
