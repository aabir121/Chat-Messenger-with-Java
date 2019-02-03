package tcpserver;

import java.net.*;
import java.io.*;
import java.util.ArrayList;

public class TCPServer implements Runnable {

    private ChatServerThread clients[] = new ChatServerThread[50];
    private ServerSocket server = null;
    private Thread thread = null;
    private int clientCount = 0;
    public static ArrayList<String> names = new ArrayList<>();

    private ArrayList<ArrayList<String>> profiles = new ArrayList<>();
    private ArrayList<ArrayList<String>> clientInfo = new ArrayList<>();

    public TCPServer(int port) {
        try {
            System.out.println("Binding to port " + port + ", please wait  ...");
            server = new ServerSocket(port);
            System.out.println("Server started: " + server);

            names.add("abir");
            names.add("rakib");
            names.add("sakib");
            names.add("hasan");
            //generating friend List
            for (int i = 0; i < names.size(); i++) {
                ArrayList<String> friends = new ArrayList<>();
                friends.add(names.get(i));
                profiles.add(friends);
            }

            start();
        } catch (IOException ioe) {
            System.out.println("Can not bind to port " + port + ": " + ioe.getMessage());
        }
    }

    @Override
    public void run() {
        while (thread != null) {
            try {
                System.out.println("Waiting for a client ...");
                addThread(server.accept());
            } catch (IOException ioe) {
                System.out.println("Server accept error: " + ioe);
                stop();
            }
        }
    }

    public void start() {
        if (thread == null) {
            thread = new Thread(this);
            thread.start();
        }
    }

    public void stop() {
        if (thread != null) {
            thread.stop();
            thread = null;
        }
    }

    private int findClient(int ID) {
        for (int i = 0; i < clientCount; i++) {
            if (clients[i].getID() == ID) {
                return i;
            }
        }
        return -1;
    }

