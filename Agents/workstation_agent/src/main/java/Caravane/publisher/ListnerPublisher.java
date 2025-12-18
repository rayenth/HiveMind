package Caravane.publisher;

import Caravane.events.FileChangedEvent;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

@Component
public class ListnerPublisher {

    @Autowired
    private ApplicationEventPublisher ap;

    @Value("${watch.path}")
    private Path pathToWatch;

    private final Map<Path, Long> lastPointer = new HashMap<>();

    @PostConstruct
    public void init() {
        startwatching();
    }

    public void startwatching() {
        new Thread(() -> {
            try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
                // Register the directory for events
                pathToWatch.register(watchService,
                        StandardWatchEventKinds.ENTRY_CREATE,
                        StandardWatchEventKinds.ENTRY_DELETE,
                        StandardWatchEventKinds.ENTRY_MODIFY);

                while (true) {
                    WatchKey key = watchService.take(); // Wait for OS event

                    for (WatchEvent<?> event : key.pollEvents()) {
                        WatchEvent.Kind<?> kind = event.kind();
                        String filename = event.context().toString();
                        Path file = pathToWatch.resolve((Path) event.context());

                        String typechange = "";
                        String newcontent = "";

                        if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                            typechange = "CREATED";
                            lastPointer.put(file, 0L);
                        } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                            typechange = "DELETED";
                            lastPointer.remove(file);
                        } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                            typechange = "MODIFIED";
                            // Small delay for Linux filesystem sync
                            Thread.sleep(100);
                            newcontent = readFromLastPointer(file);
                        }

                        // Publish the event ONLY if there is actual data to send
                        if (newcontent != null && !newcontent.isEmpty()) {
                            System.out.println("Event Triggered for: " + filename);
                            ap.publishEvent(new FileChangedEvent(filename, typechange, newcontent));
                        }
                    }
                    key.reset();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private String readFromLastPointer(Path file) throws IOException {
        if (!Files.exists(file) || Files.isDirectory(file)) {
            lastPointer.remove(file);
            return null;
        }

        long pointer = lastPointer.getOrDefault(file, 0L);

        try (RandomAccessFile raf = new RandomAccessFile(file.toFile(), "r")) {
            long fileLength = raf.length();
            if (fileLength < pointer) pointer = 0;
            if (fileLength == pointer) return null;

            raf.seek(pointer);
            byte[] buffer = new byte[(int) (fileLength - pointer)];
            raf.readFully(buffer);

            lastPointer.put(file, raf.getFilePointer());
            return new String(buffer).trim();
        }
    }
}