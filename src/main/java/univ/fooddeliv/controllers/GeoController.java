package univ.fooddeliv.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import univ.fooddeliv.dto.GeoAddressDto;
import univ.fooddeliv.service.impl.GeocodingService;
import univ.fooddeliv.utils.GeoUtil;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/geo")
public class GeoController {
    private final GeocodingService geocodingService;

    @GetMapping("/search")
    public List<GeoAddressDto> search(@RequestParam(name = "q", defaultValue = "") String query) {
        return geocodingService.search(query);
    }

    @GetMapping("/reverse")
    public ResponseEntity<GeoAddressDto> reverse(@RequestParam double lat, @RequestParam double lng) {
        if (!GeoUtil.isValid(lat, lng)) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(geocodingService.reverse(lat, lng));
    }
}
