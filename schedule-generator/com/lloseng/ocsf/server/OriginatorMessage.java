// This file contains material supporting the textbook:
// "Object Oriented Software Engineering" and is issued under the open-source
// license found at www.lloseng.com

package com.lloseng.ocsf.server;

/**
 * A message class used by the Observable layer of the OCSF in order to conserve
 * information about the originator of a message.
 *
 * @author Dr. Robert Lagani&egrave;re
 * @version July 2001
 */
public class OriginatorMessage
{
  /**
   * The connection that originated the message
   */
  private ConnectionToClient originator;

  /**
   * The message.
   */
  private Object message;

// Constructor ***************************************************************

  /**
   * Constructs an instance of an OriginatorMessage
   *
   * @param originator The client who created this message
   * @param message The contents of the message
   */
  public OriginatorMessage(ConnectionToClient originator, Object message)
  {
    this.originator = originator;
    this.message = message;
  }

// Accessor methods *********************************************************

  /**
   * Returns the originating connection.
   *
   * @return The connection from which the message originated.
   */
  public ConnectionToClient getOriginator()
  {
    return originator;
  }

  /**
   * Returns the message's contents.
   *
   * @return The content of the message.
   */
  public Object getMessage()
  {
    return message;
  }
}
