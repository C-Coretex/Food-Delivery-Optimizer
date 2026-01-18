const map = L.map("map").setView([56.9337, 24.1258], 11);

const colors = [
  "#f44336",
  "#e81e63",
  "#9c27b0",
  "#673ab7",
  "#3f51b5",
  "#2196f3",
  "#03a9f4",
  "#00bcd4",
  "#009688",
  "#4caf50",
  "#8bc34a",
  "#cddc39",
  "#ffeb3b",
  "#ffc107",
  "#ff9800",
  "#ff5722",
];

let colorIdx = 0;

const createFaDivIcon = (faClass, color) =>
  L.divIcon({
    html: `<i class="${faClass}"${color ? ` style="color: ${color}"` : ""}></i>`,
  });

const ICON_RED = "#ff0000";

const icons = {
  restaurant: createFaDivIcon("fas fa-utensils"),
  restaurantRed: createFaDivIcon("fas fa-utensils", ICON_RED),
  customer: createFaDivIcon("fas fa-warehouse"),
  customerRed: createFaDivIcon("fas fa-warehouse", ICON_RED),
};

$(document).ready(() => {
  const solutionId = new URLSearchParams(window.location.search).get("id");

  L.tileLayer("https://tile.openstreetmap.org/{z}/{x}/{y}.png", {
    maxZoom: 19,
    attribution:
      '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>',
  }).addTo(map);

  if (!solutionId) return;

  $.getJSON(`/fdo/score/${solutionId}`, (analysis) => renderScore(analysis));
  $.getJSON(`/fdo/${solutionId}`, (solution) => {
    $.getJSON(`/fdo/indictments/${solutionId}`, (indictments) => {
      renderRoutes(solution, indictments);
      initPopovers();
    });
  });
});

function initPopovers() {
  $('[data-toggle="popover"]').popover();
}

function renderScore(analysis) {
  const badgeClass =
    getHardScore(analysis.score) == 0 ? "badge bg-success" : "badge bg-danger";

  $("#score_a").attr({
    title: "Score Brakedown",
    "data-bs-content": String(getScorePopoverContent(analysis.constraints)),
    "data-bs-html": "true",
  });

  $("#score_text").text(analysis.score).attr({ class: badgeClass });

  initPopovers();
}

// marker overlap fix
function jitterLatLng(lat, lon, index, total, radiusMeters = 10) {
  if (total <= 1) return [lat, lon];

  const angle = (2 * Math.PI * index) / total;

  const dLat = (radiusMeters / 111320) * Math.sin(angle);
  const dLon =
    (radiusMeters / (111320 * Math.cos((lat * Math.PI) / 180))) *
    Math.cos(angle);

  return [lat + dLat, lon + dLon];
}

function renderRoutes(solution, indictments) {
  $("#solutionTitle").text(
    `Version 18/Jan/2026${solution.name ?? ""}  ${solution.solverStatus}`,
  );

  const indictmentMap = Object.fromEntries(
    (indictments ?? []).map((i) => [String(i.indictedObjectID), i]),
  );

  const shifts = solution.courierShifts ?? [];
  if (shifts.length === 0) return;

  const overlayLayers = {};
  const shiftGroups = [];

  shifts.forEach((shift, shiftIdx) => {
    const visits = shift.visits ?? [];
    if (visits.length === 0) return;
    if (!visits[0].location) return;

    const routeColor = getColor();

    const shiftLabel = `CourierShift ${shiftIdx + 1}${shift.id != null ? ` (id=${shift.id})` : ""}`;
    const shiftGroup = L.layerGroup().addTo(map);

    overlayLayers[shiftLabel] = shiftGroup;
    shiftGroups.push(shiftGroup);

    const coordKey = (loc) => `${loc.lat.toFixed(6)},${loc.lon.toFixed(6)}`;
    const dupMap = new Map();

    visits.forEach((v) => {
      if (!v.location) return;
      const key = coordKey(v.location);
      const arr = dupMap.get(key) ?? [];
      arr.push(v.id);
      dupMap.set(key, arr);
    });

    const dupSeen = new Map();

    visits.forEach((visit, idx) => {
      if (!visit.location) return;

      const key = coordKey(visit.location);
      const total = (dupMap.get(key) ?? []).length;
      const seen = dupSeen.get(key) ?? 0;

      const [jLat, jLon] = jitterLatLng(
        visit.location.lat,
        visit.location.lon,
        seen,
        total,
      );

      dupSeen.set(key, seen + 1);

      const marker = L.marker([jLat, jLon]).addTo(shiftGroup);

      marker.setIcon(getVisitIcon(visit.type, indictmentMap[String(visit.id)]));
      marker.bindPopup(buildVisitPopup(visit, idx, shift, indictmentMap));

      drawPathToNext(
        visit,
        [jLat, jLon],
        visits[idx + 1],
        routeColor,
        shiftGroup,
      );
    });
  });

  if (map._shiftLayersControl) {
    map.removeControl(map._shiftLayersControl);
  }
  map._shiftLayersControl = L.control
    .layers(null, overlayLayers, {
      collapsed: false,
    })
    .addTo(map);

  const allLayers = shiftGroups.flatMap((g) => g.getLayers());
  const latLngs = [];
  allLayers.forEach((layer) => {
    if (layer.getLatLng) latLngs.push(layer.getLatLng());
    if (layer.getLatLngs) {
      const pts = layer.getLatLngs();
      pts.flat?.(10)?.forEach?.((p) => p?.lat != null && latLngs.push(p));
    }
  });
  if (latLngs.length > 0) {
    map.fitBounds(L.latLngBounds(latLngs), { padding: [20, 20] });
  }
}