    public synchronized void handle(int ID, String input) {
        if (input.equals("exit")) {
            clients[findClient(ID)].send(".bye");
            remove(ID);
        } else if (input.endsWith("verify")) {

            if (input.startsWith("reg")) {
                System.out.println("registration");
                boolean valid = true;
                String[] parse = input.split(" ");
                for (int i = 0; i < names.size(); i++) {
                    if (names.get(i).equals(parse[1])) {
                        valid = false;
                    }
                }
                if (valid) {
                    names.add(parse[1]);
                    ArrayList<String> friends = new ArrayList<>();
                    friends.add(parse[1]);
                    profiles.add(friends);
                    clients[findClient(ID)].send("registered");
                    StringBuilder onlineClients = new StringBuilder("Online:\n");
                    ArrayList<String> newProfile = new ArrayList<>();
                    newProfile.add(parse[1]);
                    newProfile.add(findClient(ID) + "");
                    newProfile.add(ID + "");
                    clientInfo.add(newProfile);
                    for (int j = 0; j < clientInfo.size(); j++) {
                        onlineClients.append(clientInfo.get(j).get(0) + "\n");
                    }

                    for (int j = 0; j < clientCount; j++) {
                        clients[j].send(onlineClients.toString());
                        System.out.println(getClientNameByNumber(j) + ":" + onlineClients.toString());
                    }

                }

            } else {
                String[] parseStr = input.split(" ");
                for (int i = 0; i < names.size(); i++) {
                    if (parseStr[0].equals(names.get(i))) {

                        boolean alreadyOnline = false;

                        for (int j = 0; j < clientInfo.size(); j++) {
                            if (names.get(i).equals(clientInfo.get(j).get(0))) {
                                alreadyOnline = true;
                                System.out.println("logged in alrdy");
                                break;
                            }
                        }
                        if (!alreadyOnline) {
                            System.out.println("allow login");
                            StringBuilder onlineClients = new StringBuilder("Online:\n");
                            ArrayList<String> newProfile = new ArrayList<>();
                            newProfile.add(parseStr[0]);
                            newProfile.add(findClient(ID) + "");
                            newProfile.add(ID + "");
                            clientInfo.add(newProfile);
                            for (int j = 0; j < clientInfo.size(); j++) {
                                onlineClients.append(clientInfo.get(j).get(0) + "\n");
                            }
                            clients[findClient(ID)].send("verified " + parseStr[0]);

                            for (int j = 0; j < clientCount; j++) {
                                clients[j].send(onlineClients.toString());
                                System.out.println(getClientNameByNumber(j) + ":" + onlineClients.toString());
                            }

                            clients[findClient(ID)].send(readOfflineData(names.get(i)));

                        } else {
                            clients[findClient(ID)].send("User already Logged in.");
                        }
                        break;
                    }
                }
            }

        } else if (input.equals("online")) {
            StringBuilder onlineClients = new StringBuilder("Online:");
            for (int j = 0; j < clientInfo.size(); j++) {
                onlineClients.append(clientInfo.get(j).get(0) + "\t");
            }
            clients[findClient(ID)].send(onlineClients.toString());

        } else if (input.startsWith("friend")) {
            String[] parse = input.split(" ");
            clients[getClientNumberByName(parse[1])].send(getClientNameByID(ID) + " wants to add you as friend");
        } else if (input.startsWith("accept")) {
            String[] parse = input.split(" ");
            for (int i = 0; i < profiles.size(); i++) {
                if (profiles.get(i).get(0).equals(parse[1])) {
                    profiles.get(i).add(getClientNameByID(ID));
                    clients[getClientNumberByName(parse[1])].send(getClientNameByID(ID) + " accepted your request");
                    break;
                }
            }
            for (int i = 0; i < profiles.size(); i++) {
                if (profiles.get(i).get(0).equals(getClientNameByID(ID))) {
                    profiles.get(i).add(parse[1]);
                }
            }
            for (int i = 0; i < profiles.size(); i++) {
                System.out.println("{" + profiles.get(i).get(0));
                for (int j = 1; j < profiles.get(i).size(); j++) {
                    System.out.print(profiles.get(i).get(j) + ",");
                }
                System.out.println("}");
            }
        } else if (input.equals("history")) {
            clients[findClient(ID)].send("Chat history: \n" + readHistoryData());
        } else if (input.startsWith("rejected")) {
            String[] parse = input.split(" ");
            clients[getClientNumberByName(parse[1])].send("Request rejected");
        } else if (input.startsWith("single")) {
            String[] parse = input.split(" ");
            StringBuilder msg = new StringBuilder("");
            for (int i = 2; i < parse.length; i++) {
                msg.append(" " + parse[i]);
            }
            if (checkFriend(getClientNameByID(ID), parse[1])) {
                if (checkOnline(parse[1])) {
                    historyWrite(getClientNameByID(ID), msg.toString());
                    clients[getClientNumberByName(parse[1])].send(getClientNameByID(ID) + ": " + msg.toString());
                } else {
                    offlineWrite(parse[1], getClientNameByID(ID), msg.toString());
                }
            } else {
                clients[findClient(ID)].send(parse[1] + " is not in your friendlist.");
            }
        } else if (input.startsWith("group")) {
            String[] parse = input.split(" ");
            String[] clnames = parse[1].split(",");
            StringBuilder msg = new StringBuilder("");
            for (int i = 2; i < parse.length; i++) {
                msg.append(" " + parse[i]);
            }

            for (int i = 0; i < clnames.length; i++) {
                if (checkFriend(getClientNameByID(ID), clnames[i])) {
                    historyWrite(getClientNameByID(ID), msg.toString());
                    clients[getClientNumberByName(clnames[i])].send(getClientNameByID(ID) + ": " + msg.toString());
                } else {
                    clients[findClient(ID)].send(clnames[i] + " is not in your friendlist.");
                }

            }
            //         clients[getClientNumberByName(parse[1])].send(getClientNameByNumber(findClient(ID)) + ": " + msg.toString());
        } //        else if (input.equals("exit")) {
        //            clients[findClient(ID)].send(input);
        //        } 
        else {
            historyWrite(getClientNameByID(ID), input);

            for (int i = 0; i < clientCount; i++) {
                clients[i].send(getClientNameByID(ID) + ": " + input);

            }
        }
    }

    public synchronized void remove(int ID) {
        int pos = findClient(ID);
        for (int i = 0; i < clientInfo.size(); i++) {
            if (clientInfo.get(i).get(0).equals(getClientNameByNumber(pos))) {
                clientInfo.remove(i);
            }
        }
        StringBuilder onlineClients = new StringBuilder("Online:\n");
        for (int j = 0; j < clientInfo.size(); j++) {
            onlineClients.append(clientInfo.get(j).get(0) + "\n");
        }
        System.out.println(onlineClients.toString());
        System.out.println("Profiles");
        printPofiles();

        for (int j = 0; j < clientCount; j++) {
            clients[j].send(onlineClients.toString());
            System.out.println(getClientNameByNumber(j) + ":" + onlineClients.toString());
        }
        for (int j = 0; j < clientCount; j++) {
            clients[j].send(onlineClients.toString());
            System.out.println(getClientNameByNumber(j) + ":" + onlineClients.toString());
        }

        if (pos >= 0) {
            ChatServerThread toTerminate = clients[pos];
            System.out.println("Removing client thread " + ID + " at " + pos);
            if (pos < clientCount - 1) {
                for (int i = pos + 1; i < clientCount; i++) {
                    clients[i - 1] = clients[i];
                }
            }
            clientCount--;
            try {
                toTerminate.close();
            } catch (IOException ioe) {
                System.out.println("Error closing thread: " + ioe);
            }
            toTerminate.stop();
        }
    }

