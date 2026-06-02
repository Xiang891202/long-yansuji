// composables/useLoading.js
import { ref } from 'vue';

export function useLoading() {
  const isLoading = ref(false);
  const withLoading = async (fn) => {
    if (isLoading.value) return;
    isLoading.value = true;
    try {
      return await fn();
    } finally {
      isLoading.value = false;
    }
  };
  return { isLoading, withLoading };
}