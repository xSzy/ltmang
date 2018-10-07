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
import java.util.HashSet;
import java.util.Set;
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
            if(protocol.equals("Channel-list"))
            {
                ArrayList<Channel> listChannel = new ArrayList<>();
                //read number of channel
                int channelSize = dis.readInt();
                for(int i = 0; i < channelSize; i++)
                {
                    Channel channel = new Channel();
                    //read channel name
                    String channelName = dis.readUTF();
                    channel.setName(channelName);
                    //read number of client
                    int clientSize = dis.readInt();
                    ArrayList<Client> listClient = new ArrayList<>();
                    for(int j = 0; j < clientSize; j++)
                    {
                        Client client = new Client();
                        //read client's name
                        String clientName = dis.readUTF();
                        client.setUsername(clientName);
                        listClient.add(client);
                    }
                    channel.setListClient(listClient);
                    listChannel.add(channel);
                }
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
