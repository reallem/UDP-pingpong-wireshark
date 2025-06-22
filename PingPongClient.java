import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class PingPongClient {
	
	
	 	private DatagramSocket client_socket;
	    private InetAddress serverAddress;
	    private int serverPort;
	    private static final int TIMEOUT_MS = 1000;
	    private static final int NUM_PINGS = 10;
	    
	    
	    public PingPongClient(String serverIP, int port) throws Exception {
	    	client_socket = new DatagramSocket();
	    	client_socket.setSoTimeout(TIMEOUT_MS);
	        serverAddress = InetAddress.getByName(serverIP);
	        serverPort = port;
	        
	        ArrayList<Double> rttTimes = new ArrayList<>();
	        int packetsLost = 0;

	        System.out.println("Pinging " + serverAddress.getHostAddress() + " with " + NUM_PINGS + " packets:\n");
	        
	        
	        for (int pingseq = 1; pingseq <= NUM_PINGS; pingseq++) {
	        	
	            try {
	                long startTime = System.nanoTime();//get current time when send PING
	                byte[] message = "PING".getBytes();
	                DatagramPacket Ping_packet = new DatagramPacket(message, message.length, serverAddress, serverPort);
	                client_socket.send(Ping_packet);//send PING request

	                //receive PONG
	                byte[] buffer = new byte[1024];
	                DatagramPacket response_PONG = new DatagramPacket(buffer, buffer.length);
	                client_socket.receive(response_PONG);

	                long endTime = System.nanoTime();///get current time after receive PONG
	                String received = new String(response_PONG.getData(), 0, response_PONG.getLength());

	                if (received.equals("PONG")) {
	                	  double rtt = (endTime - startTime) / 1000000.0; // Convert to milliseconds
	                    rttTimes.add(rtt);
	                    System.out.println("Reply from " + serverAddress.getHostAddress() + ": seq=" + pingseq + " time=" + String.format("%.2f", rtt) + "ms");
	                } else {
	                    packetsLost++;
	                    System.out.println("Reply from " + serverAddress.getHostAddress() + ": seq=" + pingseq + " Destination host unreachable");
	                }
	            } //end try
	            catch (SocketTimeoutException e) {
	                packetsLost++;
	                System.out.println("Reply from " + serverAddress.getHostAddress() + ": seq=" + pingseq + " Destination host unreachable");
	            } catch (Exception e) {
	                packetsLost++;
	                System.out.println("Error: " + e.getMessage());
	            }
	            
	        }//end for
	        
	        
	        if (!rttTimes.isEmpty()) {
	            double minRtt = rttTimes.get(0);
	            double maxRtt = rttTimes.get(0);
	            double sumRtt = 0.0;
	            
	            for (int i = 0; i < rttTimes.size(); i++) {
	                double rtt = rttTimes.get(i);
	                
	                if (rtt < minRtt)
	                	minRtt = rtt;
	                if (rtt > maxRtt) 
	                	maxRtt = rtt;
	                
	                sumRtt += rtt;
	            }
	            
	            double avgRtt = sumRtt / rttTimes.size();
	            double lossRatio = (packetsLost / (double) NUM_PINGS) * 100;

	            System.out.println("\n ***PING PONG UDP Application Statistics *** ");
	            System.out.println("  Packets: Sent = " + NUM_PINGS + ", Received = " + (NUM_PINGS - packetsLost) + ", Lost = " + packetsLost + " (" + String.format("%.1f", lossRatio) + "% loss)");
	            System.out.println("  Approximate round trip times in milli-seconds:");
	            System.out.println("  Minimum = " + String.format("%.2f", minRtt) + "ms, Maximum = " + String.format("%.2f", maxRtt) + "ms, Average = " + String.format("%.2f", avgRtt) + "ms");
	        } else {
	            System.out.println("\nNo successful pings received.");
	        }
	        
	        compareWithStandardPing(serverAddress.getHostAddress());
	        
	        client_socket.close();

	        
	        
	        
	    }//end constructor
	    
	    
	    private void compareWithStandardPing(String ip) {
	        System.out.println("\n **** System Standard Ping Statistics ****");
	        String os = System.getProperty("os.name").toLowerCase();
	        String pingCmd;

	        if (os.contains("win")) {
	            pingCmd = "ping -n 10 " + ip;
	        } else {
	            pingCmd = "ping -c 10 " + ip;
	        }

	        try {
	            Process process = Runtime.getRuntime().exec(pingCmd);
	            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
	            String line;
	           

	            while ((line = reader.readLine()) != null) {
	                line = line.trim();

	                // Print all lines once statistics start
	                if (line.startsWith("Packets:") || line.startsWith("Approximate round trip times")
	                    || line.startsWith("Minimum") || line.contains("packet loss")
	                    || line.contains("min/avg/max") ) {

	                    System.out.println(line);
	                    
	                }


	            }

	            reader.close();
	        } catch (IOException e) {
	            System.out.println("Unable to perform system ping: " + e.getMessage());
	        }
	    }//end compareWithStandardPing

	    
	    //////////////////////////////////////////////////////////////////////////////////////////////
	    
	    public static void main(String[] args) {
	        try {
	            String serverIP = "192.168.1.183";
	            int port = 9999;
	            PingPongClient client = new PingPongClient(serverIP, port);
	           
	        } catch (Exception e) {
	            System.err.println("Error: " + e.getMessage());
	        }
	    }
	    
	
	

}//end class
