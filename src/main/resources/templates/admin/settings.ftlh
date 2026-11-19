<#import "../layout.ftlh" as layout>
<#import "nav.ftlh" as admin>
<@layout.layout title="Настройки">
<div class="container mt-4 mb-5">
    <h2>Настройки</h2>
    <p class="text-muted">Время этапов влияет на новые заказы с живым отслеживанием. Город и телефон видны в подвале сайта.</p>
    <@admin.nav "settings"/>
    <div class="admin-panel">
        <div class="p-4">
            <form action="/admin/settings" method="post" class="row g-3">
                <#if _csrf??><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/></#if>
                <div class="col-md-4">
                    <label class="form-label" for="acceptedSeconds">Секунд в статусе «Новый»</label>
                    <input class="form-control" id="acceptedSeconds" name="acceptedSeconds" type="number" min="5" max="1800" required value="${settings.acceptedSeconds!20}">
                </div>
                <div class="col-md-4">
                    <label class="form-label" for="cookingSeconds">Секунд готовки</label>
                    <input class="form-control" id="cookingSeconds" name="cookingSeconds" type="number" min="5" max="1800" required value="${settings.cookingSeconds!30}">
                </div>
                <div class="col-md-4">
                    <label class="form-label" for="deliverySeconds">Секунд в пути</label>
                    <input class="form-control" id="deliverySeconds" name="deliverySeconds" type="number" min="5" max="1800" required value="${settings.deliverySeconds!60}">
                </div>
                <div class="col-md-6">
                    <label class="form-label" for="city">Город</label>
                    <input class="form-control" id="city" name="city" value="${settings.city!''}">
                </div>
                <div class="col-md-6">
                    <label class="form-label" for="supportPhone">Телефон поддержки</label>
                    <input class="form-control" id="supportPhone" name="supportPhone" value="${settings.supportPhone!''}">
                </div>
                <div class="col-12">
                    <button class="btn btn-primary" type="submit">Сохранить</button>
                </div>
            </form>
        </div>
    </div>
</div>
</@layout.layout>
