import java.math.BigInteger;
import java.net.*;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class BrokerImpl implements Broker {

    public String ip;
    public int port;
    public String id;
    public ArrayList<PublisherImpl> publishersList = new ArrayList<>();

    public BrokerImpl(String ip,int port, String id) {
        this.ip = ip;
        this.port = port;
        this.id = id;
    }

    public static List<BrokerImpl> brokers = new ArrayList<>();

    @Override
    public void init() throws IOException{
        System.out.println("BrokerID: " + id + " connected.\nPort: " + port);
        /**This waits for publishers to connect and receive info about their artists and their port*/
        ServerSocket s = new ServerSocket(port);
        if(publishersList.isEmpty()) {

            Message success;
            Message request=null;

            Socket requestSocket;
            ObjectOutputStream out;
            ObjectInputStream in;
            for (int i = 0; i < 2; i++) {

                requestSocket = s.accept();
                out = new ObjectOutputStream(requestSocket.getOutputStream());
                in = new ObjectInputStream(requestSocket.getInputStream());

                try {
                    request = (Message) in.readObject();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }

                publishersList.add(new PublisherImpl(request.getPort(), request.getAvailableArtists(), request.getID()));
                System.out.println(publishersList.get(i).availableArtists.toString());
                success = new Message(brokers.get(i + 1));
                out.writeObject(success);

                requestSocket.close();
                in.close();
                out.close();
                System.out.println("publishersList.size() = " + publishersList.size());
            }

        }
        System.out.println(port);
        /**For runs to send a list of artist names and the port of their publisher to other brokers*/
        for(int i = 0; i<2; i++) {
            Socket initConnect = s.accept();
            ObjectOutputStream dos = new ObjectOutputStream(initConnect.getOutputStream());

            Message sendPubs = new Message(publishersList);
            dos.writeObject(sendPubs);
            initConnect.close();
            dos.close();
        }
        /**While runs to accept connections from consumers and send information about other brokers*/
        Socket initConnect;
        Message request;
        while(true) {
            initConnect = s.accept();
            System.out.println("A new client is connected : " + initConnect);
            ObjectInputStream dis = new ObjectInputStream(initConnect.getInputStream());
            ObjectOutputStream dos = new ObjectOutputStream(initConnect.getOutputStream());

                //ο broker1 στελνει IP,PORT,ID για καθε broker
            try {
                for(int i =0; i<3; i++) {
                    request = new Message(brokers.get(i));
                    dos.writeObject(request);
                    System.out.println(((Message) dis.readObject()).getMessage());
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

                //τα hash των IP+PORT για τον broker 2 και broker 3
            dos.writeObject(new Message(getMd5(brokers.get(1).ip+brokers.get(1).port)));
            dos.writeObject(new Message(getMd5(brokers.get(2).ip+brokers.get(2).port)));

            dis.close();
            dos.close();

            }
    }

    /**This runs only for broker2 and broker3 to accept the list of publishers from broker1*/
    public void init_2() {
        Socket requestSocket;
        ObjectInputStream dis;
        try {
            requestSocket = new Socket(getSystemIP(), 6000);
            dis = new ObjectInputStream(requestSocket.getInputStream());
            publishersList = new ArrayList<>();
            publishersList = ((Message) dis.readObject()).getPub();
            requestSocket.close();
            dis.close();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Problem accepting publisher information from broker 1");
        }
    }

    @Override
    public ConsumerImpl acceptConnection(ConsumerImpl c) throws IOException{
        ServerSocket server = new ServerSocket(port);
        System.out.println("BrokerID: " + id + " connected.\nPort: " + port);
        while(true) {
            Socket connection;
                connection = server.accept();
                System.out.println("A new request received from : " + connection);
                ObjectInputStream dis = new ObjectInputStream(connection.getInputStream());
                ObjectOutputStream dos = new ObjectOutputStream(connection.getOutputStream());
                System.out.println("Assigning new thread for this request");
                Thread t = new ActionsforConsumer(connection, dis, dos, ip, port,id, publishersList);
                t.start();
        }

    }

    @Override
    public String notifyPublisher(String requestedName,int port) {
        try {
            Socket initConnect;
            initConnect = new Socket(getSystemIP(), port);

            ObjectInputStream dis = new ObjectInputStream(initConnect.getInputStream());
            ObjectOutputStream dos = new ObjectOutputStream(initConnect.getOutputStream());
            dos.writeObject(new Message("returnartistexistance"));
            System.out.println(((Message)dis.readObject()).getMessage());
            dos.writeObject(new Message(requestedName));
            Message found = (Message) dis.readObject();
            initConnect.close();
            dis.close();
            dos.close();
            return found.getMessage();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public ArtistName pullArtist(String n, int port) {
        try {
            Socket initConnect;
            initConnect = new Socket(getSystemIP(), port);

            ObjectInputStream dis = new ObjectInputStream(initConnect.getInputStream());
            ObjectOutputStream dos = new ObjectOutputStream(initConnect.getOutputStream());
            dos.writeObject(new Message("returnartist"));
            System.out.println(((Message)dis.readObject()).getMessage());
            dos.writeObject(new Message(n));
            Message found = (Message) dis.readObject();
            initConnect.close();
            dis.close();
            dos.close();
            return new ArtistName(found.getArtistName(), found.getSongnames());
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Message> pullSong(String songName, int port) {
        try {
            Socket initConnect;
            initConnect = new Socket(getSystemIP(), port);

            ObjectInputStream dis = new ObjectInputStream(initConnect.getInputStream());
            ObjectOutputStream dos = new ObjectOutputStream(initConnect.getOutputStream());
            dos.writeObject(new Message("returnsongchunk"));
            System.out.println(((Message)dis.readObject()).getMessage());
            dos.writeObject(new Message(songName));
            List<Message> array = new ArrayList<>();
            Message toreturn = (Message) dis.readObject();
            array.add(0, toreturn);
            for(int i = 1; i<toreturn.getNumbers(); i++){
                array.add(i, (Message) dis.readObject());
            }
            initConnect.close();
            dis.close();
            dos.close();
            return array;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) throws IOException{
        if(args[0].equalsIgnoreCase("1")) {
            BrokerImpl broker1 = new BrokerImpl(getSystemIP(),6000, args[0]);
            brokers.add(broker1);
            brokers.add(new BrokerImpl(getSystemIP(),7000,"2"));
            brokers.add(new BrokerImpl(getSystemIP(),8000,"3"));
            broker1.init();
        }else if(args[0].equalsIgnoreCase("2")){
            BrokerImpl broker2 = new BrokerImpl(getSystemIP(),7000, args[0]);
            ConsumerImpl c = new ConsumerImpl();
            broker2.init_2();
            broker2.acceptConnection(c);
        }else if(args[0].equalsIgnoreCase("3")){
            BrokerImpl broker3 = new BrokerImpl(getSystemIP(),8000, args[0]);
            ConsumerImpl c = new ConsumerImpl();
            broker3.init_2();
            broker3.acceptConnection(c);
        }
    }

    private static String getSystemIP()
    {
        String current_ip = null;
        try(final DatagramSocket socket = new DatagramSocket())
        {
            socket.connect(InetAddress.getByName("1.1.1.1"), 10002);
            current_ip = socket.getLocalAddress().getHostAddress();
        }
        catch (SocketException | UnknownHostException e)
        {
            e.printStackTrace();
        }
        return current_ip;
    }

    public static int getMd5(String input)
    {
        try
        {
            MessageDigest md = MessageDigest.getInstance("MD5");

            byte[] messageDigest = md.digest(input.getBytes());
            BigInteger no = new BigInteger(1, messageDigest);

            String hashtext = no.toString(16);
            while (hashtext.length() < 32)
            {
                hashtext = "0" + hashtext;
            }
            int md5Dec = Integer.parseInt(hashtext.substring(0, 5), 16);
            return md5Dec;
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

}


