package Caravane.publisher;

import Caravane.events.FileChangedEvent;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;





@Component
public class ListnerPublisher {



    @Autowired
private ApplicationEventPublisher ap;
    @Value("${watch.path}")
    private Path pathToWatch;


    @PostConstruct
    public void init() {
        startwatching(); // starts thread automatically after bean creation
    }






public void startwatching(){
    new Thread(() -> {
        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {



            //this will define what kind of changes the watcher will listens to in our case we the creation and the modification and the delition
            pathToWatch.register(watchService,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE,
                    StandardWatchEventKinds.ENTRY_MODIFY);

            //now we will use something callsed the watchkey this objects will be the key to access all the event that happened
            while (true){


                WatchKey key = watchService.take();


                //just making the output more human-readable
                for (WatchEvent<?> event : key.pollEvents()){
                    WatchEvent.Kind<?> kind = event.kind();
                    String filename = event.context().toString();
                    String typechange;

                    //i can't use a switch because the kind is not an enum it is an object
                    if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                        typechange = "Created new folder or file";
                    } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                        typechange = "Deleted a file or folder";
                    } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                        typechange = "Changed a file or folder";
                    } else {
                        typechange = "Type change not registered in the system";
                    }

                    // Publish the event
                    ap.publishEvent(new FileChangedEvent(filename, typechange));
                } key.reset();
            } } catch (Exception e) {
            e.printStackTrace(); }
    }).start();
}
}