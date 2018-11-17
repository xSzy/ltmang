/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server.Controller;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.ssl.SslConfigurationFactory;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;


/**
 *
 * @author ns_red
 */
public class FTPServer {
    private FtpServer server;
    
    public FTPServer(){
        init();        
    }
    
    private void init(){
        FtpServerFactory serverFactory = new FtpServerFactory();
        ListenerFactory listenerFactory = new ListenerFactory();        
        SslConfigurationFactory sslFactory = new SslConfigurationFactory();
        PropertiesUserManagerFactory userFactory = new PropertiesUserManagerFactory();        
        
        sslFactory.setKeystoreFile(new File("apache-ftpserver-1.1.1/res/ftpserver.jks"));
        sslFactory.setKeystorePassword("password");
        
        listenerFactory.setPort(2121);
        listenerFactory.setSslConfiguration(sslFactory.createSslConfiguration());
                     
        userFactory.setFile(new File("apache-ftpserver-1.1.1/res/conf/users.properties"));                
        
        serverFactory.addListener("default", listenerFactory.createListener());        
        serverFactory.setUserManager(userFactory.createUserManager());
        
        this.server = serverFactory.createServer();        
    }
    
    public void start(){
        try {
            server.start();
        } catch (FtpException ex) {
            ex.printStackTrace();
        }
    }
    
    public void stop(){
        server.stop();
    }
    
    public void suspend(){
        server.suspend();
    }
    
}
