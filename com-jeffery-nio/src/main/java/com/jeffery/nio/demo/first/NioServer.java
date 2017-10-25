package com.jeffery.nio.demo.first;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by jefferyHy on 2017/10/25.
 */
public class NioServer {
    private static final int PORT=8888;
    private Selector selector;
    private  static final int BUFFER_SIZE = 1024;
    private ByteBuffer sendBuffer = ByteBuffer.allocate(BUFFER_SIZE);
    private  ByteBuffer receivedBuffer = ByteBuffer.allocate(BUFFER_SIZE);
    private int i=0;
    public NioServer() throws IOException {
        ServerSocketChannel serverSocketChannel=ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        ServerSocket serverSocket=serverSocketChannel.socket();
        serverSocket.bind(new InetSocketAddress("192.168.14.53",PORT));
        selector=Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("start NioServer success,port:"+PORT);
    }

    private void listen() throws IOException{
        while (true){
            selector.select();
            Set<SelectionKey> selectionKeys=selector.selectedKeys();
            Iterator<SelectionKey> iterator=selectionKeys.iterator();
            while(iterator.hasNext()){
                SelectionKey selectionKey=iterator.next();
                iterator.remove();
                handlerSelectionKey(selectionKey);
            }
        }
    }

    private void handlerSelectionKey(SelectionKey selectionKey)throws IOException{
        SocketChannel client;
        if(selectionKey.isAcceptable()){
            ServerSocketChannel serverSocketChannel=(ServerSocketChannel)selectionKey.channel();
            client=serverSocketChannel.accept();
            client.configureBlocking(false);
            client.register(selector,SelectionKey.OP_READ);
        }else if(selectionKey.isReadable()){
            client=(SocketChannel)selectionKey.channel();
            receivedBuffer.clear();
            int count=client.read(receivedBuffer);
            if(count>0){
                System.out.println("The Server received data from Client,data:"
                        +new String(receivedBuffer.array(),0,count));
                client.register(selector,SelectionKey.OP_WRITE);
            }
        }else if(selectionKey.isWritable()){
            client=(SocketChannel)selectionKey.channel();
            sendBuffer.clear();
            String data=String.valueOf(++i);
            sendBuffer.put(data.getBytes());
            sendBuffer.flip();
            client.write(sendBuffer);
            System.out.println("The Server send data to Client,data:"+data);
            client.register(selector,SelectionKey.OP_READ);
        }
    }
    public static void main(String[] args) {
        try {
            NioServer nioServer=new NioServer();
            nioServer.listen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
