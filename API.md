OrganiCity Scenario Tool REST API Specification
===============================================

Scenarios
---------

GET http://localhost:9000/scenarios returns a listing of all available scenarios.

```
curl -v -H "Accept: application/json" http://localhost:9000/scenarios
```

```
[
  {
    "id":"AU5kFdCNQx5N3_WagHt1",
    "title":"CONTEXT-AWARE MULTIMODAL REAL TIME TRAVEL PLANNER",
    "summary":"Helps people to get from A to B",
    "narrative":"Tony...",
    "creator":"TODO WHAT SHALL WE DO HERE?"
  },
  [...]
]
```