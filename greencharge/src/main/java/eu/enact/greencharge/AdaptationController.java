package eu.enact.greencharge;

import org.springframework.web.bind.annotation.*;

import eu.enact.greencharge.model.AdaptationRecommendation;
import eu.enact.greencharge.model.ResourceMetrics;

@RestController
@RequestMapping("/adaptation")
public class AdaptationController {

    private final AdaptationService adaptationService;

    public AdaptationController(AdaptationService adaptationService) {
        this.adaptationService = adaptationService;
    }

    @GetMapping("/test")
    public AdaptationRecommendation test() {
        ResourceMetrics metrics = new ResourceMetrics(
                "enact-dev-worker",
                "eu-west",
                0.80,
                0.95,
                50.0,
                50.0
        );

        return adaptationService.evaluate(metrics);
    }

    @PostMapping("/evaluate")
    public AdaptationRecommendation evaluate(
            @RequestBody ResourceMetrics metrics) {

        return adaptationService.evaluate(metrics);
    }
}
