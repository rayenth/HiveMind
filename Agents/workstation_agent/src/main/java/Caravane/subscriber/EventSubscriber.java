package Caravane.subscriber;

import Caravane.events.FileChangedEvent;

public interface EventSubscriber {

    public void handlefilechanged(FileChangedEvent event);





}
