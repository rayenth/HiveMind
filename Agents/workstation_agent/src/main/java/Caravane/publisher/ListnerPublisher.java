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
        startwatching(); // starts thread automatically after bean creation
    }


    public void startwatching() {
        new Thread(() -> {
            try (WatchService watchService = FileSystems.getDefault().newWatchService()) {


                //this will define what kind of changes the watcher will listens to in our case we the creation and the modification and the delition
                pathToWatch.register(watchService,
                        StandardWatchEventKinds.ENTRY_CREATE,
                        StandardWatchEventKinds.ENTRY_DELETE,
                        StandardWatchEventKinds.ENTRY_MODIFY);

                //now we will use something callsed the watchkey this objects will be the key to access all the event that happened
                while (true) {


                    WatchKey key = watchService.take();

                    String filename = "";
                    String typechange = "";
                    Path file;
                    String newcontent = "";



                    //just making the output more human-readable
                    for (WatchEvent<?> event : key.pollEvents()) {
                        WatchEvent.Kind<?> kind = event.kind();
                        filename = event.context().toString();
                        typechange = "";
                        file = pathToWatch.resolve((Path) event.context());
                        newcontent = "";


                        //i can't use a switch because the kind is not an enum it is an object
                        if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                            typechange = "Created new folder or file";

                        } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                            typechange = "Deleted a file or folder";

                        } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                            typechange = "Changed a file or folder";
                            newcontent = readFromLastPointer(file);


                        } else {
                            typechange = "Type change not registered in the system";
                        }


                        // Publish the event
                        
                    }
                    ap.publishEvent(new FileChangedEvent(filename, typechange, newcontent));
                    key.reset();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private String readFromLastPointer(Path file) throws IOException {
        // Check if file exists
        if (!Files.exists(file)) {
            System.out.println("File does not exist: " + file);
            lastPointer.remove(file);
            return null;
        }

        long pointer = lastPointer.getOrDefault(file, 0L);

        try (RandomAccessFile raf = new RandomAccessFile(file.toFile(), "r")) {
            long fileLength = raf.length();

            // Handle truncation: if file got smaller, reset pointer to 0
            if (fileLength < pointer) {
                pointer = 0;
            }

            raf.seek(pointer);

            // If pointer now equals file length, nothing new to read
            if (fileLength == pointer) {
                return "";
            }

            byte[] buffer = new byte[(int) (fileLength - pointer)];
            raf.readFully(buffer);

            long newPointer = raf.getFilePointer();
            lastPointer.put(file, newPointer);

            return new String(buffer);
        }
    }
}