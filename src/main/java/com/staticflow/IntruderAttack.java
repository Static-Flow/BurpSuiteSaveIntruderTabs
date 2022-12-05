package main.java.com.staticflow;

import burp.IHttpService;
import burp.IIntruderAttack;

import java.io.Serializable;

/**
 * This class implements the IIntruderAttack which represents the data contained in an Intruder Tab.
 * It has an {@link HttpService} which holds the protocol, host, and port the Intruder attack is for
 * and the bytes of the Intruder request with the injection markers.
 */
public class IntruderAttack implements IIntruderAttack, Serializable {

    private final HttpService service;
    private final byte[] requestTemplate;

    public IntruderAttack(IHttpService service, byte[] requestTemplate) {
        this.service = (HttpService) service;
        this.requestTemplate = requestTemplate;
    }

    @Override
    public IHttpService getHttpService() {
        return this.service;
    }

    @Override
    public byte[] getRequestTemplate() {
        return this.requestTemplate;
    }
}
