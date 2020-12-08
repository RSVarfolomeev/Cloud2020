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
                            Path file = Paths.get(message.replace("Server ready for upload - ", ""));
                            if (Files.exists(file)){
                                inFile = new FileInputStream(String.valueOf(file));
                                byte[] buffer = new byte[inFile.available()];
                                // считываем файл в буфер
                                inFile.read(buffer, 0, buffer.length);
                                // записываем из буфера в поток
                                out.write(buffer, 0, buffer.length);
                            }
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
