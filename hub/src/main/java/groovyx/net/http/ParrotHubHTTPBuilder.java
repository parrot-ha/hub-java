/**
 * Copyright (c) 2021-2022 by the respective copyright holders.
 * All rights reserved.
 * <p>
 * This file is part of Parrot Home Automation Hub.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * This is the original license of this file and it applies to any changes made below
 *
 * Copyright 2008-2011 Thomas Nichols.  http://blog.thomnichols.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You are receiving this code free of charge, which represents many hours of
 * effort from other individuals and corporations.  As a responsible member
 * of the community, you are encouraged (but not required) to donate any
 * enhancements or improvements back to the community under a similar open
 * source license.  Thank you. -TMN
 *
 *
 * This is a copy of HTTPBuilder from the org.codehaus.groovy.modules.http-builder:http-builder:0.7.1
 * which is out of date and does not work with the latest groovy runtime.  It includes fixes to make it
 * work with newer versions of groovy and removes unused methods.
 */
package groovyx.net.http;

import groovy.lang.Closure;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.codehaus.groovy.runtime.MethodClosure;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import static groovyx.net.http.URIBuilder.convertToURI;

public class ParrotHubHTTPBuilder {
    //+--- org.codehaus.groovy.modules.http-builder:http-builder:0.7.1
    //|    +--- org.apache.httpcomponents:httpclient:4.2.1 -> 4.5.13
    //|    |    +--- org.apache.httpcomponents:httpcore:4.4.13
    //|    |    +--- commons-logging:commons-logging:1.2
    //|    |    \--- commons-codec:commons-codec:1.11
    //|    +--- net.sf.json-lib:json-lib:2.3
    //|    |    +--- commons-beanutils:commons-beanutils:1.8.0
    //|    |    |    \--- commons-logging:commons-logging:1.1.1 -> 1.2
    //|    |    +--- commons-collections:commons-collections:3.2.1
    //|    |    +--- commons-lang:commons-lang:2.4
    //|    |    +--- commons-logging:commons-logging:1.1.1 -> 1.2
    //|    |    \--- net.sf.ezmorph:ezmorph:1.0.6
    //|    |         \--- commons-lang:commons-lang:2.3 -> 2.4
    //|    +--- net.sourceforge.nekohtml:nekohtml:1.9.16
    //|    |    \--- xerces:xercesImpl:2.9.1
    //|    |         \--- xml-apis:xml-apis:1.3.04
    //|    \--- xml-resolver:xml-resolver:1.2

    private HttpClient client;

    protected final Log log = LogFactory.getLog(getClass());

    protected Object defaultContentType = ContentType.ANY;
    protected Object defaultRequestContentType = null;
    protected boolean autoAcceptHeader = true;
    protected final Map<Object, Closure> defaultResponseHandlers =
            new StringHashMap<>(buildDefaultResponseHandlers());

    protected final Map<Object, Object> defaultRequestHeaders = new StringHashMap<>();

    protected EncoderRegistry encoders = new ParrotHubEncoderRegistry();
    protected ParserRegistry parsers = new ParserRegistry();

    /**
     * Creates a new instance with a <code>null</code> default URI.
     */
    public ParrotHubHTTPBuilder() {
        encoders.putAt(ContentType.ANY.toString(), new MethodClosure(encoders, "encodeText"));
    }

    /**
     * <p>Convenience method to perform an HTTP GET.  It will use the HTTPBuilder's
     * registered response handlers to handle success or
     * failure status codes.  By default, the <code>success</code> response
     * handler will attempt to parse the data and simply return the parsed
     * object.</p>
     *
     * <p><strong>Note:</strong> If using the {@link #defaultSuccessHandler(HttpResponseDecorator, Object)
     * default <code>success</code> response handler}, be sure to read the
     * caveat regarding streaming response data.</p>
     *
     * @param args see {@link ParrotHubHTTPBuilder.RequestConfigDelegate#setPropertiesFromMap(Map)}
     * @return whatever was returned from the response closure.
     * @throws URISyntaxException      if a uri argument is given which does not
     *                                 represent a valid URI
     * @throws IOException             IO Exception
     * @throws ClientProtocolException Client Protocol Exception
     * @see #defaultSuccessHandler(HttpResponseDecorator, Object)
     * @see #defaultFailureHandler(HttpResponseDecorator, Object)
     */
    public Object get(Map<String, ?> args)
            throws ClientProtocolException, IOException, URISyntaxException {
        return this.get(args, null);
    }

