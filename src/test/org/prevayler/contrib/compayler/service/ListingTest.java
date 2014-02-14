package org.prevayler.contrib.compayler.service;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ListingTest {

  @Test
  public void testMerged() {
    Listing listing = new Listing();
    listing.add("");
    assertEquals("", listing.list().get(0));
    listing.inc();
    listing.add("");
    assertEquals("  ", listing.list().get(1));
    listing.dec();
    listing.add("");
    assertEquals("", listing.list().get(2));
  }

}
