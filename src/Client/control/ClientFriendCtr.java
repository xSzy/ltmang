/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client.control;

import Client.view.ClientFriendFrm;

/**
 *
 * @author xSzy
 */
public class ClientFriendCtr
{
    private ClientFriendFrm cff;
    private ClientMainCtr cmc;
    
    public ClientFriendCtr(ClientMainCtr cmc)
    {
        this.cmc = cmc;
        cff = new ClientFriendFrm();
        cff.setVisible(true);
    }
}
