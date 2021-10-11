# Distributed Systems

Distributed application project for the couse of Distributed Systems at AUEB implementing a music streaming on demand service based on the publisher/ subscriber model.
 
<h3>Event Delivery System Breakdown</h3>

<h4>Publisher Node</h4>

This node is necessary to store, search and export all the music tracks that listeners will want to listen to. 
It is essentially our source from which we will read the data and each node will be responsible for a subset of artists. 

So this node, in an appropriate way and at an appropriate time, sends its data to the intermediate nodes.

This means that it should be able to serve multiple requests for different tracks of the same artist or even for different
artists, as is the case in a real system.

The basic function of Publisher Node, is to push data for the keys for which it is responsible, to the brokers responsible for servicing those keys.

If the data is information about the track it is send as a whole, the music file is transferred by small chunks and not as a whole.

<h4>Broker Node</h4>

The intermediate nodes, each responsible for servicing certain artists using MD5 hashing.

These nodes receive user requests which are then send to the appropriate Publisher Node, once the request is serviced by the Publisher the response is then send back to the user

A very important element of brokers is that the information which they send to the consumer nodes must be sent at the same time to all of them. For this reason, because the information must be transmitted simultaneously music to the listeners, multithreaded programming is used.
<h4>Consumer Node</h4>

This particular node is responsible for accepting the required information from the user. Based on the name of the artist the user provided, connection to the right broker is established to fullfill his request.

Two use cases are implemented:
<ol>
 <li>The user is always connected to a broker and the song can be streamed in real-time
 <li>The user is not always connected a broker and is given the option to download the song locally for offline use
</ol>

<h3>How To</h3>

<ol>
 <li>Clone the project to your preffered code editor (IntelliJ IDEA was used to produce and test the project)
 <li>Add songs with appropriate metadata on the "dataset2" folder, there is one song for testing purposes
 <li>Run "BrokerImpl" with Program Arguments the number "1"
 <li>Run "PublisherImpl" with Program Arguments the number "1"
 <li>Run "PublisherImpl" with Program Arguments the number "2"
 <li>Run "BrokerImpl" with Program Arguments the number "2"
 <li>Run "BrokerImpl" with Program Arguments the number "3"
 <li>Run "ConsumerImpl", multiple consumer nodes can be run at the same time.
 <li>Follow the instructions on the terminal to use the app as an actual user.
</ol>
