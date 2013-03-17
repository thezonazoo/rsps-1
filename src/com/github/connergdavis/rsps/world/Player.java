package com.github.connergdavis.rsps.world;

/**
 * Temporary
 *
 * @author Conner Davis <connergdavis@gmail.com>
 */
public final class Player
{
    /**
     * Every player has an index in the world, denoted by a number.
     * This is what makes them unique and is important in player updating
     * because it serves as their only identification for other clients.
     */
    private int worldId;
    /**
     * This isn't literally a "server" session key, however it is a part of a pair of
     * session keys that are used per-client to handle ISAAC ciphering of packets.  Again,
     * this will change per-client, thus why it's not static or stored somewhere else.
     */
    private final long serverSessionKey = ((long) (Math.random() * 99999999D) << 32) +
        (long) (Math.random() * 99999999D);

    /**
     * Their progress so far in connecting to the server/world.
     */
    private PlayerConnectionState connectionState = PlayerConnectionState.LOGIN_REQUEST;

    public int getWorldId() {
        return worldId;
    }

    /**
     * Set the player's finalized world ID.  This should be done immediately
     * after their socket is connected successfully.
     *
     * @param worldId   The next empty slot in the world player index.
     */
    public final void setWorldId(int worldId) {
        if (this.worldId != 0)
        {
            throw new IllegalStateException("The player at index " + this.worldId + " has already had their world ID set..");
        }
        this.worldId = worldId;
    }

    public PlayerConnectionState getConnectionState() {
        return connectionState;
    }

    public final void setConnectionState(PlayerConnectionState state)
    {
        this.connectionState = state;
    }

    public long getServerSessionKey() {
        return serverSessionKey;
    }

    @Override
    public String toString()
    {
        return "niggers";
    }

}
