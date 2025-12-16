package Caravane.events;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Objects;




public class FileChangedEvent {


    private final String filename;
    private final String chngetype;
    private final String newcontent;


    public FileChangedEvent(String filename, String chngetype, String newcontent) {
        this.filename = filename;
        this.chngetype = chngetype;
        this.newcontent = newcontent;
    }

    public String getFilename() {
        return filename;
    }

    public String getChngetype() {
        return chngetype;
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        FileChangedEvent that = (FileChangedEvent) o;
        return Objects.equals(filename, that.filename) && Objects.equals(chngetype, that.chngetype) && Objects.equals(newcontent, that.newcontent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(filename, chngetype, newcontent);
    }

    @Override
    public String toString() {
        return "FileChangedEvent{" +
                "filename='" + filename + '\'' +
                ", chngetype='" + chngetype + '\'' +
                '}';
    }
}
