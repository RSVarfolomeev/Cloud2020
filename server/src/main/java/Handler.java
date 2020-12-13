import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Handler implements Runnable {

    private String userDir = "server/FileServer";

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
                }
                if (message.startsWith("Go Upload!")) {
                    String fileName = in.readUTF();
                    System.out.println(fileName);
                    long size = in.readLong();
                    byte[] buffer = new byte[256];
                    Path path = Paths.get(userDir, fileName);
                    if (Files.notExists(path)) {
                        Files.createFile(path);
                    }
                    FileOutputStream fos = new FileOutputStream(new File(userDir + "/" + fileName));
                    for (int i = 0; i < (size + 255) / 256; i++) {
                        if (i == (size + 255) / 256 - 1) {
                            for (int j = 0; j < size % 256; j++) {
                                fos.write(in.readByte());
                            }
                        } else {
                            int read = in.read(buffer);
                            fos.write(buffer, 0, read);
                        }
                    }
                    fos.close();
//                    Files.createFile(Paths.get("D:\\java\\Cloud\\server\\FileServer\\test.txt"));
//                    outFile = new FileOutputStream("D:\\java\\Cloud\\server\\FileServer\\test.txt");
//                    byte[] buffer = new byte[in.available()];
//                    // считываем входящий поток
//                    in.read(buffer, 0, buffer.length);
//                    // записываем из буфера в файл
//                    outFile.write(buffer, 0, buffer.length);
//                    outFile.close();
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