function buildVisitPopup(visit, idx, shift, indictmentMap) {
  const isRestaurant = visit.type === "RESTAURANT";

  const restaurantBlock = isRestaurant
    ? `<br>restaurantId=${visit.restaurantId ?? "null"}<br>restaurantChainId=${visit.restaurantChainId ?? "null"}`
    : "";

  const roadTime = visit.roadTime != null ? `${visit.roadTime}min` : "null";

  return `
    <b>visit id=${visit.id}</b> (this courier's ${idx + 1} visit)
    <br>visit type=${visit.type ?? "null"}${restaurantBlock}
    <br>departure time=${formatTime(visit.minuteTime) ?? "null"}
    <br>road time=${roadTime}
    <br>order id=${visit.orderId ?? "null"}
    <hr>
    <br>courier shift id=${shift.id ?? "null"}
    <br>hotCapacity=${shift.hotCapacity}
    <br>coldCapacity=${shift.coldCapacity}
    <hr>
    ${getEntityPopoverContent(String(visit.id), indictmentMap)}
  `.trim();
}

function drawPathToNext(visit, startLocation, nextVisit, color, layerGroup) {
  const path = visit.pathToNext;
  if (!Array.isArray(path) || path.length === 0) return;

  let prev = startLocation;

  path.forEach((point) => {
    if (!point || point.lat == null || point.lon == null) return;

    const cur = [point.lat, point.lon];
    L.polyline([prev, cur], { color }).addTo(layerGroup);
    prev = cur;
  });

  if (nextVisit?.location) {
    const end = [nextVisit.location.lat, nextVisit.location.lon];
    L.polyline([prev, end], { color }).addTo(layerGroup);
  }
}

function getEntityPopoverContent(entityId, indictmentMap) {
  const indictment = indictmentMap[entityId];
  if (!indictment) return "";

  let html = `Total score: <b>${indictment.score}</b> (${indictment.matchCount})<br>`;

  (indictment.constraintMatches ?? []).forEach((match) => {
    const line = `${match.constraintName} : ${match.score}<br>`;
    html += getHardScore(match.score) == 0 ? line : `<b>${line}</b>`;
  });

  return html;
}

function getVisitIcon(visitType, indictment) {
  const isCustomer = String(visitType) === "CUSTOMER";
  const ok = !indictment || getHardScore(indictment.score) == 0;

  if (isCustomer) return ok ? icons.customer : icons.customerRed;
  return ok ? icons.restaurant : icons.restaurantRed;
}

function getColor() {
  colorIdx = (colorIdx + 4) % colors.length;
  return colors[colorIdx];
}

function getScorePopoverContent(constraints) {
  let html = "";
  (constraints ?? []).forEach((constraint) => {
    const line = `${constraint.name} : ${constraint.score}<br>`;
    html += getHardScore(constraint.score) == 0 ? line : `<b>${line}</b>`;
  });
  return html;
}

function getHardScore(score) {
  return score.slice(0, score.indexOf("hard"));
}

function formatTime(minutes) {
  const h = Math.floor(minutes / 60);
  const m = minutes % 60;
  return `${String(h).padStart(2, "0")}:${String(m).padStart(2, "0")}`;
}
