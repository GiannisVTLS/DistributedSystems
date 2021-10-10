import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public class Message implements Serializable {
    private String ip;
    private int port;
    private String id;
    private String msg;
    private String artistName;
    private List<String> songnames;
    private byte[] songChunk;
    private List<String> s;
    private int numbers;
    private ArrayList<PublisherImpl> pub = new ArrayList<>();

    private List<String> availableArtists;

    public Message(String msg) {
        this.msg = msg;
    }

    public Message(int n) {
        this.numbers = n;
    }

    public Message(BrokerImpl b) {
        this.ip = b.ip;
        this.port = b.port;
        this.id = b.id;
    }

    public Message(ArrayList<PublisherImpl> pub) {
        this.pub = pub;
    }

    public Message(List<String> n) {
        this.s = n;
    }

    public Message(ArtistName n) {
        this.artistName = n.getArtistName();
        this.songnames = n.getSongNames();
    }

    public Message(int p, List<String> n,String id){
        this.port = p;
        this.availableArtists = n;
        this.id = id;
    }

    public List<String> getAvailableArtists() {
        return availableArtists;
    }

    public ArrayList<PublisherImpl> getPub() {
        return pub;
    }

    public List<String> getS() {
        return s;
    }

    public Message(byte[] songChunk,int n) {
        this.songChunk = songChunk;
        this.numbers = n;
    }

    public int getNumbers() {
        return numbers;
    }

    public byte[] getSongChunk() {
        return songChunk;
    }

    public String getArtistName() {
        return artistName;
    }

    public List<String> getSongnames() {
        return songnames;
    }

    public String getIP() {
        return ip;
    }
    public int getPort() {
        return port;
    }
    public String getID() {
        return id;
    }

    public String getMessage() {
        return msg;
    }
}