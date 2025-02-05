import Assignment2.FileShareServer;
import Assignment2.FileShareServerHelper;
import Assignment2.KeyVal;
import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.NotFound;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.nio.file.Files;

public class FileSharingClient {
    // Port to run server socket from
    private int port;
    // Path location of download and file to serve
    private String path;
    // The server instance for corba
    private FileShareServer server;
    // Files that are to be shared by this client
    private ArrayList<File> filesToShare = new ArrayList<>();
    // Ip address of the client
    private InetAddress ip = InetAddress.getByName("127.0.0.1");

    private SharedFile sharedFile;

    private GUIClient gui;

    public FileSharingClient(String[] orbArgs, String path, int port, GUIClient gui) throws UnknownHostException, InvalidName, org.omg.CosNaming.NamingContextPackage.InvalidName, CannotProceed, NotFound {
        this.path = path;
        this.port = port;
        this.exec(orbArgs);
        setGui(gui);
        System.out.println("Client Initialized with path: " + this.path + " and port: " + this.port);
    }

    public void setGui(GUIClient gui){
        this.gui = gui;
    }

    private void exec(String[] orbArgs) throws InvalidName, org.omg.CosNaming.NamingContextPackage.InvalidName, CannotProceed, NotFound {
        runServer();
        runClient();
        initCorba(orbArgs);
        getFilesToShare();
        getCurrentlySharedFiles();
        shareFiles();
    }

    public String getPath(){
        return this.path;
    }
    // Mechanism to change the parent directory. Due to time constraint i wasnt able to implement this on gui
    public void setPath(String path){
        this.path = path;
    }

    /**
     * Reads the directory and returns all the files in the directory. Ignores subdirectories
     * @return
     */
    public ArrayList<File> getFilesToShare(){
        File[] files = new File(this.path).listFiles();
        this.filesToShare = new ArrayList<>();

        if (files == null){return this.filesToShare;}

        for (File file : files){
            if (file.isFile()){
                this.filesToShare.add(file);
            }
        }
        return this.filesToShare;
    }

    /**
     * Retrieves shared files through corba
     * @return
     */
    public Set<String> getCurrentlySharedFiles(){
        KeyVal[] allFiles = this.server.getFiles();
        ArrayList<KeyVal> filtered = new ArrayList<>();
        for (KeyVal keyVal : allFiles){
            int p = Integer.parseInt(keyVal.port);
            String h = this.ip.getHostAddress();
            if ((h.equals("127.0.0.1") && p != this.port) || !h.equals(keyVal.key)){
                filtered.add(keyVal);
            }
        }
        KeyVal[] f = new KeyVal[filtered.size()];
        filtered.toArray(f);
        this.sharedFile = new SharedFile(f);
        return this.sharedFile.getFiles();
    }

    /**
     * Insert files using corba
     * @return
     */
    public ArrayList<File> shareFiles(){
        getFilesToShare();
        for (File file : filesToShare){
            this.server.insertFile(this.ip.getHostAddress(), this.port, file.getName());
        }
        return this.filesToShare;
    }

    /**
     * Checks if file is available in the host and port
     * @param file
     * @return
     * @throws IOException
     */
    public boolean isAvailable(String file) throws IOException {
        HashMap<String, String> h;
        h = this.sharedFile.getHost(file);
        if (h == null){return false;} else {
            return this.confirmAvailability(h.get("host"), Integer.parseInt(h.get("port")), h.get("file"));
        }
    }

    /**
     * Downloads file and place them into the downloads folder
     * @param fileName
     * @return
     * @throws IOException
     */
    public String downloadFile(String fileName) throws IOException {
        String pathToSave = this.path + "/downloads";
        Path path = Paths.get(pathToSave);
        if (!Files.exists(path)){
            new File(pathToSave).mkdir();
        }
        HashMap<String,String> h = new HashMap<>();
        h = this.sharedFile.getHost(fileName);
        if (h == null){return null;}

        return this.downloadFileFromServer(h.get("host"), Integer.parseInt(h.get("port")), h.get("file"));
    }

    /**
     * Helper to check availability of file
     * @param host
     * @param port
     * @param filename
     * @return
     * @throws IOException
     */
    private boolean confirmAvailability(String host, int port, String filename) throws IOException {
        Socket clientSocket = new Socket(host, port);
        BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        OutputStream output = new BufferedOutputStream(clientSocket.getOutputStream());
        String p = "check\r\n" + filename + "\r\n";
        output.write(p.getBytes());
        output.flush();
        String line = input.readLine();
        if (line.equals("true")){
            return true;
        } else {
            return false;
        }
    }

