package Caravane.events;


import org.springframework.stereotype.Component;

import java.util.Objects;



@Component
public class FileChangedEvent {

    private final String filename;
    private final String chngetype;


    public FileChangedEvent(String filename, String chngetype) {
        this.filename = filename;
        this.chngetype = chngetype;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        FileChangedEvent that = (FileChangedEvent) o;
        return Objects.equals(filename, that.filename) && Objects.equals(chngetype, that.chngetype);
    }

    @Override
    public int hashCode() {
        return Objects.hash(filename, chngetype);
    }

    public String getFilename() {
        return filename;
    }

    public String getChngetype() {
        return chngetype;
    }


    @Override
    public String toString() {
        return "FileChangedEvent{" +
                "filename='" + filename + '\'' +
                ", chngetype='" + chngetype + '\'' +
                '}';
    }
}
