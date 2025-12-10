package br.com.brisabr.helpdesk_api.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * Health indicator para verificar o espaço em disco disponível
 * para armazenamento de anexos de tickets.
 */
@Component
public class DiskSpaceHealthIndicator implements HealthIndicator {

    private static final Logger logger = LoggerFactory.getLogger(DiskSpaceHealthIndicator.class);

    // Limite mínimo: 1 GB (1.073.741.824 bytes)
    private static final long MIN_DISK_SPACE_BYTES = 1024L * 1024L * 1024L;

    // Limite de aviso: 5 GB
    private static final long WARNING_DISK_SPACE_BYTES = 5L * 1024L * 1024L * 1024L;

    @Value("${file.storage.location:./uploads}")
    private String storageLocation;

    @Override
    public Health health() {
        try {
            File storageDir = new File(storageLocation);

            // Criar diretório se não existir
            if (!storageDir.exists()) {
                boolean created = storageDir.mkdirs();
                if (!created) {
                    return Health.down()
                            .withDetail("error", "Não foi possível criar diretório de uploads")
                            .withDetail("path", storageLocation)
                            .build();
                }
            }

            long freeSpace = storageDir.getFreeSpace();
            long totalSpace = storageDir.getTotalSpace();
            long usableSpace = storageDir.getUsableSpace();

            double freePercentage = (double) freeSpace / totalSpace * 100;

            // Converte bytes para GB para exibição
            double freeSpaceGB = freeSpace / (1024.0 * 1024.0 * 1024.0);
            double totalSpaceGB = totalSpace / (1024.0 * 1024.0 * 1024.0);
            double usableSpaceGB = usableSpace / (1024.0 * 1024.0 * 1024.0);

            // Determinar status
            if (freeSpace < MIN_DISK_SPACE_BYTES) {
                return Health.down()
                        .withDetail("status", "CRÍTICO - Espaço em disco insuficiente")
                        .withDetail("path", storageDir.getAbsolutePath())
                        .withDetail("freeSpace", String.format("%.2f GB", freeSpaceGB))
                        .withDetail("totalSpace", String.format("%.2f GB", totalSpaceGB))
                        .withDetail("freePercentage", String.format("%.2f%%", freePercentage))
                        .withDetail("threshold", "1 GB")
                        .build();
            } else if (freeSpace < WARNING_DISK_SPACE_BYTES) {
                return Health.up()
                        .withDetail("status", "WARNING - Espaço em disco baixo")
                        .withDetail("path", storageDir.getAbsolutePath())
                        .withDetail("freeSpace", String.format("%.2f GB", freeSpaceGB))
                        .withDetail("totalSpace", String.format("%.2f GB", totalSpaceGB))
                        .withDetail("usableSpace", String.format("%.2f GB", usableSpaceGB))
                        .withDetail("freePercentage", String.format("%.2f%%", freePercentage))
                        .build();
            }

            return Health.up()
                    .withDetail("status", "Espaço em disco adequado")
                    .withDetail("path", storageDir.getAbsolutePath())
                    .withDetail("freeSpace", String.format("%.2f GB", freeSpaceGB))
                    .withDetail("totalSpace", String.format("%.2f GB", totalSpaceGB))
                    .withDetail("usableSpace", String.format("%.2f GB", usableSpaceGB))
                    .withDetail("freePercentage", String.format("%.2f%%", freePercentage))
                    .build();

        } catch (Exception e) {
            logger.error("Erro ao verificar espaço em disco", e);
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .withException(e)
                    .build();
        }
    }
}