    /**
     * <p>Convenience method to perform an HTTP GET.  The response closure will
     * be called only on a successful response.  </p>
     *
     * <p>A 'failed' response (i.e. any HTTP status code > 399) will be handled
     * by the registered 'failure' handler.  The
     * {@link #defaultFailureHandler(HttpResponseDecorator, Object) default failure handler}
     * throws an {@link HttpResponseException}.</p>
     *
     * @param args            see {@link ParrotHubHTTPBuilder.RequestConfigDelegate#setPropertiesFromMap(Map)}
     * @param responseClosure code to handle a successful HTTP response
     * @return any value returned by the response closure.
     * @throws ClientProtocolException
     * @throws IOException
     * @throws URISyntaxException      if a uri argument is given which does not
     *                                 represent a valid URI
     */
    public Object get(Map<String, ?> args, Closure responseClosure)
            throws ClientProtocolException, IOException, URISyntaxException {
        ParrotHubHTTPBuilder.RequestConfigDelegate delegate = new ParrotHubHTTPBuilder.RequestConfigDelegate(new HttpGet(),
                this.defaultContentType,
                this.defaultRequestHeaders,
                this.defaultResponseHandlers);

        delegate.setPropertiesFromMap(args);
        if (responseClosure != null) delegate.getResponse().put(
                Status.SUCCESS, responseClosure);
        return this.doRequest(delegate);
    }

    /**
     * <p>Convenience method to perform an HTTP POST.  It will use the HTTPBuilder's
     * registered response handlers to handle success or
     * failure status codes.  By default, the <code>success</code> response
     * handler will attempt to parse the data and simply return the parsed
     * object. </p>
     *
     * <p><strong>Note:</strong> If using the {@link #defaultSuccessHandler(HttpResponseDecorator, Object)
     * default <code>success</code> response handler}, be sure to read the
     * caveat regarding streaming response data.</p>
     *
     * @param args see {@link ParrotHubHTTPBuilder.RequestConfigDelegate#setPropertiesFromMap(Map)}
     * @return whatever was returned from the response closure.
     * @throws IOException
     * @throws URISyntaxException      if a uri argument is given which does not
     *                                 represent a valid URI
     * @throws ClientProtocolException
     * @see #defaultSuccessHandler(HttpResponseDecorator, Object)
     * @see #defaultFailureHandler(HttpResponseDecorator, Object)
     */
    public Object post(Map<String, ?> args)
            throws ClientProtocolException, URISyntaxException, IOException {
        return this.post(args, null);
    }

    /**
     * <p>
     * Convenience method to perform an HTTP form POST.  The response closure will be
     * called only on a successful response.</p>
     *
     * <p>A 'failed' response (i.e. any
     * HTTP status code > 399) will be handled by the registered 'failure'
     * handler.  The {@link #defaultFailureHandler(HttpResponseDecorator, Object) default
     * failure handler} throws an {@link HttpResponseException}.</p>
     *
     * <p>The request body (specified by a <code>body</code> named parameter)
     * will be converted to a url-encoded form string unless a different
     * <code>requestContentType</code> named parameter is passed to this method.
     * (See {@link EncoderRegistry#encodeForm(Map)}.) </p>
     *
     * @param args            see {@link ParrotHubHTTPBuilder.RequestConfigDelegate#setPropertiesFromMap(Map)}
     * @param responseClosure code to handle a successful HTTP response
     * @return any value returned by the response closure.
     * @throws ClientProtocolException Client Protocol Exception
     * @throws IOException             IO Exception
     * @throws URISyntaxException      if a uri argument is given which does not
     *                                 represent a valid URI
     */
    public Object post(Map<String, ?> args, Closure responseClosure)
            throws URISyntaxException, ClientProtocolException, IOException {
        ParrotHubHTTPBuilder.RequestConfigDelegate delegate = new ParrotHubHTTPBuilder.RequestConfigDelegate(new HttpPost(),
                this.defaultContentType,
                this.defaultRequestHeaders,
                this.defaultResponseHandlers);

        /* by default assume the request body will be URLEncoded, but allow
           the 'requestContentType' named argument to override this if it is
           given */
        delegate.setRequestContentType(ContentType.URLENC.toString());
        delegate.setPropertiesFromMap(args);

        if (responseClosure != null) delegate.getResponse().put(
                Status.SUCCESS.toString(), responseClosure);

        return this.doRequest(delegate);
    }

