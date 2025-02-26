package src.mg.itu.prom16.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import jakarta.servlet.http.Part;
import src.mg.itu.prom16.FrontController;

public class FilePart {
    private final Part part;
    public FilePart(Part part) {
        this.part = part;
    }

    public byte[] getBytes() throws IOException {
        return part.getInputStream().readAllBytes();
    }

    public String getSubmittedFileName() {
        return part.getSubmittedFileName();
    }

    public InputStream getInputStream() throws IOException {
        return part.getInputStream();
    }

    public long getSize() {
        return part.getSize();
    }

    public boolean isEmpty() {
        return part.getSize() == 0;
    }
    public void save(String destinationPath) throws IOException {
        File file = new File(destinationPath);
        try (InputStream inputStream = part.getInputStream();
             FileOutputStream outputStream = new FileOutputStream(file)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
    }

}
