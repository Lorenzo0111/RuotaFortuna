package game.packets.server;

/**
 * Rappresenta un tentativo di indovinare una frase o una lettera errata. Come dati contiene il gioco serializzato
 * Inviato dal server al client errante
 */
public class ErrorPacket extends UpdatePacket {

    public ErrorPacket() {
        super("ERROR");
    }
}
