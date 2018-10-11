/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

import model.*;
import demo.Socket.ServerSocketDemo;
import java.io.*;
import java.io.IOException;
import java.net.*;
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
public class Server extends javax.swing.JFrame
{
    //global variables
    private ArrayList<Client> listClient;
    private ServerSocket server;
    private final int port = 9713;
    private ArrayList<Channel> listChannel;
    private Channel lobby;
    
    /**
     * Creates new form Server
     */
    public Server()
    {
        initComponents();
        try
        {
            server = new ServerSocket(port);
            initialize();
            
            Thread t = new Thread(new ConnectionThread());
            t.start();
        }
        catch (IOException ex)
        {
            printConsole("Error while starting server!");
        }
    }
    
    /**
     * This function starts when server starts
     */
    private void initialize()
    {
        //create lobby
        listClient = new ArrayList<>();
        listChannel = new ArrayList<>();
        lobby = new Channel("Lobby", "");
        listChannel.add(lobby);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtConsole = new javax.swing.JTextArea();
        btnTest = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setText("Console");

        txtConsole.setEditable(false);
        txtConsole.setColumns(20);
        txtConsole.setRows(5);
        jScrollPane1.setViewportView(txtConsole);

        btnTest.setText("Test");
        btnTest.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                btnTestActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addGap(159, 159, 159)
                .addComponent(btnTest)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 248, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnTest)
                .addContainerGap(15, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnTestActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btnTestActionPerformed
    {//GEN-HEADEREND:event_btnTestActionPerformed
        for(Client client : listClient)
        {
            DataOutputStream dos = null;
            try
            {
                dos = new DataOutputStream(client.getSocket().getOutputStream());
                dos.writeUTF("TEST");
            }
            catch (IOException ex)
            {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        printConsole("TEST Protocol sent to " + listClient.size() + " clients");
    }//GEN-LAST:event_btnTestActionPerformed

    /**
     * This function starts when a client succeed in login
     */
    private void initializeClient(Client client)
    {
        //add client to connected list
        listClient.add(client);
        
        //add client to lobby
        lobby.addClient(client);
        client.setChannel(lobby);
        printConsole("Client " + client.getUsername() + " entered server lobby!");
        
        //send all channel list to allclient
        for(Client c : listClient)
            updateChannelList(c);
    }
    
    /**
     * This function change client's channel
     */
    private void changeChannel(Client client, Channel channel)
    {
        //remove client from old channel clientlist
        for(Channel ch : listChannel)
        {
            if(ch.isClientInChannel(client))
                ch.removeClient(client);
        }
        //add client to new channel clientlist
        channel.addClient(client);
        client.setChannel(channel);
        //update client's channel list
        for(Client c : listClient)
        {
            updateChannelList(c);
        }
    }
    
    /**
     * This function send all channel available to client
     */
    private void updateChannelList(Client client)
    {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run()
            {
                try
                {
                    DataOutputStream dos = new DataOutputStream(client.getSocket().getOutputStream());
                    dos.writeUTF("Channel-list");
                    //write the number of available channel
                    dos.writeInt(listChannel.size());
                    //write all channel's info
                    for(Channel channel : listChannel)
                    {
                        //write channel name
                        dos.writeUTF(channel.getName());
                        //write the number of channel's client
                        dos.writeInt(channel.getListClient().size());
                        //write all channel's username
                        for(Client c : channel.getListClient())
                        {
                            dos.writeUTF(c.getUsername());
                        }
                    }
                    printConsole("Channel list sent to client " + client.getSocket().getRemoteSocketAddress());
                }
                catch (IOException ex)
                {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        t.start();
    }
    
    /**
     * This function handle incoming login request from client
     */
    private void authorizeLogin(Client client)
    {
        DataInputStream dis = null;
        DataOutputStream dos = null;
        try
        {
            dis = new DataInputStream(client.getSocket().getInputStream());
            dos = new DataOutputStream(client.getSocket().getOutputStream());
            String username = dis.readUTF();
            String password = dis.readUTF();
            int result = ServerDAO.checkLogin(username, password);
            dos.writeInt(result);
            if(result == 0) //client login successful
            {
                client.setUsername(username);
                client.setPassword(password);
            }
        }
        catch (IOException ex)
        {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * This function handle incoming register request from client
     */
    private void registerAccount(Client client)
    {
        DataInputStream dis = null;
        DataOutputStream dos = null;
        try
        {
            dis = new DataInputStream(client.getSocket().getInputStream());
            dos = new DataOutputStream(client.getSocket().getOutputStream());
            String username = dis.readUTF();
            String password = dis.readUTF();
            int result = ServerDAO.register(username, password);
            dos.writeInt(result);
        }
        catch (IOException ex)
        {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Read protocol head from client
     */
    private void readProtocol(Client client)
    {
        DataInputStream dis = null;
        DataOutputStream dos = null;
        try
        {
            dis = new DataInputStream(client.getSocket().getInputStream());
            dos = new DataOutputStream(client.getSocket().getOutputStream());
            String protocol = dis.readUTF();
            printConsole("Client " + client.getSocket().getRemoteSocketAddress().toString() + ": " + protocol);
            if(protocol.equals("Login"))
            {
                authorizeLogin(client);
                return;
            }
            else if(protocol.equals("Register"))
            {
                registerAccount(client);
                client.getSocket().close();
                return;
            }
            else if(protocol.equals("Ready"))
            {
                clientReady(client);
            }
            else if(protocol.equals("Channel-change"))
            {
                clientChangeChannel(client);
            }
            else if(protocol.equals("Channel-create"))
            {
                clientCreateChannel(client);
            }
            else if(protocol.equals("Chat-message"))
            {
                clientSendMessage(client);
            }
        }
        catch (IOException ex)
        {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////PROTOCOL HANDLING SECTION////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    /**
     * This function start when server received Ready protocol
     */
    private void clientReady(Client client)
    {
        DataOutputStream dos = null;
        try
        {
            dos = new DataOutputStream(client.getSocket().getOutputStream());
            client.setReady(true);
            printConsole("Ready received from client " + client.getSocket().getLocalAddress().toString());
            dos.writeUTF("Ready received");
            initializeClient(client);
        }
        catch (IOException ex)
        {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * This function start when client request to change channel
     */
    private void clientChangeChannel(Client client)
    {
        try
        {
            DataInputStream dis = new DataInputStream(client.getSocket().getInputStream());
            DataOutputStream dos = new DataOutputStream(client.getSocket().getOutputStream());
            String channelName = dis.readUTF();
            Channel channel = searchChannel(channelName);
            if(channel != null)
            {
                if(channel.getName().equals("Lobby"))
                {
                    changeChannel(client, channel);
                    return;
                }
                //password handling here
                String channelPassword = dis.readUTF();
                if(channelPassword.equals(channel.getPassword()))
                {
                    //change channel for client
                    changeChannel(client, channel);
                    //send the success response to client
                    dos.writeUTF("Channel-change-success");
                }
                else    //wrong password
                {
                    dos.writeUTF("Wrong-password");
                }
            }
        }
        catch (IOException ex)
        {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * This function starts when client request to create a channel
     */
    private void clientCreateChannel(Client client)
    {
        DataOutputStream dos = null;
        DataInputStream dis = null;
        try
        {
            dis = new DataInputStream(client.getSocket().getInputStream());
            dos = new DataOutputStream(client.getSocket().getOutputStream());
            String channelName = dis.readUTF();
            String channelPassword = dis.readUTF();
            String channelTopic = dis.readUTF();
            String channelDesc = dis.readUTF();
            //check availability
            for(Channel channel : listChannel)
            {
                if(channel.getName().equals(channelName)) //name exist
                {
                    //send channel exist response
                    dos.writeUTF("Channel-exist");
                    return;
                }
            }
            //create channel
            Channel channel = createChannel(client, channelName, channelPassword, channelTopic, channelDesc);
            //move client to channel
            changeChannel(client, channel);
            //send the success response to client
            dos.writeUTF("Channel-change-success");
            
        }
        catch(IOException ex)
        {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }   
    }
    
    /**
     * This function starts when client send a message to a channel
     */
    private void clientSendMessage(Client client)
    {
        DataInputStream dis = null;
        DataOutputStream dos = null;
        try
        {
            dis = new DataInputStream(client.getSocket().getInputStream());
            String msg = dis.readUTF();
            Channel channel = client.getChannel();
            
            printConsole("Channel: " + channel.getName() + " - " + client.getSocket().getRemoteSocketAddress().toString() + " " + client.getUsername() + ": " + msg);
            
            for(Client c : channel.getListClient())
            {
                dos = new DataOutputStream(c.getSocket().getOutputStream());
                dos.writeUTF("Broadcast-message");
                dos.writeUTF(client.getUsername());
                dos.writeUTF(msg);
            }
        }
        catch(IOException ex)
        {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////MISCELLANEOUS///////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    /**
     * Search channel by name
     */
    private Channel searchChannel(String channelName)
    {
        for(Channel channel : listChannel)
            if(channelName.equals(channel.getName()))
                return channel;
        return null;
    }
    
    /**
     * Create new channel
     */
    private Channel createChannel(Client owner, String channelName, String channelPassword, String channelTopic, String channelDesc)
    {
        Channel channel = new Channel(channelName, channelPassword);
        channel.setTopic(channelTopic);
        channel.setDescription(channelDesc);
        channel.setOwner(owner);
        listChannel.add(channel);
        printConsole("Channel " + channelName + " has been created!");
        return channel;
    }
    
    /**
     * Print a new line on console
     */
    private void printConsole(String s)
    {
        txtConsole.setText(txtConsole.getText() + "\n" + s);
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[])
    {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try
        {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels())
            {
                if ("Windows".equals(info.getName()))
                {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        }
        catch (ClassNotFoundException ex)
        {
            java.util.logging.Logger.getLogger(Server.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        catch (InstantiationException ex)
        {
            java.util.logging.Logger.getLogger(Server.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        catch (IllegalAccessException ex)
        {
            java.util.logging.Logger.getLogger(Server.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        catch (javax.swing.UnsupportedLookAndFeelException ex)
        {
            java.util.logging.Logger.getLogger(Server.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable()
        {
            public void run()
            {
                new Server().setVisible(true);
            }
        });
    }

    private class ConnectionThread implements Runnable
    {
        @Override
        public void run()
        {
            try
            {
                //waiting for client to connect
                Client client = new Client(server.accept());
                
                //start another thread to wait for a new client to connect
                Thread t = new Thread(new ConnectionThread());
                t.start();
                
                printConsole("Client " + client.getSocket().getRemoteSocketAddress().toString() + " connected!");
                
                while(true)
                {
                    readProtocol(client);
                }
            }
            catch (IOException ex)
            {
                Logger.getLogger(ServerSocketDemo.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnTest;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea txtConsole;
    // End of variables declaration//GEN-END:variables
}
