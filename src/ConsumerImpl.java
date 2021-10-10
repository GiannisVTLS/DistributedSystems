import com.mpatric.mp3agic.*;

import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static java.lang.System.exit;

public class ConsumerImpl{
    private static Socket requestSocket = null;
    private static ObjectOutputStream out = null;
    private static ObjectInputStream in = null;

    public static List<BrokerImpl> brokers = new ArrayList<>();

    private static int hashBroker2;
    private static int hashBroker3;

    public ConsumerImpl() {}

    /**Connects to Broker 1 and receives information about broker2 and Broker 3, then calls register()*/
    private static void init() {
        try {
            Message success;
            Message request;
            requestSocket = new Socket(getSystemIP(), 6000);
            out = new ObjectOutputStream(requestSocket.getOutputStream());
            in = new ObjectInputStream(requestSocket.getInputStream());

            for(int i=0; i<3; i++){
                request = (Message)in.readObject();
                brokers.add(new BrokerImpl(request.getIP(),request.getPort(),request.getID()));
                success = new Message("Broker: " +brokers.get(0).id + " received");
                out.writeObject(success);
            }

            hashBroker2 = ((Message)in.readObject()).getNumbers();
            hashBroker3 = ((Message)in.readObject()).getNumbers();

            System.out.println("Consumer connected to broker");
            disconnect(in, out);

            register();
        } catch (UnknownHostException unknownHost) {
            System.err.println("You are trying to connect to an unknown host!");
        } catch (IOException | ClassNotFoundException ioException) {
            ioException.printStackTrace();
        }
    }

    /**Asks the user for the artist name he wants, compares the hashes of the name and each of the brokers(IP+Port) hash, then corrects to the correct one*/
    public static void register() {
        Scanner scn = new Scanner(System.in);
        System.out.print("Enter Artist Name or type Exit: ");
        String tosend = scn.nextLine();
        if(tosend.equalsIgnoreCase("exit")){
            System.out.println("Goodbye!");
            exit(0);
        }

        BrokerImpl b2 = brokers.get(1);
        BrokerImpl b3 = brokers.get(2);

        //now we have to connect with the correct broker

        if (getMd5(tosend) < hashBroker2%100)
        {
            connect(b2, tosend);
        }else {
            connect(b3, tosend);
        }

    }

    /**Performs all the actions required to provide the user the song he wants*/
    public static void connect(BrokerImpl b, String artistName) {
        try{

            requestSocket = new Socket(getSystemIP(), b.port);
            out = new ObjectOutputStream(requestSocket.getOutputStream());
            in = new ObjectInputStream(requestSocket.getInputStream());

            Scanner scn = new Scanner(System.in);
            Message msg = new Message(artistName);
            out.writeObject(msg);

            ArrayList<PublisherImpl> pub;
            pub = ((Message)in.readObject()).getPub();
            System.out.println("Number of available artists: "+pub.size());

            String accepted = ((Message) in.readObject()).getMessage();
            msg = new Message("Artist availability received");
            out.writeObject(msg);

            if(accepted.equalsIgnoreCase("exists")) {
                System.out.println("Artist "+ accepted + "\nList of available songs: ");
                List<String> songNames;
                songNames = ((Message) in.readObject()).getSongnames();
                System.out.println(songNames);
                boolean flag = true;
                while(flag) {
                    System.out.print("Request a song: ");
                    String songRequest = scn.nextLine();
                    if(songNames.contains(songRequest)){
                        flag = false;
                        msg = new Message(songRequest);
                        out.writeObject(msg);                   //sends the name of the song the user requested
                        int size = ((Message) in.readObject()).getNumbers();
                        for (int i = 0; i<size; i++){
                            try (FileOutputStream stream = new FileOutputStream("musicDownloads\\" + songRequest + "_" + i + ".mp3")) {
                                Message info = ((Message) in.readObject());
                                stream.write(info.getSongChunk());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        MusicFile search = new MusicFile(new File("musicDownloads\\" + songRequest + "_0.mp3"));
                        for(int i = 0; i< size; i++){
                            try {
                                Mp3File mp3File = new Mp3File("musicDownloads\\" + songRequest + "_" + i + ".mp3");
                                ID3v2 id3v2Tag = new ID3v24Tag(); mp3File.setId3v2Tag(id3v2Tag);
                                id3v2Tag.setArtist(search.getArtistName());
                                id3v2Tag.setTitle(search.getTrackName());
                                id3v2Tag.setAlbum(search.getAlbumInfo());
                                mp3File.save("musicDownloads\\" + songRequest + "-" + i + ".mp3");
                            } catch (UnsupportedTagException | InvalidDataException | NotSupportedException e) {
                                e.printStackTrace();
                            }
                            File file = new File("musicDownloads\\" + songRequest + "_" + i + ".mp3");
                            file.delete();
                        }
                    }else{
                        System.out.println("Unknown song name. Please try again");
                    }
                }
            }else{
                for(int i=0; i<pub.size(); i++){
                    System.out.println(pub.get(i).availableArtists);
                }
            }
            register();
        } catch (UnknownHostException unknownHost) {
           System.err.println("You are trying to connect to an unknown host!");
        } catch (IOException | ClassNotFoundException ioException) {
            ioException.printStackTrace();
        }
    }

    /**Closes the connection*/
    private static void disconnect(ObjectInputStream in,ObjectOutputStream out) {
        try {
            requestSocket.close();
            in.close();
            out.close();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    public static void main(String[] args) {
        init();
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
            int md5Dec = Integer.parseInt(hashtext.substring(0, 5), 16)%100;
            return md5Dec;
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
