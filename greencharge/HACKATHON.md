# GreenCharge — ENACT Hackathon Brief

A city routes EV drivers to the **greenest available charger**. Grid carbon
intensity swings hour by hour, so the greenest charger changes — but the live
carbon feed belongs to the energy **utility** and is shared only under a data
contract, and the service must run **in-region** on **green-powered compute**.

The app is done and trivial. **Your challenge is to take it live entirely
through the ENACT SDK.** You edit exactly one line of app code; everything else
is SDK-driven.

## Run it locally

```bash
mvn spring-boot:run
```

The app starts on `http://localhost:8080` and **opens the frontend in your
browser automatically**. You'll see per-district carbon bars and a recommended
charger. Until you complete Step 1, it uses a built-in **mock** carbon feed
(the badge says so).

## The API

| Method | Path        | Returns                                                |
|--------|-------------|--------------------------------------------------------|
| GET    | `/chargers` | The charger fleet with availability                    |
| GET    | `/carbon`   | Per-district carbon intensity + green score + source   |
| POST   | `/route`    | The greenest available charger + the full ranking      |

## The six ENACT steps

| # | Capability      | What you do                                                                                     |
|---|-----------------|-------------------------------------------------------------------------------------------------|
| 1 | Dataspaces      | Consume the utility's `grid-carbon-intensity` asset; the transfer drops a carbon file on your machine — set `carbon.feed.file` to its path. |
| 2 | Packaging       | Generate a Helm chart: image `greencharge:1.0`, port 8080, service, ingress `greencharge.local`. |
| 3 | App Controller  | Inject **Energy efficiency + Elasticity + Load balancing** modules into the pom.                |
| 4 | Policies        | Author a RuntimePolicy: **Soft** green ≥ 0.6, **Hard** region `eu-west`, availability 0.9.      |
| 5 | Deploy          | Deploy the chart + policy; the operator places the pod.                                          |
| 6 | Monitor         | Trigger a load burst; watch **Energy** + **LoadBalancer** dashboards react.                     |

Every step has a **UI wizard** path and an equivalent **AI assistant** path
(`generate_deployment`, `configure_app_controller`, `generate_runtime_policy`).
Bonus for completing at least one step via the AI assistant.

## The one line you edit

In `src/main/resources/application.yml`:

```yaml
carbon:
  feed:
    file: REPLACE_ME   # <- path to the carbon file transferred in Step 1
```

The transferred file is JSON — a map of district to carbon intensity (gCO₂/kWh):

```json
{ "Riverside": 95, "Uptown": 180, "OldTown": 300, "Harbor": 150 }
```

The instant that file is wired in, the page's picks change (here Harbor becomes
greener than Uptown). That "aha" is the payoff of the dataspace step — and it
costs you zero code.

## Done when…

- The utility's live carbon data flows in and the page's picks change.
- A valid Helm chart + the three AC modules + a validator-clean RuntimePolicy all exist.
- The pod lands on `worker-green`; `worker-remote` is rejected for region (Hard beats Soft).
- The Energy + LoadBalancer dashboards react to the load burst.
