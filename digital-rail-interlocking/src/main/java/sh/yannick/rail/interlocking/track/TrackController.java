package sh.yannick.rail.interlocking.track;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sh.yannick.rail.api.AllocationApiResponse;

@RestController
@RequiredArgsConstructor
public class TrackController {
    private final TrackService service;

    @PostMapping("/allocate")
    public ResponseEntity<AllocationApiResponse> allocate(@RequestParam String from, @RequestParam String to) {
        AllocationResponse response = service.allocate(from, to);
        if (response.error() == null) {
            return ResponseEntity.ok(new AllocationApiResponse(response.path(), null));
        }
        return ResponseEntity.ok(new AllocationApiResponse(null, response.error().getMessage()));
    }

    @PostMapping("/release")
    public ResponseEntity<String> release(@RequestParam String element) {
        try {
            service.release(element);
            return ResponseEntity.ok(null);
        } catch (Exception e) {
            return ResponseEntity.ok(e.getMessage());
        }
    }
}
