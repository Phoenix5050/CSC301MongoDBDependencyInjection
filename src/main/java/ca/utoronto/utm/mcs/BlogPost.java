package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.io.OutputStream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.mongodb.client.*;

public class BlogPost implements HttpHandler{
	public BlogPost(MongoClient db) {
	}

	@Override
	public void handle(HttpExchange r) {
		try {
			if (r.getRequestMethod().equals("GET")) {
	            handleGet(r);
	        } else if (r.getRequestMethod().equals("PUT")) {
	            handlePut(r);
	        } else if (r.getRequestMethod().equals("DELETE")) {
	        	handleDelete(r);
	        } else {
	        	handle405(r);
	        }
		} catch (Exception e) {
            e.printStackTrace();
        }
	}

	private void handleGet(HttpExchange r) throws IOException, JSONException{
		
	}

	private void handlePut(HttpExchange r) throws IOException, JSONException{
		String body = Utils.convert(r.getRequestBody());
        JSONObject deserialized = new JSONObject(body);

        String title = null;
        String author = null;
        String content = null;
        JSONArray tags = null;

        if (deserialized.has("title") && deserialized.has("author") && deserialized.has("content") && deserialized.has("tags"))
        {
        	title = deserialized.getString("title");
        	author = deserialized.getString("author");
        	content = deserialized.getString("content");
        	tags = deserialized.getJSONArray("tags");
        }
        // if query doesn't have these, it's improperly formatted or missing info
        else
        	r.sendResponseHeaders(400, -1);
        
        try
		{
        	// get database, and posts collection
        	MongoCollection<Document> col = App.db.getDatabase("csc301a2").getCollection("posts");
        	
        	// create post to insert into database using data from fields
        	// The document type from Mongodb library works similarly to a hashmap
        	// https://stackoverflow.com/questions/49139226/java-how-to-insert-a-hashmap-into-mongodb
        	
        	List<String> tagList = new ArrayList<String>();
        	for(int i=0; i<tags.length(); i++) {
        	     tagList.add(tags.getString(i));
        	}
        	
        	Document post = new Document();
        	post.put("title", title);
        	post.put("author", author);
        	post.put("content", content);
        	post.put("tags", tagList);
        	
        	// add post DB object to database
        	col.insertOne(post);
        	
        	// the "_id" is a field automatically added to the document type after it is inserted into database
        	// take "_id" and convert to string to return in response body
        	ObjectId id = (ObjectId) post.get("_id");
        	String ret = "{\"_id\": \"" + id.toString() +"\"}";

        	// everything worked correctly			
			r.sendResponseHeaders(200, ret.length());
	        OutputStream os = r.getResponseBody();
	        os.write(ret.getBytes());
	        os.close();
		} catch (Exception e){
        	//error
        	r.sendResponseHeaders(500, -1);
        } finally {
        	//filler
        }       
	}

	private void handleDelete(HttpExchange r) throws IOException, JSONException{
		
	}
	
	private void handle405(HttpExchange r) throws IOException, JSONException{
		r.sendResponseHeaders(405, -1);
	}
}