<#import "../layout.ftlh" as layout>
<#import "../maps.ftlh" as maps>
<@layout.layout title="Доставка #${order.id}">
<#assign status = order.status?string>
<#assign active = status == "NEW" || status == "COOKING" || status == "ON_THE_WAY">
<style>
    .gps-panel { display: flex; flex-wrap: wrap; align-items: center; justify-content: space-between; gap: 0.5rem 1rem; margin-top: 0.75rem; }
    .gps-dot { display: inline-block; width: 10px; height: 10px; border-radius: 50%; background: #95a5a6; margin-right: 0.4rem; }
    .gps-dot.is-on { background: #2ecc71; box-shadow: 0 0 0 4px rgba(46, 204, 113, 0.2); }
    .gps-dot.is-error { background: #e23d3d; }
    .courier-actions .btn { min-width: 12rem; }
</style>
<div class="container mt-4 mb-5" style="max-width: 860px" id="courierOrder"
     data-order-id="${order.id}"
     data-active="${active?c}"
     data-csrf-header="${(_csrf.headerName)!'X-CSRF-TOKEN'}"
     data-csrf-token="${(_csrf.token)!''}"
     data-geo="${geoJson}">
    <div class="d-flex flex-wrap justify-content-between align-items-center gap-2 mb-3">
        <h2 class="mb-0">Заказ #${order.id}</h2>
        <div class="d-flex align-items-center gap-2">
            <span class="badge bg-${order.status.badge}">${order.status.label}</span>
            <a href="/courier" class="btn btn-outline-secondary btn-sm"><i class="bi bi-arrow-left"></i> Все доставки</a>
        </div>
    </div>

    <div class="card shadow-sm mb-3">
        <div class="card-body">
            <div id="courierMap" class="geo-map" style="height: 380px"></div>
            <#if active>
                <div class="gps-panel">
                    <div><span id="gpsDot" class="gps-dot"></span><span id="gpsStatus">Включаем геолокацию…</span></div>
                    <div class="form-check form-switch mb-0">
                        <input class="form-check-input" type="checkbox" role="switch" id="manualMode">
                        <label class="form-check-label" for="manualMode">Ручной режим: двигаться кликом по карте</label>
                    </div>
                </div>
                <div class="text-muted small mt-1" id="gpsDistance"></div>
            </#if>
        </div>
    </div>

    <#if active>
        <div class="courier-actions d-flex flex-wrap gap-2 mb-3">
            <#if status == "NEW" || status == "COOKING">
                <form action="/courier/orders/${order.id}/pickup" method="post" class="mb-0">
                    <#if _csrf??><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/></#if>
                    <button class="btn btn-primary btn-lg" type="submit"><i class="bi bi-bag-check me-1"></i>Забрал заказ</button>
                </form>
                <div class="text-muted small align-self-center">
                    <#if status == "NEW">Ресторан ещё принимает заказ.<#else>Ресторан готовит. Нажмите, когда заберёте пакет.</#if>
                </div>
            <#else>
                <form action="/courier/orders/${order.id}/deliver" method="post" class="mb-0">
                    <#if _csrf??><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/></#if>
                    <button class="btn btn-success btn-lg" type="submit"><i class="bi bi-check2-circle me-1"></i>Доставил</button>
                </form>
                <div class="text-muted small align-self-center">Кнопка сработает в пределах 500 м от клиента.</div>
            </#if>
        </div>
    </#if>

    <div class="card shadow-sm mb-3">
        <div class="card-body">
            <dl class="row mb-0">
                <dt class="col-sm-3">Ресторан</dt>
                <dd class="col-sm-9">${view.restaurantName}<#if view.restaurantAddress?has_content><div class="text-muted small">${view.restaurantAddress}</div></#if></dd>
                <dt class="col-sm-3">Клиент</dt>
                <dd class="col-sm-9">${(order.user.name)!"—"}</dd>
                <dt class="col-sm-3">Адрес</dt>
                <dd class="col-sm-9">${order.address!"—"}</dd>
                <dt class="col-sm-3">Телефон</dt>
                <dd class="col-sm-9"><#if order.phone??><a href="tel:${order.phone}">${order.phone}</a><#else>—</#if></dd>
                <#if order.note?? && order.note?has_content>
                    <dt class="col-sm-3">Комментарий</dt>
                    <dd class="col-sm-9">${order.note}</dd>
                </#if>
                <dt class="col-sm-3">Оплата</dt>
                <dd class="col-sm-9">
                    <#if view.paid>
                        <span class="badge bg-success">Оплачен онлайн</span>
                    <#else>
                        <strong>Получить ${view.totalPrice?string["0.00"]} сом</strong> · ${view.paymentLabel}
                    </#if>
                </dd>
                <dt class="col-sm-3">Состав</dt>
                <dd class="col-sm-9 mb-0">
                    <#list order.items as item>
                        <div>${item.dish.name} × ${item.quantity}</div>
                    </#list>
                </dd>
            </dl>
        </div>
    </div>
</div>
<@maps.assets/>
<script>
    (function () {
        var root = document.getElementById("courierOrder");
        if (!root || !window.L || !window.EduMap) {
            return;
        }
        var geo = JSON.parse(root.dataset.geo);
        var orderId = root.dataset.orderId;
        var active = root.dataset.active === "true";
        var csrfHeader = root.dataset.csrfHeader;
        var csrfToken = root.dataset.csrfToken;

        var map = EduMap.create(document.getElementById("courierMap"), { lat: geo.center[0], lng: geo.center[1] }, 13);
        var points = [];
        if (geo.route) {
            L.polyline(geo.route, { color: "#e23d3d", weight: 5, opacity: 0.7 }).addTo(map);
            points = points.concat(geo.route);
        }
        var destination = null;
        if (geo.restaurant) {
            L.marker(geo.restaurant, { icon: EduMap.icon("restaurant") }).addTo(map).bindTooltip("Ресторан");
            points.push(geo.restaurant);
        }
        if (geo.destination) {
            destination = L.latLng(geo.destination);
            L.marker(geo.destination, { icon: EduMap.icon("home") }).addTo(map).bindTooltip("Клиент");
            points.push(geo.destination);
        }
        var courier = null;
        if (geo.courier) {
            placeCourier(geo.courier[0], geo.courier[1]);
            points.push(geo.courier);
        }
        if (points.length > 1) {
            map.fitBounds(L.latLngBounds(points), { padding: [30, 30], maxZoom: 16 });
        } else if (points.length === 1) {
            map.setView(points[0], 15);
        }

        function placeCourier(lat, lng) {
            if (!courier) {
                courier = L.marker([lat, lng], { icon: EduMap.icon("courier"), zIndexOffset: 1000 }).addTo(map).bindTooltip("Вы");
            } else {
                courier.setLatLng([lat, lng]);
            }
            var distance = document.getElementById("gpsDistance");
            if (distance && destination) {
                var meters = Math.round(L.latLng(lat, lng).distanceTo(destination));
                distance.textContent = "До клиента по прямой: " + (meters < 1000 ? meters + " м" : (meters / 1000).toFixed(1) + " км");
            }
        }

        if (!active) {
            return;
        }

        var dot = document.getElementById("gpsDot");
        var statusText = document.getElementById("gpsStatus");
        var manual = document.getElementById("manualMode");
        var watchId = null;
        var pending = null;
        var sending = false;
        var lastSentAt = 0;
        var stopped = false;
        var SEND_INTERVAL_MS = 3000;

        function status(text, state) {
            statusText.textContent = text;
            dot.className = "gps-dot" + (state ? " is-" + state : "");
        }

        function flush() {
            if (stopped || sending || !pending) {
                return;
            }
            var wait = lastSentAt + SEND_INTERVAL_MS - Date.now();
            if (wait > 0) {
                setTimeout(flush, wait);
                return;
            }
            var point = pending;
            pending = null;
            sending = true;
            var headers = { "Content-Type": "application/json" };
            headers[csrfHeader] = csrfToken;
            fetch("/api/courier/orders/" + orderId + "/location", {
                method: "POST",
                headers: headers,
                credentials: "same-origin",
                body: JSON.stringify({ lat: point.lat, lng: point.lng })
            }).then(function (response) {
                if (response.status === 409 || response.status === 404) {
                    stopped = true;
                    status("Заказ завершён или отменён. Обновляем страницу…", "error");
                    setTimeout(function () { location.reload(); }, 1500);
                    return;
                }
                if (!response.ok) {
                    throw new Error("HTTP " + response.status);
                }
                lastSentAt = Date.now();
                status("Клиент видит вас. Отправлено в " + new Date().toLocaleTimeString("ru-RU"), "on");
            }).catch(function () {
                status("Нет связи с сервером, повторим…", "error");
                lastSentAt = Date.now();
                if (!pending) {
                    pending = point;
                }
            }).finally(function () {
                sending = false;
                flush();
            });
        }

        function report(lat, lng) {
            placeCourier(lat, lng);
            pending = { lat: lat, lng: lng };
            flush();
        }

        function startGps() {
            if (!window.isSecureContext) {
                status("Геолокация работает только по HTTPS или на localhost. Включите ручной режим.", "error");
                return;
            }
            if (!navigator.geolocation) {
                status("Браузер не поддерживает геолокацию. Включите ручной режим.", "error");
                return;
            }
            status("Ждём сигнал GPS…");
            watchId = navigator.geolocation.watchPosition(function (position) {
                report(position.coords.latitude, position.coords.longitude);
            }, function (error) {
                var text = error.code === 1
                    ? "Доступ к геолокации запрещён. Разрешите его в настройках браузера."
                    : "GPS не отвечает. Выйдите на открытое место или включите ручной режим.";
                status(text, "error");
            }, { enableHighAccuracy: true, maximumAge: 3000, timeout: 20000 });
            keepScreenOn();
        }

        function stopGps() {
            if (watchId !== null) {
                navigator.geolocation.clearWatch(watchId);
                watchId = null;
            }
        }

        var wakeLock = null;
        function keepScreenOn() {
            if (!("wakeLock" in navigator) || wakeLock) {
                return;
            }
            navigator.wakeLock.request("screen").then(function (lock) {
                wakeLock = lock;
                lock.addEventListener("release", function () { wakeLock = null; });
            }).catch(function () {});
        }
        document.addEventListener("visibilitychange", function () {
            if (document.visibilityState === "visible" && watchId !== null) {
                keepScreenOn();
            }
        });

        function applyMode() {
            if (manual.checked) {
                stopGps();
                status("Ручной режим: кликните по карте, чтобы переместиться.");
            } else {
                startGps();
            }
        }
        manual.addEventListener("change", function () {
            sessionStorage.setItem("courierManualMode", manual.checked ? "1" : "0");
            applyMode();
        });
        map.on("click", function (event) {
            if (manual.checked) {
                report(event.latlng.lat, event.latlng.lng);
            }
        });

        manual.checked = sessionStorage.getItem("courierManualMode") === "1";
        applyMode();
    })();
</script>
</@layout.layout>
