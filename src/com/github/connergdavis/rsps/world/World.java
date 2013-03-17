package com.github.connergdavis.rsps.world;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Conner Davis <connergdavis@gmail.com>
 */
public final class World {

    /**
     * Master index of every player in this game world.  The player objects
     * are always accessed indirectly by drawing from its {@link java.nio.channels.SelectionKey}'s
     * attachment, which is the player object.
     */
    private static final List<Player> PLAYERS = new ArrayList<Player>(256);

    /**
     * Append a new player to the world.
     *
     * @return  The player object, default values already set up, ready to continue.
     */
    public static Player add()
    {
        Player player = new Player();
        player.setConnectionState(PlayerConnectionState.LOGIN_REQUEST);

        PLAYERS.add(player);
        player.setWorldId(PLAYERS.indexOf(player));

        return player;
    }

    public static List<Player> getPlayers() {
        return PLAYERS;
    }

}
