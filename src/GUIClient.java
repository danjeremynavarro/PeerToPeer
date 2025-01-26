import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Set;

public class GUIClient extends JFrame {
    private JPanel panel1;
    private JButton syncNowButton;
    private JList<String> itemSelection;
    private JTextArea status;
    private JLabel syncLabel;
    private JButton fetchFilesButton;
    private JButton downloadButton;
    private FileSharingClient client = null;
    DefaultListModel<String> listModel = new DefaultListModel<>();

    GUIClient(){
        this.setContentPane(panel1);
        this.setupButtonListeners();
        this.itemSelection.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.pack();
        this.setTitle("Peer to Peer Client");
        this.setSize(800, 600);
        this.setLocationRelativeTo(null);
    }

    private void setupButtonListeners(){
        this.fetchFilesButton.addActionListener(e -> {
           if (client != null) {
               this.getList();
           }
        });
        this.syncNowButton.addActionListener(e -> {
            if (client != null) {
                ArrayList<File> filesShared = this.client.shareFiles();
                this.addStatus("Sharing files in: " + this.client.getPath());
                this.addStatus("Files shared: " + filesShared.size());
                for (File file : filesShared) {
                    this.addStatus("File: " + file.getName());
                }
            }
        });
        this.downloadButton.addActionListener(e -> {
            String getSelectedFile = itemSelection.getSelectedValue();
            this.addStatus("Downloading: " + getSelectedFile);
            Thread thread = new Thread(() -> {
                this.addStatus("Checking server and file availability ...");
                if (this.isAvailable(getSelectedFile)){
                    this.addStatus("File is available. Starting Download  ...");
                    try {
                        String file = this.client.downloadFile(getSelectedFile);
                        if (file != null) {
                            if (Files.exists(Paths.get(file))) {
                                this.addStatus("File downloaded: " + file);
                            }
                        } else {
                            this.addStatus("Unknown error occurred when downloading file");
                        }
                    } catch (IOException ex) {
                        this.addStatus("Download failed: " + ex.getMessage());
                    }
                } else {
                    this.addStatus("File is unavailable. Stopping Download  ...");
                }
            });
            thread.start();
        });
    }

    public boolean isAvailable(String file){
        try {
            return this.client.isAvailable(file);
        } catch (IOException e) {
            this.addStatus("Error: " + e.getMessage());
        }
        return false;
    }

    public void setClient (FileSharingClient client){
        this.client = client;
        this.addStatus("Set parent directory: " + this.client.getPath());
        this.getList();
        this.itemSelection.setModel(listModel);
    }

    public void addStatus(String status){
        this.status.append(status + "\n");
    }

    public void getList(){
        this.addStatus("Fetching Files ...");
        Set<String> files = this.client.getCurrentlySharedFiles();
        listModel.clear();
        for (String file : files){
            listModel.addElement(file);
        }
        this.addStatus("Files Fetched!");
    }
}
