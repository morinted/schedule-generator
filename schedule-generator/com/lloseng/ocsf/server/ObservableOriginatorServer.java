// This file contains material supporting the textbook:
// "Object Oriented Software Engineering" and is issued under the open-source
// license found at www.lloseng.com

package com.lloseng.ocsf.server;

/**
* The <code> ObservableOriginatorServer </code> is a subclass
* of <code> ObservableServer </code> that sends
* <code> OriginatorMessage </code> instances to its observers.
* This class should be used when the observers need to know
* the orginator of the messages received. The originator
* is null when the message sent concerns the server.
*
* Project Name: OCSF (Object Client-Server Framework)<p>
*
* @author Dr Robert Lagani&egrave;re
* @author Dr Timothy C. Lethbridge
* @author Fran&ccedil;ois B&eacute;langer
* @author Paul Holden
* @version February 2001 (2.12)
* @see com.lloseng.ocsf.server.OriginatorMessage
*/
public class ObservableOriginatorServer extends ObservableServer
{
  // Constructor ******************************************************

  /**
   * Constructs a new server.
   *
   * @param port the port on which to listen.
   */
  public ObservableOriginatorServer(int port)
  {
    super(port);
  }

  // Instance methods ************************************************

  /**
   * This method is used to handle messages coming from the client.
   * Observers are notfied by receiveing an instance of OriginatorMessage
   * that contains both the message received and a reference to the
   * client who sent the message.
   *
   * @param message The message received from the client.
   * @param client The connection to the client.
   */
  protected synchronized void handleMessageFromClient
    (Object message, ConnectionToClient client)
  {
    setChanged();
    notifyObservers(new OriginatorMessage(client, message));
  }

  /**
   * Method called each time a new client connection is
   * accepted. It notifies observers by sending an
   * <code> OriginatorMessage </code> instance
   * containing a reference to that client and
   * the message defined by the static variable CLIENT_CONNECTED.
   *
   * @param client the connection connected to the client.
   */
  protected synchronized void clientConnected(ConnectionToClient client)
  {
    setChanged();
    notifyObservers(new OriginatorMessage(client, CLIENT_CONNECTED));
  }

  /**
   * Method called each time a client connection is
   * disconnected. It notifies observers by sending an
   * <code> OriginatorMessage </code> instance
   * containing a reference to that client and
   * the message defined by the static variable CLIENT_DISCONNECTED.
   *
   * @param client the connection connected to the client.
   */
  synchronized protected void clientDisconnected(ConnectionToClient client)
  {
    setChanged();
    notifyObservers(new OriginatorMessage(client, CLIENT_DISCONNECTED));
  }


  /**
   * Method called each time an exception is raised
   * by a client connection.
   * It notifies observers by sending an
   * <code> OriginatorMessage </code> instance
   * containing a reference to that client and
   * the message defined by the static variable CLIENT_EXCEPTION
   * to which is appended the exception message.
   *
   * @param client the client that raised the exception.
   * @param Throwable the exception thrown.
   */
  synchronized protected void clientException(
    ConnectionToClient client, Throwable exception)
  {
    setChanged();
    notifyObservers(
      new OriginatorMessage(client,
        CLIENT_EXCEPTION + exception.getMessage()));
  }

  /**
   * Method called each time an exception is raised
   * while listening.
   * It notifies observers by sending an
   * <code> OriginatorMessage </code> instance
   * containing the message defined by the static variable LISTENING_EXCEPTION
   * to which is appended the exception message.
   * The originator is set to null.
   *
   * @param exception the exception raised.
   */
  protected synchronized void listeningException(Throwable exception)
  {
    setChanged();
    notifyObservers(
      new OriginatorMessage(null,
        LISTENING_EXCEPTION + exception.getMessage()));
  }

  /**
   * Method called each time the server is started.
   * It notifies observers by sending an
   * <code> OriginatorMessage </code> instance
   * containing the message defined by the static variable SERVER_STARTED.
   * The originator is set to null.
   */
  protected synchronized void serverStarted()
  {
    setChanged();
    notifyObservers(new OriginatorMessage(null, SERVER_STARTED));
  }

  /**
   * Method called each time the server is stopped.
   * It notifies observers by sending an
   * <code> OriginatorMessage </code> instance
   * containing the message defined by the static variable SERVER_STOPPED.
   * The originator is set to null.
   */
  synchronized protected void serverStopped()
  {
    setChanged();
    notifyObservers(new OriginatorMessage(null, SERVER_STOPPED));
  }

  /**
   * Method called each time the server is closed.
   * It notifies observers by sending an
   * <code> OriginatorMessage </code> instance
   * containing the message defined by the static variable SERVER_CLOSED.
   * The originator is set to null.
   */
  synchronized protected void serverClosed()
  {
    setChanged();
    notifyObservers(new OriginatorMessage(null, SERVER_CLOSED));
  }
}
