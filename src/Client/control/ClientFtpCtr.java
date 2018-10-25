/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client.control;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import model.FtpFile;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

/**
 *
 * @author ns_red
 */
public class ClientFtpCtr {
    private static File config;
    private Properties prop;
    private String userHomeDirectory;
    private FTPClient ftpClient;
    private int replyCode;
    
    private FtpFile ftpFile;
    
    static {
        config = new File("ftpConfig.properties");
    }
    
    public ClientFtpCtr(){
        try {
            FileInputStream fis = new FileInputStream(config);
            prop = new Properties();
            prop.load(fis);
            
        } catch (IOException ex) {
            System.out.println("no such a file");
        }
    }

    public boolean connect(){
        ftpClient = new FTPClient();                          
        String serverIp = prop.getProperty("serverIp");
        byte[] ip = ipParse(serverIp);
        try {
            InetAddress address = InetAddress.getByAddress(ip);
            int port = Integer.parseInt(prop.getProperty("port"));
            ftpClient.connect(address, port);                                  
            replyCode = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(replyCode)){
                System.out.println("cant connect to server");
                return false;
            }
                        
            boolean loginFlag = ftpClient.login(prop.getProperty("user.username"), prop.getProperty("user.password"));
            if (!loginFlag){
                System.out.println("acess denied");
            }
            System.out.println(ftpClient.getStatus());
        } catch (Exception ex) {
            ex.printStackTrace();
        }                
        return true;
    }
    
    public void uploadFile(File fileUpload){
        try {
            ftpClient.storeFile(fileUpload.getName(), new FileInputStream(fileUpload));
            replyCode = ftpClient.getReplyCode();            
                
        } catch (IOException ex) {
            System.out.println("fail on uploadFile");
        }
    }
    
    public ArrayList<FtpFile> listFile(String folder){
        ArrayList<FtpFile> list = new ArrayList<>();
        
        try {            
            boolean flagCd = ftpClient.changeWorkingDirectory(folder);
            //System.out.println("Cd operation is " + flagCd);
            FTPFile[] files = ftpClient.listFiles();
            for (int i = 0; i < files.length; i++)
                list.add(new FtpFile(files[i].getName(), files[i].getSize(), files[i].getType()));                                              
        } catch (IOException ex) {
            ex.printStackTrace();            
        }
        
        return list;
    }
       
    public int makeChanelFolder(String name){
        int code = 550;
        try {
            code = ftpClient.mkd(name);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return code;
    }
    
    public int deleteChanelFolder(String name){
        int code = 550;
        try{
            code = ftpClient.dele(name);
        } catch (IOException ex){
            ex.printStackTrace();                    
        }
        return code;
    }
    
    public boolean downloadFile(String fileName){
        File downloadFile = new File(userHomeDirectory + "\\" +fileName);        
        boolean flag = false;
        try {
            OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(downloadFile));            
            flag = ftpClient.retrieveFile(fileName, outputStream);
            outputStream.close();            
        } catch (IOException ex) {
            System.out.println("File not found");
        }  
        return flag;
    }
    
    public byte[] ipParse(String serverIp){
        String[] temp = serverIp.split("\\.");
        byte[] ip = {0,0,0,0};
        ip[0] = (byte) Integer.parseInt(temp[0]);
        ip[1] = (byte) Integer.parseInt(temp[1]);
        ip[2] = (byte) Integer.parseInt(temp[2]);
        ip[3] = (byte) Integer.parseInt(temp[3]);
        
        return ip;
    }
    
    public int getReplyCode(){
        return replyCode;
    }
    
    public String getReplyString(){
        return ftpClient.getReplyString();
    }
   
    public void setUserHomeDirectory(String userHomeDirectory){
        this.userHomeDirectory = userHomeDirectory;
    }
}
