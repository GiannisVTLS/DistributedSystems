import java.io.*;
import java.net.*;
import java.util.List;
import java.util.ArrayList;

class ActionsforBroker extends Thread {

    private final ObjectInputStream in;
    private final ObjectOutputStream out;
    private final Socket s;
    private List<String> availableArtists;

    // Constructor
    ActionsforBroker(Socket s, ObjectInputStream dis, ObjectOutputStream dos, List<String> p) {
        this.s = s;
        this.in = dis;
        this.out = dos;
        availableArtists = p;

    }

    @Override
    public void run() {

        System.out.println("Thread started");
        try {
            String whattodo = ((Message) in.readObject()).getMessage();
            out.writeObject(new Message("Request Received"));
            if(whattodo.equalsIgnoreCase("returnartist")) {
                String receivedArtist = ((Message) in.readObject()).getMessage();
                if (availableArtists.contains(receivedArtist)) {
                    ArtistName n = new ArtistName(receivedArtist);
                    out.writeObject(new Message(n));
                }
            }else if(whattodo.equalsIgnoreCase("returnartistexistance")){
                String receivedArtist = ((Message) in.readObject()).getMessage();
                if (availableArtists.contains(receivedArtist)) {
                    out.writeObject(new Message("exists"));
                }else{
                    out.writeObject(new Message("doesntexist"));
                }
            }else{
                String requestedSong = ((Message) in.readObject()).getMessage();
                System.out.println(requestedSong);
                MusicFile toreturn = new MusicFile(requestedSong, 0);
                out.writeObject(new Message(toreturn.getChunk(),toreturn.getChunkArray().length));
                for(int i=1; i<toreturn.getChunkArray().length; i++){
                    byte[] array= toreturn.getSong(i);
                    out.writeObject(new Message(array,i));
                }
            }
        } catch (IOException | ClassNotFoundException ioException) {
            ioException.printStackTrace();
        }
        try {
            // closing resources
            this.s.close();
            this.in.close();
            this.out.close();
            System.out.println("Closed");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}