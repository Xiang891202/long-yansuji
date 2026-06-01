<template>
  <div class="home">
    <!-- 品牌標頭 Banner -->
    <header class="brand-header">
      <div class="container">
        <h1 class="logo">龍鹽酥雞</h1>
        <p class="slogan">經典台灣味・現點現炸</p>
      </div>
    </header>

    <!-- 分類導航列 -->
    <nav class="category-nav">
      <div class="container">
        <ul>
          <li :class="{ active: currentCategory === null }" @click="filterByCategory(null)">全部分類</li>
          <li v-for="cat in categories" :key="cat.id" :class="{ active: currentCategory === cat.id }" @click="filterByCategory(cat.id)">
            {{ cat.name }}
          </li>
        </ul>
      </div>
    </nav>

    <!-- 商品展示區 -->
    <main class="product-grid container">
      <div v-for="product in products" :key="product.id" class="product-card">
        <div class="product-image">
          <img :src="product.imageUrl || '/default-product.png'" :alt="product.name">
        </div>
        <div class="product-info">
          <h3>{{ product.name }}</h3>
          <p class="desc">{{ product.description || '嚴選食材，新鮮美味' }}</p>
          <div class="price-list">
            <span v-for="price in product.prices" :key="price.label" class="price-tag">
              {{ price.label }} ${{ price.price }}
            </span>
          </div>
          <!-- <button class="order-btn">立即點餐</button> -->
        </div>
      </div>
    </main>

    <!-- 頁尾 -->
    <footer class="footer">
      <div class="container">
        <p>© 2026 龍鹽酥雞 | 客服專線：0987-654-321 | 新市店</p>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import api from '@/api/axios';

const products = ref([]);
const categories = ref([]);
const currentCategory = ref(null);

// 載入分類
const loadCategories = async () => {
  try {
    const res = await api.get('/public/categories');
    categories.value = res.data;
  } catch (err) {
    console.error('載入分類失敗', err);
  }
};

// 載入商品
const loadProducts = async (categoryId = null) => {
  try {
    const params = categoryId ? { categoryId } : {};
    const res = await api.get('/public/products', { params });
    products.value = res.data;
  } catch (err) {
    console.error('載入商品失敗', err);
  }
};

const filterByCategory = (categoryId) => {
  currentCategory.value = categoryId;
  loadProducts(categoryId);
};

onMounted(() => {
  loadCategories();
  loadProducts();
});
</script>

<style scoped>
/* 全局樣式重置 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

/* 品牌標頭 */
.brand-header {
  background: linear-gradient(135deg, #d32f2f 0%, #b71c1c 100%);
  color: white;
  text-align: center;
  padding: 60px 20px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}
.logo {
  font-size: 3rem;
  letter-spacing: 4px;
  text-shadow: 2px 2px 4px rgba(0,0,0,0.3);
}
.slogan {
  font-size: 1.2rem;
  margin-top: 10px;
  opacity: 0.9;
}

/* 分類導航 */
.category-nav {
  background-color: #fff6e5;
  border-bottom: 1px solid #ffcc80;
  position: sticky;
  top: 0;
  z-index: 100;
}
.category-nav ul {
  display: flex;
  list-style: none;
  overflow-x: auto;
  padding: 12px 0;
  gap: 8px;
}
.category-nav li {
  padding: 8px 20px;
  background: #ffe0b2;
  border-radius: 30px;
  cursor: pointer;
  transition: all 0.2s;
  font-weight: bold;
  white-space: nowrap;
}
.category-nav li.active {
  background: #d32f2f;
  color: white;
  box-shadow: 0 2px 6px rgba(0,0,0,0.2);
}
.category-nav li:hover {
  background: #ffb74d;
  transform: translateY(-2px);
}

/* 商品卡片網格 */
.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 30px;
  padding: 40px 20px;
}
.product-card {
  background: white;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 10px 20px rgba(0,0,0,0.1);
  transition: transform 0.3s ease, box-shadow 0.3s;
}
.product-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 20px 30px rgba(0,0,0,0.15);
}
.product-image {
  height: 200px;
  background-color: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
}
.product-image img {
  max-width: 100%;
  max-height: 100%;
  object-fit: cover;
}
.product-info {
  padding: 16px;
}
.product-info h3 {
  font-size: 1.4rem;
  color: #333;
  margin-bottom: 8px;
}
.desc {
  color: #777;
  font-size: 0.85rem;
  margin-bottom: 12px;
  line-height: 1.4;
}
.price-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin: 12px 0;
}
.price-tag {
  background: #ffecb3;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: bold;
  color: #c62828;
}
.order-btn {
  background: #d32f2f;
  color: white;
  border: none;
  width: 100%;
  padding: 10px;
  border-radius: 40px;
  font-weight: bold;
  cursor: pointer;
  transition: background 0.2s;
  margin-top: 8px;
}
.order-btn:hover {
  background: #b71c1c;
}

/* 頁尾 */
.footer {
  background-color: #2c3e50;
  color: #ecf0f1;
  text-align: center;
  padding: 20px;
  font-size: 0.9rem;
}

/* 預設無圖片時 */
.product-image img[src=""] {
  display: none;
}
.product-image:has(img[src=""]) {
  background: url('https://via.placeholder.com/200x150?text=No+Image') center/contain no-repeat;
}

@media (max-width: 480px) {
  .product-grid {
    grid-template-columns: 1fr;  /* 手機上單列 */
  }
}
</style>