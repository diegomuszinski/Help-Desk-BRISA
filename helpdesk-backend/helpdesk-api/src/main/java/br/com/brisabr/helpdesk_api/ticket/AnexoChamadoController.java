package br.com.brisabr.helpdesk_api.ticket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Base64;

/**
 * Controller para download de anexos de tickets.
 *
 * Endpoints disponíveis:
 * - GET /api/anexos/{id}/download - Faz download de um anexo
 *
 * @author HelpDesk Team
 */
@RestController
@RequestMapping("/api/anexos")
@SuppressWarnings("null")
public class AnexoChamadoController {

    private static final Logger logger = LoggerFactory.getLogger(AnexoChamadoController.class);

    private final TicketService ticketService;

    public AnexoChamadoController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    /**
     * Realiza download de um anexo de ticket.
     *
     * @param id ID do anexo
     * @return Arquivo para download
     */
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadAnexo(@PathVariable Long id) {
        logger.info("Download de anexo solicitado: ID={}", id);

        AnexoChamado anexo = ticketService.getAnexoById(id);
        byte[] dadosDecodificados = Base64.getDecoder().decode(anexo.getDados());
        ByteArrayResource resource = new ByteArrayResource(dadosDecodificados);

        logger.debug("Anexo recuperado: arquivo={}, tamanho={} bytes",
                    anexo.getNomeArquivo(), dadosDecodificados.length);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=\"" + anexo.getNomeArquivo() + "\"")
                .contentType(MediaType.parseMediaType(anexo.getTipoArquivo()))
                .contentLength(dadosDecodificados.length)
                .body(resource);
    }
}
