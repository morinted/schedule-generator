// This file contains material supporting section 6.13 of the textbook:
// "Object Oriented Software Engineering" and is issued under the open-source
// license found at www.lloseng.com

package com.lloseng.ocsf.client;

import java.io.*;
import java.net.*;
import java.util.*;

/**
* The <code> AdaptableClient </code> is a class
* that extends the <code> AbstractClient </code> in place of
* the <code> ObservableClient </code>.<p>
*
* Project Name: OCSF (Object Client-Server Framework)<p>
*
* @author Dr. Robert Lagani&egrave;re
* @version Febuary 2001
*/
class AdaptableClient extends AbstractClient
{
  //Instance variables **********************************************

  /**
   * The proxy used to simulate multiple class inheritance.
   */
  private ObservableClient client;

// CONSTRUCTORS *****************************************************

  /**
   * Constructs the client adapter.
   *
   * @param  host  the server's host name.
   * @param  port  the port number.
   */
  public AdaptableClient(String host, int port, ObservableClient client)
  {
    super(host, port);
    this.client = client;
  }

// OVERRIDDEN METHODS *************************************************

  /**
   * Hook method called after the connection has been closed.
   */
  final protected void connectionClosed()
  {
    client.connectionClosed();
  }

  /**
   * Hook method called after an exception
   * is raised by the client listening thread.
   *
   * @param exception the exception raised.
   */
  final protected void connectionException(Exception exception)
  {
    client.connectionException(exception);
  }

  /**
   * Hook method called after a connection has been established.
   */
  final protected void connectionEstablished()
  {
    client.connectionEstablished();
  }

  /**
   * Handles a message sent from the server to this client.
   *
   * @param msg   the message sent.
   */
  final protected void handleMessageFromServer(Object msg)
  {
    client.handleMessageFromServer(msg);
  }
}
