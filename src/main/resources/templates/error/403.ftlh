<#import "../layout.ftlh" as layout>
<@layout.layout title="Нет доступа">
<div class="auth-container text-center">
    <i class="bi bi-shield-lock" style="font-size: 3rem; color: #ff6b6b;"></i>
    <h2 class="mt-3">Нет доступа</h2>
    <p class="text-muted">Эта страница доступна только администратору.</p>
    <a class="btn btn-primary" href="/restaurants">К ресторанам</a>
</div>
</@layout.layout>
