import Assignment2.FileShareServer;
import Assignment2.FileShareServerHelper;
import Assignment2.KeyVal;
import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CosNaming.NamingContext;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextHelper;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.NotFound;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.nio.file.Files;

public class FileSharingClient {
    // Port to run server socket from
    private int port;
    // Path location of download and file to serve
    private String path;

    private FileShareServer server;

    private ArrayList<File> filesToShare = new ArrayList<>();

    private ArrayList<File> currentlySharedFiles = new ArrayList<>();

    private InetAddress ip = InetAddress.getByName("127.0.0.1");

    public FileSharingClient(String[] orbArgs, String path, int port) throws UnknownHostException, InvalidName, org.omg.CosNaming.NamingContextPackage.InvalidName, CannotProceed, NotFound {
        this.path = path;
        this.port = port;
        this.exec(orbArgs);
        System.out.println("Client Initialized with path: " + this.path + " and port: " + this.port);
    }

    public void setNewPath(String newPath) {
        this.path = newPath;
    }

    private void exec(String[] orbArgs) throws InvalidName, org.omg.CosNaming.NamingContextPackage.InvalidName, CannotProceed, NotFound {
        runServer();
        runClient();
        initCorba(orbArgs);
        getFilesToShare();
        getCurrentlySharedFiles();
        shareFiles();
    }

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

    private void getCurrentlySharedFiles(){
        KeyVal[] allFiles = this.server.getFiles();
        System.out.println(allFiles.length);
    }

    public ArrayList<File> shareFiles(){
        getFilesToShare();
        for (File file : filesToShare){
            this.server.insertFile(this.ip.getHostAddress(), file.getName());
        }
        return this.filesToShare;
    }

    private void initCorba(String[] orbArgs) throws InvalidName, org.omg.CosNaming.NamingContextPackage.InvalidName, CannotProceed, NotFound {
        ORB orb = ORB.init(orbArgs, null);
        org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
        NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
        String name = "FileShareServer";
        this.server = FileShareServerHelper.narrow(ncRef.resolve_str(name));
        System.out.println("Corba Client Initialized");
    }

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

    private void runServer(){
        Thread server = new Thread(new Server());
        server.start();
    }

    private class Server implements Runnable{
        private final int MAX_THREADS = Runtime.getRuntime().availableProcessors();

        /**
         * Socket SErver
         * @param connection
         */
        public void processClient(Socket connection) throws IOException {
            try (
                    BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    OutputStream output = new BufferedOutputStream(connection.getOutputStream())
            ) {
                boolean startFile = false;
                String line;
                while((line = input.readLine()) != null){
                    if (startFile){
                        output.write(this.getFile(line));
                    }
                    if (line.equalsIgnoreCase("get")){
                        startFile = true;
                    } else {
                        output.write("Pass get command \n".getBytes());
                        output.flush();
                    }
                }
            }
        }

        private byte[] getFile(String fileName) throws IOException {
            if (fileName == null) {
                return null;
            }
            File file = new File(path, fileName);
            try {
                byte[] fileContent = Files.readAllBytes(file.toPath());
                return fileContent;
            } finally {

            }
        }

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
                               throw new RuntimeException(e);
                           }
                       }
                   });
               }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

