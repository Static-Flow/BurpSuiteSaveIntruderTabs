package main.java.com.staticflow;

import burp.IHttpService;
import burp.IIntruderAttack;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * This class implements the IIntruderAttack which represents the data contained in an Intruder Tab.
 * It has an {@link HttpService} which holds the protocol, host, and port the Intruder attack is for
 * and the bytes of the Intruder request with the injection markers.
 */
public class IntruderAttack implements IIntruderAttack, Serializable {

    private final HttpService service;
    private final byte[] requestTemplate;

    /**
     * Constructor for an IntruderAttack which represents the data required to build an Intruder tab in Burp Suite
     * @param service The {@link HttpService} which holds the protocol, host, and port the Intruder attack is for
     * @param requestTemplate The bytes of the Intruder Request
     */
    @JsonCreator
    public IntruderAttack(@JsonProperty("httpService") HttpService service, @JsonProperty("requestTemplate") byte[] requestTemplate) {
        this.service = service;
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
