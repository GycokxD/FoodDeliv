package univ.fooddeliv.controllers;

import univ.fooddeliv.utils.FileUtil;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;

@Controller
@AllArgsConstructor
@RequestMapping
public class ImageController {
    private final FileUtil fileUtil;

    @GetMapping("/uploads/avatars/{filename}")
    public ResponseEntity<?> getAvatar(@PathVariable String filename) {
        try {
            return fileUtil.getAvatar(filename);
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/uploads/catalog/{filename}")
    public ResponseEntity<?> getCatalog(@PathVariable String filename) {
        try {
            return fileUtil.getCatalogImage(filename);
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
