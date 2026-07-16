package de.gigabitzauber.jancontrol.cruise;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;

public interface JcSchedulable {
    void schedule(ListeningScheduledExecutorService executor, FutureCallback<Object> callback);
}
