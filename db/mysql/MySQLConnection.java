package db.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import entity.Item.ItemBuilder;
import db.DBConnection;
import entity.Item;
import external.TicketMasterAPI;

public class MySQLConnection implements DBConnection {
	private Connection conn;

	public MySQLConnection() { // 构造函数, 创建一个连接;
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			conn = DriverManager.getConnection(MySQLDBUtil.URL);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void close() { // 如果数据操作结束之后 connection 没有关掉, 它会帮你关掉;
		if (conn != null) {
			try {
				conn.close();
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
	}

	@Override
	public void setFavoriteItems(String userId, List<String> itemIds) {
		if (conn == null) {
			return;
		}
		try {
			String sql = "INSERT IGNORE INTO history (user_id, item_id) VALUES (?, ?)";
			PreparedStatement stmt = conn.prepareStatement(sql);
			for (String itemId : itemIds) {
				stmt.setString(1, userId);
				stmt.setString(2, itemId);
				stmt.execute();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void unsetFavoriteItems(String userId, List<String> itemIds) {
		if (conn == null) {
			return;
		}
		try {
			String sql = "DELETE FROM history WHERE user_id = ? and item_id = ?";

			PreparedStatement stmt = conn.prepareStatement(sql);
			for (String itemId : itemIds) {
				stmt.setString(1, userId);
				stmt.setString(2, itemId);
				stmt.execute();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Set<String> getFavoriteItemIds(String userId) {
		if (conn == null) {
			return new HashSet<>();
		}
		Set<String> favoriteItems = new HashSet<>();

		try {
			String sql = "SELECT item_id FROM history WHERE user_id = ?";
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, userId);
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				favoriteItems.add(rs.getString("item_id"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return favoriteItems;
	}

	@Override
	public Set<Item> getFavoriteItems(String userId) {
		if (conn == null) {
			return new HashSet<>();
		}
		Set<Item> favoriteItems = new HashSet<>();
		Set<String> itemIds = getFavoriteItemIds(userId);
		try {
			for (String itemId : itemIds) {
				String sql = "SELECT * FROM items WHERE item_id = ?";
				PreparedStatement statement = conn.prepareStatement(sql);
				statement.setString(1, itemId);
				ItemBuilder builder = new ItemBuilder();
				ResultSet rs = statement.executeQuery();

				while (rs.next()) {
					builder.setItemId(rs.getString("item_id"));
					builder.setName(rs.getString("name"));
					builder.setAddress(rs.getString("address"));
					builder.setImageUrl(rs.getString("image_url"));
					builder.setUrl(rs.getString("url"));
					builder.setCategories(getCategories(itemId));
					builder.setDistance(rs.getDouble("distance"));
					builder.setRating(rs.getDouble("rating"));

					favoriteItems.add(builder.build());
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return favoriteItems;
	}

	@Override
	public Set<String> getCategories(String itemId) {
		Set<String> favoriteItems = new HashSet<>();
		try {
			String sql = "SELECT category FROM categories WHERE item_id = ?";
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, itemId);
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				favoriteItems.add(rs.getString("category"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return favoriteItems;
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
		if (conn == null) {
			return;
		}
		try {// SQL 语句, 用问号来占位, 预防SQL injection 的安全漏洞
				// IGNORE keyword 用来去重, 如果之前有存在 item, 那么就无视, 否则再插入;
			String sql = "INSERT IGNORE INTO items VALUES (?, ?, ?, ?, ?, ?, ?)"; // SQL injection
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setString(1, item.getItemId()); // 第一个参数是, 要替换第几个问号, 第二个参数是替换的参数;
			stmt.setString(2, item.getName());
			stmt.setDouble(3, item.getRating());
			stmt.setString(4, item.getAddress());
			stmt.setString(5, item.getImageUrl());
			stmt.setString(6, item.getUrl());
			stmt.setDouble(7, item.getDistance());
			stmt.execute(); // 把语句传给 MySql 去执行了;
			// sql injection: 安全漏洞;
			sql = "INSERT IGNORE INTO categories VALUES (?,?)";
			for (String category : item.getCategories()) {
				stmt = conn.prepareStatement(sql);
				stmt.setString(1, item.getItemId());
				stmt.setString(2, category);
				stmt.execute();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	@Override
	public String getFullname(String userId) {
		if (conn == null) {
			return null;
		}
		String name = "";
		try {
			String sql = "SELECT first_name, last_name from users WHERE user_id = ?";
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, userId);
			ResultSet rs = statement.executeQuery();
			if (rs.next()) {
				name = String.join(" ", rs.getString("first_name"), rs.getString("last_name"));
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return name;
	}

	@Override
	public boolean verifyLogin(String userId, String password) {
		if (conn == null) {
			return false;
		}
		try {
			String sql = "SELECT user_id from users WHERE user_id = ? and password = ?";
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, userId);
			statement.setString(2, password);
			ResultSet rs = statement.executeQuery();
			if (rs.next()) {
				return true;
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return false;
	}

}
