/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client.control;

import Client.view.ClientMainFrm;
import java.awt.event.MouseEvent;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import model.*;
import org.apache.commons.net.ftp.FTPClient;
import java.util.Properties;

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
    public Client user;
    DatagramSocket udpServer;
    public Channel currentChannel;
    private int udpPort;
    private int dataSize;
    //audio settings
    private float sampleRate = 8000;
    private int sampleSizeInBits = 8;
    private int channel = 1;
    private boolean signed = true;
    private boolean bigEndian = true;
    byte buffer[];
    TargetDataLine tdline;
    SourceDataLine sdline;
    public ArrayList<Channel> listChannel;
    Thread t, t1, t2;
    
    //ftp
    private FTPClient ftpClient;
    
    public ClientMainCtr(ClientMainFrm cmf, Socket server, Client user)
    {
        try
        {
            this.server = server;
            this.user = user;
            
            //attemp to find an available udp port
            udpPort = getAvailablePort();
            udpServer = new DatagramSocket(udpPort);
            
            this.cmf = cmf;
            dis = new DataInputStream(server.getInputStream());
            dos = new DataOutputStream(server.getOutputStream());
            
            
            t = new Thread(new ListeningThread());
            t.start();
            t1 = new Thread(new UDPSendThread());
            t1.start();
            t2 = new Thread(new UDPReceiveThread());
            t2.start();
            ready();
        }
        catch (IOException ex)
        {
            Logger.getLogger(ClientMainCtr.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * This function sends the ready signal to server
     */
    public void ready()
    {
        try
        {
            dos.writeUTF("UDP-Port");
            dos.writeInt(udpPort);
        }
        catch (IOException ex)
        {
            Logger.getLogger(ClientMainCtr.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * This function sends the disconnect signal to server
     */
    public void disconnect()
    {
        try
        {
            dos.writeUTF("Disconnect");
            t1.stop();
            t.stop();
            t2.stop();
        }
        catch(IOException ex)
        {
            Logger.getLogger(ClientMainCtr.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally
        {
            try
            {
                dos.close();
                dis.close();
                //server.close();
                udpServer.close();
                tdline.close();
                sdline.close();
            }
            catch(IOException ex)
            {
                Logger.getLogger(ClientMainCtr.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
    }
    
    /**
     * This function handle the protocol sent from the server
     */
    public void readProtocol()
    {
        try
        {
            cmf.writeConsole("waiting for protocol...");
            String protocol = dis.readUTF();
            cmf.writeConsole("protocol received:" + protocol);
            if(protocol.equals("Channel-list"))
            {
                readChannelList();
                cmf.writeConsole("Channel list received");
                return;
            }
            else if(protocol.equals("Friend-list"))
            {
                readFriendList();
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
            else if(protocol.equals("Friend-request"))
            {
                receiveFriendRequest();
            }
            else if(protocol.equals("Friend-request-accepted"))
            {
                String friendName = dis.readUTF();
                JOptionPane.showMessageDialog(cmf, friendName + " has accepted your friend request.", "Friend", JOptionPane.INFORMATION_MESSAGE);
            }
            else if(protocol.equals("Friend-request-declined"))
            {
                String friendName = dis.readUTF();
                JOptionPane.showMessageDialog(cmf, friendName + " has declined your friend request.", "Friend", JOptionPane.INFORMATION_MESSAGE);
            }
            else if(protocol.equals("Redirect-invitation"))
            {
                receiveInviteRequest();
            }
            else if(protocol.equals("Friend-offline"))
            {
                JOptionPane.showMessageDialog(cmf, "Your friend is offline at the moment.", "Invite to channel", JOptionPane.INFORMATION_MESSAGE);
            }
            else if(protocol.equals("Same-channel"))
            {
                JOptionPane.showMessageDialog(cmf, "You are in the same channel with your friend.", "Invite to channel", JOptionPane.INFORMATION_MESSAGE);
            }
            else if(protocol.equals("Kicked"))
            {
                String channelName = dis.readUTF();
                JOptionPane.showMessageDialog(cmf, "You have been kicked from channel " + channelName, "Kicked", JOptionPane.INFORMATION_MESSAGE);
            }
            else if(protocol.equals("Edit-channel-success"))
            {
                JOptionPane.showMessageDialog(cmf, "Channel edit successful! ", "Information", JOptionPane.INFORMATION_MESSAGE);
            }
        }
        catch (IOException ex)
        {
            Logger.getLogger(ClientMainCtr.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * This function handles when user clicked the channellist
     * @param evt 
     */
    public void ChannelListClicked(MouseEvent evt)
    {
        //check if doubleclick
        if(evt.getClickCount() == 2)
        {
            this.itemConnectClicked();
        }
        cmf.listChannel.setSelectedIndex(cmf.listChannel.locationToIndex(evt.getPoint()));
        //check if right mouse button clicked
        if(SwingUtilities.isRightMouseButton(evt))
        {
            if(cmf.getSelectedChannel() != null)
            {
                cmf.showChannelPopupMenu(evt.getPoint());
                cmf.updateInfoPanel(searchChannel(cmf.getSelectedChannel()));
                //enable all button and menu item
                cmf.setChannelItem(true);
                cmf.setFriendItem(false);
            }
            else if(cmf.getSelectedClient() != null)
            {
                cmf.showClientPopupMenu(evt.getPoint());
                cmf.updateInfoPanel(cmf.getSelectedClient());
                cmf.setChannelItem(false);
                cmf.setFriendItem(true);
            }
            return;
        }
        if(SwingUtilities.isLeftMouseButton(evt))
        {
            if(cmf.getSelectedChannel() != null)
            {
                cmf.updateInfoPanel(searchChannel(cmf.getSelectedChannel()));
                cmf.setChannelItem(true);
                cmf.setFriendItem(false);
            }
            else if(cmf.getSelectedClient() != null)
            {
                cmf.updateInfoPanel(cmf.getSelectedClient());
                cmf.setChannelItem(false);
                cmf.setFriendItem(true);
            }
            return;
        }
    }
    
    /**
     * This function start when user click connect to channel
     */
    public void itemConnectClicked()
    {
        //get channel name
        String channelName = cmf.getSelectedChannel();
        if(channelName == null)
            return;
        if(currentChannel.getName().equals(channelName))
        {
            JOptionPane.showMessageDialog(cmf, "You are already in this channel!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
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
    
    /**
     * This function starts when user click create channel
     */
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
    
    /**
     * This function starts when user click edit channel
     */
    public void itemEditChannelClicked()
    {
        String channelName = cmf.getSelectedChannel();
        if(channelName == null)
            return;
        Channel channel = getChannelbyName(channelName);
        if(channel == null)
            return;
        if(!user.getUsername().equals(channel.getOwner().getUsername()))
        {
            JOptionPane.showMessageDialog(cmf, "You do not have permission to edit this channel!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Channel newChannel = cmf.showEditChannelDialog(channel);
        if(newChannel == null)
            return;
        try
        {
            dos.writeUTF("Edit-channel");
            dos.writeUTF(channel.getName());
            dos.writeUTF(newChannel.getName());
            dos.writeUTF(newChannel.getPassword());
            dos.writeUTF(newChannel.getTopic());
            dos.writeUTF(newChannel.getDescription());
        }
        catch(IOException ex)
        {
            Logger.getLogger(ClientMainCtr.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * This function starts when user click delete channel
     */
    public void itemDeleteChannelClicked()
    {
        String channelName = cmf.getSelectedChannel();
        if(channelName == null)
            return;
        Channel channel = getChannelbyName(channelName);
        if(channel == null)
            return;
        if(!user.getUsername().equals(channel.getOwner().getUsername()))
        {
            JOptionPane.showMessageDialog(cmf, "You do not have permission to edit this channel!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try
        {
            dos.writeUTF("Delete-channel");
            dos.writeUTF(channel.getName());
        }
        catch(IOException ex)
        {
            Logger.getLogger(ClientMainCtr.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * This function starts when user click kick client
     */
    public void itemKickClicked()
    {
        String username = cmf.getSelectedClient();
        if(username == null)
            return;
        String theirchannelName = cmf.getUserChannel(username);
        Channel theirchannel = getChannelbyName(theirchannelName);
        if(theirchannel == null)
            return;
        if(!user.getUsername().equals(theirchannel.getOwner().getUsername()))
        {
            JOptionPane.showMessageDialog(cmf, "You do not have permission to kick this user!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try
        {
            dos.writeUTF("Kick-client");
            dos.writeUTF(username);
        }
        catch(IOException ex)
        {
            Logger.getLogger(ClientMainCtr.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * This function receive the invite request and show it
     */
    private void receiveInviteRequest()
    {
        try
        {
            String sender = dis.readUTF();
            String channelName = dis.readUTF(); 
            if(cmf.showInviteRequest(sender, channelName))
            {
                dos.writeUTF("Invitation-accepted");
                dos.writeUTF(sender);
            }
        }
        catch(IOException ex)
        {
            Logger.getLogger(ClientMainCtr.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * This function gets the channel by name
     * @param name - channel name
     * @return - the channel
     */
    private Channel getChannelbyName(String name)
    {
        for(Channel c : listChannel)
            if(c.getName().equals(name))
                return c;
        return null;
    }
    
    /**
     * This function sends chat message to server
     * @param msg - message
     * @param isGlobal - true = global mode / false = channel only
     */
    public void sendMsg(String msg, boolean isGlobal){
        try {
            dos.writeUTF("Chat-message");
            if(isGlobal)
                dos.writeUTF("Server");
            else
                dos.writeUTF("Channel");
            dos.writeUTF(msg);
        }
        catch(IOException ex)
        {
            Logger.getLogger(ClientMainCtr.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * This function receive broadcast message from server
     */
    private void receiveMessage()
    {
        try
        {
            String mode = dis.readUTF();
            String sender = dis.readUTF();
            String msg = dis.readUTF();
            cmf.printMessage(sender, msg, mode);
        }
        catch(IOException ex)
        {
            Logger.getLogger(ClientMainCtr.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * This function receive the friend request from other user and show it
     */
    private void receiveFriendRequest()
    {
        try
        {
            String senderName = dis.readUTF();
            int result = cmf.showFriendRequest(senderName);
            if(result == 1)
            {
                dos.writeUTF("Friend-request-accepted");
                dos.writeUTF(senderName);
            }
            else
            {
                dos.writeUTF("Friend-request-declined");
                dos.writeUTF(senderName);
            }
        }
        catch(IOException ex)
        {
            Logger.getLogger(ClientMainCtr.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * This function reads the channel list from server
     */
    private void readChannelList()
    {
        try
        {
            listChannel = new ArrayList<>();
            //read number of channel
            int channelSize = dis.readInt();
            for(int i = 0; i < channelSize; i++)
            {
                Channel c = new Channel();
                //read channel name
                String channelName = dis.readUTF();
                c.setName(channelName);
                //read channel password
                String channelPassword = dis.readUTF();
                c.setPassword(channelPassword);
                //read channel owner
                Client owner = new Client();
                owner.setUsername(dis.readUTF());
                c.setOwner(owner);
                //read channel topic
                String channelTopic = dis.readUTF();
                c.setTopic(channelTopic);
                //read channel desc
                String channelDesc = dis.readUTF();
                c.setDescription(channelDesc);
                //read number of client
                int clientSize = dis.readInt();
                ArrayList<Client> listClient = new ArrayList<>();
                for(int j = 0; j < clientSize; j++)
                {
                    Client client = new Client();
                    //read client's name
                    String clientName = dis.readUTF();
                    if(clientName.equals(user.getUsername()))
                    {
                        currentChannel = c;
                    }
                    client.setUsername(clientName);
                    listClient.add(client);
                }
                c.setListClient(listClient);
                listChannel.add(c);
            }
            cmf.updateChannelList(listChannel);
        }
        catch(IOException ex)
        {
            Logger.getLogger(ClientMainCtr.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * This function read the friendlist from server
     */
    private void readFriendList()
    {
        ArrayList<String> onlineList = new ArrayList<>();
        ArrayList<String> offlineList = new ArrayList<>();
        try
        {
            int onlineSize = dis.readInt();
            for(int i = 0; i < onlineSize; i++)
            {
                onlineList.add(dis.readUTF());
            }
            int offlineSize = dis.readInt();
            for(int i = 0; i < offlineSize; i++)
            {
                offlineList.add(dis.readUTF());
            }
            cmf.cff.displayFriendList(onlineList, offlineList);
        }
        catch(IOException ex)
        {
            Logger.getLogger(ClientMainCtr.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * This function search channel by name
     * @param name - channel name
     * @return channel object
     */
    private Channel searchChannel(String name)
    {
        for(Channel c : listChannel)
        {
            if(name.equals(c.getName()))
                return c;
        }
        return null;
    }
    
    /**
     * This function send the UDP packet to server
     */
    private void sendPacket()
    {
        if(udpServer.isClosed())
            return;
        if(!cmf.isMicEnabled())
            return;
        DatagramPacket packet;
        //get sound from device
        int count = tdline.read(buffer, 0, buffer.length);
        //create the packet
        if(count > 0)
        {
            try {
                packet = new DatagramPacket(buffer, buffer.length);
                packet.setAddress(server.getInetAddress());
                packet.setData(buffer, 0, buffer.length);
                packet.setPort(9714);
                udpServer.send(packet);
            }
            catch(IOException ex) {
                Logger.getLogger(ClientMainCtr.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    /**
     * This function receive packet from server
     */
    private void receivePacket()
    {
        try
        {
            byte[] data = new byte[dataSize];
            DatagramPacket packet = new DatagramPacket(data, dataSize);
            udpServer.receive(packet);
            data = packet.getData();
            if(cmf.isVoiceEnabled())
                sdline.write(data, 0, data.length);
        }
        catch(IOException ex)
        {
            Logger.getLogger(ClientMainCtr.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * This thread listens protocols from server
     */
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
    
    /**
     * This thread sends UDP packet to server
     */
    private class UDPSendThread implements Runnable
    {
        @Override
        public void run()
        {
            try
            {
                //create an audio format for the recording
                AudioFormat format = new AudioFormat(sampleRate, sampleSizeInBits, channel, signed, bigEndian);
                
                //get the input dataline
                DataLine.Info tdi = new DataLine.Info(TargetDataLine.class, format);
                tdline = (TargetDataLine) AudioSystem.getLine(tdi);
                
                //get the output dataline
                DataLine.Info sdi = new DataLine.Info(SourceDataLine.class, format);
                sdline = (SourceDataLine) AudioSystem.getLine(sdi);
                
                //open and start both line
                tdline.open(format);
                tdline.start();
                sdline.open(format);
                sdline.start();
                
                //initialize buffer
                dataSize = (int) format.getSampleRate()*format.getFrameSize();
                buffer = new byte[dataSize];
                
                while(true)
                {
                    //System.out.println(currentChannel.getName());
                    Thread.sleep(1);
                    if(currentChannel != null && !currentChannel.getName().equals("Lobby"))
                    {
                        //System.out.println("Sending packet to server...");
                        sendPacket();
                    }
                }
            }
            catch(LineUnavailableException ex)
            {
                Logger.getLogger(ClientMainCtr.class.getName()).log(Level.SEVERE, null, ex);
            }
            catch(InterruptedException ex)
            {
                Logger.getLogger(ClientMainCtr.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    /**
     * This function gets an available port to use UDP transfer
     * @return port
     */
    public int getAvailablePort()
    {
        int startPort = 8050;
        int endPort = 27000;
        for(int i = startPort; i < endPort; i++)
        {
            if(isPortAvailable(i))
                return i;
        }
        return -1;
    }
    
    /**
     * Check if that port is available or not
     * @param port
     * @return true if available, false otherwise
     */
    private boolean isPortAvailable(int port)
    {
        DatagramSocket portChecker = null;
        try
        {
            portChecker = new DatagramSocket(port);
            portChecker.setReuseAddress(true);
            return true;
        }
        catch(SocketException ex)
        {
            return false;
        }
        finally
        {
            if(portChecker != null)
                portChecker.close();
        }
    }
    
    /**
     * This thread receive UDP packets
     */
    private class UDPReceiveThread implements Runnable
    {
        @Override
        public void run()
        {
            while(!udpServer.isClosed())
                receivePacket();
        }
    }               
}