    private void addThread(Socket socket) {
        if (clientCount < clients.length) {
            System.out.println("Client accepted: " + socket);
            clients[clientCount] = new ChatServerThread(this, socket);
            try {
                clients[clientCount].open();
                clients[clientCount].start();
                clientCount++;

            } catch (IOException ioe) {
                System.out.println("Error opening thread: " + ioe);
            }

        } else {
            System.out.println("Client refused: maximum " + clients.length + " reached.");
        }
    }

    public int getClientNumberByName(String name) {
        int clNumber = -1;
        System.out.println(name);
        for (int i = 0; i < clientInfo.size(); i++) {

            if (clientInfo.get(i).get(0).equals(name)) {
                clNumber = Integer.parseInt(clientInfo.get(i).get(1));

            }

        }
        return clNumber;
    }

    public String getClientNameByNumber(int num) {
        String name = "";
        for (int i = 0; i < clientInfo.size(); i++) {

            if (Integer.parseInt(clientInfo.get(i).get(1)) == num) {
                name = clientInfo.get(i).get(0);
            }

        }
        return name;
    }

    public String getClientNameByID(int ID) {
        String name = "";
        for (int i = 0; i < clientInfo.size(); i++) {

            if (Integer.parseInt(clientInfo.get(i).get(2)) == ID) {
                name = clientInfo.get(i).get(0);
            }

        }
        return name;
    }

    public boolean checkFriend(String n1, String n2) {
        System.out.println("Checking " + n1 + "& " + n2);
        printPofiles();
        for (int i = 0; i < profiles.size(); i++) {
            if (profiles.get(i).get(0).equals(n1)) {
                for (int j = 1; j < profiles.get(i).size(); j++) {
                    if (profiles.get(i).get(j).equals(n2)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean checkOnline(String user) {
        boolean existUser = false;
        boolean online = false;

        for (int i = 0; i < names.size(); i++) {
            if (names.get(i).equals(user)) {
                existUser = true;
            }
        }

        for (int i = 0; i < clientInfo.size(); i++) {
            if (clientInfo.get(i).get(0).equals(user)) {
                online = true;
                break;
            }
        }

        if (existUser && online) {
            return true;
        } else {
            return false;
        }
    }

    public void offlineWrite(String user, String sender, String data) {
        StringBuilder prev = new StringBuilder(readOfflineData());
        try {
            PrintWriter writer = new PrintWriter("offline.txt", "UTF-8");
            prev.append(user + " " + sender + ":" + data);
            writer.println(prev.toString());
            writer.close();
        } catch (IOException e) {
            System.out.println(e.toString());
        }
    }

    public String readHistoryData() {
        String file = "history.txt";
        StringBuilder data = new StringBuilder("");

        try {

            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String line;
            while ((line = br.readLine()) != null) {
                data.append(line+"\n");
            }

            br.close();

        } catch (IOException e) {
            System.out.println("ERROR: unable to read file " + file);
            e.printStackTrace();
        }
        return data.toString();
    }

    public void historyWrite(String sender, String data) {
        StringBuilder prev = new StringBuilder(readHistoryData());
        try {
            PrintWriter writer = new PrintWriter("history.txt", "UTF-8");
            prev.append(sender + ":" + data);
            writer.println(prev.toString());
            writer.close();
        } catch (IOException e) {
            System.out.println(e.toString());
        }
    }

    public void printPofiles() {
        for (int i = 0; i < profiles.size(); i++) {
            for (int j = 0; j < profiles.get(i).size(); j++) {
                System.out.print(profiles.get(i).get(j));
            }
            System.out.println();
        }
    }

    public String readOfflineData(String user) {
        String file = "offline.txt";
        StringBuilder data = new StringBuilder("");

        try {

            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith(user)) {
                    String[] parse = line.split(" ");
                    for (int i = 1; i < parse.length; i++) {
                        data.append(parse[i] + " ");
                    }
                    data.append("\n");
                }
            }
            br.close();

        } catch (IOException e) {
            System.out.println("ERROR: unable to read file " + file);
            e.printStackTrace();
        }
        return data.toString();
    }

    public String readOfflineData() {
        String file = "offline.txt";
        StringBuilder data = new StringBuilder("");

        try {

            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String line;
            while ((line = br.readLine()) != null) {
                data.append(line + "\n");
            }
            br.close();

        } catch (IOException e) {
            System.out.println("ERROR: unable to read file " + file);
            e.printStackTrace();
        }
        return data.toString();
    }

    

    public static void main(String args[]) {
        TCPServer server = null;
        server = new TCPServer(2000);
    }
}
