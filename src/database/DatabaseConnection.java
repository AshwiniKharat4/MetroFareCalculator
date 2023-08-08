package database;
import java.sql.*;

public class DatabaseConnection
{
	public static Connection getConnection() throws Exception
	{
		
		String url="jdbc:mysql://localhost:3306/travelcard?autoReconnect=true&useSSL=false";
		String uname="root";
		String pass="cdac";
		Connection con=DriverManager.getConnection(url, uname, pass);
		return con;
	}
}