    /**
     * All <code>request</code> methods delegate to this method.
     */
    protected Object doRequest(final ParrotHubHTTPBuilder.RequestConfigDelegate delegate)
            throws ClientProtocolException, IOException {
        delegate.encodeBody();
        final HttpRequestBase reqMethod = delegate.getRequest();

        final Object contentType = delegate.getContentType();

        if (this.autoAcceptHeader) {
            String acceptContentTypes = contentType.toString();
            if (contentType instanceof ContentType)
                acceptContentTypes = ((ContentType) contentType).getAcceptHeader();
            reqMethod.setHeader("Accept", acceptContentTypes);
        }

        reqMethod.setURI(delegate.getUri().toURI());
        if (reqMethod.getURI() == null)
            throw new IllegalStateException("Request URI cannot be null");

        log.debug(reqMethod.getMethod() + " " + reqMethod.getURI());

        // set any request headers from the delegate
        Map<?, ?> headers = delegate.getHeaders();
        for (Object key : headers.keySet()) {
            Object val = headers.get(key);
            if (key == null) continue;
            if (val == null) reqMethod.removeHeaders(key.toString());
            else reqMethod.setHeader(key.toString(), val.toString());
        }

        ResponseHandler<Object> responseHandler = new ResponseHandler<Object>() {
            public Object handleResponse(HttpResponse response)
                    throws ClientProtocolException, IOException {
                HttpResponseDecorator resp = new HttpResponseDecorator(
                        response, delegate.getContext(), null);
                try {
                    int status = resp.getStatusLine().getStatusCode();
                    Closure responseClosure = delegate.findResponseHandler(status);
                    log.debug("Response code: " + status + "; found handler: " + responseClosure);

                    Object[] closureArgs = null;
                    switch (responseClosure.getMaximumNumberOfParameters()) {
                        case 1:
                            closureArgs = new Object[]{resp};
                            break;
                        case 2: // parse the response entity if the response handler expects it:
                            HttpEntity entity = resp.getEntity();
                            try {
                                if (entity == null || entity.getContentLength() == 0)
                                    closureArgs = new Object[]{resp, null};
                                else closureArgs = new Object[]{resp, parseResponse(resp, contentType)};
                            } catch (Exception ex) {
                                Header h = entity.getContentType();
                                String respContentType = h != null ? h.getValue() : null;
                                log.warn("Error parsing '" + respContentType + "' response", ex);
                                throw new ResponseParseException(resp, ex);
                            }
                            break;
                        default:
                            throw new IllegalArgumentException(
                                    "Response closure must accept one or two parameters");
                    }

                    Object returnVal = responseClosure.call(closureArgs);
                    log.trace("response handler result: " + returnVal);

                    return returnVal;
                } finally {
                    HttpEntity entity = resp.getEntity();
                    if (entity != null) EntityUtils.consume(entity);
                }
            }
        };

        return getClient().execute(reqMethod, responseHandler, delegate.getContext());
    }

