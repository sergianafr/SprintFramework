package src.mg.itu.prom16.utils;

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

   public void save(String filePath) throws IOException {
    try {
        // Create the full path
        Path path = Paths.get(filePath);
        System.out.println("Absolute path: " + path.toAbsolutePath());

        // Get the parent directory of the file
        Path parentDir = path.getParent();

        // Create directories if they don't exist
        if (parentDir != null && !Files.exists(parentDir)) {
            Files.createDirectories(parentDir);
        }

        // Check directory permissions
        System.out.println("Directory readable: " + Files.isReadable(parentDir));
        System.out.println("Directory writable: " + Files.isWritable(parentDir));

        

        // Write the file content to the specified path
        Files.write(path, getBytes());
        System.out.println("File written successfully: " + path);
    } catch (AccessDeniedException e) {
        System.err.println("Access denied: Unable to write to the specified path: " + filePath);
        e.printStackTrace();
        throw e; // Re-throw the exception if needed
    } catch (IOException e) {
        System.err.println("An I/O error occurred: " + e.getMessage());
        e.printStackTrace();
        throw e; // Re-throw the exception if needed
    }
}
}
