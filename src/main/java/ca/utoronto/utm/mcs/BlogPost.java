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

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;

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
		String body = Utils.convert(r.getRequestBody());
        JSONObject deserialized = new JSONObject(body);

        String id = null;
        String title = null;
        String ret = null;
        
        if (deserialized.has("_id")) { //id given and takes precedence, title does not matter
        	id = deserialized.getString("_id");
        	
        } else if (deserialized.has("title")) {
        	title = deserialized.getString("title");
        	
        	if (title.isEmpty()) {
        		r.sendResponseHeaders(400, -1); //as on Piazza post where only title is given and is empty
        		return;
        	}
        	
        } else { // if query doesn't have these, it's improperly formatted or missing info
        	r.sendResponseHeaders(400, -1);
        	return;
        }
        
        
        try {
        	// get database, and posts collection
        	MongoCollection<Document> col = App.db.getDatabase("csc301a2").getCollection("posts");
        	
        	BasicDBObject query = new BasicDBObject();
        	
        	
        	
        	if (id != null) { //id always takes precedence
        		
        		Document myDoc = null;
        		try { //finding post with _id
        			query.put("_id", new ObjectId(id));
        			myDoc = col.find(query).first();

        		} catch (Exception e) { //_id not in database, so post does not exist
        			r.sendResponseHeaders(404, -1); 
        			return;
        		}
        	
        		
        		title = myDoc.get("title").toString();
        		String author = myDoc.get("author").toString();
        		String content = myDoc.get("content").toString();
        		
        		List<String> tagList = new ArrayList<String>();
            	tagList = (List<String>) myDoc.get("tags");
        		
            	ret = "[\n\t{\n\t\t\"_id\": {\n\t\t\t\"$oid\": \"" + id + "\"\n\t\t},\n\t\t\"title\": \"";
            	ret += title + "\",\n\t\t\"author\": \"" + author + "\",\n\t\t\"content\": \"" + content;
            	ret += "\",\n\t\t\"tags\": [";
            	
            	for (int i=0; i < tagList.size(); i++) { 
            		if (i == tagList.size() - 1) {
            			ret += "\n\t\t\t\"" + tagList.get(i) + "\""; //no comma for last tag
            		} else {
            			ret += "\n\t\t\t\"" + tagList.get(i) + "\",";
            		}
            		
            	}
            	
            	ret += "\n\t\t]\n\t}\n]";
            	
            	
        		
        	} else { //only title is given
        		
        		MongoCursor<Document> cursor = col.find(Filters.regex("title", ".*\\b" + title + "\\b.*")).iterator();
        															//match title as a word, not as part of another word
        			
        		if (!cursor.hasNext()) { //empty, so blog post does not exist
        			r.sendResponseHeaders(404, -1); 
        			return;
        		}

        		ret = "[\n";
        		
        		while (cursor.hasNext()) {
        	        Document myDoc = cursor.next();
        	        
        	        id = myDoc.get("_id").toString();
        	        title = myDoc.get("title").toString();
            		String author = myDoc.get("author").toString();
            		String content = myDoc.get("content").toString();
            		
            		List<String> tagList = new ArrayList<String>();
                	tagList = (List<String>) myDoc.get("tags");
            		
                	ret += "\t{\n\t\t\"_id\": {\n\t\t\t\"$oid\": \"" + id + "\"\n\t\t},\n\t\t\"title\": \"";
                	ret += title + "\",\n\t\t\"author\": \"" + author + "\",\n\t\t\"content\": \"" + content;
                	ret += "\",\n\t\t\"tags\": [";
                	
                	for (int i=0; i < tagList.size(); i++) { 
                		if (i == tagList.size() - 1) {
                			ret += "\n\t\t\t\"" + tagList.get(i) + "\""; //no comma for last tag
                		} else {
                			ret += "\n\t\t\t\"" + tagList.get(i) + "\",";
                		}
                		
                	}
                	
                	if (cursor.hasNext()) {
                		ret += "\n\t\t]\n\t},\n";
                	} else {
                		ret += "\n\t\t]\n\t}\n"; //no comma for last blog post
                	}
        	    }
        		
        		ret += "]";
        		
        		
        	}

            byte[] bs = ret.getBytes("UTF-8");
            r.sendResponseHeaders(200, bs.length);
            OutputStream os = r.getResponseBody();
            os.write(bs);
            os.close();
        	
        	
        } catch (Exception e){
        	r.sendResponseHeaders(500, -1);
        	
        } finally {
        	//filler
        }
        
     
        
        
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
		String body = Utils.convert(r.getRequestBody());
        JSONObject deserialized = new JSONObject(body);

        String id = null;
        
        if (deserialized.has("_id"))
        {
        	id = deserialized.getString("_id");
        }
        // if query doesn't have these, it's improperly formatted or missing info
        else
        	r.sendResponseHeaders(400, -1);
        
        try
		{
        	// get database, and posts collection
        	MongoCollection<Document> col = App.db.getDatabase("csc301a2").getCollection("posts");

        	// create document to specify what to delete and add id
        	Document rem = new Document();
        	ObjectId oId = new ObjectId(id);
        	rem.put("_id", oId);
        	
        	// delete from database
        	if (col.findOneAndDelete(rem) == null)
        	{
        		// object could not be found in database
            	r.sendResponseHeaders(404, -1);
        	}
        	else {
        		// everything worked correctly
            	r.sendResponseHeaders(200, -1);
        	}
		} catch (Exception e){
        	//error
        	r.sendResponseHeaders(500, -1);
        } finally {
        	//filler
        }  
	}
	
	private void handle405(HttpExchange r) throws IOException, JSONException{
		r.sendResponseHeaders(405, -1);
	}
}