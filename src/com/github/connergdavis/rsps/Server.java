package com.github.connergdavis.rsps;

import com.github.connergdavis.rsps.packet.Packet;
import com.github.connergdavis.rsps.world.Player;
import com.github.connergdavis.rsps.world.World;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;

/**
 * @author Conner Davis <connergdavis@gmail.com>
 */
public final class Server
{

    /*
        Singleton stuff here.
     */
    private static final Server INSTANCE = new Server();
    private Server() {}     // No pun intended
    public static Server getInstance()
    {
        return INSTANCE;
    }
    /*
        Singleton stuff stops here.
     */

    private static ServerSocketChannel serverChannel;

    /**
     * A constantly flushing raw buffer of data received from clients.
     */
    private static ByteBuffer inBuffer = ByteBuffer.allocate(512);  // More or less max capacity of an RS packet

    /**
     * One central gateway selector that handles all requests: to accept,
     * read and write.  However, it doesn't do the bulk work of each job:
     * it just recognizes when a client shows its interest in one of the
     * opcodes and notifies the appropriate handler.
     */
    private static Selector selector;

    /**
     * Define all our variables (that don't really vary) and more or
     * less prepare the server to be used.
     */
    static void define()
    {
        try
        {
            selector = SelectorProvider.provider().openSelector();
        } catch (IOException e)
        {
            System.err.println("Could not open the selector");
            System.exit(1);
        }

        try
        {
            serverChannel = ServerSocketChannel.open();

            serverChannel.configureBlocking(false);
            serverChannel.bind(new InetSocketAddress(43594));   // 43594: Port used for RS, all but JAGGRAB which is separate anyway

            serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e)
        {
            System.err.println("Could not open server socket channel");
            System.exit(1);
        }
    }

    /**
     * This executable will run the entirety of the server's life span
     * to try and accept any clients that are waiting to connect.
     */
    static final Runnable serverGateway = new Runnable()
    {
        @Override
        public void run()
        {
            if (selector == null)
            {
                throw new IllegalStateException("Cannot start server's loop without first calling define()");
            }

            while (true)
            {
                try
                {
                    if (selector.select() <= 0)
                    {
                        continue;
                    }

                    Iterator it = selector.selectedKeys().iterator();
                    while (it.hasNext())
                    {
                        SelectionKey next = (SelectionKey) it.next();

                        // Make sure this key is valid
                        if (next.isValid())
                        {
                            switch (next.interestOps())
                            {
                                case SelectionKey.OP_ACCEPT:
                                    accept(next);
                                    break;
                                case SelectionKey.OP_READ:
                                    read(next);
                                    break;
                            }
                        }

                        it.remove();
                    }
                } catch (IOException e)
                {
                    System.err.println("Selector broke at some point.");
                    System.exit(2);
                }
            }
        }

        /**
         * Accept an individual client key and prepare it to be read from.
         *
         * @param key   The key to accept.
         * @throws IOException  This could be thrown while registering the selector.
         */
        private void accept(SelectionKey key) throws IOException
        {
            SocketChannel channel = serverChannel.accept();
            channel.configureBlocking(false);
            channel.register(selector, SelectionKey.OP_READ, World.add());

            System.out.printf("New channel registered from %s\n",
                channel.getRemoteAddress().toString().substring(1, channel.getRemoteAddress().toString().indexOf(":")));
        }

        private void read(SelectionKey key) throws IOException
        {
            SocketChannel channel = (SocketChannel) key.channel();
            Player player = (Player) key.attachment();

            // Empty the incoming buffer so it's ready to be filled
            inBuffer.clear();

            // The amount of bytes read from the client this turn.
            int read;
            try
            {
                read = channel.read(inBuffer);

                // Stream ended, meaning the client disconnected
                if (read == -1)
                {
                    key.cancel();
                    channel.close();
                    return;
                }
            } catch (IOException e)
            {
                key.cancel();
                channel.close();
                return;
            }

            // Convert data inside the in buffer to a raw array
            byte[] data = new byte[inBuffer.position()];
            System.arraycopy(inBuffer.array(), 0, data, 0, data.length);

            // Based on what their progress in logging in is, their packets will vary, so we have to know how to decode.
            switch (player.getConnectionState())
            {
                case LOGIN_REQUEST:
                case LOGIN_DETAILS_EXCHANGE:
                    try
                    {
                        /*
                            Since packets before entering the game aren't framed exactly, we can just write raw data,
                            no fancy encoding necessary.
                         */
                        channel.write(Packet.handleLoginGateway(player, data));
                    } catch (Exception e)
                    {
                        System.err.printf("Couldn't decode login for player at index [%d]",
                                World.getPlayers().indexOf(player));
                    }
                    break;
            }
        }
    };

}
