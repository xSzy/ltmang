/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.io.Serializable;
import java.net.*;
import java.util.ArrayList;

/**
 *
 * @author xSzy
 */
public class Client implements Serializable
{
    private Socket socket;
    private String username;
    private String password;
    private Channel channel;
    private int udpPort;
    private ArrayList<Client> friendList;

    public Client()
    {
        username = new String();
        password = new String();
        friendList = new ArrayList<>();
    }

    public int getUdpPort()
    {
        return udpPort;
    }

    public void setUdpPort(int udpPort)
    {
        this.udpPort = udpPort;
    }

    public Channel getChannel()
    {
        return channel;
    }

    public void setChannel(Channel channel)
    {
        this.channel = channel;
    }

    public Client(Socket s)
    {
        this.socket = s;
        
    }

    public Socket getSocket()
    {
        return socket;
    }

    public void setSocket(Socket socket)
    {
        this.socket = socket;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }
    
    public ArrayList<Client> getFriendList()
    {
        return friendList;
    }

    public void setFriendList(ArrayList<Client> friendList)
    {
        this.friendList = friendList;
    }
    
    public void addToFriendList(Client c)
    {
        friendList.add(c);
    }
    
    public void removeFromFriendList(String clientName)
    {
        for(Client c : friendList)
        {
            if(c.getUsername().equals(clientName))
                friendList.remove(c);
        }
    }
}
