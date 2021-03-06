/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client.control;

import Client.view.*;
import java.io.*;
import java.io.IOException;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Client;

/**
 *
 * @author xSzy
 */
public class ClientLoginCtr
{
    //global variables
    public Socket socket;
    public DataInputStream dis;
    public DataOutputStream dos;
    private ClientLoginFrm clf;
    public Client user;
    
    /**
     * Constructor
     * @param clf - login frame
     */
    public ClientLoginCtr(ClientLoginFrm clf)
    {
        this.clf = clf;
    }
    
    /**
     * Login attemp
     * @param serverIP
     * @param port
     * @param username
     * @param password
     * @return 0: success / 1: network error / 2: username not available / 3: wrong password
     */
    public int login(String serverIP, int port, String username, char[] password)
    {
        try
        {
            user = new Client();
            user.setUsername(username);
            user.setPassword(new String(password));
            socket = new Socket(serverIP, port);
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
            dos.writeUTF("Login");
            dos.writeUTF(username);
            dos.writeUTF(new String(password));
            int result = dis.readInt();
            return result;
        }
        catch (IOException ex)
        {
            try
            {
                if(socket != null)
                    socket.close();
                return 1;
            }
            catch(IOException ex1)
            {
                Logger.getLogger(ClientLoginCtr.class.getName()).log(Level.SEVERE, null, ex1);
                return 1;
            }
        }
    }
    /**
     * Register attemp
     * @param serverIP
     * @param port
     * @param username
     * @param password
     * @return 0: success / 1: network error / 2: used username
     */
    public int register(String serverIP, int port, String username, char[] password)
    {
        try
        {
            socket = new Socket(serverIP, port);
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
            dos.writeUTF("Register");
            dos.writeUTF(username);
            dos.writeUTF(new String(password));
            int result = dis.readInt();
            dis.close();
            dos.close();
            socket.close();
            return result;
        }
        catch (IOException ex)
        {
            return 1;
        }
    }
}
