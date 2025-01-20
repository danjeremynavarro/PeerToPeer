import Assignment2.FileShareServer;
import Assignment2.FileShareServerHelper;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

public class PeerClient {
    public static void main(String[] args){
        try {
            ORB orb = ORB.init(new String[]{"-ORBInitialPort", "1050", "-ORBInitialHost", "localhost"}, null);
            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
            String name = "FileShareServer";
            FileShareServer server = FileShareServerHelper.narrow(ncRef.resolve_str(name));
            System.out.println("Corba Client Initialized");
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
