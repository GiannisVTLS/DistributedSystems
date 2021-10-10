import java.io.IOException;
import java.util.List;

public interface Broker {

    /**Initializes Broker1 that gets info from the publishers, then accepts connection from the consumer*/
    void init() throws IOException;

    /***/
    ConsumerImpl acceptConnection(ConsumerImpl c) throws IOException;

    /**Returns to thread a message about an artists existance in our database*/
    String notifyPublisher(String requestedName,int port);

    /**Returns to thread the name of the artist and a list of his songs*/
    ArtistName pullArtist(String n, int port);

    /**Returns to thread the chunks of the song*/
    List<Message> pullSong(String songName, int port);

}

/**The thread for the brokers is ActionsforConsumer.java*/
