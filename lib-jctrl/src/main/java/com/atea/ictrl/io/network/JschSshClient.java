/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.atea.ictrl.io.network;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.atea.ictrl.io.DeviceConnection;

/**
 *
 * @author Martin
 */
public class JschSshClient extends DeviceConnection {
    private JSch jsch = new JSch();        
    public String host, user, password;
    private Session session;
    private Channel channel;
    public JschSshClient(String host, String login, String password) {
        this.host = host;
        this.user = login;
        this.password = password;
    }
    public JschSshClient(String host, String login) {
        this.host = host;
        this.user = login;        
    }
    @Override
    public void connectImpl() throws Exception {        
        session = jsch.getSession(user, host, 22);        
        session.setConfig("StrictHostKeyChecking", "no");
        if(password == null) {
            session.setConfig("PreferredAuthentications", "publickey");
        } else {
            session.setPassword(password);
        }
        session.connect(25000);        
        channel = session.openChannel("shell");
        rawRx = channel.getInputStream();
        rawTx = channel.getOutputStream();
        channel.connect(7000);
    }

    @Override
    public void disconnectImpl() throws Exception {
        if(session != null) {
            session.disconnect();
            session = null;
        }
        if(channel != null) {
            channel.disconnect();
            channel = null;
        }
    }

    
    
}
