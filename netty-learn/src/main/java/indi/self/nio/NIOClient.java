package indi.self.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Scanner;

/**
 * @author ChenHQ
 * @date 2022/4/15 10:01
 */
public class NIOClient {

    public static final String host = "127.0.0.1";

    public static final int port = 8888;

    private Selector selector;

    private SocketChannel socketChannel;

    private String username;

    public NIOClient() throws Exception {
        selector = Selector.open();
        socketChannel = SocketChannel.open(new InetSocketAddress(host, port));
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ);
        //得到 username
        username = socketChannel.getLocalAddress().toString().substring(1);
        System.out.println(username + " is ok...");
    }


    public static void main(String[] args) throws Exception {

        NIOClient nioClient = new NIOClient();

        new Thread(() -> {
            while (true) {
                try {
                    nioClient.readInfo();
                    Thread.sleep(2000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();


        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String s = scanner.nextLine();
            nioClient.sendInfo(s);
        }

    }

    private void readInfo() {
        try {
            int count = selector.select(1000);
            if (count > 0) {
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    if (key.isReadable()) {
                        SocketChannel socketChannel = (SocketChannel) key.channel();
                        ByteBuffer buffer = ByteBuffer.allocate(32);
                        socketChannel.read(buffer);
                        String msg = new String(buffer.array());
                        System.out.println("系统通知:" + msg);
                    }
                    iterator.remove();
                }
            } else {

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendInfo(String msg) {
        String info = username + "说：" + msg;
        System.out.println(info);
        try {
            socketChannel.write(ByteBuffer.wrap(info.getBytes()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
