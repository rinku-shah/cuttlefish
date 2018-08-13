package net.floodlightcontroller.sdnepcCuttlefish;
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
/*    class Con {
        ByteBuffer req;
        ByteBuffer resp;
        SocketAddress sa;

        public Con() {
            req = ByteBuffer.allocate(BUF_SZ);
        }
    }
*/
    //static int port = 8340;
    
    public InterControllerClient(String serverIP, int serverPort) throws IOException { //,String str) {
		String IP = serverIP;
		int port = serverPort;
  
            Selector selector = Selector.open();
            
            DatagramChannel channel = DatagramChannel.open();
            InetSocketAddress isa = new InetSocketAddress(serverIP,port);
          //  InetAddress address = new InetAddress("127.0.0.1");
            //channel.socket().bind(isa);
            channel.configureBlocking(false);
            clientKey = channel.register(selector, SelectionKey.OP_READ);
            chan = (DatagramChannel)clientKey.channel();
            //clientKey.attach(new Con());
            channel.socket().connect(isa);
           
           /* while (true) {
                try {
                    selector.select();
                    java.util.Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();
                    while (selectedKeys.hasNext()) {
                        try {
                            SelectionKey key = (SelectionKey) selectedKeys.next();
                            selectedKeys.remove();

                            if (!key.isValid()) {
                              continue;
                            }

                            if (key.isReadable()) {
                                read(key);
                                key.interestOps(SelectionKey.OP_WRITE);
                            } else if (key.isWritable()) {
                                write(key);
                                key.interestOps(SelectionKey.OP_READ);
                            }
                        } catch (IOException e) {
                            System.err.println("glitch, continuing... " +(e.getMessage()!=null?e.getMessage():""));
                        }
                    }
                } catch (IOException e) {
                    System.err.println("glitch, continuing... " +(e.getMessage()!=null?e.getMessage():""));
                }
            }*/
            
            
            
           /* while (true) {
				
				//log("i'm a server and i'm waiting for new connection and buffer select...");
				// Selects a set of keys whose corresponding channels are ready for I/O operations
				selector.select();
	 
				// token representing the registration of a SelectableChannel with a Selector
				java.util.Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();
											 
				while (selectedKeys.hasNext()) {
					SelectionKey myKey = selectedKeys.next();
	 
					// Tests whether this key's channel is ready to accept a new socket connection
					if (myKey.isAcceptable()) {
						
						//SocketChannel Client = serverSocket.accept();
	 
						// Adjusts this channel's blocking mode to false
						channel.configureBlocking(false);
	 
						// Operation-set bit for read operations
						channel.register(selector, SelectionKey.OP_READ);
						//log("Connection Accepted: " + Client.getLocalAddress() + "\n");
	 
						// Tests whether this key's channel is ready for reading
					} else if (myKey.isReadable()) {
						
						SocketChannel Client = (SocketChannel) myKey.channel();
						ByteBuffer Buffer = ByteBuffer.allocate(256);
						
						Client.read(Buffer);
						
						String result = new String(Buffer.array()).trim();
						
						System.out.println("Message received: " + result);
						
						
						
					}
					selectedKeys.remove();
				}
			}
            
            
*/	       
        
    }

  /*  private void read(SelectionKey key) throws IOException {
        DatagramChannel chan = (DatagramChannel)key.channel();
        Con con = (Con)key.attachment();
        con.sa = chan.receive(con.req);
        System.out.println(new String(con.req.array(), "UTF-8"));
        con.resp = Charset.forName( "UTF-8" ).newEncoder().encode(CharBuffer.wrap("send the same string"));
    }*/

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
