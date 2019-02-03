/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tcpclient;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.*;
import java.io.*;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class TCPclient implements Runnable {

    private JFrame window;
    private JTextArea textBox;
    private JButton btn;
    private JTextArea msgPanel;
    private String consoleMsg = "";
    private JTextArea onlineList;

    private Socket socket = null;
    private Thread thread = null;
    private DataInputStream console = null;
    private DataOutputStream streamOut = null;
    private ChatClientThread client = null;
    private String userName = null;
    private Scanner sc = new Scanner(System.in);
    private boolean init = false;

    public TCPclient(String serverName, int serverPort) {

        initComponents();
        //System.out.println("Establishing connection. Please wait ...");
        try {
            socket = new Socket(serverName, serverPort);
            System.out.println("Connected: " + socket);
            start();
        } catch (UnknownHostException uhe) {
            System.out.println("Host unknown: " + uhe.getMessage());
        } catch (IOException ioe) {
            System.out.println("Unexpected exception: " + ioe.getMessage());
        }
    }

    public void run() {
        while (thread != null) {

        }
    }

    public void handle(String msg) {
        if (msg.endsWith("exit")) {
            System.out.println("Good bye. Press RETURN to exit ...");
            msgPanel.setText(msgPanel.getText().concat("\n" + "Good bye. Press RETURN to exit ..."));

            stop();
        } else if (msg.contains("wants to add you as friend")) {
            System.out.println(msg);
            msgPanel.setText(msgPanel.getText().concat("\n" + msg + "\nType Yes to accept and No to reject."));

            System.out.println("Type Yes to accept and No to reject.");
            String[] parse = msg.split(" ");
            String response = "";
            try {
                while (consoleMsg.equals("")) {

                }
                response = consoleMsg;

                System.out.println(response + "it is");

            } catch (Exception ex) {
                System.out.println("Error: " + ex.toString());
            }

            try {
                if (response.equals("Yes") || response.equals("yes")) {
                    streamOut.writeUTF("accept " + parse[0]);
                    streamOut.flush();
                    msgPanel.setText(msgPanel.getText().concat("\n" + "Accepted " + parse[0]));

                    // System.out.println("Accepted "+parse[0]);
                } else {
                    streamOut.writeUTF("rejected " + parse[0]);
                    streamOut.flush();
                    msgPanel.setText(msgPanel.getText().concat("\n" + "Rejected " + parse[0]));
                    // System.out.println("rejected "+parse[0]);
                }

            } catch (IOException ioe) {
                System.out.println("Sending error: " + ioe.getMessage());
                stop();
            }
        } else if (msg.startsWith("Online:")) {
            onlineList.setText(msg);
        } else if (msg.equals("registered")) {
            init = true;
            msgPanel.setText(msgPanel.getText().concat("\n" + "Successfully Registered\n"));

        } else {
            try {
                String[] parseStr = msg.split(" ");
                if (parseStr[0].equals("verified")) {
                    init = true;
                    window.setTitle(parseStr[1]);
                    msgPanel.setText(msgPanel.getText().concat("\n" + "Successfully Logged In\n" + parseStr[1]));
                    System.out.println("Successfully Logged In\n" + parseStr[1]);
                } else {
                    System.out.println(msg);
                    msgPanel.setText(msgPanel.getText().concat("\n" + msg));

                }
            } catch (Exception e) {
                System.out.println(msg);
            }
        }

    }

    public void start() throws IOException {
        console = new DataInputStream(System.in);
        streamOut = new DataOutputStream(socket.getOutputStream());
        if (thread == null) {
            client = new ChatClientThread(this, socket);
            thread = new Thread(this);
            thread.start();
        }
    }

    public void stop() {
        if (thread != null) {
            thread.stop();
            thread = null;
        }
        try {
            if (console != null) {
                console.close();
            }
            if (streamOut != null) {
                streamOut.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException ioe) {
            System.out.println("Error closing ...");
        }
        client.close();
        client.stop();
    }

    public void initComponents() {
        window = new JFrame("Messenger");
        window.setSize(500, 500);
        window.setDefaultCloseOperation(window.EXIT_ON_CLOSE);
        window.setLayout(null);

        JPanel pnl = new JPanel();
        pnl.setBounds(0, 0, 500, 500);
        pnl.setLayout(null);

        textBox = new JTextArea("");

        textBox.setBounds(0, 400, 380, 50);
        


        msgPanel = new JTextArea();
        msgPanel.setEditable(false);
        msgPanel.setBackground(Color.lightGray);
       
        
        msgPanel.setText("Please Enter your User Name: ");

                 JScrollPane scroll = new JScrollPane (msgPanel);
                  scroll.setBounds(0, 0, 390, 340);
    scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
          scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        
        onlineList = new JTextArea();
        onlineList.setEditable(false);
        onlineList.setBackground(Color.GRAY);
        onlineList.setBounds(390, 0, 500, 340);
        onlineList.setCaretColor(Color.white);

        JButton logoutBtn = new JButton();
        logoutBtn.setText("Logout");
        logoutBtn.setBounds(380, 350, 100, 30);
        logoutBtn.setBackground(Color.red);

        logoutBtn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    streamOut.writeUTF("exit");
                    streamOut.flush();
                    window.dispose();
                } catch (IOException ex) {
                    Logger.getLogger(TCPclient.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        });

        JButton onlineBtn = new JButton();
        onlineBtn.setText("Online");
        onlineBtn.setBounds(10, 350, 70, 30);

        onlineBtn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    streamOut.writeUTF("online");
                    streamOut.flush();
                } catch (IOException ex) {
                    Logger.getLogger(TCPclient.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        JButton historyBtn = new JButton();
        historyBtn.setText("History");
        historyBtn.setBounds(90, 350, 80, 30);

        historyBtn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    streamOut.writeUTF("history");
                    streamOut.flush();
                } catch (IOException ex) {
                    Logger.getLogger(TCPclient.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        });
        
        JButton clearBtn=new JButton();
        clearBtn.setText("Clear");
        clearBtn.setBounds(190,350,80,30);
        clearBtn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                msgPanel.setText(null);
            }
        });

        btn = new JButton("Send");
        btn.setText("Send");
        btn.setBounds(390, 400, 80, 50);
        btn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                consoleMsg = textBox.getText().toString();
                System.out.println(consoleMsg);
                try {
                    if (!init) {

                //        System.out.println("Please Enter your User Name: ");
//                    userName = console.readLine();
                        streamOut.writeUTF(consoleMsg + " verify");
                        streamOut.flush();
                        consoleMsg = "";
                        //System.out.println("init "+init);
                    } else {

                        streamOut.writeUTF(consoleMsg);
                        streamOut.flush();
                        consoleMsg = "";
                    }

                } catch (IOException ioe) {
                    System.out.println("Sending error: " + ioe.getMessage());
                    stop();
                }
                textBox.setText("");

            }

        });
        
            pnl.add (scroll);

            pnl.add (onlineList);

            pnl.add (btn);

            pnl.add (textBox);
            
            pnl.add(clearBtn);

            pnl.add (onlineBtn);

            pnl.add (historyBtn);

            pnl.add (logoutBtn);

            window.add (pnl);

            window.show ();

        }

    public static void main(String args[]) {
        TCPclient client = null;
        client = new TCPclient("localhost", 2000);

    }

}
