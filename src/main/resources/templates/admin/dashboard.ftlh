<#import "../layout.ftlh" as layout>
<#import "nav.ftlh" as admin>
<@layout.layout title="Статистика">
<div class="container mt-4 mb-5">
    <h2 class="mb-1">Панель администратора</h2>
    <p class="text-muted">Сводка по заказам, выручке и популярным блюдам</p>
    <@admin.nav "dashboard"/>

    <div class="row g-3 mb-4">
        <div class="col-6 col-lg-2"><div class="card stat-card"><div class="card-body"><div class="text-muted small">Пользователи</div><div class="value">${userCount}</div></div></div></div>
        <div class="col-6 col-lg-2"><div class="card stat-card"><div class="card-body"><div class="text-muted small">Рестораны</div><div class="value">${restaurantCount}</div></div></div></div>
        <div class="col-6 col-lg-2"><div class="card stat-card"><div class="card-body"><div class="text-muted small">Блюда</div><div class="value">${dishCount}</div></div></div></div>
        <div class="col-6 col-lg-2"><div class="card stat-card"><div class="card-body"><div class="text-muted small">Заказы</div><div class="value">${orderCount}</div></div></div></div>
        <div class="col-6 col-lg-2"><div class="card stat-card"><div class="card-body"><div class="text-muted small">Выручка</div><div class="value">${revenue?string["0"]}</div></div></div></div>
        <div class="col-6 col-lg-2"><div class="card stat-card"><div class="card-body"><div class="text-muted small">Средний чек</div><div class="value">${averageCheck?string["0"]}</div></div></div></div>
    </div>

    <div class="row g-4 mb-4">
        <div class="col-lg-5">
            <div class="card stat-card h-100">
                <div class="card-body">
                    <h5>Заказы по статусам</h5>
                    <div style="height: 260px"><canvas id="statusChart"></canvas></div>
                </div>
            </div>
        </div>
        <div class="col-lg-7">
            <div class="card stat-card h-100">
                <div class="card-body">
                    <h5>Выручка по ресторанам</h5>
                    <div style="height: 260px"><canvas id="restaurantChart"></canvas></div>
                </div>
            </div>
        </div>
    </div>

    <div class="card stat-card mb-4">
        <div class="card-body">
            <h5>Заказы и выручка за 30 дней</h5>
            <div style="height: 280px"><canvas id="dayChart"></canvas></div>
        </div>
    </div>

    <div class="row g-4">
        <div class="col-lg-5">
            <div class="card stat-card h-100">
                <div class="card-body">
                    <h5>Статусы</h5>
                    <table class="admin-table">
                        <thead><tr><th>Статус</th><th class="text-end">Заказов</th></tr></thead>
                        <tbody>
                        <#list statusRows as row>
                            <tr><td>${row.label}</td><td class="text-end">${row.count}</td></tr>
                        </#list>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
        <div class="col-lg-7">
            <div class="card stat-card h-100">
                <div class="card-body">
                    <h5>Популярные блюда</h5>
                    <div class="table-responsive">
                    <table class="admin-table">
                        <thead><tr><th>Блюдо</th><th>Ресторан</th><th class="text-end">Штук</th><th class="text-end">Сумма</th></tr></thead>
                        <tbody>
                        <#list topDishes as dish>
                            <tr>
                                <td class="break-text">${dish.name}</td>
                                <td class="break-text">${dish.restaurant}</td>
                                <td class="text-end col-nowrap">${dish.quantity}</td>
                                <td class="text-end col-nowrap">${dish.revenue?string["0.00"]}</td>
                            </tr>
                        </#list>
                        <#if !topDishes?has_content>
                            <tr><td colspan="4" class="text-muted">Пока нет данных</td></tr>
                        </#if>
                        </tbody>
                    </table>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <div class="card stat-card table-card mt-4">
        <div class="card-body">
            <h5>Последние заказы</h5>
            <div class="table-responsive">
                <table class="admin-table">
                    <thead><tr><th>№</th><th>Клиент</th><th>Статус</th><th>Дата</th><th class="text-end">Сумма</th></tr></thead>
                    <tbody>
                    <#list recentOrders as order>
                        <tr>
                            <td class="col-nowrap"><a href="/orders/${order.id}">#${order.id}</a></td>
                            <td class="break-text">${order.customer}</td>
                            <td class="col-nowrap"><span class="badge bg-${order.badge}">${order.status}</span></td>
                            <td class="col-nowrap">${order.createdAt}</td>
                            <td class="text-end col-nowrap">${order.total?string["0.00"]}</td>
                        </tr>
                    </#list>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>
<script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.1/dist/chart.umd.min.js"></script>
<script>
    const brand = ["#ff6b6b", "#ffa502", "#1e90ff", "#2ed573", "#747d8c"];
    new Chart(document.getElementById("statusChart"), {
        type: "doughnut",
        data: {
            labels: ${statusLabelsJson?no_esc},
            datasets: [{ data: ${statusCountsJson?no_esc}, backgroundColor: brand }]
        },
        options: { maintainAspectRatio: false, plugins: { legend: { position: "bottom" } } }
    });
    new Chart(document.getElementById("restaurantChart"), {
        type: "bar",
        data: {
            labels: ${restaurantLabelsJson?no_esc},
            datasets: [{ label: "Сом", data: ${restaurantRevenueJson?no_esc}, backgroundColor: "#ff6b6b" }]
        },
        options: { maintainAspectRatio: false, plugins: { legend: { display: false } }, scales: { y: { beginAtZero: true } } }
    });
    new Chart(document.getElementById("dayChart"), {
        type: "bar",
        data: {
            labels: ${dayLabelsJson?no_esc},
            datasets: [
                { type: "bar", label: "Заказы", data: ${dayOrdersJson?no_esc}, backgroundColor: "#ffa502", yAxisID: "orders" },
                { type: "line", label: "Выручка", data: ${dayRevenueJson?no_esc}, borderColor: "#ff6b6b", backgroundColor: "#ff6b6b", yAxisID: "money", tension: 0.3 }
            ]
        },
        options: {
            maintainAspectRatio: false,
            scales: {
                orders: { beginAtZero: true, position: "left", title: { display: true, text: "Заказы" } },
                money: { beginAtZero: true, position: "right", grid: { drawOnChartArea: false }, title: { display: true, text: "Сом" } }
            }
        }
    });
</script>
</@layout.layout>
