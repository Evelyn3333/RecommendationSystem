package db;

import db.mongodb.MongoDBConnection;
import db.mysql.MySQLConnection;

//for different db instance;
public class DBConnectionFactory {
	//default 的数据类型, 如果不提供 mySQL/MongoDb 的话
	private static final String DEFAULT_DB = "mysql";
	
	public static DBConnection getDBConnection(String db) {
		switch (db) {
		case "mysql":
			return new MySQLConnection();
		case "mongodb":
			return new MongoDBConnection();
		// You may try other dbs and add them here.
		default:
			throw new IllegalArgumentException("Invalid db " + db);
		}
	}

	
	public static DBConnection getDBConnection() {
		return getDBConnection(DEFAULT_DB);
	}
}
