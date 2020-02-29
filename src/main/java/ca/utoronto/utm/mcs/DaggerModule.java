package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.util.Arrays;

import java.net.InetSocketAddress;
import com.sun.net.httpserver.HttpServer;

import dagger.Module;
import dagger.Provides;

import com.mongodb.MongoClientSettings;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.MongoCredential;


@Module
public class DaggerModule {
	static int port = 8080;
	private static HttpServer server;
	private static MongoClient db;

    @Provides public MongoClient provideMongoClient() {
    	db = MongoClients.create();
    	return db;
    }

    @Provides public HttpServer provideHttpServer() {
    	try {
			server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);
		} catch (IOException e) {
			e.printStackTrace();
		}
        return server;
    }
}
