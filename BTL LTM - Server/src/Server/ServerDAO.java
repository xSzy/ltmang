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
    
    /**
     * Constructor
     */
    public ServerDAO(){
        String dbClass = "com.mysql.cj.jdbc.Driver";
        try {
            Class.forName(dbClass);
            String url = "jdbc:mysql://localhost:3306/btlltm?useSSL=false";
            conn = DriverManager.getConnection(url, "root", "ffieosgc");
        } catch (ClassNotFoundException | SQLException ex) {
            System.out.println("No sql server class or can connect to the database");
        }        
    }        
    
    /**
     * Check login from database
     * @param username - client name
     * @param password - client password
     * @return - 0: success / 1: connection error / 2: unavailable username / 3: wrong password
     */           
    public static int checkLogin(String username, String password)
    {
        try
        {
            PreparedStatement ps = conn.prepareStatement("SELECT password FROM tblaccount WHERE account = ?");
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
        catch (SQLException ex)
        {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("SQL Exception");
            return 1;
        }
    }
    
    /**
     * Check register from database
     * @param username
     * @param password
     * @return 
     */
    public static int register(String username, String password)
    {                                      
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM tblaccount WHERE account = ?");
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                return 2;
            }
            ps = conn.prepareStatement("INSERT INTO tblaccount(account, password) VALUES (?, ?)");
            ps.setString(1, username);
            ps.setString(2, password);
            ps.executeUpdate();            
        }
        catch(SQLException ex)
        {
            Logger.getLogger(ServerDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 0;
    }
    
    /**
     * Add new friend entry to database
     * @param sender - username
     * @param receiver - username
     * @return true if success, false otherwise
     */
    public static boolean addFriend(String sender, String receiver)
    {
        try
        {
            PreparedStatement ps = conn.prepareStatement("SELECT id FROM tblaccount WHERE account = ?");
            ps.setString(1, sender);
            ResultSet rs = ps.executeQuery();
            int senderId, receiverId;
            if(rs.next())
                senderId = rs.getInt(1);
            else
                return false;
            ps = conn.prepareStatement("SELECT id FROM tblaccount WHERE account = ?");
            ps.setString(1, receiver);
            rs = ps.executeQuery();
            if(rs.next())
                receiverId = rs.getInt(1);
            else
                return false;
            ps = conn.prepareStatement("INSERT INTO tblfriend VALUES (?,?)");
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
    
    /**
     * Get all friend of a client
     * @param username - client to search
     * @return list of friends
     */
    public static ArrayList<String> getFriendList(String username)
    {
        try
        {
            ArrayList<String> result = new ArrayList<>();
            PreparedStatement ps = conn.prepareStatement("SELECT id FROM tblaccount WHERE account = ?");
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            int userId = 0;
            while(rs.next())
            {
                userId = rs.getInt(1);
            }
            ArrayList<Integer> friendId = new ArrayList<>();
            ps = conn.prepareStatement("SELECT idReceiver FROM tblfriend WHERE idSender = ?");
            ps.setInt(1, userId);
            rs = ps.executeQuery();
            while(rs.next())
            {
                friendId.add(rs.getInt(1));
            }
            ps = conn.prepareStatement("SELECT idSender FROM tblfriend WHERE idReceiver = ?");
            ps.setInt(1, userId);
            rs = ps.executeQuery();
            while(rs.next())
            {
                friendId.add(rs.getInt(1));
            }
            for(Integer i : friendId)
            {
                ps = conn.prepareStatement("SELECT account FROM tblaccount WHERE id = ?");
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
    
    /**
     * Remove friend acquaintance from database
     * @param user1
     * @param user2 
     */
    public static void removeFriend(String user1, String user2)
    {
        try
        {
            int uid1, uid2;
            PreparedStatement ps = conn.prepareStatement("SELECT id FROM tblaccount WHERE account = ?");
            ps.setString(1, user1);
            ResultSet rs = ps.executeQuery();
            if(rs.next())
                uid1 = rs.getInt(1);
            else
                return;
            ps.setString(1, user2);
            rs = ps.executeQuery();
            if(rs.next())
                uid2 = rs.getInt(1);
            else
                return;
            ps = conn.prepareStatement("DELETE FROM tblfriend WHERE idSender = ? AND idReceiver = ?");
            ps.setInt(1, uid1);
            ps.setInt(2, uid2);
            if(ps.executeUpdate() > 0)
                return;
            ps.setInt(1, uid2);
            ps.setInt(2, uid1);
            ps.executeUpdate();
            return;
        }
        catch(SQLException ex)
        {
            Logger.getLogger(ServerDAO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
