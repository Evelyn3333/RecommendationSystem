package db.mysql;

public class MySQLDBUtil {
	  private static final String HOSTNAME = "localhost";
	  private static final String PORT_NUM = "3306"; // change it to your mysql port number
	  public static final String DB_NAME = "projectOne";
	  private static final String USERNAME = "root";	//MySQL 的默认用户名和密码;
	  private static final String PASSWORD = "root";
	  public static final String URL = "jdbc:mysql://" + HOSTNAME + ":" + PORT_NUM + "/" + DB_NAME
	      + "?user=" + USERNAME + "&password=" + PASSWORD + "&autoReconnect=true";// java 和 mysql 的链接断了的话会自动链接;
	}
