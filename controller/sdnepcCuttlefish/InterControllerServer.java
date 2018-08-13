package net.floodlightcontroller.sdnepcCuttlefish;
import java.io.IOException;
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



public class InterControllerServer extends Thread implements Runnable{
    static int BUF_SZ = 4096;
    int sport;
    OnlineThread m;
    
    class Con {
        ByteBuffer req;
        ByteBuffer resp;
        SocketAddress sa;

        public Con() {
            req = ByteBuffer.allocate(BUF_SZ);
        }
    }
    public InterControllerServer(OnlineThread mme, int port) throws IOException {
	     
	  sport=port;
	  m=mme;

	         Thread t = new Thread(this);
	         t.start();
	
  
  }
  
    public void run(){
        try {
            Selector selector = Selector.open();
            DatagramChannel channel = DatagramChannel.open();
            InetSocketAddress isa = new InetSocketAddress(sport);
            channel.socket().bind(isa);
            channel.configureBlocking(false);
            SelectionKey clientKey = channel.register(selector, SelectionKey.OP_READ);
            clientKey.attach(new Con());
            while (true) {
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
                            	 //read(key);
                                //key.interestOps(SelectionKey.OP_WRITE);
                            	DatagramChannel chan = (DatagramChannel)key.channel();
                    			//Con con = (Con)key.attachment();
                    			//con.sa = chan.receive(con.req);
                    			ByteBuffer buff = ByteBuffer.allocate(BUF_SZ);
                    			chan.receive(buff);
                    			/*System.out.println(new String(con.req.array(), "UTF-8"));
                    			
                                DatagramChannel Client = (DatagramChannel) key.channel();
                               	ByteBuffer Buffer = ByteBuffer.allocate(256);
    							
    							Client.read(Buffer);*/
    							
    							String result = new String(buff.array()).trim();
    							
    							//System.out.println("Message received: " + result);
    							m.processControllerPacket(result);
                                
                            } else if (key.isWritable()) {
                                //write(key);
                                key.interestOps(SelectionKey.OP_READ);
                            }
                        } catch (IOException e) {
                            System.err.println("glitch, continuing... " +(e.getMessage()!=null?e.getMessage():""));
                        }
                    }
                } catch (IOException e) {
                    System.err.println("glitch, continuing... " +(e.getMessage()!=null?e.getMessage():""));
                }
            }
        } catch (IOException e) {
            System.err.println("network error: " + (e.getMessage()!=null?e.getMessage():""));
        }
    }

  

  
}