    /**
     * Parse the response data based on the given content-type.
     * If the given content-type is {@link ContentType#ANY}, the
     * <code>content-type</code> header from the response will be used to
     * determine how to parse the response.
     *
     * @param resp
     * @param contentType
     * @return whatever was returned from the parser retrieved for the given
     * content-type, or <code>null</code> if no parser could be found for this
     * content-type.  The parser will also return <code>null</code> if the
     * response does not contain any content (e.g. in response to a HEAD request).
     * @throws HttpResponseException if there is a error parsing the response
     */
    protected Object parseResponse(HttpResponse resp, Object contentType)
            throws HttpResponseException {
        // For HEAD or OPTIONS requests, there should be no response entity.
        if (resp.getEntity() == null) {
            log.debug("Response contains no entity.  Parsed data is null.");
            return null;
        }
        // first, start with the _given_ content-type
        String responseContentType = contentType.toString();
        // if the given content-type is ANY ("*/*") then use the response content-type
        try {
            if (ContentType.ANY.toString().equals(responseContentType))
                responseContentType = ParserRegistry.getContentType(resp);
        } catch (RuntimeException ex) {
            log.warn("Could not parse content-type: " + ex.getMessage());
            /* if for whatever reason we can't determine the content-type, but
             * still want to attempt to parse the data, use the BINARY
             * content-type so that the response will be buffered into a
             * ByteArrayInputStream. */
            responseContentType = ContentType.BINARY.toString();
        }

        Object parsedData = null;
        Closure parser = parsers.getAt(responseContentType);
        if (parser == null) log.warn("No parser found for content-type: "
                + responseContentType);
        else {
            log.debug("Parsing response as: " + responseContentType);
            parsedData = parser.call(resp);
            if (parsedData == null) log.warn("Parser returned null!");
            else log.debug("Parsed data to instance of: " + parsedData.getClass());
        }
        return parsedData;
    }

    /**
     * Creates default response handlers for {@link Status#SUCCESS success} and
     * {@link Status#FAILURE failure} status codes.  This is used to populate
     * the handler map when a new HTTPBuilder instance is created.
     *
     * @return the default response handler map.
     * @see #defaultSuccessHandler(HttpResponseDecorator, Object)
     * @see #defaultFailureHandler(HttpResponseDecorator, Object)
     */
    protected Map<Object, Closure> buildDefaultResponseHandlers() {
        Map<Object, Closure> map = new StringHashMap<Closure>();
        map.put(Status.SUCCESS,
                new MethodClosure(this, "defaultSuccessHandler"));
        map.put(Status.FAILURE,
                new MethodClosure(this, "defaultFailureHandler"));

        return map;
    }

    /**
     * <p>This is the default <code>response.success</code> handler.  It will be
     * executed if the response is not handled by a status-code-specific handler
     * (i.e. <code>response.'200'= {..}</code>) and no generic 'success' handler
     * is given (i.e. <code>response.success = {..}</code>.)  This handler simply
     * returns the parsed data from the response body.  In most cases you will
     * probably want to define a <code>response.success = {...}</code> handler
     * from the request closure, which will replace the response handler defined
     * by this method.  </p>
     *
     * <h4>Note for parsers that return streaming content:</h4>
     * <p>For responses parsed as {@link ParserRegistry#parseStream(HttpResponse)
     * BINARY} or {@link ParserRegistry#parseText(HttpResponse) TEXT}, the
     * parser will return streaming content -- an <code>InputStream</code> or
     * <code>Reader</code>.  In these cases, this handler will buffer the the
     * response content before the network connection is closed.  </p>
     *
     * <p>In practice, a user-supplied response handler closure is
     * <i>designed</i> to handle streaming content so it can be read directly from
     * the response stream without buffering, which will be much more efficient.
     * Therefore, it is recommended that request method variants be used which
     * explicitly accept a response handler closure in these cases.</p>
     *
     * @param resp       HTTP response
     * @param parsedData parsed data as resolved from this instance's {@link ParserRegistry}
     * @return the parsed data object (whatever the parser returns).
     * @throws ResponseParseException if there is an error buffering a streaming
     *                                response.
     */
    protected Object defaultSuccessHandler(HttpResponseDecorator resp, Object parsedData)
            throws ResponseParseException {
        try {
            //If response is streaming, buffer it in a byte array:
            if (parsedData instanceof InputStream) {
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                ((InputStream) parsedData).transferTo(buffer);
                //DefaultGroovyMethods.leftShift( buffer, (InputStream)parsedData );
                parsedData = new ByteArrayInputStream(buffer.toByteArray());
            } else if (parsedData instanceof Reader) {
                StringWriter buffer = new StringWriter();
                ((Reader) parsedData).transferTo(buffer);
                //DefaultGroovyMethods.leftShift( buffer, (Reader)parsedData );
                parsedData = new StringReader(buffer.toString());
            } else if (parsedData instanceof Closeable)
                log.warn("Parsed data is streaming, but will be accessible after " +
                        "the network connection is closed.  Use at your own risk!");
            resp.setData(parsedData);
            return resp;
            //return parsedData;
        } catch (IOException ex) {
            throw new ResponseParseException(resp, ex);
        }
    }

