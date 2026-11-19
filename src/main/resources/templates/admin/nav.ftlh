<#macro nav active="">
<ul class="nav admin-nav nav-pills mb-4 flex-nowrap overflow-auto">
    <li class="nav-item"><a class="nav-link ${(active == 'dashboard')?string('active','')}" href="/admin"><i class="bi bi-bar-chart me-1"></i>Статистика</a></li>
    <li class="nav-item"><a class="nav-link ${(active == 'orders')?string('active','')}" href="/admin/orders"><i class="bi bi-bag me-1"></i>Заказы</a></li>
    <li class="nav-item"><a class="nav-link ${(active == 'restaurants')?string('active','')}" href="/admin/restaurants"><i class="bi bi-shop me-1"></i>Рестораны</a></li>
    <li class="nav-item"><a class="nav-link ${(active == 'dishes')?string('active','')}" href="/admin/dishes"><i class="bi bi-egg-fried me-1"></i>Блюда</a></li>
    <li class="nav-item"><a class="nav-link ${(active == 'users')?string('active','')}" href="/admin/users"><i class="bi bi-people me-1"></i>Пользователи</a></li>
    <li class="nav-item"><a class="nav-link ${(active == 'settings')?string('active','')}" href="/admin/settings"><i class="bi bi-sliders me-1"></i>Настройки</a></li>
</ul>
</#macro>

<#function roleLabel name>
    <#if name == "ADMIN"><#return "Администратор"></#if>
    <#if name == "CUSTOMER"><#return "Покупатель"></#if>
    <#if name == "COURIER"><#return "Курьер"></#if>
    <#return name!"">
</#function>

<#macro date value>
    <#if value??>
        ${value.dayOfMonth?string["00"]}.${value.monthValue?string["00"]}.${value.year?c} ${value.hour?string["00"]}:${value.minute?string["00"]}
    <#else>
        —
    </#if>
</#macro>

<#function media path fallback>
    <#if !path?? || path?trim == "">
        <#return fallback>
    </#if>
    <#if path?starts_with("/") || path?starts_with("http")>
        <#return path>
    </#if>
    <#return "/static/" + path>
</#function>
