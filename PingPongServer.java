import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;



public class PingPongServer {
	
	 private DatagramSocket socket; // Declare a DatagramSocket for sending and receiving UDP packets from clients
	 private double errorProbability; // To store the probability of dropping PONGs (e.g., 0.3,0.5,0.8)
	 private List<ClientPingBatchState> clientStates; // Declare a List to store ClientBatchState objects for all clients
	 
	 
	  public PingPongServer(int port, double errorProb) throws SocketException {
		  
		  socket = new DatagramSocket(port); // Create a DatagramSocket bound to the specified port for UDP communication
		  errorProbability = errorProb;
		  clientStates =  new ArrayList<>(); // To store client Ping batch states
		  System.out.println("Server started on port " + port + " with error probability " + errorProb); 
		  
	        try {
	            while (true) { // Enter an infinite loop to continuously process PING packets from Clients
	            	
	                byte[] buffer = new byte[1024]; // Create a byte array to store incoming PING packet data
	                DatagramPacket packet = new DatagramPacket(buffer, buffer.length); // Create a DatagramPacket to receive data into the buffer
	                socket.receive(packet); // Wait for and receive an incoming UDP packet

	                // Create a new thread to process the packet
	                Thread packetThread = new Thread(() -> processPacket(packet));
	                packetThread.start(); // Start the thread to handle the packet
	                
	              //processPacket(packet); // Process the received PING packet
	                
	            }//end while for infinity
	        }//end try
	        catch (IOException e) { 
	            System.err.println("Server error: " + e.getMessage()); // Print the exception details
	        }
	        
	        finally { // Ensure the socket is closed when the server stops
	            socket.close(); // Close the DatagramSocket to release resources
	        }
		  
		  
		  
	  }//end PingPongServer Constructor
	  
//////////////////////////////////Inner Class ClientPingBatchState//////////////////////////////////////////////////////////////

	    // A class that encapsulates the state for a single client’s batch of 10 PINGs
	    private class ClientPingBatchState {
	    	
	        private String clientKey; //Store the client’s IP:port identifier (e.g., "127.0.0.1:12345")
	        private int pingCounter; // Track the number of PINGs received from the client
	        private List<Integer> dropIndices; // Declare a List to store indices (0-9) where PONGs should be dropped

	        public ClientPingBatchState(String clientKey) {
	            this.clientKey = clientKey; // Set the clientKey to the provided IP:port identifier
	            pingCounter = 0; // Initialize pingCounter to 0 for the new PING batch
	            dropIndices = new ArrayList<>(); // An empty ArrayList to store drop positions
	            initializeDropIndices(); // Call initializeDropIndices to set up drop positions for the batch
	        }//end constructor

	        private void initializeDropIndices() { // Method to set up random drop positions for a batch
	            dropIndices.clear(); // Clear the dropIndices list to prepare for a new batch
	            
	            int numToDrop = (int) (errorProbability * 10); // Calculate the number of PONGs to drop (e.g., 3 for errorProbability = 0.3)
	            List<Integer> indices = new ArrayList<>(); // Create a List to store indices 0-9 for shuffling
	            
	            for (int i = 0; i < 10; i++) { // Loop to populate indices with numbers 0 to 9
	            	indices.add(i); // Add the current index to the indices list
	            }
	            
	            Collections.shuffle(indices, new Random()); // Shuffle the indices list randomly to select drop positions
	            for (int i = 0; i < numToDrop; i++) { // Loop to select the first numToDrop indices as drop positions
	                dropIndices.add(indices.get(i)); // Add the shuffled index to dropIndices
	            }
	          
	            System.out.println("Server Will drop PINGs at positions " + dropIndices + "for Client: "+ clientKey); 
	        }//end initializeDropIndices
	        

	        public boolean shouldDrop(int positionInPingRequests) { // Method to check if a PONG should be dropped at the given batch position
	            return dropIndices.contains(positionInPingRequests); // Return true if positionInBatch is in dropIndices, false otherwise
	        }

