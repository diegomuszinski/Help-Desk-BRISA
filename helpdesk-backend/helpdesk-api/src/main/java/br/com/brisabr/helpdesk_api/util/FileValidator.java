package br.com.brisabr.helpdesk_api.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Validador de arquivos enviados via upload.
 * Valida tipo MIME real (não apenas extensão do nome do arquivo).
 * Suporta detecção de malware via ClamAV quando habilitado.
 */
@Component
public class FileValidator {

    private static final Logger logger = LoggerFactory.getLogger(FileValidator.class);

    @Value("${file.upload.max-size:10485760}") // 10MB padrão
    private long maxFileSize;

    @Value("${file.upload.max-files:5}")
    private int maxFiles;

    @Value("${file.upload.antivirus.enabled:false}")
    private boolean antivirusEnabled;

    @Value("${file.upload.antivirus.clamav.host:localhost}")
    private String clamavHost;

    @Value("${file.upload.antivirus.clamav.port:3310}")
    private int clamavPort;

    // Padrão para nomes de arquivo perigosos
    private static final Pattern DANGEROUS_FILENAME_PATTERN = Pattern.compile(
        ".*[<>:\"|?*\\x00-\\x1F].*|^(CON|PRN|AUX|NUL|COM[1-9]|LPT[1-9])(\\..*)?$",
        Pattern.CASE_INSENSITIVE
    );

    // Extensões proibidas (executáveis)
    private static final List<String> FORBIDDEN_EXTENSIONS = Arrays.asList(
        ".exe", ".bat", ".cmd", ".com", ".pif", ".scr", ".vbs", ".js", ".jar",
        ".msi", ".dll", ".sh", ".app", ".deb", ".rpm"
    );

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

    // Magic numbers (assinaturas de arquivo) para validação adicional
    private static final Map<String, byte[]> MAGIC_NUMBERS = new HashMap<>();

