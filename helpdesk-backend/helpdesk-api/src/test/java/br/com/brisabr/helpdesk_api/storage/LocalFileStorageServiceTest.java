package br.com.brisabr.helpdesk_api.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para LocalFileStorageService.
 */
@DisplayName("LocalFileStorageService - Testes Unitários")
class LocalFileStorageServiceTest {

    @TempDir
    Path tempDir;

    private LocalFileStorageService storageService;

    @BeforeEach
    void setUp() {
        storageService = new LocalFileStorageService(tempDir.toString());
    }

    @Test
    @DisplayName("Deve armazenar arquivo com sucesso")
    void shouldStoreFileSuccessfully() throws IOException {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "Hello World".getBytes()
        );

        // When
        String fileId = storageService.store(file);

        // Then
        assertThat(fileId).isNotNull();
        assertThat(fileId).contains("test.txt");
        assertThat(storageService.exists(fileId)).isTrue();
    }

    @Test
    @DisplayName("Deve carregar arquivo armazenado")
    void shouldLoadStoredFile() throws IOException {
        // Given
        byte[] content = "Test Content".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                content
        );
        String fileId = storageService.store(file);

        // When
        byte[] loadedContent = storageService.load(fileId);

        // Then
        assertThat(loadedContent).isEqualTo(content);
    }

    @Test
    @DisplayName("Deve lançar IOException ao carregar arquivo inexistente")
    void shouldThrowIOExceptionWhenLoadingNonExistentFile() {
        // When & Then
        assertThatThrownBy(() -> storageService.load("non-existent.txt"))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("não encontrado");
    }

    @Test
    @DisplayName("Deve deletar arquivo com sucesso")
    void shouldDeleteFileSuccessfully() throws IOException {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "Content".getBytes()
        );
        String fileId = storageService.store(file);

        // When
        storageService.delete(fileId);

        // Then
        assertThat(storageService.exists(fileId)).isFalse();
    }

    @Test
    @DisplayName("Deve verificar existência de arquivo")
    void shouldCheckFileExistence() throws IOException {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "Content".getBytes()
        );
        String fileId = storageService.store(file);

        // When & Then
        assertThat(storageService.exists(fileId)).isTrue();
        assertThat(storageService.exists("non-existent.txt")).isFalse();
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException para arquivo vazio")
    void shouldThrowIllegalArgumentExceptionForEmptyFile() {
        // Given
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                new byte[0]
        );

        // When & Then
        assertThatThrownBy(() -> storageService.store(emptyFile))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("vazio");
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException para nome de arquivo inválido")
    void shouldThrowIllegalArgumentExceptionForInvalidFilename() {
        // Given
        MockMultipartFile fileWithInvalidName = new MockMultipartFile(
                "file",
                "../../../etc/passwd",
                "text/plain",
                "Malicious".getBytes()
        );

        // When & Then
        assertThatThrownBy(() -> storageService.store(fileWithInvalidName))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("inválido");
    }

    @Test
    @DisplayName("Deve criar estrutura de diretórios por data")
    void shouldCreateDateBasedDirectoryStructure() throws IOException {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "Content".getBytes()
        );

        // When
        String fileId = storageService.store(file);

        // Then
        assertThat(fileId).matches("\\d{4}/\\d{2}/\\d{2}/.*test\\.txt");
    }

    @Test
    @DisplayName("Deve obter caminho do arquivo")
    void shouldGetFilePath() throws IOException {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "Content".getBytes()
        );
        String fileId = storageService.store(file);

        // When
        Path filePath = storageService.getFilePath(fileId);

        // Then
        assertThat(filePath).exists();
        assertThat(filePath.toString()).endsWith("test.txt");
    }
}
