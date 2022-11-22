package pl.edu.agh.kis.pz1;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class PokerClient {
    public static void main(String[] args) {
        try {
            String[] messages = {"I like non-blocking servers", "Hello non-blocking world!", "One more message..", "exit"};
            System.out.println("Starting client...");
            SocketChannel client = SocketChannel.open(new InetSocketAddress("localhost", 31415));

            for (String msg : messages) {
                System.out.println("Prepared message: " + msg);
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                buffer.put(msg.getBytes());
                buffer.flip();
                int bytesWritten = client.write(buffer);
                System.out.printf("Sending Message: %s\nbufforBytes: %d%n", msg, bytesWritten);
            }

            client.close();
            System.out.println("Client connection closed");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
