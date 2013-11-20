// This file contains material supporting section 3.8 of the textbook:
// "Object Oriented Software Engineering" and is issued under the open-source
// license found at www.lloseng.com

package com.lloseng.ocsf.server;

import java.net.*;
import java.util.*;
import java.io.*;

/**
* The <code> AbstractServer </code> class maintains a thread that waits
* for connection attempts from clients. When a connection attempt occurs
* it creates a new <code> ConnectionToClient </code> instance which
* runs as a thread. When a client is thus connected to the
* server, the two programs can then exchange <code> Object </code>
* instances.<p>
*
* Method <code> handleMessageFromClient </code> must be defined by
* a concrete subclass. Several other hook methods may also be
* overriden.<p>
*
* Several public service methods are provided to applications that use
* this framework, and several hook methods are also available<p>
*
* The modifications made to this class in version 2.2 are:
* <ul>
* <li> The synchronization of the <code>close()</code> method
* is now limited to the client threads closing sequence. The
* call to <code>serverClosed()</code> is outside the synchronized
* block and is preceeded by a join that garantees that
* <code>serverStopped()</code> is always called before.
* <li> Method <code>isClosed()</code> has been added.
* <li> When a client is accepted, the corresponding
* connection thread will be created only if the server
* has not been stopped.
* </ul>
* The modifications made to this class in version 2.3 are:
* <ul>
* <li> An instance variable refering to the current connection
* factory. Refer to null value by default, in this case regular
* <code>ConnectionToClient</code> instances are created as in the
* previous versions. 
* <li> Method <code>setConnectionFactory()</code> has been added.
* <li> In the run method, a call to the connection factory
* is made if such a factory is available.  
* <li> Method <code>handleMessageFromClient</code> is not always
* called depending on the value returned by the 
* <code>handleMessageFromClient</code> of the <code>ConnectionToClient</code>
* class.
* <li> The <code>clientException</code> method is still the one called when
* an exception is thrown when handling the connection with one client. However
* <code>ClassNotFoundException</code> and <code>RuntimeException</code> instances
* can now be received.  
* <li> The call to <code>serverStopped()</code> has been moved in 
* the <code>run</code> method.
* <li> Method <code>isListening()</code> has been modified.
* <li> Instance variable <code>readToStop</code> is now initialized to <code>true</code>
* </ul><p>
*
* Project Name: OCSF (Object Client-Server Framework)<p>
*
* @author Dr Robert Lagani&egrave;re
* @author Dr Timothy C. Lethbridge
* @author Fran&ccedil;ois B&eacute;langer
* @author Paul Holden
* @version December 2003 (2.31)
* @see com.lloseng.ocsf.server.ConnectionToClient
* @see com.lloseng.ocsf.server.AbstractConnectionFactory
*/
public abstract class AbstractServer implements Runnable
{
  // INSTANCE VARIABLES *********************************************

  /**
   * The server socket: listens for clients who want to connect.
   */
  private ServerSocket serverSocket = null;

  /**
   * The connection listener thread.
   */
  private Thread connectionListener = null;

  /**
   * The port number
   */
  private int port;

  /**
   * The server timeout while for accepting connections.
   * After timing out, the server will check to see if a command to
   * stop the server has been issued; it not it will resume accepting
   * connections.
   * Set to half a second by default.
   */
  private int timeout = 500;

  /**
   * The maximum queue length; i.e. the maximum number of clients that
   * can be waiting to connect.
   * Set to 10 by default.
   */
  private int backlog = 10;

  /**
   * The thread group associated with client threads. Each member of the
   * thread group is a <code> ConnectionToClient </code>.
   */
  private ThreadGroup clientThreadGroup;

  /**
   * Indicates if the listening thread is ready to stop.  Set to
   * false by default.
   */
  private boolean readyToStop = true; // modified in version 2.31

  /**
   * The factory used to create new connections to clients.
   * Is null by default, meaning that regular <code>ConnectionToClient</code>
   * instances will be created. Added in version 2.3
   */
  private AbstractConnectionFactory connectionFactory = null;
  
// CONSTRUCTOR ******************************************************

  /**
   * Constructs a new server.
   *
   * @param port the port number on which to listen.
   */
  public AbstractServer(int port)
  {
    this.port = port;

    this.clientThreadGroup =
      new ThreadGroup("ConnectionToClient threads")
      {
        // All uncaught exceptions in connection threads will
        // be sent to the clientException callback method.
        public void uncaughtException(
          Thread thread, Throwable exception)
        {
          clientException((ConnectionToClient)thread, exception);
        }
      };
  }


// INSTANCE METHODS *************************************************

  /**
   * Begins the thread that waits for new clients.
   * If the server is already in listening mode, this
   * call has no effect.
   *
   * @exception IOException if an I/O error occurs
   * when creating the server socket.
   */
  final public void listen() throws IOException
  {
    if (!isListening())
    {
      if (serverSocket == null)
      {
        serverSocket = new ServerSocket(getPort(), backlog);
      }

      serverSocket.setSoTimeout(timeout);
      
      connectionListener = new Thread(this);
      connectionListener.start();
    }
  }

