package com.github.connergdavis.rsps;

/**
 * Initializes an instance of the server.
 *
 * @author Conner Davis <connergdavis@gmail.com>
 */
final class Init
{

    /**
     * @param args      Not applicable so far, but may be useful eventually.
     */
    public static void main(String[] args)
    {
        Server.define();
        new Thread(Server.serverGateway).start();
    }

}
