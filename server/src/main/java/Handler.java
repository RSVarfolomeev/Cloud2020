import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Handler implements Runnable {

    private static int inc = 0;
    private String userName;
    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private FileOutputStream outFile;
    private boolean running;

    public Handler(Server server, Socket socket) throws IOException {
        this.server = server;
        this.socket = socket;
        inc++;
        userName = "User" + inc;
        initStreams();
        running = true;
    }

    private void initStreams() throws IOException {
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
    }

    private String readMessage() throws IOException {
        return in.readUTF();
    }

    public void writeMessage(String message) throws IOException {
        out.writeUTF(message);
        out.flush();
    }

    private String wrapMessageWithName(String message) {
        return userName + ": " + message;
    }

    @Override
    public void run() {
        try {
            while (running) {
                String message = readMessage();
                System.out.println("Received: " + message);
                server.sendMessageForAll(wrapMessageWithName(message));

                if (message.startsWith("Upload: ")) {
                    server.sendMessageForAll("Server ready for upload - " + message.replace("Upload: ", ""));
                    Files.createFile(Paths.get("D:\\java\\Cloud\\server\\FileServer\\test.txt"));
                    outFile = new FileOutputStream("D:\\java\\Cloud\\server\\FileServer\\test.txt");
                    byte[] buffer = new byte[in.available()];
                    // считываем входящий поток
                    in.read(buffer, 0, buffer.length);
                    // записываем из буфера в файл
                    outFile.write(buffer, 0, buffer.length);
                }
            }
        } catch (Exception e) {
            System.err.println("Exception while read or write!");
            server.kick(this);
        } finally {
            close();
        }
    }

    public void close() {
        try {
            out.close();
            in.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
