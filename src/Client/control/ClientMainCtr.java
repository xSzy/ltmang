/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client.control;

import Client.view.ClientMainFrm;
import java.awt.event.MouseEvent;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
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
    Client user;
    public Channel currentChannel;
    
    public ClientMainCtr(Socket server, Client user)
    {
        try
        {
            this.server = server;
            this.user = user;
            cmf = new ClientMainFrm(this);
            dis = new DataInputStream(server.getInputStream());
            dos = new DataOutputStream(server.getOutputStream());
            currentChannel = new Channel("Lobby", "");
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
                        if(clientName == user.getUsername())
                        {
                            currentChannel.setName(channel.getName());
                        }
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
            else if(protocol.equals("Wrong-password"))
            {
                JOptionPane.showMessageDialog(cmf, "Wrong password!", "ERROR", JOptionPane.ERROR_MESSAGE);
            }
            else if(protocol.equals("Channel-exist"))
            {
                JOptionPane.showMessageDialog(cmf, "Channel name exist!", "ERROR", JOptionPane.ERROR_MESSAGE);
            }
            else if(protocol.equals("Channel-change-success"))
            {
                cmf.setMsgPanel();
            }
            else if(protocol.equals("Broadcast-message"))
            {
                receiveMessage();
            }
        }
        catch (IOException ex)
        {
            Logger.getLogger(ClientMainCtr.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void ChannelListClicked(MouseEvent evt)
    {
        //check if right mouse button clicked
        if(SwingUtilities.isRightMouseButton(evt))
        {
        }
    }
    
    public void itemConnectClicked()
    {
        //get channel name
        String channelName = cmf.getSelectedChannel();
        if(channelName == null)
            return;
        String channelPassword = null;
        if(!channelName.equals("Lobby"))
        {
            channelPassword = cmf.getPassword();
            if(channelPassword == null)
                return;
        }
        try
        {
            dos.writeUTF("Channel-change");
            dos.writeUTF(channelName);
            if(!channelName.equals("Lobby"))
                dos.writeUTF(channelPassword);
        }
        catch (IOException ex)
        {
            Logger.getLogger(ClientMainCtr.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void itemCreateClicked()
    {
        //get channel info
        Channel channel = cmf.showCreateChannelDialog();
        if(channel == null)
            return;
        try
        {
            //send channel create request
            dos.writeUTF("Channel-create");
            dos.writeUTF(channel.getName());
            dos.writeUTF(channel.getPassword());
            dos.writeUTF(channel.getTopic());
            dos.writeUTF(channel.getDescription());
        }
        catch(IOException ex)
        {
            Logger.getLogger(ClientMainCtr.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void sendMsg(String msg){
        try {
            dos.writeUTF("Chat-message");
            dos.writeUTF(msg);
        }
        catch(IOException ex)
        {
            Logger.getLogger(ClientMainCtr.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void receiveMessage()
    {
        try
        {
            String sender = dis.readUTF();
            String msg = dis.readUTF();
            cmf.printMessage(sender, msg);
        }
        catch(IOException ex)
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
