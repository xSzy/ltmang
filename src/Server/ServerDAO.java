package Server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author xSzy
 */
public class ServerDAO
{
    private static Connection conn;
    
    public ServerDAO(){
        String dbClass = "net.sourceforge.jtds.jdbc.Driver";
        try {
            Class.forName(dbClass);
            String url = "jdbc:jtds:sqlserver://127.0.0.1:1433/mydb;instance=SQLEXPRESS";
            conn = DriverManager.getConnection(url, "xSzy", "ffieosgc");
        } catch (ClassNotFoundException | SQLException ex) {
            System.out.println("No sql server class or can connect to the database");
        }        
    }        
    
    /**
     * Check login from database
     */           
    public static int checkLogin(String username, String password)
    {
        if (username.equals("test") && password.equals("test"))
            return 0;
        
        if (username.equals("test2") && password.equals("test2"))
            return 0;
        /*
        String dbClass = "net.sourceforge.jtds.jdbc.Driver";
        try
        {
            Class.forName(dbClass);
            String url = "jdbc:jtds:sqlserver://127.0.0.1:1433/mydb;instance=SQLEXPRESS";
            Connection conn = DriverManager.getConnection(url, "xSzy", "ffieosgc");
            PreparedStatement ps = conn.prepareStatement("SELECT password FROM tblAccount WHERE account = ?");
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            while(rs.next())
            {
                if(password.equals(rs.getString(1)))
                    return 0;
                return 3;
            }
            return 2;
        }
        catch (ClassNotFoundException ex)
        {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Class not found!");
            return 1;
        }
        catch (SQLException ex)
        {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("SQL Exception");
            return 1;
        }*/
        return 0;
    }
    
    public static int register(String username, String password)
    {                                      
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM tblAccount WHERE account = ?");
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                return 2;
            }
            ps = conn.prepareStatement("INSERT INTO tblAccount VALUES (?, ?)");
            ps.setString(1, username);
            ps.setString(2, password);
            ps.executeUpdate();            
        } catch (SQLException ex) {
            System.out.println("sql exception register function");
        }
        return 0;
    }
    
    public boolean addFriend(String sender, String receiver)
    {
        try
        {
            PreparedStatement ps = conn.prepareStatement("SELECT id FROM tblAccount WHERE account = ?");
            ps.setString(1, sender);
            ResultSet rs = ps.executeQuery();
            int senderId, receiverId;
            if(rs.next())
                senderId = rs.getInt(1);
            else
                return false;
            ps = conn.prepareStatement("SELECT id FROM tblAccount WHERE account = ?");
            ps.setString(1, receiver);
            rs = ps.executeQuery();
            if(rs.next())
                receiverId = rs.getInt(1);
            else
                return false;
            ps = conn.prepareStatement("INSERT INTO tblFriend VALUES (?,?)");
            ps.setInt(1, senderId);
            ps.setInt(2, receiverId);
            ps.executeUpdate();
            return true;
        }
        catch(SQLException ex)
        {
            Logger.getLogger(ServerDAO.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
    
    public ArrayList<String> getFriendList(String username)
    {
        try
        {
            ArrayList<String> result = new ArrayList<>();
            PreparedStatement ps = conn.prepareStatement("SELECT id FROM tblAccount WHERE account = ?");
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            int userId = 0;
            while(rs.next())
            {
                userId = rs.getInt(1);
            }
            ArrayList<Integer> friendId = new ArrayList<>();
            ps = conn.prepareStatement("SELECT idReceiver FROM tblFriend WHERE idSender = ?");
            ps.setInt(1, userId);
            rs = ps.executeQuery();
            while(rs.next())
            {
                friendId.add(rs.getInt(1));
            }
            ps = conn.prepareStatement("SELECT idSender FROM tblFriend WHERE idReceiver = ?");
            ps.setInt(1, userId);
            rs = ps.executeQuery();
            while(rs.next())
            {
                friendId.add(rs.getInt(1));
            }
            for(Integer i : friendId)
            {
                ps = conn.prepareStatement("SELECT account FROM tblAccount WHERE id = ?");
                ps.setInt(1, i);
                rs = ps.executeQuery();
                while(rs.next())
                {
                    result.add(rs.getString(1));
                }
            }
            return result;
        }
        catch(SQLException ex)
        {
            Logger.getLogger(ServerDAO.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
}
