function getScorePopoverContent(constraint_list) {
  var popover_content = "";
  constraint_list.forEach((constraint) => {
    if (getHardScore(constraint.score) == 0) {
      popover_content =
        popover_content + constraint.name + " : " + constraint.score + "<br>";
    } else {
      popover_content =
        popover_content +
        "<b>" +
        constraint.name +
        " : " +
        constraint.score +
        "</b><br>";
    }
  });
  return popover_content;
}

function getEntityPopoverContent(entityId, indictmentMap) {
  var popover_content = "";
  const indictment = indictmentMap[entityId];
  if (indictment != null) {
    popover_content =
      popover_content +
      "Total score: <b>" +
      indictment.score +
      "</b> (" +
      indictment.matchCount +
      ")<br>";
    indictment.constraintMatches.forEach((match) => {
      if (getHardScore(match.score) == 0) {
        popover_content =
          popover_content + match.constraintName + " : " + match.score + "<br>";
      } else {
        popover_content =
          popover_content +
          "<b>" +
          match.constraintName +
          " : " +
          match.score +
          "</b><br>";
      }
    });
  }
  return popover_content;
}

function getHardScore(score) {
  return score.slice(0, score.indexOf("hard"));
}

function getSoftScore(score) {
  return score.slice(score.indexOf("hard/"), score.indexOf("soft"));
}

$(document).ready(function () {
  const urlParams = new URLSearchParams(window.location.search);
  const solutionId = urlParams.get("id");

  $.getJSON("/fdo/score/" + solutionId, function (analysis) {
    var badge = "badge bg-danger";
    if (getHardScore(analysis.score) == 0) {
      badge = "badge bg-success";
    }

    $("#score_a").attr({
      title: "Score Breakdown",
      "data-bs-content": "" + getScorePopoverContent(analysis.constraints) + "",
      "data-bs-html": "true",
    });

    $("#score_text").text(analysis.score);
    $("#score_text").attr({ class: badge });
  });

  $.getJSON("/fdo/" + solutionId, function (solution) {
    $.getJSON("/fdo/indictments/" + solutionId, function (indictments) {
      renderRoutes(solution, indictments);
      $(function () {
        $('[data-toggle="popover"]').popover();
      });
    });
  });
});

function renderRoutes(solution, indictments) {
  var indictmentMap = {};
  indictments.forEach((indictment) => {
    indictmentMap[indictment.indictedObjectID] = indictment;
  });

  const container = $("#shift_container");
  container.empty();

  const shifts = solution.courierShifts ?? [];
  if (shifts.length === 0) {
    container.append($("<div>No courier shifts.</div>"));
    return;
  }

  shifts.forEach((shift) => {
    const shiftId = String(shift.id);

    var shift_badge = "badge bg-danger";
    if (
      indictmentMap[shiftId] == null ||
      getHardScore(indictmentMap[shiftId].score) == 0
    ) {
      shift_badge = "badge bg-success";
    }

    const shiftLines = [];
    if (shift.hotCapacity != null)
      shiftLines.push("hotCapacity=" + shift.hotCapacity);
    if (shift.coldCapacity != null)
      shiftLines.push("coldCapacity=" + shift.coldCapacity);

    container.append(
      $(
        '<a data-toggle="popover" data-bs-html="true" data-bs-content="' +
          escapeHtml(shiftLines.join("<br>")) +
          "<hr>" +
          getEntityPopoverContent(shiftId, indictmentMap) +
          '" data-bs-original-title="' +
          escapeHtml("CourierShift " + shiftId) +
          '"><span class="' +
          shift_badge +
          '">' +
          escapeHtml("CourierShift " + shiftId) +
          "</span></a>",
      ),
    );

    const visits = shift.visits ?? [];
    if (visits.length === 0) {
      container.append($("<div class='ms-3'>no visits</div>"));
      container.append($("<br>"));
      return;
    }

    var visit_nr = 1;
    visits.forEach((visit) => {
      const visitId = visit?.id != null ? String(visit.id) : "null";

      var visit_badge = "badge bg-danger";
      if (
        indictmentMap[visitId] == null ||
        getHardScore(indictmentMap[visitId].score) == 0
      ) {
        visit_badge = "badge bg-success";
      }

      const titleParts = [];
      titleParts.push("#" + visit_nr);
      titleParts.push("Visit " + visitId);
      if (visit.type != null) titleParts.push(String(visit.type));

      const lines = [];

      if (visit.type != null) {
        lines.push("type: " + visit.type);
      }

      if (visit.minuteTime != null) {
        lines.push("minuteTime: " + visit.minuteTime);
      }

      if (visit.roadTime != null) {
        lines.push("roadTime: " + visit.roadTime);
      }

      if (visit.orderId != null) {
        lines.push("orderId: " + visit.orderId);
      }

      if (visit.restaurantName != null) {
        lines.push("restaurant: " + visit.restaurantName);
      }

      container.append(
        $(
          '<a data-toggle="popover" data-bs-html="true" data-bs-content="' +
            escapeHtml(lines.join("<br>")) +
            "<hr>" +
            getEntityPopoverContent(visitId, indictmentMap) +
            '" data-bs-original-title="' +
            escapeHtml(titleParts.join(" | ")) +
            '"><span class="' +
            visit_badge +
            '">' +
            escapeHtml(titleParts.join(" | ")) +
            "</span></a>",
        ),
      );

      visit_nr += 1;
    });

    container.append($("<br><br>"));
  });
}

function escapeHtml(s) {
  return String(s)
    .replaceAll("&", "&amp;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;");
}
