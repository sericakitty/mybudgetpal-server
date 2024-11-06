package hh.sof03.mybudgetpal.security.jwt;

import java.io.IOException;
import java.util.Enumeration;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class HttpRequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(HttpRequestLoggingFilter.class);

    /**
     * This method will be triggered for every incoming HTTP request and logs the request details.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        long startTime = System.currentTimeMillis();

        logger.info("Request URL: {}", request.getRequestURL().toString());
        logger.info("Request Method: {}", request.getMethod());
        logger.info("Request Headers: {}", getHeadersInfo(request));
        logger.info("Request Parameters: {}", request.getParameterMap());

        filterChain.doFilter(request, response);

        long duration = System.currentTimeMillis() - startTime;
        logger.info("Response Status: {}", response.getStatus());
        logger.info("Request Duration: {} ms", duration);
    }

    /**
     * Get headers information from request
     * 
     * @param request
     * @return String
     */
    private String getHeadersInfo(HttpServletRequest request) {
      Enumeration<String> headerNames = request.getHeaderNames();
      StringBuilder headers = new StringBuilder();
      
      while (headerNames.hasMoreElements()) {
          String headerName = headerNames.nextElement();
          String headerValue = request.getHeader(headerName);
          headers.append(headerName).append(": ").append(headerValue).append(", ");
      }
      
      if (headers.length() > 0) {
          headers.setLength(headers.length() - 2);
      }
      
      return headers.toString();
  }
}
