package src.mg.itu.prom16.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.Part;
import src.mg.itu.prom16.FrontController;

public class FilePart {
    private final Part part;
    private final ServletContext context;
    public FilePart(Part part, ServletContext context) {
        this.part = part;
        this.context = context;
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
    
    public void save(String filePath) throws IOException {
        String projectRoot = context.getRealPath("/");
        // Create the full path
        Path path = Paths.get(projectRoot, "static", filePath, getSubmittedFileName());

        // Get the parent directory of the file
        Path parentDir = path.getParent();

        System.out.println("chemin complet : " + parentDir);

        // Create directories if they don't exist
        if (parentDir != null && !Files.exists(parentDir)) {
            Files.createDirectories(parentDir);
        }

        // Write the file content to the specified path
        Files.write(path, getBytes());
    }
    // public void save(String destinationPath) throws IOException {
    //     File file = new File(destinationPath);
    //     try (InputStream inputStream = part.getInputStream();
    //          FileOutputStream outputStream = new FileOutputStream(file)) {
    //         byte[] buffer = new byte[1024];
    //         int bytesRead;
    //         while ((bytesRead = inputStream.read(buffer)) != -1) {
    //             outputStream.write(buffer, 0, bytesRead);
    //         }
    //     }
    // }

}
