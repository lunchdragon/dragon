package dragon.comm;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.*;
import org.apache.commons.httpclient.protocol.Protocol;

import java.io.*;
import java.util.Map;

public class EasyHttpClient {
    private boolean useHttps;
    private String server;
    private int port;
    private HttpClient httpclient = null;
    private int statusCode;
    private String statusText;
    private Map<String, String> headers;
    private String responseText;
    private Boolean basic = false;

    public EasyHttpClient(String server, int port) {
        this(server, port, true, false);
    }

    public EasyHttpClient(String server, int port, boolean useHttps) {
        this.server = server;
        this.port = port;
        this.useHttps = useHttps;

        init();
    }

    public EasyHttpClient(String server, int port, boolean useHttps, boolean basic) {
        this.server = server;
        this.port = port;
        this.useHttps = useHttps;
        this.basic = basic;

        init();
    }

    private void init() {
        httpclient = new HttpClient();
        if(basic){
            httpclient.getParams().setAuthenticationPreemptive(true);
        }

        if (useHttps) {
            Protocol myhttps = new Protocol("https", new EasySSLProtocolSocketFactory(), port);
            httpclient.getHostConfiguration().setHost(server, port, myhttps);
        } else {
            httpclient.getHostConfiguration().setHost(server, port, "http");
        }
    }

    public HttpClient getHttpclient() {
        return httpclient;
    }

    public void setCredentials(String username, String password) {
        Credentials credentials = new UsernamePasswordCredentials(username, password);
        httpclient.getState().setCredentials(new AuthScope(server, port), credentials);
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String executeGet(String uri) throws IOException {
        GetMethod httpGet = new GetMethod(uri);
        setHeader(httpGet);
        try {
            httpclient.executeMethod(httpclient.getHostConfiguration(), httpGet, httpclient.getState());
            statusCode = httpGet.getStatusCode();
            statusText = httpGet.getStatusText();
            responseText = httpGet.getResponseBodyAsString();

            return responseText;
        } finally {
            httpGet.releaseConnection();
        }
    }

    public void download(String uri, File file) throws IOException {
        GetMethod httpGet = new GetMethod(uri);
        setHeader(httpGet);
        try {
            httpclient.executeMethod(httpGet);
            statusCode = httpGet.getStatusCode();
            statusText = httpGet.getStatusText();
            InputStream in = null;
            OutputStream out = null;
            try {
                in = new BufferedInputStream(httpGet.getResponseBodyAsStream());
                out = new BufferedOutputStream(new FileOutputStream(file));
                byte[] buffer = new byte[8192];
                int count = 0;
                while ((count = in.read(buffer)) != -1) {
                    out.write(buffer, 0, count);
                }
            } finally {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            }
        } finally {
            httpGet.releaseConnection();
        }
    }

    public int executePut(String uri, String data) throws IOException {
        RequestEntity requestEntity = new StringRequestEntity(data, "text/xml", "UTF-8");
        return executePut(uri, requestEntity);
    }

    public int executePut(String uri, InputStream in, String contentType) throws IOException {
        return executePut(uri, new InputStreamRequestEntity(in, contentType));
    }

    public int executePut(String uri, File file, String contentType) throws IOException {
        return executePut(uri, new FileRequestEntity(file, contentType));
    }

    private int executePut(String uri, RequestEntity requestEntity) throws IOException {
        PutMethod httpPut = new PutMethod(uri);
        setHeader(httpPut);
        httpPut.setRequestEntity(requestEntity);
        try {
            httpclient.executeMethod(httpPut);
            statusCode = httpPut.getStatusCode();
            statusText = httpPut.getStatusText();
            responseText = httpPut.getResponseBodyAsString();
            return statusCode;
        } finally {
            httpPut.releaseConnection();
        }
    }

    public int executePost(String uri, String data) throws IOException {
        RequestEntity requestEntity = new StringRequestEntity(data, "text/xml", "UTF-8");
        return executePost(uri, requestEntity);
    }

    public int executePost(String uri, InputStream in, String contentType) throws IOException {
        return executePost(uri, new InputStreamRequestEntity(in, contentType));
    }

    public int executePost(String uri, File file, String contentType) throws IOException {
        return executePost(uri, new FileRequestEntity(file, contentType));
    }

    private int executePost(String uri, RequestEntity requestEntity) throws IOException {
        PostMethod httpPost = new PostMethod(uri);
        setHeader(httpPost);
        httpPost.setRequestEntity(requestEntity);
        try {
            httpclient.executeMethod(httpPost);
            statusCode = httpPost.getStatusCode();
            statusText = httpPost.getStatusText();
            responseText = httpPost.getResponseBodyAsString();
            return statusCode;
        } finally {
            httpPost.releaseConnection();
        }
    }

    public int executeDelete(String uri) throws IOException {
        DeleteMethod httpDelete = new DeleteMethod(uri);
        setHeader(httpDelete);
        try {
            httpclient.executeMethod(httpDelete);
            statusCode = httpDelete.getStatusCode();
            statusText = httpDelete.getStatusText();
            responseText = httpDelete.getResponseBodyAsString();
            return statusCode;
        } finally {
            httpDelete.releaseConnection();
        }
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getStatusText() {
        return statusText;
    }

    public String getResponseText() {
        return responseText;
    }

    private void setHeader(HttpMethod httpMethod) {
        if (headers != null) {
            for (String key : headers.keySet()) {
                String value = headers.get(key);
                httpMethod.setRequestHeader(key, value);
            }
        }
    }
}
