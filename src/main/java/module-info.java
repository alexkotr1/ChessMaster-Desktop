module com.alexk.chessgui {
    requires javafx.web;
    requires javafx.media;
    requires tyrus.standalone.client;
    requires com.fasterxml.jackson.databind;
    requires java.desktop;
    exports com.alexk.chess;
}