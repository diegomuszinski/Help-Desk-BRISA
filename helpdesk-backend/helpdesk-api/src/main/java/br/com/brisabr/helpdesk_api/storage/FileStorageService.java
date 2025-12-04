package br.com.brisabr.helpdesk_api.storage;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Interface para serviços de armazenamento de arquivos.
 * Permite trocar facilmente entre diferentes estratégias (filesystem, S3, Azure Blob, etc.)
 */
public interface FileStorageService {
    
    /**
     * Armazena um arquivo e retorna o identificador/caminho.
     * 
     * @param file Arquivo a ser armazenado
     * @return Identificador único do arquivo armazenado
     * @throws IOException Se houver erro no armazenamento
     */
    String store(MultipartFile file) throws IOException;
    
    /**
     * Carrega um arquivo como bytes.
     * 
     * @param fileId Identificador do arquivo
     * @return Conteúdo do arquivo em bytes
     * @throws IOException Se houver erro na leitura
     */
    byte[] load(String fileId) throws IOException;
    
    /**
     * Obtém o caminho completo para um arquivo.
     * 
     * @param fileId Identificador do arquivo
     * @return Caminho do arquivo
     */
    Path getFilePath(String fileId);
    
    /**
     * Deleta um arquivo.
     * 
     * @param fileId Identificador do arquivo
     * @throws IOException Se houver erro na exclusão
     */
    void delete(String fileId) throws IOException;
    
    /**
     * Verifica se um arquivo existe.
     * 
     * @param fileId Identificador do arquivo
     * @return true se o arquivo existe
     */
    boolean exists(String fileId);
}
