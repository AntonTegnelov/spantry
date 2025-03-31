package com.spantry.testsupport;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A simple Logback appender that stores logging events in a thread-safe list for testing purposes.
 */
public class ListAppender extends AppenderBase<ILoggingEvent> {

  // Use a synchronized list for basic thread safety, though tests usually run sequentially
  private final List<ILoggingEvent> events = Collections.synchronizedList(new ArrayList<>());

  /** Default constructor. */
  public ListAppender() {
    super();
    // Default constructor added to satisfy PMD rule
  }

  @Override
  protected void append(final ILoggingEvent eventObject) {
    // Ensure the event is fully processed before adding (e.g., caller data)
    eventObject.prepareForDeferredProcessing();
    events.add(eventObject);
  }

  /**
   * Returns a snapshot of the captured logging events.
   *
   * @return A new list containing the events captured so far.
   */
  public List<ILoggingEvent> getEvents() {
    // Return a copy to prevent modification issues and ConcurrentModificationException
    synchronized (events) {
      return new ArrayList<>(events);
    }
  }

  /** Clears all captured events. */
  public void clearEvents() {
    events.clear();
  }
}
