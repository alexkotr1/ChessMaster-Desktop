module com.alexk.chessgui {
    requires tyrus.standalone.client;
    requires com.fasterxml.jackson.databind;
    requires java.desktop;
    requires javafx.web;
    requires java.logging;
    exports com.alexk.chess;
    exports com.alexk.chess.Pionia;
    exports com.alexk.chess.ChessBoard;
    exports com.alexk.chess.ChessEngine;
    opens com.alexk.chess.Pionia to com.fasterxml.jackson.databind;
    opens com.alexk.chess.ChessBoard to com.fasterxml.jackson.databind;
    exports com.alexk.chess.Serializers;
    opens com.alexk.chess.Serializers to com.fasterxml.jackson.databind;
}