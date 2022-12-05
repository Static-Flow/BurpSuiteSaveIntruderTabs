package main.java.com.staticflow;

import burp.IHttpService;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * This class implements the IHttpService which is used to hold the host, port, and protocol of an Intruder attack
 */
public class HttpService implements IHttpService, Serializable {

    private final String  protocol;
    private final String  host;
    private final int     port;

    /**
     * Constructor for HttpService, converts the target input field in an Intruder tab into a HttpService object
     * @param target contents of the target input field in an Intruder tab
     * @throws MalformedURLException if the target set by the user is not a valid URL
     */
    public HttpService(String target) throws MalformedURLException {
        //convert target string into a URL
        URL url = new URL(target);
        //extract host from URL
        this.host = url.getHost();
        //extract protocol from URL
        this.protocol = url.getProtocol();
        // If the URL specifies a port
        if(url.getPort() != -1) {
            //extract the port from the URL
           this.port = url.getPort();
        } else {
            //If no port is specified, use the default port for the URL protocol
           this.port = url.getDefaultPort();
        }
    }

    @Override
    public String getHost() {
        return this.host;
    }

    @Override
    public int getPort() {
        return this.port;
    }

    @Override
    public String getProtocol() {
        return this.protocol;
    }
}
