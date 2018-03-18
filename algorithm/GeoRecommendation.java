package algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import db.DBConnection;
import db.DBConnectionFactory;
import entity.Item;


// Recommendation based on geo distance and similar categories.
public class GeoRecommendation {

  public List<Item> recommendItems(String userId, double lat, double lon) {
		// step 1 Get all favorited items
		DBConnection conn = DBConnectionFactory.getDBConnection();
		if (conn == null) {
			return new ArrayList<>();
		}
		List<Item> recommendedItems = new ArrayList<>();
		Set<String> favoriteitems = conn.getFavoriteItemIds(userId);
		// step 2 Get all categories of favorited items, sort by count
		Map<String, Integer> allCategories = new HashMap<>();
		for(String item : favoriteitems) {
			Set<String> categoreis = conn.getCategories(item);
			for(String category : categoreis) {
				Integer count = allCategories.get(category);
				if (count == null) {
					allCategories.put(category, 1);
				} else {
					allCategories.put(category, count + 1);
				}
			}
		}
		List<Entry<String, Integer>> categoryList = new ArrayList<Entry<String, Integer>>(allCategories.entrySet());
		Collections.sort(categoryList, new Comparator<Entry<String, Integer>>() {
			public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
				return Integer.compare(o2.getValue(), o1.getValue());
			}
		});

		// Step 3, do search based on category, filter out favorite events, sort by distance
		Set<Item> visitedItem = new HashSet<>();
		for (Entry<String, Integer> category : categoryList) {
			List<Item> items = conn.searchItems(lat, lon, category.getKey());
			List<Item> filterItem = new ArrayList<>();
			for (Item item : items) {
				if (!favoriteitems.contains(item.getItemId()) && !visitedItem.contains(item)) {
				filterItem.add(item);
				}
			}
			Collections.sort(filterItem, new Comparator<Item>() {
				@Override
				public int compare(Item i1, Item i2) {
					return Double.compare(i1.getDistance(), i2.getDistance());
				}
			});
			visitedItem.addAll(filterItem);
			recommendedItems.addAll(filterItem);
		}
		

		return recommendedItems;
  }
}
