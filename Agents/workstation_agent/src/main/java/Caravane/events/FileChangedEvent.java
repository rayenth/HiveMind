package Caravane.events;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Objects;




public class FileChangedEvent {


    private final String filename;
    private String changetype;
    private final String newcontent;


    public FileChangedEvent(String filename, String chngetype, String newcontent) {
        this.filename = filename;
        this.changetype = chngetype;
        this.newcontent = newcontent;
    }

    public String getFilename() {
        return filename;
    }

    public String getChngetype() {
        return changetype;
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        FileChangedEvent that = (FileChangedEvent) o;
        return Objects.equals(filename, that.filename) && Objects.equals(changetype, that.changetype) && Objects.equals(newcontent, that.newcontent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(filename, changetype, newcontent);
    }

    public String getNewcontent() {
        return newcontent;
    }

    @Override
    public String toString() {
        return "FileChangedEvent{" +
                "filename='" + filename + '\'' +
                ", chngetype='" + changetype + '\'' +
                '}';
    }

    public Object getTypechange() {
        return changetype;
    }

    public void setTypechange(Object typechange) {
        this.changetype = typechange.toString();
    }
}
