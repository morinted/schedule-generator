// This file contains material supporting section 6.13 of the textbook:
// "Object Oriented Software Engineering" and is issued under the open-source
// license found at www.lloseng.com

package com.lloseng.ocsf.server;

import java.util.*;
import java.io.*;
import java.net.*;

/**
 * This class acts as a subclass of <code>AbstractServer</code>
 * and is also an <code>Observable</code> class.
 * This means that when a message is received, all observers
 * are notified.
 *
 * @author Fran&ccedil;ois B&eacute;lange
 * @author Dr Timothy C. Lethbridge
 * @author Dr Robert Lagani&egrave;re
 * @version August 2000
 */

public class ObservableServer extends Observable
{
  // Class variables ************************************************

  /**
   * The string sent to the observers when a client has connected.
   */
  public static final String CLIENT_CONNECTED= "#OS:Client connected.";

  /**
   * The string sent to the observers when a client has disconnected.
   */
  public static final String CLIENT_DISCONNECTED= "#OS:Client disconnected.";

  /**
   * The string sent to the observers when an exception occurred with a client.
   * The error message of that exception will be appended to this string.
   */
  public static final String CLIENT_EXCEPTION= "#OS:Client exception.";

  /**
   * The string sent to the observers when a listening exception occurred.
   * The error message of that exception will be appended to this string.
   */
  public static final String LISTENING_EXCEPTION= "#OS:Listening exception.";

  /**
   * The string sent to the observers when the server has closed.
   */
  public static final String SERVER_CLOSED= "#OS:Server closed.";

  /**
   * The string sent to the observers when the server has started.
   */
  public static final String SERVER_STARTED= "#OS:Server started.";

  /**
   * The string sent to the observers when the server has stopped.
   */
  public static final String SERVER_STOPPED= "#OS:Server stopped.";


  //Instance variables **********************************************

  /**
   * The service used to simulate multiple class inheritance.
   */
  private AdaptableServer service;


  //Constructor *****************************************************

  /**
   * Constructs a new server.
   *
   * @param port the port on which to listen.
   */
  public ObservableServer(int port)
  {
    service = new AdaptableServer(port, this);
  }

  //Instance methods ************************************************

  /**
   * Begins the thread that waits for new clients
   */
  final public void listen() throws IOException
  {
    service.listen();
  }

  /**
   * Causes the server to stop accepting new connections.
   */
  final public void stopListening()
  {
    service.stopListening();
  }

  /**
   * Closes the server's connections with all clients.
   */
  final public void close() throws IOException
  {
    service.close();
  }

  /**
   * Sends a message to every client connected to the server.
   *
   * @param msg   The message to be sent
   */
  public void sendToAllClients(Object msg)
  {
    service.sendToAllClients(msg);
  }

// ACCESSING METHODS ------------------------------------------------

  /**
   * Used to find out if the server is accepting new clients.
   */
  final public boolean isListening()
  {
    return service.isListening();
  }

  /**
   * Returns an array of containing the existing
   * client connections. This can be used by
   * concrete subclasses to implement messages that do something with
   * each connection (e.g. kill it, send a message to it etc.)
   *
   * @return an array of <code>Thread</code> containing
   * <code>ConnectionToClient</code> instances.
   */
  final public Thread[] getClientConnections()
  {
    return service.getClientConnections();
  }

  /**
   * @return the number of clients currently connected.
   */
  final public int getNumberOfClients()
  {
    return service.getNumberOfClients();
  }

  /**
   * @return the port number.
   */
  final public int getPort()
  {
    return service.getPort();
  }

  /**
   * Sets the port number for the next connection.
   * Only has effect if the server is not currently listening.
   *
   * @param port the port number.
   */
  final public void setPort(int port)
  {
    service.setPort(port);
  }

  /**
   * Sets the timeout time when accepting connection.
   * The default is half a second.
   * The server must be stopped and restarted for the timeout
   * change be in effect.
   *
   * @param timeout the timeout time in ms.
   */
  final public void setTimeout(int timeout)
  {
    service.setTimeout(timeout);
  }

  /**
   * Sets the maximum number of
   * waiting connections accepted by the operating system.
   * The default is 20.
   * The server must be closed and restart for the backlog
   * change be in effect.
   *
   * @param backlog the maximum number of connections.
   */
  final public void setBacklog(int backlog)
  {
    service.setBacklog(backlog);
  }

  /**
   * Hook method called each time a new client connection is
   * accepted. The method may be overridden by subclasses.
   *
   * @param client the connection connected to the client.
   */
  protected synchronized void clientConnected(ConnectionToClient client)
  {
    setChanged();
    notifyObservers(CLIENT_CONNECTED);
  }

  /**
   * Hook method called each time a client disconnects.
   * The method may be overridden by subclasses.
   *
   * @param client the connection with the client.
   */
  protected synchronized void clientDisconnected(ConnectionToClient client)
  {
    setChanged();
    notifyObservers(CLIENT_DISCONNECTED);
  }

  /**
   * Hook method called each time an exception
   * is raised in a client thread.
   * This implementation simply closes the
   * client connection, ignoring any exception.
   * The method may be overridden by subclasses.
   *
   * @param client the client that raised the exception.
   * @param exception the exception raised.
   */
  protected synchronized void clientException(ConnectionToClient client,
                                        Throwable exception)
  {
    setChanged();
    notifyObservers(CLIENT_EXCEPTION);
    try
    {
      client.close();
    }
    catch (Exception e) {}
  }

  /**
   * This method is called when the server stops accepting
   * connections because an exception has been raised.
   * This implementation
   * simply calls <code>stopListening</code>.
   * This method may be overriden by subclasses.
   *
   * @param exception the exception raised.
   */
  protected synchronized void listeningException(Throwable exception)
  {
    setChanged();
    notifyObservers(LISTENING_EXCEPTION);
    stopListening();
  }

  /**
   * This method is called when the server stops accepting
   * connections for any reason.  This method may be overriden by
   * subclasses.
   */
  synchronized protected void serverStopped()
  {
    setChanged();
    notifyObservers(SERVER_STOPPED);
  }

  /**
   * This method is called when the server is closed.
   * This method may be overriden by subclasses.
   */
  synchronized protected void serverClosed()
  {
    setChanged();
    notifyObservers(SERVER_CLOSED);
  }

  /**
   * This method is called when the server starts listening for
   * connections. The method may be overridden by subclasses.
   */
  protected synchronized void serverStarted()
  {
    setChanged();
    notifyObservers(SERVER_STARTED);
  }

  /**
   * This method is used to handle messages coming from the client.
   * Observers are notfied by receiveing the transmitted message.
   * Note that, in this implementation, the information concerning
   * the client that sent the message is lost.
   * It can be overriden, but is still expected to call notifyObservers().
   *
   * @param message The message received from the client.
   * @param client The connection to the client.
   * @see com.lloseng.ocsf.server.ObservableOriginatorServer
   */
  protected synchronized void handleMessageFromClient
    (Object message, ConnectionToClient client)
  {
     setChanged();
     notifyObservers(message);
  }
}
