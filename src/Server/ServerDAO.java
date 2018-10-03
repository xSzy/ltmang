package Server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author xSzy
 */
public class ServerDAO
{
    /**
     * Check login from database
     */
    public static int checkLogin(String username, String password)
    {
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
        }
    }
    
    public static int register(String username, String password)
    {
        String dbClass = "net.sourceforge.jtds.jdbc.Driver";
        try
        {
            Class.forName(dbClass);
            String url = "jdbc:jtds:sqlserver://127.0.0.1:1433/mydb;instance=SQLEXPRESS";
            Connection conn = DriverManager.getConnection(url, "xSzy", "ffieosgc");
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM tblAccount WHERE account = ?");
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            while(rs.next())
                return 2;
            ps = conn.prepareStatement("INSERT INTO tblAccount VALUES (?, ?)");
            ps.setString(1, username);
            ps.setString(2, password);
            ps.executeUpdate();
            return 0;
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
        }
    }
}
