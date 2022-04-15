package indi.self.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

/**
 * @author ChenHQ
 * @date 2022/4/15 10:22
 */
public class GroupChatServer {

    private Selector selector;

    private ServerSocketChannel listenChannel;

    private static final int port = 8888;

    public static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy年MM月dd日 HH时mm分ss秒");

    public GroupChatServer() {
        try {
            //初始化对象
            selector = Selector.open();
            listenChannel = ServerSocketChannel.open();

            listenChannel.configureBlocking(false);
            listenChannel.socket().bind(new InetSocketAddress(port));
            listenChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void listen() {

        while (true) {
            int count = 0;
            try {
                count = selector.select(1000);

                if (count == 0) {
                    continue;
                }

                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();

                while (iterator.hasNext()) {
                    SelectionKey selectionKey = iterator.next();
                    if (selectionKey.isAcceptable()) {
                        //连接请求
                        SocketChannel socketChannel = listenChannel.accept();
                        socketChannel.configureBlocking(false);
                        socketChannel.register(selector, SelectionKey.OP_READ, ByteBuffer.allocate(24));
                        System.out.println(socketChannel.getRemoteAddress().toString().substring(1) + "上线了....");
                    }

                    if (selectionKey.isReadable()) {
                        readData(selectionKey);
                    }
                    iterator.remove();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void readData(SelectionKey selectionKey) {
        SocketChannel channel = (SocketChannel) selectionKey.channel();
        try {
            ByteBuffer byteBuffer = (ByteBuffer) selectionKey.attachment();
            int read = channel.read(byteBuffer);
            if (read > 0) {
                String msg = new String(byteBuffer.array());
                byteBuffer.clear();
                System.out.println(SIMPLE_DATE_FORMAT.format(new Date()) + "\r\n" + channel.getRemoteAddress() + "  说：" + msg);
                //转发消息给其他客户端
                sendMessageToOtherClient(msg,channel);
            } else {
            }
        } catch (IOException e) {
            try {
                System.out.println(channel.getRemoteAddress().toString().substring(1) + "离线了..");
                selectionKey.cancel();
                channel.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void sendMessageToOtherClient(String msg, SocketChannel channel) {
        System.out.println("服务器转发消息中...");
        try {
            //遍历所有注册到 selector 上的 SocketChannel,并排除 self
            for (SelectionKey key : selector.keys()) {
                //通过 key 取出对应的 SocketChannel
                Channel targetChannel = key.channel();
                //排除自己
                if (targetChannel instanceof SocketChannel && targetChannel != channel) {
                    //转型
                    SocketChannel dest = (SocketChannel) targetChannel;
                    //将 msg 存储到 buffer
                    ByteBuffer buffer = ByteBuffer.wrap(msg.getBytes());
                    //将 buffer 的数据写入通道
                    dest.write(buffer);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        GroupChatServer groupChatServer = new GroupChatServer();
        groupChatServer.listen();

    }


}
