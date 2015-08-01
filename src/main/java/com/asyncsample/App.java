package com.asyncsample;

import java.io.IOException;
import java.util.Date;
import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.eclipse.jetty.alpn.ALPN;
//import org.eclipse.jetty.alpn.ALPN;
import org.eclipse.jetty.alpn.server.ALPNServerConnectionFactory;
import org.eclipse.jetty.http2.HTTP2Cipher;
import org.eclipse.jetty.http2.server.HTTP2CServerConnectionFactory;
import org.eclipse.jetty.http2.server.HTTP2ServerConnectionFactory;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.NegotiatingServerConnectionFactory;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlets.PushSessionCacheFilter;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import com.asyncsample.api.AsyncServletSample;
import com.asyncsample.api.HelloServlet;

public class App {

    public static void main(String... args) throws Exception {
        Server server = new Server();

//        // Setup JMX
//        MBeanContainer mbContainer = new MBeanContainer(ManagementFactory.getPlatformMBeanServer());
//        server.addEventListener(mbContainer);
//        server.addBean(mbContainer);
//
//        // Add loggers MBean to server (will be picked up by MBeanContainer above)
//        server.addBean(Log.getLog());

        ServletContextHandler context = new ServletContextHandler(server, "/", ServletContextHandler.SESSIONS);
        context.addFilter(PushSessionCacheFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
        context.addFilter(PushedTilesFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
        context.addServlet(new ServletHolder(new HelloServlet()), "/hello/*");
        context.addServlet(new ServletHolder(new AsyncServletSample()), "/async/*");
        context.addServlet(DefaultServlet.class, "/").setInitParameter("maxCacheSize", "81920");
        server.setHandler(context);

        // HTTP Configuration
        HttpConfiguration http_config = new HttpConfiguration();
        http_config.setSecureScheme("https");
        http_config.setSecurePort(8443);
        http_config.setSendXPoweredBy(true);
        http_config.setSendServerVersion(true);

        // HTTP Connector
        ServerConnector http = new ServerConnector(server, new HttpConnectionFactory(http_config), new HTTP2CServerConnectionFactory(http_config));
        http.setPort(8080);
        server.addConnector(http);

        // SSL Context Factory for HTTPS and HTTP/2
        // SSL requires a certificate so we configure a factory for ssl contents with information pointing to what
        // keystore the ssl connection needs to know about. Much more configuration is available the ssl context,
        // including things like choosing the particular certificate out of a keystore to be used.
        SslContextFactory sslContextFactory = new SslContextFactory();

        sslContextFactory.setKeyStoreResource(Resource.newClassPathResource("keystore"));
        sslContextFactory.setKeyStorePassword("OBF:1v9o1saj1zer1zej1sar1v8y");
        sslContextFactory.setKeyManagerPassword("OBF:19iy19j019j219j419j619j8");
        sslContextFactory.setCipherComparator(HTTP2Cipher.COMPARATOR);

        // HTTPS Configuration
        HttpConfiguration https_config = new HttpConfiguration(http_config);
        https_config.addCustomizer(new SecureRequestCustomizer());

        // HTTP/2 Connection Factory
        HTTP2ServerConnectionFactory h2 = new HTTP2ServerConnectionFactory(https_config);

        NegotiatingServerConnectionFactory.checkProtocolNegotiationAvailable();
        ALPNServerConnectionFactory alpn = new ALPNServerConnectionFactory();
        alpn.setDefaultProtocol(http.getDefaultProtocol());

        // SSL Connection Factory
        SslConnectionFactory ssl = new SslConnectionFactory(sslContextFactory, alpn.getProtocol());

        // HTTP/2 Connector
        ServerConnector http2Connector = new ServerConnector(server, ssl, alpn, h2, new HttpConnectionFactory(https_config));
        http2Connector.setPort(8443);
        server.addConnector(http2Connector);

        ALPN.debug = true;

        System.err.println("Starting!...");
        try {
            server.start();
            server.dumpStdErr();
            server.join();
        } finally {
            server.destroy();
        }
    }

    public static class PushedTilesFilter implements Filter {
        @Override
        public void init(FilterConfig filterConfig) throws ServletException {
        }

        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
            Request baseRequest = Request.getBaseRequest(request);

            if (baseRequest.isPush() && baseRequest.getRequestURI().contains("tiles")) {
                String uri = baseRequest.getRequestURI().replace("tiles", "pushed").substring(baseRequest.getContextPath().length());
                request.getRequestDispatcher(uri).forward(request, response);
                return;
            }

            chain.doFilter(request, response);
        }

        @Override
        public void destroy() {
        }
    };

}