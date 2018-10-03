/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client.control;

import Client.view.ClientMainFrm;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.*;

/**
 *
 * @author xSzy
 */
public class ClientMainCtr
{
    Socket server;
    ClientMainFrm cmf;
    DataInputStream dis;
    DataOutputStream dos;
    
    public ClientMainCtr(Socket server)
    {
        try
        {
            this.server = server;
            cmf = new ClientMainFrm();
            dis = new DataInputStream(server.getInputStream());
            dos = new DataOutputStream(server.getOutputStream());
            Thread t = new Thread(new ListeningThread());
            t.start();
            ready();
        }
        catch (IOException ex)
        {
            Logger.getLogger(ClientMainCtr.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void ready()
    {
        try
        {
            dos.writeUTF("Ready");
        }
        catch (IOException ex)
        {
            Logger.getLogger(ClientMainCtr.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void readProtocol()
    {
        try
        {
            cmf.writeConsole("waiting for protocol...");
            String protocol = dis.readUTF();
            cmf.writeConsole("protocol received:" + protocol);
            if(protocol.equals("Channel list"))
            {
                ObjectInputStream ois = new ObjectInputStream(server.getInputStream());
                ArrayList<Channel> listChannel = (ArrayList<Channel>) ois.readObject();
                cmf.updateChannelList(listChannel);
                cmf.writeConsole("Channel list received");
                return;
            }
            else if(protocol.equals("TEST"))
            {
                cmf.writeConsole("TEST protocol received");
                return;
            }
            else if(protocol.equals("Ready received"))
            {
                cmf.writeConsole("Ready message has been sent.");
            }
        }
        catch (IOException ex)
        {
            Logger.getLogger(ClientMainCtr.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (ClassNotFoundException ex)
        {
            Logger.getLogger(ClientMainCtr.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private class ListeningThread implements Runnable
    {
        @Override
        public void run()
        {
            while(true)
            {
                readProtocol();
            }
        }
    }
}
