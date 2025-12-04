package br.com.brisabr.helpdesk_api.ticket;

import org.springframework.beans.factory.annotation.Autowired;
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

@RestController
@RequestMapping("/api/anexos")
public class AnexoChamadoController {

    @Autowired
    private TicketService ticketService;

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadAnexo(@PathVariable Long id) {
        AnexoChamado anexo = ticketService.getAnexoById(id);
        byte[] dadosDecodificados = Base64.getDecoder().decode(anexo.getDados());
        ByteArrayResource resource = new ByteArrayResource(dadosDecodificados);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=\"" + anexo.getNomeArquivo() + "\"")
                .contentType(MediaType.parseMediaType(anexo.getTipoArquivo()))
                .contentLength(dadosDecodificados.length)
                .body(resource);
    }
}