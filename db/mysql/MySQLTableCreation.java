package db.mysql;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Connection;

public class MySQLTableCreation {
	// Run this as Java application to reset db schema.
	//table creation function;
	public static void main(String[] args) {
		try {
			//step1: create connection;
			Connection connector = null;
			//reflection(com.xxx.xxx.xxx.YWZ)的写法;
			Class.forName("com.mysql.jdbc.Driver").newInstance();    //创建了一个XX class 对应的 instance..
			connector = DriverManager.getConnection(MySQLDBUtil.URL);//建立连接
			
			if (connector == null) {
				return;
			}
			//Step2: if table exists, delete it;
			//删除一个 table: DROP TABLE IF EXISTS table_name;
			Statement stmt = connector.createStatement();			//statement 绑定 sql 语句, 传给 mysql;
			String sql = "DROP TABLE IF EXISTS history";
			stmt.executeUpdate(sql);
			sql = "DROP TABLE IF EXISTS users";
			stmt.executeUpdate(sql);
			sql = "DROP TABLE IF EXISTS categories"; //contains foreign key; so need to be create first;
			stmt.executeUpdate(sql);
			sql = "DROP TABLE IF EXISTS items";
			stmt.executeUpdate(sql);
			
			
			//Step3: create table of history, users, items, categories;
//			CREATE TABLE table_name (column1 datatype,column2 datatype,column3 datatype);
			sql = "CREATE TABLE items " + "(item_id VARCHAR(255) NOT NULL, " + " name VARCHAR(255), " + "rating FLOAT,"
					+ "address VARCHAR(255), " + "image_url VARCHAR(255), " + "url VARCHAR(255), " + "distance FLOAT, "
					+ " PRIMARY KEY ( item_id ))";
			stmt.executeUpdate(sql);

			sql = "CREATE TABLE categories " + "(item_id VARCHAR(255) NOT NULL, " + " category VARCHAR(255) NOT NULL, "
					+ " PRIMARY KEY ( item_id, category), " + "FOREIGN KEY (item_id) REFERENCES items(item_id))";
			stmt.executeUpdate(sql);

			sql = "CREATE TABLE users " + "(user_id VARCHAR(255) NOT NULL, " + " password VARCHAR(255) NOT NULL, "
					+ " first_name VARCHAR(255), last_name VARCHAR(255), " + " PRIMARY KEY ( user_id ))";
			stmt.executeUpdate(sql);

			sql = "CREATE TABLE history " + "(user_id VARCHAR(255) NOT NULL , " + " item_id VARCHAR(255) NOT NULL, "
					+ "last_favor_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP, " + " PRIMARY KEY (user_id, item_id),"
					+ "FOREIGN KEY (item_id) REFERENCES items(item_id),"
					+ "FOREIGN KEY (user_id) REFERENCES users(user_id))";
			stmt.executeUpdate(sql);

			
			//Insert 语句:
			//INSERT INTO table_name (column1, column2, column3, ...)
			//VALUES (value1, value2, value3, ...);
			//step4 创建一个 fake id 用来测试收藏功能;
			sql = "INSERT INTO users " + "VALUES (\"1111\", "
					+ "\"3229c1097c00d497a0fd282d586be050\", "
					+ "\"John\", \"Smith\")";

			System.out.println("Executing query:\n" + sql);
			stmt.executeUpdate(sql);
			
			System.out.println("Import is done successfully.");
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
}


