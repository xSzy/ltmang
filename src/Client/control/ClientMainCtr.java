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
    
    //ftp
    private FTPClient ftpClient;
    
    public ClientMainCtr(Socket server, Client user)
    {
        try
        {
            this.server = server;
            this.user = user;
            udpPort = 9715;
            udpServer = new DatagramSocket(udpPort);
            cmf = new ClientMainFrm(this);
            dis = new DataInputStream(server.getInputStream());
            dos = new DataOutputStream(server.getOutputStream());
            currentChannel = new Channel("Lobby", "");
            Thread t = new Thread(new ListeningThread());
            t.start();
            Thread t1 = new Thread(new UDPSendThread());
            t1.start();
            Thread t2 = new Thread(new UDPReceiveThread());
            t2.start();
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
    
    private void sendPacket()
    {
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
    
    private void receivePacket()
    {
        try
        {
            byte[] data = new byte[dataSize];
            DatagramPacket packet = new DatagramPacket(data, dataSize);
            udpServer.receive(packet);
            data = packet.getData();
            sdline.write(data, 0, data.length);
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
                    System.out.println(currentChannel.getName());
                    if(!currentChannel.getName().equals("Lobby"))
                    {
                        System.out.println("Sending packet to server...");
                        sendPacket();
                    }
                }
            }
            catch(LineUnavailableException ex)
            {
                Logger.getLogger(ClientMainCtr.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private class UDPReceiveThread implements Runnable
    {
        @Override
        public void run()
        {
            while(true)
                receivePacket();
        }
    }               
}
