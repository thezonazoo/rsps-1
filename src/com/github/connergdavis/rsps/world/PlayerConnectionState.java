package com.github.connergdavis.rsps.world;

/**
 * The state of the player's current connection, which will vary depending
 * on how far along the process of updating, logging in, or playing they are.
 * The purpose of this flag is to help the decoder/encoder for packets determine
 * what format the packets of data will be in.
 *
 * @author Conner Davis <connergdavis@gmail.com>
 */
public enum PlayerConnectionState {

    /**
     * This is a type-14 login packet request that is sent when the player first
     * tries to login to the server.  It's a very brief request, but the reason
     * it must be distinguished from the login details exchange state is because
     * both of them utilize a shared method for determining the "response code",
     * but based on which stage the player is on, the set of valid response codes
     * changes.
     */
    LOGIN_REQUEST,

    /**
     * This is the stage immediately after the go-ahead response code is sent to
     * the client after {@link this#LOGIN_REQUEST} when information about the client
     * is being exchanged, and the server responds with a small chunk of info about
     * whether those details check out.
     */
    LOGIN_DETAILS_EXCHANGE,

    /**
     * Once the player's logged into the game, packets are framed much differently.
     * For example, the entire packet is encoded in ISAAC, prefixed with a byte header,
     * and usually a fixed width sent as a sequential byte.  These packets are much more
     * predictable, so they have their own subclass of the Packet object.
     */
    IN_GAME

    ,;

}