    /**
     * This is the default <code>response.failure</code> handler.  It will be
     * executed if no status-code-specific handler is set (i.e.
     * <code>response.'404'= {..}</code>).  This default handler will throw a
     * {@link HttpResponseException} when executed.  In most cases you
     * will want to define your own <code>response.failure = {...}</code>
     * handler from the request closure, if you don't want an exception to be
     * thrown for 4xx and 5xx status responses.
     *
     * @param resp
     * @throws HttpResponseException
     */
    protected void defaultFailureHandler(HttpResponseDecorator resp, Object data) throws HttpResponseException {
        resp.setData(defaultSuccessHandler(resp, data));
        throw new HttpResponseException(resp);
    }

    /**
     * Return the underlying HTTPClient that is used to handle HTTP requests.
     *
     * @return the client instance.
     */
    public HttpClient getClient() {
        if (client == null) {
            HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
            httpClientBuilder.addInterceptorLast(new GZIPEncoding().getRequestInterceptor());
            httpClientBuilder.addInterceptorLast(new GZIPEncoding().getResponseInterceptor());
            httpClientBuilder.addInterceptorLast(new DeflateEncoding().getRequestInterceptor());
            httpClientBuilder.addInterceptorLast(new DeflateEncoding().getResponseInterceptor());
            client = httpClientBuilder.build();
        }
        return client;
    }

    /**
     * <p>Encloses all properties and method calls used within the
     * {@link HTTPBuilder#request(Object, Method, Object, Closure)} 'config'
     * closure argument.  That is, an instance of this class is set as the
     * closure's delegate.  This allows the user to configure various parameters
     * within the scope of a single request.  </p>
     *
     * <p>All properties of this class are available from within the closure.
     * For example, you can manipulate various aspects of the
     * {@link HTTPBuilder#setUri(Object) default request URI} for this request
     * by calling <code>uri.path = '/api/location'</code>.  This allows for the
     * ability to modify parameters per-request while leaving any values set
     * directly on the HTTPBuilder instance unchanged for subsequent requests.
     * </p>
     */
    protected class RequestConfigDelegate {
        private HttpRequestBase request;
        private Object contentType;
        private Object requestContentType;
        private Map<Object, Closure> responseHandlers = new StringHashMap<>();
        private URIBuilder uri;
        private Map<Object, Object> headers = new StringHashMap<>();
        private HttpContextDecorator context = new HttpContextDecorator();
        private Object body;

        public RequestConfigDelegate(HttpRequestBase request, Object contentType,
                                     Map<?, ?> defaultRequestHeaders,
                                     Map<?, Closure> defaultResponseHandlers) {
            if (request == null) throw new IllegalArgumentException(
                    "Internal error - HttpRequest instance cannot be null");
            this.request = request;
            this.headers.putAll(defaultRequestHeaders);
            this.contentType = contentType;
            if (defaultRequestContentType != null)
                this.requestContentType = defaultRequestContentType.toString();
            this.responseHandlers.putAll(defaultResponseHandlers);
            URI uri = request.getURI();
            if (uri != null) this.uri = new URIBuilder(uri);
        }

        /**
         * Use this object to manipulate parts of the request URI, like
         * query params and request path.  Example:
         * <pre>
         * builder.request(GET,XML) {
         *   uri.path = '../other/request.jsp'
         *   uri.query = [p1:1, p2:2]
         *   ...
         * }</pre>
         *
         * @return {@link URIBuilder} to manipulate the request URI
         */
        public URIBuilder getUri() {
            return this.uri;
        }

        /**
         * Directly access the Apache HttpClient instance that will
         * be used to execute this request.
         *
         * @see HttpRequestBase
         */
        protected HttpRequestBase getRequest() {
            return this.request;
        }

