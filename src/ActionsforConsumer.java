import java.io.*;
import java.net.*;
import java.util.List;
import java.util.ArrayList;

class ActionsforConsumer extends Thread {

    private final ObjectInputStream dis;
    private final ObjectOutputStream dos;
    private final Socket s;
    private String requestedName;
    private BrokerImpl currentBroker;
    private ArrayList<PublisherImpl> available_pubs;

    // Constructor
    ActionsforConsumer(Socket s, ObjectInputStream dis, ObjectOutputStream dos, String ip, int port, String id, ArrayList<PublisherImpl> p) {
        this.s = s;
        this.dis = dis;
        this.dos = dos;
        currentBroker = new BrokerImpl(ip, port, id);
        available_pubs = p;
    }

    @Override
    public void run() {

        System.out.println("Thread started");
            try {

                String received = ((Message) dis.readObject()).getMessage();
                System.out.println(received);

                int received_port;
                if(available_pubs.get(0).availableArtists.contains(received)){
                    received_port = available_pubs.get(0).getPort();
                }else{
                    received_port = available_pubs.get(1).getPort();
                }

                requestedName = received;
                dos.writeObject(new Message(available_pubs));
                Message toreturn = new Message(currentBroker.notifyPublisher(requestedName,received_port));
                dos.writeObject(toreturn);                                                              //sends if the artist exists in our library
                System.out.println(((Message) dis.readObject()).getMessage());

                if (toreturn.getMessage().equalsIgnoreCase("exists")) {
                    dos.writeObject(new Message(currentBroker.pullArtist(requestedName, received_port)));
                    String requestedSong = ((Message) dis.readObject()).getMessage();                    //reads the name of the song the user requested

                    List<Message> song = currentBroker.pullSong(requestedSong,received_port);
                    dos.writeObject(new Message(song.size()));
                    for (int i = 0; i < song.size(); i++) {
                        dos.writeObject(song.get(i));
                    }
                }
            } catch (IOException | ClassNotFoundException ioException) {
                ioException.printStackTrace();
            }
        try {
            // closing resources
            this.s.close();
            this.dis.close();
            this.dos.close();
            System.out.println("Closed");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}