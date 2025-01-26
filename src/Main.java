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
import java.net.UnknownHostException;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) throws SQLException, UnknownHostException {
      if (args[0].equals("client")) {
          GUIClient gui = new GUIClient();
          gui.setVisible(true);
          try {
              gui.addStatus("Loading client class ....");
              FileSharingClient client = new FileSharingClient(new String[]{args[1], args[2], args[3], args[4]}, args[5], Integer.parseInt(args[6]));
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

              System.out.println("HelloServer ready and waiting ...");

              // wait for invocations from clients
              orb.run();
          } catch (InvalidName e) {
          } catch (WrongPolicy e) {
              throw new RuntimeException(e);
          } catch (org.omg.CosNaming.NamingContextPackage.InvalidName e) {
              throw new RuntimeException(e);
          } catch (SQLException e) {
              throw new RuntimeException(e);
          } catch (AdapterInactive e) {
              throw new RuntimeException(e);
          } catch (ServantNotActive e) {
              throw new RuntimeException(e);
          } catch (CannotProceed e) {
              throw new RuntimeException(e);
          } catch (NotFound e) {
              throw new RuntimeException(e);
          }
      }
    }
}