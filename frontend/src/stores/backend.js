import { ref } from 'vue';

const backendReady = ref(false);
const listeners = [];

export function useBackendState() {
  const setBackendReady = (ready) => {
    backendReady.value = ready;
    listeners.forEach(fn => fn(ready));
  };
  const onBackendReady = (callback) => {
    listeners.push(callback);
    if (backendReady.value) callback(true);
  };
  return { backendReady, setBackendReady, onBackendReady };
}