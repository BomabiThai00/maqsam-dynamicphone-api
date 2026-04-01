package com.yourcompany.telephony.filter;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.Enumeration;

@Component
public class RawRequestLoggingFilter extends OncePerRequestFilter {

    // This file will be created in the root folder of your project (telephony/)
    private static final Path LOG_FILE = Paths.get("maqsam_raw_requests.log");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Wrap the request so we can safely read the body without destroying it
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request,1024);

        // 2. Pass it down the chain. This lets your Controller process it normally.
        // During this process, the wrapper "caches" the raw body data.
        filterChain.doFilter(wrappedRequest, response);

        // 3. Now that the request is finished, grab the cached data and write it to a file
        logRawRequestToFile(wrappedRequest);
    }

    private void logRawRequestToFile(ContentCachingRequestWrapper request) {
        try {
            StringBuilder rawData = new StringBuilder();
            rawData.append("=== NEW REQUEST: ").append(LocalDateTime.now()).append(" ===\n");
            rawData.append(request.getMethod()).append(" ").append(request.getRequestURI());
            
            if (request.getQueryString() != null) {
                rawData.append("?").append(request.getQueryString());
            }
            rawData.append("\n");

            // Capture all HTTP Headers
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames != null && headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                rawData.append(headerName).append(": ").append(request.getHeader(headerName)).append("\n");
            }
            rawData.append("\n"); // Blank line between headers and body

            // Capture the raw body payload
            byte[] bodyContent = request.getContentAsByteArray();
            if (bodyContent.length > 0) {
                rawData.append(new String(bodyContent, request.getCharacterEncoding()));
            } else {
                rawData.append("[Empty Body]");
            }
            rawData.append("\n====================================================\n\n");

            // Append to our log file
            Files.writeString(LOG_FILE, rawData.toString(), 
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);

        } catch (Exception e) {
            System.err.println("Failed to write to raw request log: " + e.getMessage());
        }
    }
}
