import Assignment2.FileShareServer;
import Assignment2.FileShareServerHelper;
import Assignment2.FileShareServerPOA;
import Assignment2.KeyVal;
import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.NotFound;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.*;
import org.omg.PortableServer.POAManagerPackage.AdapterInactive;
import org.omg.PortableServer.POAPackage.ServantNotActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;
import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import org.sqlite.JDBC;

/**
 *   orbd -ORBInitialPort 1050 -ORBInitialHost localhost&
 */

///usr/lib/jvm/java-1.8.0-openjdk-amd64/bin/java -cp "/home/user/Nextcloud/COMP489DistributedComputing/Assignment2/lib/*:." FileSharingServer -ORBInitialPort 1050 -ORBInitialHost localhost&
public class FileSharingServer extends FileShareServerPOA {
    private String databasePath = "jdbc:sqlite:";
    private Connection connection;

    FileSharingServer() throws SQLException {
        databasePath += System.getProperty("user.dir") + File.separator + "identifier.sqlite";
        System.out.println("Database Path: " + databasePath);

        this.connect();
        System.out.println("Connected to database");
    }

    private void connect() throws SQLException {
        connection = DriverManager.getConnection(this.databasePath);
    }

    public boolean insertFile(String host, String fileName) {
        try {
            if (this.connection == null) {
                this.connect();
            }
            PreparedStatement insertPreparedStatement = connection.prepareStatement("INSERT INTO file_share (host, file) VALUES (?, ?)");
            insertPreparedStatement.setString(1, host);
            insertPreparedStatement.setString(2, fileName);
            insertPreparedStatement.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return false;
        }
    }

    @Override
    public String getFile(String filename) {
        return "";
    }

    public KeyVal[] getFiles() {
        try {
            Statement getStatement = connection.createStatement();
            ResultSet results = getStatement.executeQuery("SELECT * FROM file_share");
            ArrayList<KeyVal> files = new ArrayList<>();
            while (results.next()) {
//                files.put(results.getString("host"), results.getString("file"));
                files.add(new KeyVal(results.getString("host"), results.getString("file")));
            }

            if (files.isEmpty()) {
                return new KeyVal[0];
            }
            KeyVal[] fileArray = new KeyVal[files.size()];
            for (int i = 0; i < files.size(); i++) {
                fileArray[i] = files.get(i);
            }
            return fileArray;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        try {
            ORB orb = ORB.init(args, null);

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
