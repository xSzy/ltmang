/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 *
 * @author xSzy
 */
public class Channel implements Serializable
{
    private String name;
    private String password;
    private ArrayList<Client> listClient;
    private String topic;
    private String description;
    private Client owner;

    public Client getOwner()
    {
        return owner;
    }

    public void setOwner(Client owner)
    {
        this.owner = owner;
    }

    public String getTopic()
    {
        return topic;
    }

    public void setTopic(String topic)
    {
        this.topic = topic;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public Channel(String name, String password)
    {
        this.name = name;
        this.password = password;
        topic = "";
        description = "";
        listClient = new ArrayList<>();
    }

    public Channel()
    {
        name = "";
        password = "";
        topic = "";
        description = "";
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }
    
    public void addClient(Client client)
    {
        listClient.add(client);
    }
    
    public void removeClient(Client client)
    {
        listClient.remove(client);
    }

    public ArrayList<Client> getListClient()
    {
        return listClient;
    }

    public void setListClient(ArrayList<Client> listClient)
    {
        this.listClient = listClient;
    }
    
    public boolean isClientInChannel(Client client)
    {
        for(Client c : listClient)
            if(c == client)
                return true;
        return false;
    }
}
