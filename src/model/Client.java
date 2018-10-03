/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.io.Serializable;
import java.net.*;

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
    private boolean ready;

    public boolean isReady()
    {
        return ready;
    }

    public void setReady(boolean ready)
    {
        this.ready = ready;
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
        ready = false;
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
}
