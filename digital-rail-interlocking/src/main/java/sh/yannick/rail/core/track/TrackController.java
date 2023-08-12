package sh.yannick.rail.core.track;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sh.yannick.rail.api.AllocationResponse;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TrackController {
    private final TrackService service;

    @PostMapping("/allocate")
    public ResponseEntity<AllocationResponse> allocate(@RequestParam String from, @RequestParam String to) {
        try {
            List<String> path = service.allocate(from, to);
            return ResponseEntity.ok(new AllocationResponse(path, null));
        } catch (Exception e) {
            return ResponseEntity.ok(new AllocationResponse(null, e.getMessage()));
        }
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
