package mars.common;

public class MySqlDBUtil {
	private static final String HOST_NAME = "localhost";
	private static final String PORT_NUM = "3306"; // change it to your mysql port number
	private static final String DB_NAME = "projectOne";
	
	public static final String DRIVER_CLASS_NAME = "com.mysql.jdbc.Driver";
	public static final String USERNAME = "root";
	public static final String PASSWORD = "root";
	public static final String URL = "jdbc:mysql://" + HOST_NAME + ":" + PORT_NUM + "/" + DB_NAME + "?serverTimezone=UTC";
}