    static {
        // Imagens
        MAGIC_NUMBERS.put("image/jpeg", new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF});
        MAGIC_NUMBERS.put("image/jpg", new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF});
        MAGIC_NUMBERS.put("image/png", new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A});
        MAGIC_NUMBERS.put("image/gif", new byte[]{0x47, 0x49, 0x46, 0x38}); // GIF8
        MAGIC_NUMBERS.put("image/bmp", new byte[]{0x42, 0x4D}); // BM
        MAGIC_NUMBERS.put("image/webp", new byte[]{0x52, 0x49, 0x46, 0x46}); // RIFF

        // Documentos
        MAGIC_NUMBERS.put("application/pdf", new byte[]{0x25, 0x50, 0x44, 0x46}); // %PDF

        // Office (DOCX, XLSX, PPTX são ZIP)
        MAGIC_NUMBERS.put("application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            new byte[]{0x50, 0x4B, 0x03, 0x04}); // DOCX
        MAGIC_NUMBERS.put("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            new byte[]{0x50, 0x4B, 0x03, 0x04}); // XLSX
        MAGIC_NUMBERS.put("application/vnd.openxmlformats-officedocument.presentationml.presentation",
            new byte[]{0x50, 0x4B, 0x03, 0x04}); // PPTX

        // Office antigo (DOC, XLS, PPT)
        MAGIC_NUMBERS.put("application/msword", new byte[]{(byte) 0xD0, (byte) 0xCF, 0x11, (byte) 0xE0}); // DOC
        MAGIC_NUMBERS.put("application/vnd.ms-excel", new byte[]{(byte) 0xD0, (byte) 0xCF, 0x11, (byte) 0xE0}); // XLS
        MAGIC_NUMBERS.put("application/vnd.ms-powerpoint", new byte[]{(byte) 0xD0, (byte) 0xCF, 0x11, (byte) 0xE0}); // PPT

        // Compactados
        MAGIC_NUMBERS.put("application/zip", new byte[]{0x50, 0x4B, 0x03, 0x04}); // PK
        MAGIC_NUMBERS.put("application/x-zip-compressed", new byte[]{0x50, 0x4B, 0x03, 0x04});
        MAGIC_NUMBERS.put("application/x-rar-compressed", new byte[]{0x52, 0x61, 0x72, 0x21}); // Rar!
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

        // Validar nome do arquivo
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do arquivo não pode estar vazio");
        }

        // Validar caracteres perigosos no nome
        if (DANGEROUS_FILENAME_PATTERN.matcher(originalFilename).matches()) {
            logger.warn("Nome de arquivo perigoso detectado: {}", originalFilename);
            throw new IllegalArgumentException("Nome de arquivo contém caracteres inválidos ou é um nome reservado do sistema");
        }

        // Validar extensão perigosa
        String lowercaseFilename = originalFilename.toLowerCase();
        for (String forbiddenExt : FORBIDDEN_EXTENSIONS) {
            if (lowercaseFilename.endsWith(forbiddenExt)) {
                logger.warn("Extensão de arquivo proibida: {}", forbiddenExt);
                throw new IllegalArgumentException("Tipo de arquivo não permitido: " + forbiddenExt);
            }
        }

        // Validar path traversal
        if (originalFilename.contains("..") || originalFilename.contains("/") || originalFilename.contains("\\")) {
            logger.warn("Tentativa de path traversal detectada: {}", originalFilename);
            throw new IllegalArgumentException("Nome de arquivo contém caracteres inválidos");
        }

        // Validar tamanho
        if (file.getSize() > maxFileSize) {
            logger.warn("Arquivo muito grande: {} bytes (máx: {} bytes)", file.getSize(), maxFileSize);
            throw new IllegalArgumentException(String.format("Arquivo muito grande. Tamanho máximo: %.2f MB", maxFileSize / (1024.0 * 1024.0)));
        }

        // Validar tamanho mínimo (evitar arquivos vazios)
        if (file.getSize() < 10) {
            logger.warn("Arquivo muito pequeno: {} bytes", file.getSize());
            throw new IllegalArgumentException("Arquivo muito pequeno ou corrompido");
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

        // Scan antivírus se habilitado
        if (antivirusEnabled) {
            scanForViruses(file);
        }

        logger.debug("Arquivo validado com sucesso: {} ({})", file.getOriginalFilename(), contentType);
    }

    /**
     * Escaneia arquivo em busca de vírus usando ClamAV.
     *
     * @param file Arquivo a ser escaneado
     * @throws IOException se houver erro de I/O
     * @throws IllegalArgumentException se vírus for detectado
     */
    private void scanForViruses(MultipartFile file) throws IOException {
        try (java.net.Socket socket = new java.net.Socket(clamavHost, clamavPort)) {
            socket.setSoTimeout(30000); // 30 segundos timeout

            // Enviar comando INSTREAM
            try (java.io.OutputStream out = socket.getOutputStream();
                 InputStream in = socket.getInputStream()) {

                out.write("zINSTREAM\0".getBytes());
                out.flush();

                // Enviar arquivo em chunks
                byte[] buffer = new byte[2048];
                try (InputStream fileStream = file.getInputStream()) {
                    int read;
                    while ((read = fileStream.read(buffer)) > 0) {
                        // Enviar tamanho do chunk (4 bytes, big-endian)
                        out.write(new byte[]{
                            (byte) (read >> 24),
                            (byte) (read >> 16),
                            (byte) (read >> 8),
                            (byte) read
                        });
                        out.write(buffer, 0, read);
                    }
                }

                // Enviar fim do stream (chunk size = 0)
                out.write(new byte[]{0, 0, 0, 0});
                out.flush();

                // Ler resposta
                byte[] response = new byte[1024];
                int responseLength = in.read(response);
                String result = new String(response, 0, responseLength).trim();

                if (!result.contains("OK")) {
                    logger.error("Vírus detectado no arquivo {}: {}", file.getOriginalFilename(), result);
                    throw new IllegalArgumentException("Arquivo contém malware e foi rejeitado");
                }

                logger.debug("Arquivo {} passou no scan antivírus", file.getOriginalFilename());
            }
        } catch (java.net.ConnectException e) {
            logger.error("Não foi possível conectar ao ClamAV em {}:{}", clamavHost, clamavPort);
            // Em produção, você pode querer falhar aqui
            // Por enquanto, apenas logamos o erro
            logger.warn("Scan antivírus desabilitado devido a erro de conexão");
        } catch (IOException e) {
            logger.error("Erro ao escanear arquivo com ClamAV", e);
            throw new IOException("Erro ao escanear arquivo em busca de vírus", e);
        }
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
            logger.debug("Magic number não disponível para tipo: {}", declaredType);
            return;
        }

        byte[] fileHeader = new byte[magicNumber.length];
        try (InputStream inputStream = file.getInputStream()) {
            int bytesRead = inputStream.read(fileHeader);

            if (bytesRead < magicNumber.length) {
                logger.warn("Arquivo muito pequeno para validar magic number: {} bytes (necessário: {})",
                    bytesRead, magicNumber.length);
                throw new IllegalArgumentException("Arquivo corrompido ou inválido");
            }

            // Comparar primeiros bytes
            boolean matches = true;
            for (int i = 0; i < magicNumber.length; i++) {
                if (fileHeader[i] != magicNumber[i]) {
                    matches = false;
                    break;
                }
            }

            if (!matches) {
                logger.warn("Magic number não corresponde ao tipo declarado '{}'. Esperado: {}, Encontrado: {}",
                    declaredType,
                    bytesToHex(magicNumber),
                    bytesToHex(fileHeader));
                throw new IllegalArgumentException(
                    "Tipo de arquivo não corresponde ao conteúdo real. " +
                    "O arquivo pode estar corrompido ou foi renomeado incorretamente."
                );
            }

            logger.debug("Magic number validado para tipo: {}", declaredType);
        }
    }

    /**
     * Converte bytes para representação hexadecimal (para logging).
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
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

        // Validar quantidade de arquivos
        long nonEmptyFiles = files.stream().filter(f -> !f.isEmpty()).count();
        if (nonEmptyFiles > maxFiles) {
            logger.warn("Número de arquivos excede o limite: {} (máx: {})", nonEmptyFiles, maxFiles);
            throw new IllegalArgumentException(
                String.format("Número máximo de arquivos excedido. Máximo permitido: %d", maxFiles)
            );
        }

        // Validar tamanho total
        long totalSize = files.stream()
            .filter(f -> !f.isEmpty())
            .mapToLong(MultipartFile::getSize)
            .sum();

        long maxTotalSize = maxFileSize * maxFiles;
        if (totalSize > maxTotalSize) {
            logger.warn("Tamanho total dos arquivos excede o limite: {} bytes (máx: {} bytes)",
                totalSize, maxTotalSize);
            throw new IllegalArgumentException(
                String.format("Tamanho total dos arquivos excede o limite. Máximo: %.2f MB",
                    maxTotalSize / (1024.0 * 1024.0))
            );
        }

        // Validar cada arquivo individualmente
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                validateFile(file);
            }
        }

        logger.info("Validados {} arquivos com sucesso. Tamanho total: {} bytes", nonEmptyFiles, totalSize);
    }
}
