// This file contains material supporting the textbook:
// "Object Oriented Software Engineering" and is issued under the open-source
// license found at www.lloseng.com

package com.lloseng.ocsf.server;

import java.net.*;
import java.io.*;

/**
* The <code> AbstractConnectionFactory </code> is an abstract class
* that must be subclassed when one want to use a  
* subclass of <code> ConnectionToClient </code> class. 
* The role of this class is to create these connections
* when required. The creation of such factory is however
* optional.<p>
*
* A factory and the corresponding client connections
* should be defined when one to handle the received
* messages inside each <code>ConnectionToClient</code>
* instances instead of having a centralized handling
* through the 
* <code>AbstractServer.handleMessageFromClient()</code>
* method.
*
* Project Name: OCSF (Object Client-Server Framework)<p>
*
* @author Dr Robert Lagani&egrave;re
* @author Dr Timothy C. Lethbridge
* @author Fran&ccedil;ois B&eacute;langer
* @author Paul Holden
* @version August 2003 (2.3)
* @see com.lloseng.ocsf.server.AbstractServer
* @see com.lloseng.ocsf.server.ConnectionToClient
*/
public abstract class AbstractConnectionFactory
{
// METHOD DESIGNED TO BE OVERRIDDEN BY CONCRETE SUBCLASSES ---------

  /**
   * Hook method called each time a new client connection must
   * be created. This method should simply call the constructor
   * of the defined subclass of <code> ConnectionToClient </code>
   * and return the created instance.
   *
   * @param group the thread group that contains the connections.
   * @param clientSocket contains the client's socket.
   * @param server a reference to the server that created this instance.
   * @exception IOException if an I/O error occur when creating the connection.
   */
  protected abstract ConnectionToClient createConnection(ThreadGroup group, 
     Socket clientSocket, AbstractServer server) throws IOException;
}