  /**
   * Causes the server to stop accepting new connections.
   */
  final public void stopListening()
  {
    readyToStop = true;
  }

  /**
   * Closes the server socket and the connections with all clients.
   * Any exception thrown while closing a client is ignored.
   * If one wishes to catch these exceptions, then clients
   * should be individually closed before calling this method.
   * The method also stops listening if this thread is running.
   * If the server is already closed, this
   * call has no effect.
   *
   * @exception IOException if an I/O error occurs while
   * closing the server socket.
   */
  final public void close() throws IOException
  {
    if (serverSocket == null)
      return;
    stopListening();

    try
    {
      serverSocket.close();
    }
    finally
    {
      synchronized (this)
      {
        // Close the client sockets of the already connected clients
        Thread[] clientThreadList = getClientConnections();
        for (int i=0; i<clientThreadList.length; i++)
        {
          try
          {
            ((ConnectionToClient)clientThreadList[i]).close();
          }
          // Ignore all exceptions when closing clients.
          catch(Exception ex) {}
        }
        serverSocket = null;
      }

      try
      {
        connectionListener.join(); // Wait for the end of listening thread.
      }
      catch(InterruptedException ex) {}
      catch(NullPointerException ex) {} // When thread already dead.

      serverClosed();
    }
  }

  /**
   * Sends a message to every client connected to the server.
   * This is merely a utility; a subclass may want to do some checks
   * before actually sending messages to all clients.
   * This method can be overriden, but if so it should still perform
   * the general function of sending to all clients, perhaps after some kind
   * of filtering is done. Any exception thrown while
   * sending the message to a particular client is ignored.
   *
   * @param msg   Object The message to be sent
   */
  public void sendToAllClients(Object msg)
  {
    Thread[] clientThreadList = getClientConnections();

    for (int i=0; i<clientThreadList.length; i++)
    {
      try
      {
        ((ConnectionToClient)clientThreadList[i]).sendToClient(msg);
      }
      catch (Exception ex) {}
    }
  }


// ACCESSING METHODS ------------------------------------------------

  /**
   * Returns true if the server is ready to accept new clients.
   *
   * @return true if the server is listening.
   */
  final public boolean isListening()
  {
    return connectionListener!=null && connectionListener.isAlive(); // modified in version 2.31
  }

  /**
   * Returns true if the server is closed.
   *
   * @return true if the server is closed.
   * @since version 2.2
   */
  final public boolean isClosed()
  {
    return (serverSocket == null);
  }

  /**
   * Returns an array containing the existing
   * client connections. This can be used by
   * concrete subclasses to implement messages that do something with
   * each connection (e.g. kill it, send a message to it etc.).
   * Remember that after this array is obtained, some clients
   * in this migth disconnect. New clients can also connect,
   * these later will not appear in the array.
   *
   * @return an array of <code>Thread</code> containing
   * <code>ConnectionToClient</code> instances.
   */
  synchronized final public Thread[] getClientConnections()
  {
    Thread[] clientThreadList = new
      Thread[clientThreadGroup.activeCount()];

    clientThreadGroup.enumerate(clientThreadList);

    return clientThreadList;
  }

  /**
   * Counts the number of clients currently connected.
   *
   * @return the number of clients currently connected.
   */
  final public int getNumberOfClients()
  {
    return clientThreadGroup.activeCount();
  }

  /**
   * Returns the port number.
   *
   * @return the port number.
   */
  final public int getPort()
  {
    return port;
  }

  /**
   * Sets the port number for the next connection.
   * The server must be closed and restarted for the port
   * change to be in effect.
   *
   * @param port the port number.
   */
  final public void setPort(int port)
  {
    this.port = port;
  }

  /**
   * Sets the timeout time when accepting connections.
   * The default is half a second. This means that stopping the
   * server may take up to timeout duration to actually stop.
   * The server must be stopped and restarted for the timeout
   * change to be effective.
   *
   * @param timeout the timeout time in ms.
   */
  final public void setTimeout(int timeout)
  {
    this.timeout = timeout;
  }

  /**
   * Sets the maximum number of waiting connections accepted by the
   * operating system. The default is 20.
   * The server must be closed and restarted for the backlog
   * change to be in effect.
   *
   * @param backlog the maximum number of connections.
   */
  final public void setBacklog(int backlog)
  {
    this.backlog = backlog;
  }

  /**
   * Sets the connection factory.
   * Once set, this one will be used in the creation
   * of new <code>ConnectionToClient</code> instances.
   * The call to this method is optional; if not called
   * Then regular <code>ConnectionToClient</code> instances
   * are created. Added in version 2.3
   *
   * @param factory the connection factory.
   */
  final public void setConnectionFactory(AbstractConnectionFactory factory)
  {
    this.connectionFactory = factory;
  }

// RUN METHOD -------------------------------------------------------

