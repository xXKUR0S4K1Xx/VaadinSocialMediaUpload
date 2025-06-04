package org.vaadin.example.social;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/avatar")
public class AvatarController {

    @GetMapping("/{username}/{filename}")
    public ResponseEntity<Resource> getAvatar(@PathVariable String username, @PathVariable String filename) {
        try {
            Path avatarDir = Paths.get("users", username, "Avatar");
            Path file = avatarDir.resolve(filename);

            if (!Files.exists(file)) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new UrlResource(file.toUri());

            // Determine content type based on extension
            MediaType contentType = filename.toLowerCase().endsWith(".png") ?
                    MediaType.IMAGE_PNG :
                    MediaType.IMAGE_JPEG;

            return ResponseEntity.ok()
                    .contentType(contentType)
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
