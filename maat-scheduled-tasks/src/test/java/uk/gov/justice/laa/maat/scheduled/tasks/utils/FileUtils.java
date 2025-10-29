package uk.gov.justice.laa.maat.scheduled.tasks.utils;

import java.io.IOException;
import java.nio.file.Files;
import org.springframework.core.io.ClassPathResource;

public class FileUtils {

    public static String readResourceToString(String filename) throws IOException {
        ClassPathResource resource = new ClassPathResource(filename);
        return Files.readString(resource.getFile().toPath());
    }
}