  /**
   * Runs the listening thread that allows clients to connect.
   * Not to be called.
   */
  final public void run()
  {
    // call the hook method to notify that the server is starting
    readyToStop= false;  // added in version 2.31
    serverStarted();

    try
    {
      // Repeatedly waits for a new client connection, accepts it, and
      // starts a new thread to handle data exchange.
      while(!readyToStop)
      {
        try
        {
          // Wait here for new connection attempts, or a timeout
          Socket clientSocket = serverSocket.accept();

          // When a client is accepted, create a thread to handle
          // the data exchange, then add it to thread group

          synchronized(this)
          {
            if (!readyToStop)  // added in version 2.2
            {
              if (connectionFactory == null) {

                new ConnectionToClient(
                  this.clientThreadGroup, clientSocket, this);
                  
              } else {        // added in version 2.3

                connectionFactory.createConnection(
                  this.clientThreadGroup, clientSocket, this);
              }
            }
          }
        }
        catch (InterruptedIOException exception)
        {
          // This will be thrown when a timeout occurs.
          // The server will continue to listen if not ready to stop.
        }
      }
    }
    catch (IOException exception)
    {
      if (!readyToStop)
      {
        // Closing the socket must have thrown a SocketException
        listeningException(exception);
      }
    }
    finally
    {
      readyToStop = true;
      connectionListener = null;

      // call the hook method to notify that the server has stopped
      serverStopped(); // moved in version 2.31
    }
  }


// METHODS DESIGNED TO BE OVERRIDDEN BY CONCRETE SUBCLASSES ---------

  /**
   * Hook method called each time a new client connection is
   * accepted. The default implementation does nothing.
   * This method does not have to be synchronized since only
   * one client can be accepted at a time.
   *
   * @param client the connection connected to the client.
   */
  protected void clientConnected(ConnectionToClient client) {}

  /**
   * Hook method called each time a client disconnects.
   * The client is garantee to be disconnected but the thread
   * is still active until it is asynchronously removed from the thread group. 
   * The default implementation does nothing. The method
   * may be overridden by subclasses but should remains synchronized.
   *
   * @param client the connection with the client.
   */
  synchronized protected void clientDisconnected(
    ConnectionToClient client) {}

  /**
   * Hook method called each time an exception is thrown in a
   * ConnectionToClient thread.
   * The method may be overridden by subclasses but should remains
   * synchronized. 
   * Most exceptions will cause the end of the client's thread except for
   * <code>ClassNotFoundException<\code>s received when an object of
   * unknown class is received and for the <code>RuntimeException</code>s
   * that can be thrown by the message handling method implemented by the user.
   *
   * @param client the client that raised the exception.
   * @param Throwable the exception thrown.
   */
  synchronized protected void clientException(
    ConnectionToClient client, Throwable exception) {}

  /**
   * Hook method called when the server stops accepting
   * connections because an exception has been raised.
   * The default implementation does nothing.
   * This method may be overriden by subclasses.
   *
   * @param exception the exception raised.
   */
  protected void listeningException(Throwable exception) {}

  /**
   * Hook method called when the server starts listening for
   * connections.  The default implementation does nothing.
   * The method may be overridden by subclasses.
   */
  protected void serverStarted() {}

  /**
   * Hook method called when the server stops accepting
   * connections.  The default implementation
   * does nothing. This method may be overriden by subclasses.
   */
  protected void serverStopped() {}

  /**
   * Hook method called when the server is clased.
   * The default implementation does nothing. This method may be
   * overriden by subclasses. When the server is closed while still
   * listening, serverStopped() will also be called.
   */
  protected void serverClosed() {}

  /**
   * Handles a command sent from one client to the server.
   * This MUST be implemented by subclasses, who should respond to
   * messages.
   * This method is called by a synchronized method so it is also
   * implcitly synchronized.
   *
   * @param msg   the message sent.
   * @param client the connection connected to the client that
   *  sent the message.
   */
  protected abstract void handleMessageFromClient(
    Object msg, ConnectionToClient client);


// METHODS TO BE USED FROM WITHIN THE FRAMEWORK ONLY ----------------

  /**
   * Receives a command sent from the client to the server.
   * Called by the run method of <code>ConnectionToClient</code>
   * instances that are watching for messages coming from the server
   * This method is synchronized to ensure that whatever effects it has
   * do not conflict with work being done by other threads. The method
   * simply calls the <code>handleMessageFromClient</code> slot method.
   *
   * @param msg   the message sent.
   * @param client the connection connected to the client that
   *  sent the message.
   */
  final synchronized void receiveMessageFromClient(
    Object msg, ConnectionToClient client)
  {
    this.handleMessageFromClient(msg, client);
  }
}
// End of AbstractServer Class
