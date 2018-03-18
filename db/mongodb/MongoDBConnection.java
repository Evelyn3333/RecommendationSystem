package db.mongodb;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;

import db.DBConnection;
import entity.Item;
import entity.Item.ItemBuilder;
import external.TicketMasterAPI;

import static com.mongodb.client.model.Filters.eq;

//static 的原因是因为可能会重复使用;
public class MongoDBConnection implements DBConnection {
	private static final Item item = null;
	private MongoClient mongoClient;
	private MongoDatabase db;

	public MongoDBConnection() {
		// connects to local mongodb server.
		mongoClient = new MongoClient();
		db = mongoClient.getDatabase(MongoDBUtil.DB_NAME);
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		if (mongoClient != null) {
			mongoClient.close();
		}
	}

	@Override
	public void setFavoriteItems(String userId, List<String> itemIds) {
		db.getCollection("users").updateOne(new Document("user_id", userId), new Document("$push", new Document("favorite", new Document("$each", itemIds))));

	}

	@Override
	public void unsetFavoriteItems(String userId, List<String> itemIds) {
		db.getCollection("users").updateOne(new Document("user_id", userId), new Document("$pullAll", new Document("favorite",itemIds)));


	}

	@Override
	public Set<String> getFavoriteItemIds(String userId) {
		Set<String> favoriteItems = new HashSet<String>();
		FindIterable<Document> iterable = db.getCollection("users").find(eq("user_id", userId));
		if (iterable.first().containsKey("favorite")) {
			@SuppressWarnings("unchecked")
			List<String> list = (List<String>) iterable.first().get("favorite");
			favoriteItems.addAll(list);
		}
		return favoriteItems;
	}

	@Override
	public Set<Item> getFavoriteItems(String userId) {
		Set<String> itemIds = getFavoriteItemIds(userId);
		Set<Item> favoriteItems = new HashSet<>();
		for (String itemId : itemIds) {
			FindIterable<Document> iterable = db.getCollection("items").find(eq("item_id", itemId));
			if (iterable.first() != null) {
				// 如果第一个存在, 那么拿出第一个就可以了
				Document doc = iterable.first();
				ItemBuilder builder = new ItemBuilder();

				builder.setItemId(doc.getString("item_id"));
				builder.setName(doc.getString("name"));
				builder.setAddress(doc.getString("address"));
				builder.setImageUrl(doc.getString("image_url"));
				builder.setUrl(doc.getString("url"));
				builder.setCategories(getCategories(itemId));
				builder.setDistance(doc.getDouble("distance"));
				builder.setRating(doc.getDouble("rating"));

				favoriteItems.add(builder.build());
			}

		}
		return favoriteItems;
	}

	@Override
	public Set<String> getCategories(String itemId) {
		Set<String> categories = new HashSet<String>();
		FindIterable<Document> iterable = db.getCollection("items").find(eq("item_id", itemId));
		@SuppressWarnings("unchecked")
		List<String> list = (List<String>) iterable.first().get("categories");
		if (list != null) {
			categories.addAll(list);
		}
		return categories;
	}

	@Override
	public List<Item> searchItems(double lat, double lon, String term) {
		TicketMasterAPI tmAPI = new TicketMasterAPI();
		List<Item> items = tmAPI.search(lat, lon, term);
		for (Item item : items) {
			saveItem(item);
		}
		return items;
	}

	@Override
	public void saveItem(Item item) {
		if (db == null) {
			return;
		}
		FindIterable<Document> iterable = db.getCollection("items").find(eq("item_id", item.getItemId()));
		if (iterable.first() == null) {
			// itemid 是否存在? 不检查的话有可能会有重复itemid
			db.getCollection("items")
					.insertOne(new Document().append("item_id", item.getItemId()).append("name", item.getName())
							.append("rating", item.getRating()).append("address", item.getAddress())
							.append("image_url", item.getImageUrl()).append("url", item.getUrl())
							.append("categories", item.getCategories()).append("distance", item.getDistance()));
		}

	}

	@Override
	public String getFullname(String userId) {
		FindIterable<Document> iterable = db.getCollection("users").find(eq("user_id", userId));
		Document document = iterable.first();
		String firstName = document.getString("first_name");
		String lastName = document.getString("last_name");
		return firstName + " " + lastName;
	}

	@Override
	public boolean verifyLogin(String userId, String password) {
		FindIterable<Document> iterable = db.getCollection("users").find(eq("user_id", userId));
		return (iterable.first() != null) && (iterable.first().getString("password").equals(password));

	}

}
