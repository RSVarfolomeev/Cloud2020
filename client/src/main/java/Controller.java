import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class Controller implements Initializable {

    public ListView<String> listView;
    public TextField txt;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private FileInputStream inFile;

    public void sendMessage(ActionEvent event) throws IOException {
        String text = txt.getText();
        out.writeUTF(text);
        out.flush();
        txt.clear();
    }

    private void initStreams() throws IOException {
        socket = new Socket("localhost", 8189);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            initStreams();
            Thread reader = new Thread(() -> {
                try {
                    while (true) {
                        String message = in.readUTF();
                        listView.getItems().add(message);
                        if (message.startsWith("Server ready for upload -")){
                            out.writeUTF("Go Upload!");
                            out.flush();

//                            Upload: E:\test.txt
//                            Upload: E:\troll.jpg

                            Path filePath = Paths.get(message.replace("Server ready for upload - ", ""));
                            String fileName = filePath.getFileName().toString();
                            System.out.println(fileName);
                            out.writeUTF(fileName);
                            File file = new File(filePath.toString());
                            System.out.println(filePath.toString());
                            long size = file.length();
                            out.writeLong(size);
                            byte[] buffer = new byte[255];
                            FileInputStream fis = new FileInputStream(file);
                            for (int i = 0; i < (size + 255) / 256; i++) {
                                int read = fis.read(buffer);
                                out.write(buffer, 0, read);
                            }
                            out.flush();
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Exception while read!");
                }
            });
            reader.setDaemon(true);
            reader.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
//        listView.addEventHandler(EventType.ROOT, event -> {
//            if (event.getEventType().toString().equals("MOUSE_RELEASED")) {
//                int index = listView.getSelectionModel().getSelectedIndex();
//                listView.getItems().remove(index);
//            }
//        });
    }
}
