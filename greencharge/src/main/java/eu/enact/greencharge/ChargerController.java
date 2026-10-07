package eu.enact.greencharge;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import eu.enact.greencharge.model.CarbonSnapshot;
import eu.enact.greencharge.model.Charger;
import eu.enact.greencharge.model.RouteRequest;
import eu.enact.greencharge.model.RouteResponse;

/** The whole GreenCharge API: list chargers, read carbon, route to the greenest. */
@RestController
public class ChargerController {

    private final RoutingService routing;
    private final CarbonService carbon;

    public ChargerController(RoutingService routing, CarbonService carbon) {
        this.routing = routing;
        this.carbon = carbon;
    }

    @GetMapping("/chargers")
    public List<Charger> chargers() {
        return routing.chargers();
    }

    @GetMapping("/carbon")
    public CarbonSnapshot carbon() {
        return carbon.snapshot();
    }

    @PostMapping("/route")
    public RouteResponse route(@RequestBody(required = false) RouteRequest request) {
        return routing.route();
    }
}
