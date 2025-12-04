package br.com.brisabr.helpdesk_api.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Validador de arquivos enviados via upload.
 * Valida tipo MIME real (não apenas extensão do nome do arquivo).
 */
@Component
public class FileValidator {

    private static final Logger logger = LoggerFactory.getLogger(FileValidator.class);

    // Tipos MIME permitidos
    private static final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
        // Imagens
        "image/jpeg",
        "image/jpg",
        "image/png",
        "image/gif",
        "image/bmp",
        "image/webp",
        
        // Documentos
        "application/pdf",
        "application/msword", // .doc
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // .docx
        "application/vnd.ms-excel", // .xls
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // .xlsx
        "application/vnd.ms-powerpoint", // .ppt
        "application/vnd.openxmlformats-officedocument.presentationml.presentation", // .pptx
        
        // Texto
        "text/plain",
        "text/csv",
        
        // Compactados
        "application/zip",
        "application/x-zip-compressed",
        "application/x-rar-compressed"
    );

    // Tamanho máximo: 10MB
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    // Magic numbers (assinaturas de arquivo) para validação adicional
    private static final Map<String, byte[]> MAGIC_NUMBERS = new HashMap<>();
    
    static {
        MAGIC_NUMBERS.put("image/jpeg", new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF});
        MAGIC_NUMBERS.put("image/png", new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47});
        MAGIC_NUMBERS.put("application/pdf", new byte[]{0x25, 0x50, 0x44, 0x46}); // %PDF
        MAGIC_NUMBERS.put("application/zip", new byte[]{0x50, 0x4B, 0x03, 0x04}); // PK
    }

    /**
     * Valida um arquivo enviado via upload.
     * 
     * @param file Arquivo MultipartFile
     * @throws IllegalArgumentException se o arquivo for inválido
     */
    public void validateFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Arquivo vazio ou nulo");
        }

        // Validar tamanho
        if (file.getSize() > MAX_FILE_SIZE) {
            logger.warn("Arquivo muito grande: {} bytes (máx: {} bytes)", file.getSize(), MAX_FILE_SIZE);
            throw new IllegalArgumentException("Arquivo muito grande. Tamanho máximo: 10MB");
        }

        // Validar tipo MIME
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType)) {
            logger.warn("Tipo de arquivo não permitido: {}", contentType);
            throw new IllegalArgumentException(
                "Tipo de arquivo não permitido: " + contentType + ". " +
                "Tipos permitidos: PDF, DOC, DOCX, XLS, XLSX, imagens (JPG, PNG, GIF), ZIP"
            );
        }

        // Validar assinatura do arquivo (magic numbers) para tipos comuns
        validateMagicNumbers(file, contentType);

        logger.debug("Arquivo validado com sucesso: {} ({})", file.getOriginalFilename(), contentType);
    }

    /**
     * Valida assinatura do arquivo comparando com magic numbers conhecidos.
     * 
     * @param file Arquivo MultipartFile
     * @param declaredType Tipo MIME declarado
     * @throws IOException se houver erro ao ler o arquivo
     * @throws IllegalArgumentException se a assinatura não corresponder ao tipo declarado
     */
    private void validateMagicNumbers(MultipartFile file, String declaredType) throws IOException {
        byte[] magicNumber = MAGIC_NUMBERS.get(declaredType);
        if (magicNumber == null) {
            // Não temos magic number para este tipo, pular validação
            return;
        }

        byte[] fileHeader = new byte[magicNumber.length];
        int bytesRead = file.getInputStream().read(fileHeader);
        
        if (bytesRead < magicNumber.length) {
            logger.warn("Arquivo muito pequeno para validar magic number: {} bytes", bytesRead);
            throw new IllegalArgumentException("Arquivo corrompido ou inválido");
        }

        // Comparar primeiros bytes
        for (int i = 0; i < magicNumber.length; i++) {
            if (fileHeader[i] != magicNumber[i]) {
                logger.warn("Magic number não corresponde ao tipo declarado. Esperado: {}, Encontrado: {}", 
                    Arrays.toString(magicNumber), Arrays.toString(fileHeader));
                throw new IllegalArgumentException(
                    "Tipo de arquivo não corresponde ao conteúdo real. " +
                    "O arquivo pode estar corrompido ou foi renomeado incorretamente."
                );
            }
        }
    }

    /**
     * Valida uma lista de arquivos.
     * 
     * @param files Lista de arquivos
     * @throws IOException se houver erro ao ler os arquivos
     * @throws IllegalArgumentException se algum arquivo for inválido
     */
    public void validateFiles(List<MultipartFile> files) throws IOException {
        if (files == null || files.isEmpty()) {
            return;
        }

        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                validateFile(file);
            }
        }
    }
}
