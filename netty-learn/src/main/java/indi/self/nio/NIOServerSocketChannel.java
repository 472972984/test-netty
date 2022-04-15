package indi.self.nio;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * @author ChenHQ
 * @date 2022/4/15 09:36
 */
public class NIOServerSocketChannel {

    public static void main(String[] args) throws Exception {

        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();

        serverSocketChannel.configureBlocking(false);

        serverSocketChannel.socket().bind(new InetSocketAddress(7777));

        Selector selector = Selector.open();

        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        while (true) {

            int select = selector.select(1000);
            if (select == 0) {
                System.out.println("服务器等待1s，没有客户端连接");
                continue;
            }

            Set<SelectionKey> selectionKeys = selector.selectedKeys();

            Iterator<SelectionKey> iterator = selectionKeys.iterator();

            while (iterator.hasNext()) {

                SelectionKey selectionKey = iterator.next();

                if (selectionKey.isAcceptable()) {
                    //收到客户端连接请求: 对应一个客户端channel，注册到selector中，监听可读请求
                    SocketChannel socketChannel = serverSocketChannel.accept();
                    System.out.println("客户端连接成功....");
                    socketChannel.configureBlocking(false);
                    socketChannel.register(selector, SelectionKey.OP_READ, ByteBuffer.allocate(1024));
                }

                if (selectionKey.isReadable()) {
                    //收到客户端发送报问：SelectionKey 反向获取到对应的channel
                    SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                    ByteBuffer attachment = (ByteBuffer) selectionKey.attachment();
                    socketChannel.read(attachment);
                    System.out.println(">>>>>>" + new String(attachment.array()));
                }


                iterator.remove();
            }


        }


    }


}