        /**
         * Get the content-type of any data sent in the request body and the
         * expected response content-type.  If the request content-type is
         * expected to differ from the response content-type (i.e. a URL-encoded
         * POST that should return an HTML page) then this value will be used
         * for the <i>response</i> content-type, while
         * {@link #setRequestContentType(Object)} should be used for the request.
         *
         * @return whatever value was assigned via {@link #setContentType(Object)}
         * or passed from the {@link HTTPBuilder#defaultContentType} when this
         * RequestConfigDelegate instance was constructed.
         */
        protected Object getContentType() {
            return this.contentType;
        }

        /**
         * Set the content-type used for any data in the request body, as well
         * as the <code>Accept</code> content-type that will be used for parsing
         * the response. The value should be either a {@link ContentType} value
         * or a String, i.e. <code>"text/plain"</code>.  This will default to
         * {@link HTTPBuilder#getContentType()} for requests that do not
         * explicitly pass a <code>contentType</code> parameter (such as
         * {@link HTTPBuilder#request(Method, Object, Closure)}).
         *
         * @param ct the value that will be used for the <code>Content-Type</code>
         *           and <code>Accept</code> request headers.
         */
        protected void setContentType(Object ct) {
            if (ct == null) this.contentType = defaultContentType;
            else this.contentType = ct;
        }

        /**
         * The request content-type, if different from the {@link #contentType}.
         *
         * @return either a {@link ContentType} value or String like <code>text/plain</code>
         */
        protected Object getRequestContentType() {
            if (this.requestContentType != null) return this.requestContentType;
            else return this.getContentType();
        }

        /**
         * <p>Assign a different content-type for the request than is expected for
         * the response.  This is useful if i.e. you want to post URL-encoded
         * form data but expect the response to be XML or HTML.  The
         * {@link #getContentType()} will always control the <code>Accept</code>
         * header, and will be used for the request content <i>unless</i> this
         * value is also explicitly set.</p>
         * <p>Note that this method is used internally; calls within a request
         * configuration closure should call send(Object, Object)
         * to set the request body and content-type at the same time.</p>
         *
         * @param ct either a {@link ContentType} value or a valid content-type
         *           String.
         */
        protected void setRequestContentType(Object ct) {
            this.requestContentType = ct;
        }

        /**
         * Valid arguments:
         * <dl>
         *   <dt>uri</dt><dd>Either a URI, URL, or object whose
         *      <code>toString()</code> method produces a valid URI string.
         *      If this parameter is not supplied, the HTTPBuilder's default
         *      URI is used.</dd>
         *   <dt>path</dt><dd>Request path that is merged with the URI</dd>
         *   <dt>queryString</dt><dd>an escaped query string</dd>
         *   <dt>query</dt><dd>Map of URL query parameters</dd>
         *   <dt>headers</dt><dd>Map of HTTP headers</dd>
         *   <dt>contentType</dt><dd>Request content type and Accept header.
         *      If not supplied, the HTTPBuilder's default content-type is used.</dd>
         *   <dt>requestContentType</dt><dd>content type for the request, if it
         *      is different from the expected response content-type</dd>
         *   <dt>body</dt><dd>Request body that will be encoded based on the given contentType</dd>
         * </dl>
         * Note that if both <code>queryString</code> and <code>query</code> are given,
         * <code>query</code> will be merged with (and potentially override)
         * the parameters given as part of <code>queryString</code>.
         *
         * @param args named parameters to set properties on this delegate.
         * @throws URISyntaxException if the uri argument does not represent a valid URI
         */
        @SuppressWarnings("unchecked")
        protected void setPropertiesFromMap(Map<String, ?> args) throws URISyntaxException {
            if (args == null) return;
            if (args.containsKey("url")) throw new IllegalArgumentException(
                    "The 'url' parameter is deprecated; use 'uri' instead");
            Object uri = args.remove("uri");

            if (uri == null) throw new IllegalStateException(
                    "Default URI is null, and no 'uri' parameter was given");
            this.uri = new URIBuilder(convertToURI(uri));

            Map query = (Map) args.remove("params");
            if (query != null) {
                log.warn("'params' argument is deprecated; use 'query' instead.");
                this.uri.setQuery(query);
            }
            String queryString = (String) args.remove("queryString");
            if (queryString != null) this.uri.setRawQuery(queryString);

            query = (Map) args.remove("query");
            if (query != null) this.uri.addQueryParams(query);
            Map headers = (Map) args.remove("headers");
            if (headers != null) this.getHeaders().putAll(headers);

            Object path = args.remove("path");
            if (path != null) this.uri.setPath(path.toString());

            Object contentType = args.remove("contentType");
            if (contentType != null) this.setContentType(contentType);

            contentType = args.remove("requestContentType");
            if (contentType != null) this.setRequestContentType(contentType);

            Object body = args.remove("body");
            if (body != null) this.setBody(body);

            if (args.size() > 0) {
                String invalidArgs = "";
                for (String k : args.keySet()) invalidArgs += k + ",";
                throw new IllegalArgumentException("Unexpected keyword args: " + invalidArgs);
            }
        }

