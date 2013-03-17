package com.github.connergdavis.rsps.packet;

import com.github.connergdavis.rsps.world.Player;

import java.nio.ByteBuffer;

/**
 * @author Conner Davis <connergdavis@gmail.com>
 */
public class Packet
{   // this shits likely to change

    private int header;
    private byte[] payload;

    public Packet(int header)
    {
        this.header = header;
    }

    /**
     * Handle packets received in the login gateway stage.  Since these packets have to be responded
     * to immediately, we do the full decoding, processing and encoding response packets all in one place.
     *
     * @param player        The player that sent the login packet.
     * @param data          The raw data of the packet to process.
     * @throws Exception    Data was mutated at some point, because they sent an invalid login gateway op-code.
     * @return              The appropriate response to the packet that was sent.
     */
    public static ByteBuffer handleLoginGateway(Player player, byte[] data) throws Exception
    {
        ByteBuffer in = ByteBuffer.wrap(data);
        ByteBuffer out;

        final int loginType = in.get() & 0xFF;
        System.out.printf("Login request received: %d\n", loginType);

        switch (loginType)
        {
            case 15:    // Update protocol
                break;
            case 14:    // Login request
                final int usernameHash = in.get() & 0xFF;   // Is this important?
                final byte responseCode = determineLoginResponseCode(player);

                out = ByteBuffer.allocate(17);
                out.put((byte) 0).put((byte) 0).put((byte) 0).put((byte) 0).put((byte) 0).put((byte) 0).put((byte) 0).
                    put((byte) 0).put(responseCode).putLong(player.getServerSessionKey());
                return out;
            case 16:
            case 18:    // Login started
                System.out.printf("Real login request (type %d) received.  Ready to be handled!", loginType);
                break;
            default:
                // TODO Custom exception
                throw new Exception("A login request was received, but it's of a known type.  Possibly a client-side problem");
        }
        return null; // This shouldn't ever happen, technically it can't because of the default switch
    }

    /**
     * When the player sends a type-14 login gateway request, the server is expected to respond with a packet
     * that is suffixed with a "response code."  This byte is a flag that determines whether or not the player
     * should be allowed to continue with login, and if they can't, what message the client should display to
     * them.  This is what you see when you're told your password was incorrect, for example.
     *
     * It's also important to note that there are two stages of response codes to be sent -- in stage one, when
     * the player has only sent their name hash, the go-ahead response code is 0.  However, when that packet has
     * been responded to and session keys, username & password, etc. have been exchanged, the go-ahead code becomes
     * 2.
     *
     * @param player    The player that's trying to login.
     * @return
     */
    private static byte determineLoginResponseCode(Player player)
    {
        switch (player.getConnectionState())
        {
            case LOGIN_REQUEST:
                return 0;
            case LOGIN_DETAILS_EXCHANGE:
                return 2;
        }
        return -1; // This isn't possible, FYI
    }

    public int getHeader() {
        return header;
    }

    public byte[] getPayload() {
        return payload;
    }

}
