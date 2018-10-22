/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

/**
 *
 * @author ns_red
 */
public class FtpFile {
    private String name;
    private long size;
    private int type;   //1 is folder 0 is file

    public FtpFile(String name, long size, int type) {
        this.name = name;
        this.size = size;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public long getSize() {
        return size;
    }

    public int getType(){
        return type;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    public void setSize(long size) {
        this.size = size;
    }   
    
    public void setType(int type){
        this.type = type;
    }
}