        /**
         * <p>Get request headers (including any default headers set on this
         * {@link HTTPBuilder#setHeaders(Map) HTTPBuilder instance}).  Note that
         * this will not include any <code>Accept</code>, <code>Content-Type</code>,
         * or <code>Content-Encoding</code> headers that are automatically
         * handled by any encoder or parsers in effect.  Note that any values
         * set here <i>will</i> override any of those automatically assigned
         * values.</p>
         *
         * <p>Example: <code>headers.'Accept-Language' = 'en, en-gb;q=0.8'</code></p>
         *
         * @return a map of HTTP headers that will be sent in the request.
         */
        public Map<?, ?> getHeaders() {
            return this.headers;
        }

        /**
         * Set the request body.  This value may be of any type supported by
         * the associated {@link EncoderRegistry request encoder}.  That is,
         * the value of <code>body</code> will be interpreted by the encoder
         * associated with the current {@link #getRequestContentType() request
         * content-type}.
         *
         * @param body data or closure interpreted as the request body
         */
        public void setBody(Object body) {
            this.body = body;
        }

        public void encodeBody() {
            if (body == null) {
                return;
            }
            if (!(request instanceof HttpEntityEnclosingRequest))
                throw new IllegalArgumentException(
                        "Cannot set a request body for a " + request.getMethod() + " method");

            Closure encoder = encoders.getAt(this.getRequestContentType());

            // Either content type or encoder is empty.
            if (encoder == null)
                throw new IllegalArgumentException(
                        "No encoder found for request content type " + getRequestContentType());

            HttpEntity entity = encoder.getMaximumNumberOfParameters() == 2
                    ? (HttpEntity) encoder.call(new Object[]{body, this.getRequestContentType()})
                    : (HttpEntity) encoder.call(body);

            ((HttpEntityEnclosingRequest) this.request).setEntity(entity);
        }

        /**
         * Get the proper response handler for the response code.  This is called
         * by the {@link HTTPBuilder} class in order to find the proper handler
         * based on the response status code.
         *
         * @param statusCode HTTP response status code
         * @return the response handler
         */
        protected Closure findResponseHandler(int statusCode) {
            Closure handler = this.getResponse().get(Integer.toString(statusCode));
            if (handler == null) handler =
                    this.getResponse().get(Status.find(statusCode).toString());
            return handler;
        }

        /**
         * Access the response handler map to set response parsing logic.
         * i.e.<pre>
         * builder.request( GET, XML ) {
         *   response.success = { xml ->
         *      /* for XML content type, the default parser
         *         will return an XmlSlurper * /
         *      xml.root.children().each { println it }
         *   }
         * }</pre>
         *
         * @return
         */
        public Map<Object, Closure> getResponse() {
            return this.responseHandlers;
        }

        /**
         * Get the {@link HttpContext} that will be used for this request.  By
         * default, a new context is created for each request.
         *
         * @return
         */
        public HttpContextDecorator getContext() {
            return this.context;
        }

        /**
         * Set the {@link HttpContext} that will be used for this request.
         *
         * @param ctx
         */
        public void setContext(HttpContext ctx) {
            this.context = new HttpContextDecorator(ctx);
        }
    }
}
