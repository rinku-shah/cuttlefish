package net.floodlightcontroller.kv;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

import javax.swing.text.html.HTMLDocument.Iterator;
public class InterControllerClient {


    static int BUF_SZ = 4096;
    private static SelectionKey clientKey;
    private DatagramChannel chan;

    
    public InterControllerClient(String serverIP, int serverPort) throws IOException { 
		String IP = serverIP;
		int port = serverPort;
  
            Selector selector = Selector.open();
            
            DatagramChannel channel = DatagramChannel.open();
            InetSocketAddress isa = new InetSocketAddress(serverIP,port);
         
            channel.configureBlocking(false);
            clientKey = channel.register(selector, SelectionKey.OP_READ);
            chan = (DatagramChannel)clientKey.channel();
           
            channel.socket().connect(isa);
           
          
        
    }

    public void clientSend(String msg){
        
    	String s = msg;
		byte[] message = new String(s).getBytes();

		ByteBuffer buffer = ByteBuffer.wrap(message);
	
		try {
			chan.write(buffer);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//System.out.println("sending " + message);
		
		buffer.clear();
    }

   

}
