// This file contains material supporting section 6.13 of the textbook:
// "Object Oriented Software Engineering" and is issued under the open-source
// license found at www.lloseng.com

package com.lloseng.ocsf.client;

import java.util.*;
import java.io.*;
import java.net.*;

/**
 * This class acts as a subclass of <code>AbstractClient</code>
 * and is also an <code>Observable</code> class.
 * Each time a new message is received, observers are notified.
 * This class contains two blocking methods that can be used
 * when a user wishes to send a message and then wait for a reply
 * from the server.
 *
 * @author Dr Robert Lagani&egrave;re
 * @version April 2002
 */
public class ObservableSWRClient extends ObservableClient
{
  //Instance variables **********************************************

  /**
   * Indicates a that the client is still waiting for a reply.
   */
  public static final String WAITING_FOR_REPLY = "#OC:Waiting for reply.";

  /**
   * The service instance used to simulate multiple class inheritance.
   */
  private ArrayList expected = new ArrayList(3);
  private boolean cancelled = false;
  private int waitTime = 30000;
  private Exception exception;
  private Object received;

  //Constructor *****************************************************

  public ObservableSWRClient(String host, int port)
  {
    super(host, port);
  }

  /**
   * Sets the wait time.
   * At the end of each wait time period,
   * the instance will notify its observers with the
   * WAITING_FOR_REPLY message.
   *
   * @param waitTime The wait time in ms.
   */
  public void setWaitTime(int waitTime)
  {
    this.waitTime= waitTime;
  }

  /**
   * Connects to the server and waits. This method
   * will block until the server confirm connection.
   * At the end of each wait time period,
   * the instance will notify its observers with the
   * WAITING_FOR_REPLY message.
   *
   * @return true if successfully connected.
   * @exception IOException if an I/O error occurs when connecting.
   */
  public synchronized boolean connectAndWait() throws Exception
  {
    clearAll();
    expected.add(CONNECTION_ESTABLISHED);

    this.openConnection();

    while ( !cancelled && !expected.isEmpty() )
    {
      wait(waitTime);
      setChanged();
      notifyObservers(WAITING_FOR_REPLY);
    }

    if (exception != null)
    {
      throw exception;
    }

    if (cancelled)
      return false;
    else
      return true;
  }

  /**
   * Sends a message to the server and waits for a reply.
   * This method will block until the server sends the expected reply.
   * At the end of each wait time period,
   * the instance will notify its observers with the
   * WAITING_FOR_REPLY message.
   *
   * @param message The message sends to the server.
   * @param expectedObject The client will wait until it receives an object
   * equals to this one.
   * @return the object received.
   * @exception IOException if an I/O error occurs.
   */
  public synchronized Object sendAndWaitForReply(
                    Object message, Object expectedObject) throws Exception
  {
    clearAll();
    expected.add(expectedObject);

    return sendAndWaitForReply(message, null);
  }

  /**
   * Sends a message to the server and waits for a reply.
   * This method will block until the server sends one
   * of the expected list of replies.
   * At the end of each wait time period,
   * the instance will notify its observers with the
   * WAITING_FOR_REPLY message.
   *
   * @param message The message sends to the server.
   * @param expectedListOfObject The client will wait until it receives
   * an object equals to one of the objects in this list.
   * @return the object received.
   * @exception IOException if an I/O error occurs.
   */
  public synchronized Object sendAndWaitForReply(
              Object message, List expectedListOfObject) throws Exception
  {

    if (expectedListOfObject!=null)
    {
      clearAll();
      expected.addAll(expectedListOfObject);
    }

    this.sendToServer(message);

    while ( !cancelled && !expected.isEmpty() )
    {
      wait(waitTime);
      setChanged();
      notifyObservers(WAITING_FOR_REPLY);
    }

    if (exception != null)
    {
      throw exception;
    }

    if (cancelled)
      return null;
    else
      return received;
  }

  /**
   * Cancels the exchange with the server.
   *
   */
  public synchronized void cancel()
  {
    clearAll();
    cancelled= true;
    notifyAll();
  }

  /**
   * Returns true if cancal has been called.
   *
   */
  public boolean isCancelled()
  {
    return cancelled;
  }

  private void clearAll()
  {
    cancelled= false;
    expected.clear();
    exception= null;
    received= null;
  }

  private synchronized void notify(Exception ex)
  {
    clearAll();
    exception= ex;
    notifyAll();
  }

  private synchronized void receive(Object ob)
  {
    if (expected.contains(ob))
    {
      clearAll();
      received= ob;
      notifyAll();
    }
  }

  /**
   * This method is used to handle messages from the server.  This method
   * can be overriden but should always call notifyObservers().
   *
   * @param message The message received from the client.
   */
  protected void handleMessageFromServer(Object message)
  {
    receive(message);

    setChanged();
    notifyObservers(message);
  }

  /**
   * Hook method called after the connection has been closed.
   */
  protected void connectionClosed()
  {
    notify(null);

    setChanged();
    notifyObservers(CONNECTION_CLOSED);
  }

  /**
   * Hook method called each time an exception
   * is raised by the client listening thread.
   *
   * @param exception the exception raised.
   */
  protected void connectionException(Exception exception)
  {
    notify(exception);

    setChanged();
    notifyObservers(exception);
  }

  /**
   * Hook method called after a connection has been established.
   */
  protected void connectionEstablished()
  {
    receive(CONNECTION_ESTABLISHED);

    setChanged();
    notifyObservers(CONNECTION_ESTABLISHED);
  }
}
