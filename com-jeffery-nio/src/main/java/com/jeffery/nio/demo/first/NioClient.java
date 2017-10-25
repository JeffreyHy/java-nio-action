package com.jeffery.nio.demo.first;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by jefferyHy on 2017/10/25.
 */
public class NioClient {
    private static final int PORT=8888;
    private  static final int BUFFER_SIZE = 1024;
    private  static ByteBuffer sendBuffer = ByteBuffer.allocate(BUFFER_SIZE);
    private  static ByteBuffer receivedBuffer = ByteBuffer.allocate(BUFFER_SIZE);
    private  static int i=0;
    public static void main(String[] args) throws IOException {
        SocketChannel socketChannel=SocketChannel.open();
        socketChannel.configureBlocking(false);
        Selector selector=Selector.open();
        //socketChannel.register(selector, SelectionKey.OP_CONNECT);
        socketChannel.register(selector, SelectionKey.OP_CONNECT|SelectionKey.OP_READ|SelectionKey.OP_WRITE);
        InetSocketAddress address=new InetSocketAddress("192.168.14.53",PORT);
        socketChannel.connect(address);
        System.out.println("start connect Server,host:"+address.getAddress()+",port:"+PORT);
        //handler(selector);
        handler1(selector);
    }
    private static void handler(Selector selector) throws IOException {
        SocketChannel client;
        while(true){
            selector.select();
            Set<SelectionKey> selectionKeys=selector.selectedKeys();
            Iterator<SelectionKey> iterator=selectionKeys.iterator();
            while(iterator.hasNext()){
                SelectionKey selectionKey=iterator.next();
                client =(SocketChannel) selectionKey.channel();
                if(selectionKey.isConnectable()){
                    handleConn(client);
                    client.register(selector,SelectionKey.OP_READ);
                }else if(selectionKey.isReadable()){
                    receivedBuffer.clear();
                    int count=client.read(receivedBuffer);
                    if(count>0){
                        System.out.println("The Client received data from Server,data:"
                                +new String(receivedBuffer.array(),0,count));
                        client.register(selector,SelectionKey.OP_WRITE);
                    }
                }else if(selectionKey.isWritable()){
                    sendBuffer.clear();
                    String data=String.valueOf(++i);
                    sendBuffer.put(data.getBytes());
                    sendBuffer.flip();
                    client.write(sendBuffer);
                    System.out.println("The Client send data to Server,data:"+data);
                    client.register(selector,SelectionKey.OP_READ);
                }
            }

        }
    }
    private static void handler1(Selector selector) throws IOException {
        SocketChannel client;
        while(true){
            if(i>=5){
                selector.close();
                break;
            }
            selector.select();
            Set<SelectionKey> selectionKeys=selector.selectedKeys();
            Iterator<SelectionKey> iterator=selectionKeys.iterator();
            while(iterator.hasNext()){
                SelectionKey selectionKey=iterator.next();
                client =(SocketChannel) selectionKey.channel();
                if(selectionKey.isConnectable()){
                    handleConn(client);
                }
                if(selectionKey.isReadable()){
                    receivedBuffer.clear();
                    int count=client.read(receivedBuffer);
                    if(count>0){
                        System.out.println("The Client received data from Server,data:"
                                +new String(receivedBuffer.array(),0,count));
                    }
                }
                if(selectionKey.isWritable()){
                    sendBuffer.clear();
                    String data=String.valueOf(++i);
                    sendBuffer.put(data.getBytes());
                    sendBuffer.flip();
                    client.write(sendBuffer);
                    System.out.println("The Client send data to Server,data:"+data);
                }
            }
        }
    }
    private static void handleConn(SocketChannel client)throws IOException {
        if(client.isConnectionPending()){
            client.finishConnect();
           /*sendBuffer.clear();
            sendBuffer.put("hello Server".getBytes());
            sendBuffer.flip();
            client.write(sendBuffer);*/
            System.out.println("The Client send ack to Server");
        }
    }
}
