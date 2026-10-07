package eu.enact.greencharge;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import eu.enact.greencharge.model.CarbonSnapshot;
import eu.enact.greencharge.model.Charger;
import eu.enact.greencharge.model.RouteResponse;
import eu.enact.greencharge.model.ScoredCharger;

/**
 * Routes a driver to the greenest available charger.
 *
 * <p>The chargers are a fixed demo set. The "greenest" one changes with the
 * carbon feed: routing weights whatever carbon map {@link CarbonService} hands
 * it, so live data flowing in at Step 1 visibly changes the pick.
 */
@Service
public class RoutingService {

    /** Fixed demo fleet. Riverside Marina is full (0 slots) to show availability filtering. */
    private static final List<Charger> CHARGERS = List.of(
            new Charger("ch-01", "Riverside Central", "Riverside", 3, 4),
            new Charger("ch-02", "Riverside Marina", "Riverside", 0, 4),
            new Charger("ch-03", "Uptown Plaza", "Uptown", 2, 6),
            new Charger("ch-04", "Harbor Docks", "Harbor", 5, 6),
            new Charger("ch-05", "Old Town Square", "OldTown", 1, 4));

    private final CarbonService carbonService;

    public RoutingService(CarbonService carbonService) {
        this.carbonService = carbonService;
    }

    public List<Charger> chargers() {
        return CHARGERS;
    }

    public RouteResponse route() {
        CarbonSnapshot carbon = carbonService.snapshot();

        List<ScoredCharger> ranked = CHARGERS.stream()
                .filter(c -> c.availableSlots() > 0)
                .map(c -> new ScoredCharger(
                        c.id(),
                        c.name(),
                        c.district(),
                        carbon.intensity().getOrDefault(c.district(), Double.MAX_VALUE),
                        carbon.greenScore().getOrDefault(c.district(), 0),
                        c.availableSlots()))
                .sorted(Comparator
                        .comparingInt(ScoredCharger::greenScore).reversed()
                        .thenComparing(Comparator.comparingInt(ScoredCharger::availableSlots).reversed()))
                .toList();

        ScoredCharger recommended = ranked.isEmpty() ? null : ranked.get(0);
        return new RouteResponse(carbon.source(), recommended, ranked, Instant.now());
    }
}
