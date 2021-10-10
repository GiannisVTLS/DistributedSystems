import java.net.*;
import java.io.*;
import java.util.List;

public class PublisherImpl implements Serializable {

    private BrokerImpl connectedBroker;
    public List<String> availableArtists;
    private int port;
    public String id;

    public PublisherImpl(int port,ArtistName n, String id) {
        this.port = port;
        this.availableArtists = n.getArtistNameList();
        this.id = id;
    }

    public PublisherImpl(int port, List<String> n, String id) {
        this.port = port;
        this.availableArtists = n;
        this.id = id;
    }

    public int getPort() {
        return port;
    }

    private void init() {
        System.out.println("Publisher: " + id + " connected.\nPort: " + port);
        try {
            Socket initConnect = new Socket(getSystemIP(), 6000);
            Message request;
            System.out.println("Connection with broker initialized\nBroker : " + initConnect);
            ObjectInputStream ois = new ObjectInputStream(initConnect.getInputStream());
            ObjectOutputStream oos = new ObjectOutputStream(initConnect.getOutputStream());

            request = new Message(port,availableArtists,id);
            oos.writeObject(request);
            Message received = (Message)ois.readObject();
            connectedBroker = new BrokerImpl(received.getIP(),received.getPort(),received.getID());

            initConnect.close();
            ois.close();
            oos.close();
            connect();

        }catch (IOException | ClassNotFoundException e) {
            System.err.println("P->B Connection Problem");
        }
        connect();
    }


    public void connect() {
        try {
            ServerSocket s = new ServerSocket(getPort());
            while(true) {
                Socket requestSocket;
                System.out.println("Waiting request from Broker");
                requestSocket = s.accept();
                ObjectOutputStream out = new ObjectOutputStream(requestSocket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(requestSocket.getInputStream());
                Thread t = new ActionsforBroker(requestSocket, in, out, availableArtists);
                t.start();
            }
        } catch (UnknownHostException unknownHost) {
            System.err.println("You are trying to connect to an unknown host!");
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }


    public static void main(String[] args) {
        if(args[0].equalsIgnoreCase("1")) {
            ArtistName available = new ArtistName('A','M');
            System.out.println(available.getArtistNameList().toString());
            PublisherImpl p = new PublisherImpl(6666, available, args[0]);
            p.init();
        }else {
            ArtistName available = new ArtistName('N','Z');
            System.out.println(available.getArtistNameList().toString());
            PublisherImpl p = new PublisherImpl(7777, available, args[0]);
            p.init();
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
}
