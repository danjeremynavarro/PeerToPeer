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

    public boolean insertFile(String host, int port, String fileName) {
        try {
            if (this.connection == null) {
                this.connect();
            }
            PreparedStatement insertPreparedStatement = connection.prepareStatement("INSERT INTO file_share (host, port, file) VALUES (?, ?, ?)");
            insertPreparedStatement.setString(1, host);
            insertPreparedStatement.setInt(2, port);
            insertPreparedStatement.setString(3, fileName);
            insertPreparedStatement.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return false;
        }
    }

    @Override
    public String getFile(String filename) {
        try {
            if (this.connection == null) {
                this.connect();
            }
            PreparedStatement getPreparedStatement = connection.prepareStatement("SELECT * FROM file_share WHERE filename = ?");
            getPreparedStatement.setString(1, filename);
            ResultSet resultSet = getPreparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("host");
            } else {
                return "";
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return "";
        }
    }

    @Override
    public boolean unshareFile(String host, String filename) {
        try {
            if (this.connection == null) {
                this.connect();
            }
            PreparedStatement insertPreparedStatement = connection.prepareStatement("DELETE FROM file_share WHERE host = ? AND file = ?");
            insertPreparedStatement.setString(1, host);
            insertPreparedStatement.setString(2, filename);
            insertPreparedStatement.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            return false;
        }
    }

    public KeyVal[] getFiles() {
        try {
            Statement getStatement = connection.createStatement();
            ResultSet results = getStatement.executeQuery("SELECT * FROM file_share");
            ArrayList<KeyVal> files = new ArrayList<>();
            while (results.next()) {
//                files.put(results.getString("host"), results.getString("file"));
                files.add(new KeyVal(results.getString("host"), results.getString("port") ,results.getString("file")));
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
}
