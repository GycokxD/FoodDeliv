<#import "../layout.ftlh" as layout>
<#import "nav.ftlh" as admin>
<@layout.layout title="Блюдо">
<div class="container mt-4 mb-5">
    <h2><#if dish.id??>Редактировать блюдо<#else>Новое блюдо</#if></h2>
    <@admin.nav "dishes"/>
    <div class="card stat-card">
        <div class="card-body">
            <form action="<#if dish.id??>/admin/dishes/${dish.id}<#else>/admin/dishes</#if>" method="post" class="col-lg-8" enctype="multipart/form-data">
                <#if _csrf??><input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/></#if>
                <div class="mb-3">
                    <label class="form-label" for="restaurantId">Ресторан</label>
                    <select class="form-select" id="restaurantId" name="restaurantId" required>
                        <#list restaurants as restaurant>
                            <option value="${restaurant.id}" <#if dish.restaurant?? && dish.restaurant.id == restaurant.id>selected</#if>>${restaurant.name}</option>
                        </#list>
                    </select>
                </div>
                <div class="mb-3">
                    <label class="form-label" for="name">Название</label>
                    <input class="form-control" id="name" name="name" required value="${dish.name!''}">
                </div>
                <div class="mb-3">
                    <label class="form-label" for="description">Описание</label>
                    <textarea class="form-control" id="description" name="description" rows="3">${dish.description!''}</textarea>
                </div>
                <div class="mb-3">
                    <label class="form-label" for="price">Цена, сом</label>
                    <input class="form-control" id="price" name="price" type="number" min="1" step="0.01" required value="<#if dish.price??>${dish.price?string['0.##']}</#if>">
                </div>
                <div class="mb-3">
                    <label class="form-label" for="file">Загрузить фото</label>
                    <input class="form-control" id="file" name="file" type="file" accept="image/*">
                </div>
                <div class="mb-3">
                    <label class="form-label" for="image">Или путь к уже загруженной картинке</label>
                    <input class="form-control" id="image" name="image" value="${dish.image!''}" placeholder="images/dishes/margarita.jpg">
                </div>
                <button class="btn btn-primary" type="submit">Сохранить</button>
                <a class="btn btn-outline-secondary" href="/admin/dishes">Отмена</a>
            </form>
        </div>
    </div>
</div>
</@layout.layout>
