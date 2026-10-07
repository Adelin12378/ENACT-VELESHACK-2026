package eu.enact.greencharge;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/dataspace")
public class DataspaceController {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Path feedPath;

    public DataspaceController(
            @Value("${carbon.feed.file}") String feedFile) {
        this.feedPath = Path.of(feedFile);
    }

    @PostMapping(
            value = "/carbon",
            consumes = {
                    "application/json",
                    "application/octet-stream"
            }
    )
    public ResponseEntity<?> receiveCarbonFeed(@RequestBody byte[] body) {

        try {
            Map<String, Double> payload = objectMapper.readValue(
                    body,
                    new TypeReference<Map<String, Double>>() {}
            );

            Files.createDirectories(feedPath.getParent());

            String json = objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(payload);

            Files.writeString(
                    feedPath,
                    json,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );

            return ResponseEntity.ok(
                    Map.of(
                            "status", "updated",
                            "file", feedPath.toString(),
                            "entries", payload.size()
                    )
            );

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    Map.of(
                            "status", "error",
                            "message", e.getMessage()
                    )
            );
        }
    }
}