    /**
     * Helper to download file from other client using a socket connection
     * @param host
     * @param port
     * @param filename
     * @return
     * @throws IOException
     */
    private String downloadFileFromServer(String host, int port, String filename) throws IOException {
        Socket clientSocket = new Socket(host, port);
        BufferedInputStream input = new BufferedInputStream(clientSocket.getInputStream());
        OutputStream output = new BufferedOutputStream(clientSocket.getOutputStream());
        String p = "get\r\n" + filename + "\r\n";
        output.write(p.getBytes());
        output.flush();

        String pathToSave = this.path + "/downloads/" + filename;
        FileOutputStream fos = new FileOutputStream(pathToSave);

        int count;
        byte[] buffer = new byte[8192]; // or 4096, or more

        while ((count = input.read(buffer)) > 0) {
            fos.write(buffer, 0, count);
        }
        fos.flush();
        fos.close();
        clientSocket.close();
        return pathToSave;
    }

    /**
     * Helper to instantiate the server instance through corba
     * @param orbArgs
     * @throws InvalidName
     * @throws org.omg.CosNaming.NamingContextPackage.InvalidName
     * @throws CannotProceed
     * @throws NotFound
     */
    private void initCorba(String[] orbArgs) throws InvalidName, org.omg.CosNaming.NamingContextPackage.InvalidName, CannotProceed, NotFound {
        ORB orb = ORB.init(orbArgs, null);
        org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
        NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
        String name = "FileShareServer";
        this.server = FileShareServerHelper.narrow(ncRef.resolve_str(name));
        System.out.println("Corba Client Initialized");
    }

    /**
     * Helper that runs the client
     */
    private void runClient(){
        System.out.println("Starting Client ...");
        System.out.println("Reading available files to share ...");
        File file = new File(path);
        File [] listOfFiles = file.listFiles();
        ArrayList<String> fileList = new ArrayList<String>();
        if (listOfFiles != null){
            for (File f : listOfFiles){
                if (f.isFile()){
                    fileList.add(f.getName());
                }
            }
        } else {
            System.out.println("No files to share. Ensure you have files in the directory: " + this.path);
        }

        if (fileList.isEmpty()){
            System.out.println("No files to share. Ensure you have files in the directory: " + this.path);
        } else {
            System.out.println("Files to share: ");
            for (String fileName : fileList){
                System.out.println(fileName);
            }
            System.out.println("Total files share: " + fileList.size());
        }
    }

    /**
     * Helper to run the socket of the client
     */
    private void runServer(){
        Thread server = new Thread(new Server());
        server.start();
    }

    /**
     * A class the runs a socket for client to client communication
     */
    private class Server implements Runnable{
        private final int MAX_THREADS = Runtime.getRuntime().availableProcessors();

        /**
         * Processes the client connections. First reads if a string terminated by a line is check or get
         * If check returns true or false if file is available
         * If get returns a bytestream of the file if exist
         * @param connection
         */
        public void processClient(Socket connection) throws IOException {
            try (
                    BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    BufferedOutputStream output = new BufferedOutputStream(connection.getOutputStream())
            ) {
                boolean startFile = false;
                boolean isCheck = false;
                String line;
                while((line = input.readLine()) != null){
                    if (startFile){
                        if (isCheck){
                            if (this.isAvailable(line)){
                                output.write("true\n".getBytes());
                                output.flush();
                                break;
                            } else {
                                output.write("false\n".getBytes());
                                output.flush();
                                break;
                            }
                        } else {
                            output.write(this.getFile(line));
                            output.flush();
                            break;
                        }
                    }
                    if (line.equalsIgnoreCase("get")){
                        startFile = true;
                    } else if (line.equalsIgnoreCase("check")){
                        startFile = true;
                        isCheck = true;
                    }
                    else {
                        output.write("Pass get or check command \n".getBytes());
                        output.flush();
                        break;
                    }
                }
            }
        }

        // Helper to check if file is available
        private boolean isAvailable(String fileName){
            File file = new File(path, fileName);
            return file.exists() && file.isFile();
        }

        // Helper to read files into bytearray
        private byte[] getFile(String fileName) throws IOException {
            if (fileName == null) {
                return null;
            }
            File file = new File(path, fileName);
            byte[] fileContent = Files.readAllBytes(file.toPath());
            return fileContent;
        }

        // Run the socket in a separate thread
        @Override
        public void run() {
            ExecutorService executor = Executors.newFixedThreadPool(MAX_THREADS);
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                System.out.println("Server listening on port " + port);
               while(true){
                   Socket connection = serverSocket.accept();
                   executor.submit(new Runnable() {
                       public void run() {
                           try {
                               Server.this.processClient(connection);
                           } catch (IOException e) {
                               System.err.println(e.getMessage());
                               System.exit(1);
                           }
                       }
                   });
               }
            } catch (IOException e) {
                System.err.println(e.getMessage());
                System.exit(1);
            }
        }
    }
}

