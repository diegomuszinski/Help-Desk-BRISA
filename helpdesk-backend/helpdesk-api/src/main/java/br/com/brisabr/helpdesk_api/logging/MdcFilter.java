package br.com.brisabr.helpdesk_api.logging;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

/**
 * Filtro para adicionar contexto de rastreamento (MDC) em cada requisição.
 * 
 * Adiciona automaticamente um requestId único para cada requisição,
 * permitindo correlacionar logs de uma mesma requisição.
 * 
 * Exemplo de uso no log:
 * logger.info("Processando ticket"); // Automaticamente inclui requestId
 * 
 * Configuração no logback.xml:
 * <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level [%X{requestId}] %logger{36} - %msg%n</pattern>
 */
@Component
public class MdcFilter implements Filter {
    
    private static final String REQUEST_ID = "requestId";
    private static final String REQUEST_URI = "requestUri";
    private static final String REQUEST_METHOD = "requestMethod";
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        
        try {
            // Gerar ID único para rastreamento da requisição
            String requestId = UUID.randomUUID().toString().substring(0, 8);
            MDC.put(REQUEST_ID, requestId);
            
            // Adicionar informações da requisição
            MDC.put(REQUEST_URI, httpRequest.getRequestURI());
            MDC.put(REQUEST_METHOD, httpRequest.getMethod());
            
            // Continuar cadeia de filtros
            chain.doFilter(request, response);
            
        } finally {
            // Limpar MDC após requisição
            MDC.clear();
        }
    }
}
