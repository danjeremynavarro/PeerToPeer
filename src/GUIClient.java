import javax.swing.*;
import java.io.File;
import java.util.ArrayList;

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
        this.pack();
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
                this.client.
            }
        });
    }

    public void setClient (FileSharingClient client){
        this.client = client;
        this.getList();
        this.itemSelection.setModel(listModel);
    }

    public void addStatus(String status){
        this.status.append(status + "\n");
    }

    public void getList(){
        this.addStatus("Fetching Files ...");
        ArrayList<File> files = this.client.getFilesToShare();
        listModel.clear();
        for (File file : files){
            listModel.addElement(file.getName());
        }
        this.addStatus("Files Fetched!");
    }
}
