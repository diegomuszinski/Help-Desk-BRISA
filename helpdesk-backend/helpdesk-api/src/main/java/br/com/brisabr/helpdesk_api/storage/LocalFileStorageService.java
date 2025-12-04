package br.com.brisabr.helpdesk_api.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Implementação do serviço de armazenamento usando sistema de arquivos local.
 * 
 * Os arquivos são armazenados em: ${file.storage.location}/uploads/
 * Estrutura: uploads/YYYY/MM/DD/UUID_filename.ext
 * 
 * Benefícios sobre Base64 no banco:
 * - Reduz tamanho do banco em ~33%
 * - Melhor performance de queries
 * - Facilita backup incremental
 * - Possibilita migração futura para S3/Azure Blob
 */
@Service
public class LocalFileStorageService implements FileStorageService {
    
    private static final Logger logger = LoggerFactory.getLogger(LocalFileStorageService.class);
    
    private final Path rootLocation;
    
    public LocalFileStorageService(@Value("${file.storage.location:./uploads}") String location) {
        this.rootLocation = Paths.get(location).toAbsolutePath().normalize();
        
        try {
            Files.createDirectories(this.rootLocation);
            logger.info("Diretório de storage criado: {}", this.rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Não foi possível criar diretório de armazenamento", e);
        }
    }
    
    @Override
    public String store(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Não é possível armazenar arquivo vazio");
        }
        
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.contains("..")) {
            throw new IllegalArgumentException("Nome de arquivo inválido: " + originalFilename);
        }
        
        // Gerar nome único: UUID + nome original
        String fileId = UUID.randomUUID().toString() + "_" + originalFilename;
        
        // Criar estrutura de diretórios por data (YYYY/MM/DD)
        java.time.LocalDate today = java.time.LocalDate.now();
        Path dateDir = rootLocation.resolve(
            String.format("%d/%02d/%02d", 
                today.getYear(), 
                today.getMonthValue(), 
                today.getDayOfMonth())
        );
        Files.createDirectories(dateDir);
        
        // Caminho completo do arquivo
        Path destinationFile = dateDir.resolve(fileId).normalize().toAbsolutePath();
        
        // Verificar que o arquivo está dentro do diretório permitido
        if (!destinationFile.getParent().startsWith(this.rootLocation)) {
            throw new SecurityException("Tentativa de armazenar arquivo fora do diretório permitido");
        }
        
        // Copiar arquivo
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            logger.info("Arquivo armazenado: {}", destinationFile);
        }
        
        // Retornar caminho relativo como ID
        return rootLocation.relativize(destinationFile).toString().replace('\\', '/');
    }
    
    @Override
    public byte[] load(String fileId) throws IOException {
        Path file = getFilePath(fileId);
        
        if (!Files.exists(file)) {
            throw new IOException("Arquivo não encontrado: " + fileId);
        }
        
        return Files.readAllBytes(file);
    }
    
    @Override
    public Path getFilePath(String fileId) {
        return rootLocation.resolve(fileId).normalize().toAbsolutePath();
    }
    
    @Override
    public void delete(String fileId) throws IOException {
        Path file = getFilePath(fileId);
        
        if (Files.exists(file)) {
            Files.delete(file);
            logger.info("Arquivo deletado: {}", file);
        }
    }
    
    @Override
    public boolean exists(String fileId) {
        Path file = getFilePath(fileId);
        return Files.exists(file);
    }
}
