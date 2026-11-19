(function () {
    var ICONS = {
        restaurant: "bi-shop",
        home: "bi-house-door",
        courier: "bi-bicycle"
    };

    function create(element, center, zoom) {
        var map = L.map(element, { scrollWheelZoom: false }).setView([center.lat, center.lng], zoom || 13);
        L.tileLayer("https://tile.openstreetmap.org/{z}/{x}/{y}.png", {
            maxZoom: 19,
            attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
        }).addTo(map);
        map.on("focus", function () { map.scrollWheelZoom.enable(); });
        map.on("blur", function () { map.scrollWheelZoom.disable(); });
        return map;
    }

    function icon(kind) {
        return L.divIcon({
            className: "geo-pin geo-pin--" + kind,
            html: '<i class="bi ' + (ICONS[kind] || "bi-geo-alt") + '"></i>',
            iconSize: [36, 36],
            iconAnchor: kind === "courier" ? [18, 18] : [18, 36],
            popupAnchor: [0, -32]
        });
    }

    function getJson(url) {
        return fetch(url, { headers: { "Accept": "application/json" }, credentials: "same-origin" })
            .then(function (response) {
                if (!response.ok) {
                    throw new Error("HTTP " + response.status);
                }
                return response.json();
            });
    }

    function number(value) {
        var parsed = parseFloat(value);
        return isNaN(parsed) ? null : parsed;
    }

    window.EduMap = { create: create, icon: icon, getJson: getJson, number: number };
})();
