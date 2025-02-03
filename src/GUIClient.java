import javax.swing.*;
import java.awt.*;
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

    GUIClient() {
        this.setContentPane(panel1);
        this.setupButtonListeners();
        this.itemSelection.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.pack();
        this.setTitle("Peer to Peer Client");
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setSize(800, 600);
        this.setLocationRelativeTo(null);
    }

    private void setupButtonListeners() {
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
                if (this.isAvailable(getSelectedFile)) {
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

    public boolean isAvailable(String file) {
        try {
            return this.client.isAvailable(file);
        } catch (IOException e) {
            this.addStatus("Error: " + e.getMessage());
        }
        return false;
    }

    public void setClient(FileSharingClient client) {
        this.client = client;
        this.addStatus("Set parent directory: " + this.client.getPath());
        this.getList();
        this.itemSelection.setModel(listModel);
    }

    public void addStatus(String status) {
        this.status.append(status + "\n");
    }

    public void getList() {
        this.addStatus("Fetching Files ...");
        Set<String> files = this.client.getCurrentlySharedFiles();
        listModel.clear();
        for (String file : files) {
            listModel.addElement(file);
        }
        this.addStatus("Files Fetched!");
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        panel1 = new JPanel();
        panel1.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel2, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        syncLabel = new JLabel();
        syncLabel.setText("Sync");
        panel2.add(syncLabel, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        syncNowButton = new JButton();
        syncNowButton.setText("Sync Now");
        panel2.add(syncNowButton, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 2, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Available Files");
        panel2.add(label1, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        itemSelection = new JList();
        final DefaultListModel defaultListModel1 = new DefaultListModel();
        itemSelection.setModel(defaultListModel1);
        panel2.add(itemSelection, new com.intellij.uiDesigner.core.GridConstraints(1, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel2.add(panel3, new com.intellij.uiDesigner.core.GridConstraints(1, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        fetchFilesButton = new JButton();
        fetchFilesButton.setText("Fetch Files");
        panel3.add(fetchFilesButton, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        downloadButton = new JButton();
        downloadButton.setText("Download");
        panel3.add(downloadButton, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        panel1.add(scrollPane1, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        status = new JTextArea();
        status.setText("");
        scrollPane1.setViewportView(status);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panel1;
    }
}
