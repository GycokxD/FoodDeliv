<#import "../layout.ftlh" as layout>
<#import "nav.ftlh" as admin>
<@layout.layout title="Ресторан">
<div class="container mt-4 mb-5">
    <h2><#if restaurant.id??>Редактировать ресторан<#else>Новый ресторан</#if></h2>
    <@admin.nav "restaurants"/>
    <div class="card stat-card">
        <div class="card-body">
            <form action="<#if restaurant.id??>/admin/restaurants/${restaurant.id}<#else>/admin/restaurants</#if>" method="post" class="col-lg-8" enctype="multipart/form-data">
                <#if _csrf??><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/></#if>
                <div class="mb-3">
                    <label class="form-label" for="name">Название</label>
                    <input class="form-control" id="name" name="name" required value="${restaurant.name!''}">
                </div>
                <div class="mb-3">
                    <label class="form-label" for="address">Адрес</label>
                    <input class="form-control" id="address" name="address" required value="${restaurant.address!''}">
                </div>
                <div class="mb-3">
                    <label class="form-label" for="file">Загрузить фото</label>
                    <input class="form-control" id="file" name="file" type="file" accept="image/*">
                </div>
                <div class="mb-3">
                    <label class="form-label" for="image">Или путь к уже загруженной картинке</label>
                    <input class="form-control" id="image" name="image" value="${restaurant.image!''}" placeholder="images/restaurants/pizza-house.jpg">
                </div>
                <button class="btn btn-primary" type="submit">Сохранить</button>
                <a class="btn btn-outline-secondary" href="/admin/restaurants">Отмена</a>
            </form>
        </div>
    </div>
</div>
</@layout.layout>
