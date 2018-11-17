/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client.control;

import Client.view.ClientFriendFrm;
import java.awt.event.MouseEvent;
import java.io.*;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 *
 * @author xSzy
 */
public class ClientFriendCtr
{
    //variable initialize
    private ClientFriendFrm cff;
    Socket server;
    DataInputStream dis;
    DataOutputStream dos;
    
    /**
     * Constructor
     * @param cff - linked to friend frame
     */
    public ClientFriendCtr(ClientFriendFrm cff)
    {
        try
        {
            //get socket from the main controller
            this.cff = cff;
            server = cff.cmf.cmc.server;
            dis = new DataInputStream(server.getInputStream());
            dos = new DataOutputStream(server.getOutputStream());
        }
        catch(IOException ex)
        {
            Logger.getLogger(ClientFriendCtr.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * This function starts when add friend item is selected
     */
    public void itemAddFriendClicked()
    {
        String clientName = cff.cmf.getSelectedClient();
        if(clientName == null)
            return;
        if(cff.isFriend(clientName))
        {
            JOptionPane.showMessageDialog(cff.cmf, "This user is already your friend!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if(clientName.equals(cff.cmf.cmc.user.getUsername()))
        {
            JOptionPane.showMessageDialog(cff.cmf, "You cannot add yourself as friend!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try
        {
            dos.writeUTF("Add-friend");
            dos.writeUTF(clientName);
        }
        catch(IOException ex)
        {
            Logger.getLogger(ClientMainCtr.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * This function starts when remove friend item is selected
     */
    public void itemRemoveFriendClicked()
    {
        String clientName = cff.getSelectedClient();
        if(clientName == null)
            return;
        if(cff.showDeleteConfirmMessage(clientName) == 0)
            return;
        try
        {
            dos.writeUTF("Remove-friend");
            dos.writeUTF(clientName);
        }
        catch(IOException ex)
        {
            Logger.getLogger(ClientMainCtr.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * This function starts when invite item is clicked
     */
    public void itemInviteClicked()
    {
        String clientName = cff.getSelectedClient();
        if(clientName == null)
            return;
        if(!cff.isClientOnline(clientName))
        {
            JOptionPane.showMessageDialog(cff.cmf, clientName + " is currently offline.", "Invite", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        try
        {
            dos.writeUTF("Invite-to-channel");
            dos.writeUTF(clientName);
        }
        catch(IOException ex)
        {
            Logger.getLogger(ClientFriendCtr.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * This function starts when user clicked the friend panel
     */
    public void listFriendMouseClicked(MouseEvent evt)
    {
        //check if right mouse button clicked
        if(SwingUtilities.isRightMouseButton(evt))
        {
            cff.listFriend.setSelectedIndex(cff.listFriend.locationToIndex(evt.getPoint()));
            if(cff.getSelectedClient() != null)
            {
                cff.showClientPopupMenu(evt.getPoint());
            }
        }
    }
}
