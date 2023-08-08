package card;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import database.DatabaseConnection;

public class BLRCard {

	void insertIntoDatabase(Journey j) throws Exception
	{
		Connection con = DatabaseConnection.getConnection();
		
		PreparedStatement stmt = con.prepareStatement("insert into card values(?,?,?,?)");
		stmt.setString(1, j.day);
		stmt.setDouble(2, j.time);
		stmt.setInt(3, j.fromZone);
		stmt.setInt(4, j.toZone);
		stmt.executeUpdate();
		con.close();
		
	}
	
	ArrayList<Journey> retrieveFromDatabase() throws Exception
	{
		ArrayList<Journey> journeys = new ArrayList<>();
		
		Connection con = DatabaseConnection.getConnection();
		PreparedStatement stmt = con.prepareStatement("select * from card");
		ResultSet rs=stmt.executeQuery();
		
		while (rs.next())
		{  
			String day = rs.getString(1);
			double time = rs.getDouble(2);
			int fromZone = rs.getInt(3);
			int toZone = rs.getInt(4);
			Journey j = new Journey(day, time, fromZone, toZone);
			journeys.add(j);
			j = null;
		}
		
		return journeys;
	}
	
	int getFare(ArrayList<Journey> journeys)
	{
		int totalFare = 0;
		
		// This is to track the weekly cap
		String[] days = {"Monday", "Tuesday", "Wednsday", "Thursday", "Friday", "Saturday", "Sunday"};
			
		boolean weeklyOutOfZoneFlag = false;
		int weeklyFare = 0;
		int jzone = 0;
		for (String d : days)
		{
			if (d == "Monday")
			{
				if (weeklyOutOfZoneFlag == true)
				{
					if (weeklyFare > 600)
						weeklyFare = 600;
				}
				else
				{
					if (jzone == 1 && weeklyFare > 500)
						weeklyFare = 500;
					else 
						weeklyFare = 400;		
				}
				totalFare += weeklyFare;
				weeklyOutOfZoneFlag = false;
				weeklyFare = 0;
			}
			int dailyFare = 0;
			boolean dailyOutOfZoneFlag = false;
			int i;
			for(i=0; i<journeys.size(); i++)
			{
				Journey j = journeys.get(i);
				jzone = j.fromZone;
				if (j.day != d)
				{
					dailyOutOfZoneFlag = false;
					weeklyFare += dailyFare;
					dailyFare = 0;
				}
				if (j.fromZone == j.toZone)
				{
					//This is peak hours condition for same zone
					if ((j.day != "Saturday" && j.day != "Sunday" && (j.time >= 7.0 && j.time <= 10.5) || (j.time >= 17.0 && j.time <= 20.0)) ||
							(j.day == "Saturday" && j.day == "Sunday" && (j.time >= 9.0 && j.time <= 11.0) || (j.time >= 18.0 && j.time <= 22.0)))
					{
						if (j.fromZone == 1)
						{
							dailyFare += 30;
						}
						else
						{
							dailyFare += 25;
						}
					}
					else
					{
						if (j.fromZone == 1)
						{
							dailyFare += 25;
						}
						else
						{
							dailyFare += 20;
						}
					}	
				}
				else 
				{
					dailyOutOfZoneFlag = true;
					weeklyOutOfZoneFlag = true;
					// This is to consider the out-of-zone peak hours condition
					if ((j.day != "Saturday" && j.day != "Sunday" && j.time >= 17.0 && j.time <= 20.0) ||
							(j.day == "Saturday" && j.day == "Sunday" && j.time >= 18.0 && j.time <= 22.0))
					{
						dailyFare += 35;
					}
					else 
					{
						dailyFare += 30;
					}
				}
				if (dailyOutOfZoneFlag == true)
				{
					if (dailyFare > 120)
						dailyFare = 120;
				}
				else
				{
					if (j.fromZone == 1 && dailyFare > 100)
						dailyFare = 100;
					else 
						dailyFare = 80;		
				}
			}
			if (i == journeys.size())
			{
				break;
			}
		}
		return totalFare;
	}
	
	public static void main(String[] args) throws Exception 
	{
		Journey j1 = new Journey("Monday", 10.33, 2, 1);
		Journey j2 = new Journey("Monday", 10.75, 1, 1);
		Journey j3 = new Journey("Monday", 16.25, 1, 1);
		Journey j4 = new Journey("Monday", 18.25, 1, 1);
		Journey j5 = new Journey("Monday", 19.00, 1, 2);
		BLRCard blrCard = new BLRCard();
		blrCard.insertIntoDatabase(j1);
		blrCard.insertIntoDatabase(j2);
		blrCard.insertIntoDatabase(j3);
		blrCard.insertIntoDatabase(j4);
		blrCard.insertIntoDatabase(j5);		
		
		ArrayList<Journey> journeys = blrCard.retrieveFromDatabase();
		
		System.out.println("Total fare is "+blrCard.getFare(journeys));
	}
}
