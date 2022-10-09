package com.slimenano.sdk.console;

import org.fusesource.jansi.Ansi;

@FunctionalInterface
public interface Dealer {

    Ansi deal(Ansi ansi);

}