	        public int getAndIncrementCounter() { // Method to get and increment the pingCounter
	            return pingCounter++; // Return the current pingCounter value and increment it
	        }

	        public void resetPingBatch() { // Method to reset the batch state for a new set of 10 PINGs
	            pingCounter = 0; // Reset pingCounter to 0 --> first ping request in the batch
	            initializeDropIndices(); // Reinitialize dropIndices with new random drop positions
	        }

	        public String getClientKey() { // Method to get the client’s IP:port identifier
	            return clientKey; // Return the clientKey field
	        }
	    }
	    
	    
	  
	  /////////////////////////////////////////////////////////////////////////////
	 
	  private void processPacket(DatagramPacket packet) {
		  
		  try {
			  
			  String received_ping = new String(packet.getData(), 0, packet.getLength()); // Convert the packet’s data to a String
			  if (received_ping.equalsIgnoreCase("PING")) { 
				  			  
	                InetAddress clientAddress = packet.getAddress(); // Get the client’s IP address from the packet
	                int clientPort = packet.getPort(); // Get the client’s port number from the packet
	                
	                String clientKey = clientAddress.getHostAddress() + ":" + clientPort; // Create a clientKey by combining IP and port
	                System.out.println("Received PING from " + clientKey);
	                
	             // Find or create client state
	                ClientPingBatchState clientState = null;
	                
	                for (int i = 0; i < clientStates.size(); i++) { 
	                	ClientPingBatchState state = clientStates.get(i); // Get the ClientPingBatchState at the current index
	                    if (state.getClientKey().equals(clientKey)) { 
	                        clientState = state; // Assign the matching state to clientState
	                        break; // Exit the loop since the matching state was found
	                    }
	                }//end for
	                
	                if (clientState == null) { // Check if no matching state was found
	                    clientState = new ClientPingBatchState(clientKey); // Create a new ClientBatchState for the client
	                    clientStates.add(clientState); // Add the new state to clientStates
	                }//end if
	                
	                int currentClient_PingCount = clientState.getAndIncrementCounter(); // Get and increment the client’s pingCounter
	                int positionInBatch = currentClient_PingCount % 10; // Calculate the position in the batch (0-9) using modulo

	                boolean shouldDrop = clientState.shouldDrop(positionInBatch); // Check if the PONG should be dropped at this position

	                if (positionInBatch == 9) { // Check if this is the last PING in the batch (position 9)
	                    clientState.resetPingBatch(); // Reset the batch state for the next set of 10 PINGs
	                }
	                
	                
	                if (!shouldDrop) { // Check if the PONG should Sent Successfully
	                    byte[] response = "PONG".getBytes(); // Create a byte array for the "PONG" response
	                    // Create a DatagramPacket for the PONG response
	                    DatagramPacket responsePacket = new DatagramPacket( response, response.length, clientAddress, clientPort); 
	                    socket.send(responsePacket); // Send the PONG packet back to the client
	                    System.out.println("Sent PONG to " + clientKey); // Log that a PONG was sent to the client
	                } else { // If the PONG should be dropped
	                    System.out.println("Dropped PONG for " + clientKey + " (position " + positionInBatch + ")"); 
	                }
	                
	                

			  }//end if
			  
			  
			  
			  
		  }//end try
		  catch (IOException e) {
	            System.err.println("Packet handling error: " + e.getMessage()); 
	        }
		  
	  }//end processPacket
	 

////////////////////////////////////////////////////////////////////////////////////////////////
	  
public static void main(String[] args) {
	
try {
		int port=9999; // Declare port variable to store the server port number
		double errorProb = 0.3;
		
		PingPongServer server = new PingPongServer(port, errorProb);

} catch (Exception e) {
	System.err.println("Error: " + e.getMessage());
}
}//end main
	  
	  
}//end class
