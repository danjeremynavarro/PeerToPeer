/**
 * Assignment 2
 * title: Main.java
 * description: A Peer to Peer server that allows file sharing between two clients
 * date: January 31, 2025
 * author: Dan Jeremy Navarro
 */

import Assignment2.FileShareServer;
import Assignment2.FileShareServerHelper;
import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.NotFound;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.POAManagerPackage.AdapterInactive;
import org.omg.PortableServer.POAPackage.ServantNotActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;
import java.sql.SQLException;
/*
### Server instructions
1. First ensure that corba is installed on the computer
2. Run the corba server with the following command
    1.  <code>orbd -ORBInitialPort 1050 -ORBInitialHost localhost&</code>
3. Ensure that the identifier.sqlite database is on the same directory as the Main class file
4. Run the following command to run the server application
    1. <code>java Main server -ORBInitialPort 1050 -ORBInitialHost localhost</code>
5. The program should say it is ready and waiting

### Client instructions
1. The client command accepts the same ORB arguments and also arguments to the directory where files are shared and
where it will be downloaded and the port number to run from. The syntax to run the client program is as follows
> <code> java Main client -ORBInitialPort {orb port} -ORBInitialHost {orb host} {directory} {port} </code>
Example:
> <code>java Main client -ORBInitialPort 1050 -ORBInitialHost localhost /home/user/FileShare 2222</code>

2. On load of client the application will look into the directory provided in the argument (the share directory) and will
attempt to contact the server through corba and add the files to the database. The client will also launch a server socket
that listens on the provided port
3. The gui is made up of:
   1. Sync button - clicking this button will update the filesharing server of the contents of the share directory which allows
   other clients to download it
   2. Available files - this list all files that can be downloaded. This will not list the files that are being shared by the client itself
   3. Fetch files - this will update the available files list from the file sharing server
   4. Download - this will download the files selected in the available files list
   5. Text area - this shows messages from the client
4. Once the client is loaded. You can click on "Fetch files" button to update the list of "Available files"
5. You can also click on "Sync" to allow other clients access to the files in your share directory
6. Select the files you want to upload from the list then click "Download" button
7. The client will then first check if the file is available by creating a socket connection and sending the string "check" followed by
the file name.
8. If the file is available it will then create a socket connection then send the string "get" followed by the file name.
9. It will then check if a folder called "downloads" is in the share directory if there is none it will create it.
10. It will then create the file and save the file inside the downloads folder
 */
public class Main {
    public static void main(String[] args) throws InvalidName, AdapterInactive, WrongPolicy, ServantNotActive, org.omg.CosNaming.NamingContextPackage.InvalidName, CannotProceed, NotFound {
      if (args[0].equals("client")) {
          // Create the GUI
          GUIClient gui = new GUIClient();
          gui.setVisible(true);
          try {
              gui.addStatus("Loading client class ....");
              FileSharingClient client = new FileSharingClient(new String[]{args[1], args[2], args[3], args[4]}, args[5], Integer.parseInt(args[6]), gui);
              gui.setClient(client);
              gui.addStatus("Corba client initiated ....");
              gui.addStatus("Client port listening at " + args[6]);
              gui.getList();
          } catch (InvalidName e) {
              gui.addStatus("InvalidName error ....");
              gui.addStatus("Please restart the application");
          } catch (org.omg.CosNaming.NamingContextPackage.InvalidName e) {
              gui.addStatus("org.omg.CosNaming.NamingContextPackage.InvalidName  error ....");
              gui.addStatus("Please restart the application");
          } catch (CannotProceed e) {
              gui.addStatus("CannotProceed error ....");
              gui.addStatus("Please restart the application");
          } catch (NotFound e) {
              gui.addStatus("NotFound error ....");
              gui.addStatus("Please restart the application");
          } catch (Throwable e) {
              gui.addStatus(e.toString());
              gui.addStatus(e.getLocalizedMessage() + " error ....");
              gui.addStatus("Please restart the application");
          }
      }
      else if (args[0].equals("server")) {
          try {
              ORB orb = ORB.init(new String[]{args[1], args[2], args[3], args[4]}, null);

              // get reference to rootpoa and activate the POAManager
              POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
              rootpoa.the_POAManager().activate();

              // create servant and register it with the ORB
              FileSharingServer helloImpl = new FileSharingServer();

              // get object reference from the servant
              org.omg.CORBA.Object ref = rootpoa.servant_to_reference(helloImpl);
              FileShareServer href = FileShareServerHelper.narrow(ref);

              // get the root naming context
              org.omg.CORBA.Object objRef =
                      orb.resolve_initial_references("NameService");
              // Use NamingContextExt which is part of the Interoperable
              // Naming Service (INS) specification.
              NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

              // bind the Object Reference in Naming
              String name = "FileShareServer";
              NameComponent path[] = ncRef.to_name(name);
              ncRef.rebind(path, href);

              System.out.println("File Sharing Server ready and waiting ...");

              // wait for invocations from clients
              orb.run();
          } catch (SQLException e) {
              throw new RuntimeException(e);
          }
      }
    }
}